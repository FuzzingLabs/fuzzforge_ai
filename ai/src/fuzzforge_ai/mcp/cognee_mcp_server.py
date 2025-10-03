"""Multi-tenant aware MCP server backed by the Cognee service API."""

from __future__ import annotations

import argparse
import asyncio
import json
import logging
import os
from pathlib import Path
from typing import List, Optional

from dotenv import load_dotenv
from mcp.server import FastMCP
import mcp.types as types

from fuzzforge_ai.cognee_api_client import CogneeAPIClient, CogneeAPIError

logger = logging.getLogger(__name__)
logging.basicConfig(level=os.getenv("MCP_LOG_LEVEL", "INFO"))

mcp = FastMCP("FuzzForge-Cognee")


def _load_project_env() -> None:
    """Load environment variables from `.fuzzforge/.env` if present."""

    env_path = Path.cwd() / ".fuzzforge" / ".env"
    if env_path.exists():
        load_dotenv(env_path, override=False)


class CogneeMCPContext:
    """Holds shared state for MCP tools and manages the Cognee API client."""

    def __init__(self) -> None:
        _load_project_env()
        self.service_url = os.getenv("COGNEE_SERVICE_URL", "").rstrip("/")
        self.verify_ssl = os.getenv("COGNEE_VERIFY_SSL", "true").lower() != "false"
        self.email = os.getenv("COGNEE_SERVICE_USER_EMAIL") or os.getenv("DEFAULT_USER_EMAIL")
        self.password = os.getenv("COGNEE_SERVICE_USER_PASSWORD") or os.getenv("DEFAULT_USER_PASSWORD")
        if not (self.service_url and self.email and self.password):
            raise RuntimeError(
                "COGNEE_SERVICE_URL, COGNEE_SERVICE_USER_EMAIL and"
                " COGNEE_SERVICE_USER_PASSWORD must be set in the environment"
            )
        self._client: Optional[CogneeAPIClient] = None
        self.active_dataset_name: Optional[str] = os.getenv("COGNEE_DATASET_NAME") or self._default_dataset_name()
        self.active_dataset_id: Optional[str] = None

    async def start(self) -> None:
        self._client = CogneeAPIClient(self.service_url, verify_ssl=self.verify_ssl)
        await self._client.ensure_user(self.email, self.password, tenant_name=self._tenant_name())
        if self.active_dataset_name:
            dataset_info = await self._client.ensure_dataset(self.active_dataset_name)
            self.active_dataset_id = dataset_info.get("id")
        logger.info(
            "Connected to Cognee service",
            extra={
                "service_url": self.service_url,
                "dataset": self.active_dataset_name,
                "dataset_id": self.active_dataset_id,
            },
        )

    async def close(self) -> None:
        if self._client:
            await self._client.close()

    @property
    def client(self) -> CogneeAPIClient:
        if not self._client:
            raise RuntimeError("Cognee API client is not initialised yet")
        return self._client

    def _tenant_name(self) -> str:
        return os.getenv("COGNEE_TENANT_NAME") or os.getenv("COGNEE_PROJECT_ID") or "default-tenant"

    def _default_dataset_name(self) -> str:
        project_name = os.getenv("FUZZFORGE_PROJECT_NAME") or "project"
        return f"{project_name}_codebase"

    async def ensure_dataset(self, dataset_name: str) -> str:
        """Ensure the dataset exists and cache its id."""

        info = await self.client.ensure_dataset(dataset_name)
        dataset_id = info.get("id")
        if not dataset_id:
            raise RuntimeError(f"Failed to resolve dataset id for '{dataset_name}'")
        self.active_dataset_name = dataset_name
        self.active_dataset_id = dataset_id
        return dataset_id

    async def resolve_dataset(self, dataset: Optional[str]) -> tuple[str, str]:
        name = dataset or self.active_dataset_name
        if not name:
            raise ValueError("Dataset is not specified; set COGNEE_DATASET_NAME or call set_dataset first")
        # if the requested dataset differs from the cached one, ensure it
        if dataset and dataset != self.active_dataset_name:
            dataset_id = await self.ensure_dataset(dataset)
        else:
            dataset_id = self.active_dataset_id
            if not dataset_id:
                dataset_id = await self.ensure_dataset(name)
        return name, dataset_id


CONTEXT = CogneeMCPContext()


def _text(message: str) -> List[types.TextContent]:
    return [types.TextContent(type="text", text=message)]


@mcp.tool()
async def set_dataset(dataset: str) -> List[types.TextContent]:
    """Switch the active dataset for subsequent operations."""

    dataset_id = await CONTEXT.ensure_dataset(dataset)
    return _text(f"Active dataset set to '{dataset}' (id: {dataset_id}).")


@mcp.tool()
async def ingest_file(path: str, dataset: Optional[str] = None) -> List[types.TextContent]:
    """Upload a local file into the Cognee dataset."""

    file_path = Path(path).expanduser().resolve()
    if not file_path.is_file():
        raise ValueError(f"File not found: {file_path}")

    dataset_name, _ = await CONTEXT.resolve_dataset(dataset)
    result = await CONTEXT.client.ingest_files(dataset_name, [file_path])
    success = result.get("success", 0)
    failed = result.get("failed", 0)
    errors = result.get("errors", [])

    message = [
        f"Uploaded {success} file(s) to dataset '{dataset_name}'.",
        f"Failed: {failed}",
    ]
    if errors:
        message.append("Errors:\n" + "\n".join(errors))

    return _text("\n".join(message))


@mcp.tool()
async def cognify(run_in_background: bool = False, dataset: Optional[str] = None) -> List[types.TextContent]:
    """Trigger the Cognify pipeline for the dataset."""

    dataset_name, _ = await CONTEXT.resolve_dataset(dataset)
    response = await CONTEXT.client.cognify(dataset_name, run_in_background=run_in_background)
    return _text(json.dumps(response, indent=2))


@mcp.tool()
async def cognify_status(dataset: Optional[str] = None) -> List[types.TextContent]:
    """Return the status of recent Cognify runs."""

    _, dataset_id = await CONTEXT.resolve_dataset(dataset)
    status = await CONTEXT.client.status()
    status.setdefault("dataset_ids", []).append(dataset_id)
    return _text(json.dumps(status, indent=2))


@mcp.tool()
async def search_chunks(query: str, top_k: int = 5, dataset: Optional[str] = None) -> List[types.TextContent]:
    """Search dataset for relevant document chunks."""

    dataset_name, _ = await CONTEXT.resolve_dataset(dataset)
    result = await CONTEXT.client.search(
        query,
        datasets=[dataset_name],
        search_type="CHUNKS",
        top_k=top_k,
    )
    return _text(json.dumps(result, indent=2))


@mcp.tool()
async def search_insights(query: str, top_k: int = 5, dataset: Optional[str] = None) -> List[types.TextContent]:
    """Search dataset for graph insights."""

    dataset_name, _ = await CONTEXT.resolve_dataset(dataset)
    result = await CONTEXT.client.search(
        query,
        datasets=[dataset_name],
        search_type="INSIGHTS",
        top_k=top_k,
    )
    return _text(json.dumps(result, indent=2))


@mcp.tool()
async def list_datasets() -> List[types.TextContent]:
    """List datasets accessible to the current service user."""

    datasets = await CONTEXT.client.list_datasets()
    return _text(json.dumps(datasets, indent=2))


@mcp.tool()
async def prune_dataset(dataset: Optional[str] = None) -> List[types.TextContent]:
    """Delete the dataset and recreate it empty."""

    dataset_name, dataset_id = await CONTEXT.resolve_dataset(dataset)
    await CONTEXT.client.delete_dataset(dataset_id)
    dataset_info = await CONTEXT.client.ensure_dataset(dataset_name)
    CONTEXT.active_dataset_name = dataset_name
    CONTEXT.active_dataset_id = dataset_info.get("id")
    return _text(f"Dataset '{dataset_name}' has been pruned and recreated.")


async def _async_main(args: argparse.Namespace) -> None:
    try:
        await CONTEXT.start()
    except CogneeAPIError as exc:
        raise SystemExit(f"Failed to initialise Cognee MCP: {exc}") from exc

    transport = args.transport
    host = args.host
    port = args.port
    path = args.path

    logger.info(
        "Starting Cognee MCP server",
        extra={
            "transport": transport,
            "host": host,
            "port": port,
            "path": path,
            "dataset": CONTEXT.dataset_name,
        },
    )

    try:
        if transport == "http":
            await mcp.run_http(host=host, port=port, path=path)
        elif transport == "sse":
            await mcp.run_sse(host=host, port=port, path=path)
        else:
            await mcp.run_stdio()
    finally:
        await CONTEXT.close()


def main(argv: Optional[List[str]] = None) -> None:
    parser = argparse.ArgumentParser(description="Cognee MCP server (service-backed)")
    parser.add_argument("--transport", choices=["stdio", "sse", "http"], default=os.getenv("TRANSPORT_MODE", "stdio"))
    parser.add_argument("--host", default=os.getenv("MCP_HOST", "127.0.0.1"))
    parser.add_argument("--port", type=int, default=int(os.getenv("MCP_PORT", "8000")))
    parser.add_argument("--path", default=os.getenv("MCP_HTTP_PATH", "/mcp"))
    args = parser.parse_args(argv)

    asyncio.run(_async_main(args))


if __name__ == "__main__":
    main()
