"""Multi-tenant aware MCP server backed by the Cognee service API."""

from __future__ import annotations

import argparse
import asyncio
import json
import logging
import os
from pathlib import Path
from typing import Dict, Iterable, List, Optional
from urllib.parse import urlparse

from mcp.server import FastMCP
import mcp.types as types

from fuzzforge_ai.cognee_api_client import CogneeAPIClient, CogneeAPIError
from fuzzforge_ai.utils.project_env import load_project_env

logger = logging.getLogger(__name__)
logging.basicConfig(level=os.getenv("MCP_LOG_LEVEL", "INFO"))

mcp = FastMCP("FuzzForge-Cognee")


class TenantCredentials:
    def __init__(self, alias: str, service_url: str, email: str, password: str, dataset: Optional[str]):
        self.alias = alias.lower()
        self.service_url = service_url.rstrip("/")
        self.email = email
        self.password = password
        self.dataset = dataset


class CogneeMCPContext:
    """Manages Cognee API clients for multiple tenants."""

    T_PREFIX = "COGNEE_TENANT_"
    SUPPORTED_SEARCH_TYPES = {
        "SUMMARIES",
        "INSIGHTS",
        "CHUNKS",
        "RAG_COMPLETION",
        "GRAPH_COMPLETION",
        "GRAPH_SUMMARY_COMPLETION",
        "CODE",
        "CYPHER",
        "NATURAL_LANGUAGE",
        "GRAPH_COMPLETION_COT",
        "GRAPH_COMPLETION_CONTEXT_EXTENSION",
        "FEELING_LUCKY",
        "FEEDBACK",
        "TEMPORAL",
        "CODING_RULES",
    }
    TENANT_CONFIG_DEFAULT = Path(".fuzzforge") / "cognee.tenants.json"

    def __init__(self) -> None:
        load_project_env()
        self.verify_ssl = os.getenv("COGNEE_VERIFY_SSL", "true").lower() != "false"
        self.tenants = self._load_tenants()
        if not self.tenants:
            logger.info(
                "No preconfigured tenants detected; credentials must be provided per tool call."
            )
        self.clients: dict[str, CogneeAPIClient] = {}
        self.dataset_cache: dict[tuple[str, str], str] = {}

    def _load_tenants(self) -> dict[str, TenantCredentials]:
        tenants = self._load_tenants_from_env()
        file_tenants = self._load_tenants_from_file()
        for alias, creds in file_tenants.items():
            if alias in tenants:
                existing = tenants[alias]
                # Allow config file to augment missing fields from env-derived entry
                if not existing.dataset and creds.dataset:
                    existing.dataset = creds.dataset
                if creds.service_url and creds.service_url != existing.service_url:
                    existing.service_url = creds.service_url
                continue
            tenants[alias] = creds
        return tenants

    def _load_tenants_from_env(self) -> dict[str, TenantCredentials]:
        tenants: dict[str, TenantCredentials] = {}
        service_url_default = os.getenv("COGNEE_SERVICE_URL", "").rstrip("/")
        for key, value in os.environ.items():
            if not key.startswith(self.T_PREFIX) or not key.endswith("_EMAIL"):
                continue
            alias = key[len(self.T_PREFIX):-len("_EMAIL")].lower()
            email = value
            password = os.getenv(f"{self.T_PREFIX}{alias.upper()}_PASSWORD")
            dataset = os.getenv(f"{self.T_PREFIX}{alias.upper()}_DATASET")
            service_url = (
                os.getenv(f"{self.T_PREFIX}{alias.upper()}_SERVICE_URL")
                or service_url_default
            ).rstrip("/")
            if not (service_url and email and password):
                logger.warning("Skipping tenant '%s' due to incomplete credentials", alias)
                continue
            tenants[alias] = TenantCredentials(alias, service_url, email, password, dataset)

        # fallback to legacy single-tenant vars
        if not tenants and service_url_default:
            email = os.getenv("COGNEE_SERVICE_USER_EMAIL") or os.getenv("DEFAULT_USER_EMAIL")
            password = os.getenv("COGNEE_SERVICE_USER_PASSWORD") or os.getenv("DEFAULT_USER_PASSWORD")
            dataset = os.getenv("COGNEE_DATASET_NAME")
            if email and password:
                tenants["default"] = TenantCredentials("default", service_url_default, email, password, dataset)

        return tenants

    def _load_tenants_from_file(self) -> dict[str, TenantCredentials]:
        tenants: Dict[str, TenantCredentials] = {}
        config_path = os.getenv("COGNEE_TENANTS_FILE")
        if config_path:
            candidate = Path(config_path).expanduser()
        else:
            candidate = (Path.cwd() / self.TENANT_CONFIG_DEFAULT).resolve()

        if not candidate.exists():
            return tenants

        try:
            raw = candidate.read_text(encoding="utf-8")
            data = json.loads(raw)
        except (OSError, json.JSONDecodeError) as exc:
            logger.error("Failed to load tenant config '%s': %s", candidate, exc)
            return tenants

        entries = []
        if isinstance(data, list):
            entries = data
        elif isinstance(data, dict):
            entries = data.get("tenants", [])
        else:
            logger.error("Tenant config '%s' must be a list or dict with 'tenants'", candidate)
            return tenants

        for item in entries:
            if not isinstance(item, dict):
                continue
            alias = item.get("alias")
            email = item.get("email")
            password = item.get("password")
            dataset = item.get("dataset")
            service_url = (item.get("service_url") or os.getenv("COGNEE_SERVICE_URL", "")).rstrip("/")
            if not (alias and email and password and service_url):
                logger.warning("Skipping tenant entry with missing fields: %s", item)
                continue
            tenants[alias.lower()] = TenantCredentials(alias, service_url, email, password, dataset)

        if tenants:
            logger.info("Loaded %d tenant(s) from %s", len(tenants), candidate)
        return tenants

    async def get_client(self, alias: str) -> CogneeAPIClient:
        alias = alias.lower()
        if alias not in self.tenants:
            raise ValueError(f"Unknown tenant alias '{alias}'. Available: {', '.join(self.tenants)}")
        if alias not in self.clients:
            tenant = self.tenants[alias]
            client = CogneeAPIClient(tenant.service_url, verify_ssl=self.verify_ssl)
            await client.ensure_user(tenant.email, tenant.password, tenant_name=self._tenant_name(alias))
            self.clients[alias] = client
        return self.clients[alias]

    async def close(self) -> None:
        await asyncio.gather(*(client.close() for client in self.clients.values()))
        self.clients.clear()

    def _tenant_name(self, alias: str) -> str:
        alias = alias.lower()
        env_alias = alias.upper().replace(":", "_").replace("@", "_").replace("/", "_")
        fallback = alias.replace(":", "-").replace("@", "-").replace("/", "-")
        return os.getenv(f"COGNEE_TENANT_{env_alias}_NAME") or fallback

    def _service_url(self, override: Optional[str]) -> str:
        url = (override or os.getenv("COGNEE_SERVICE_URL", "")).strip()
        url = url.rstrip("/")
        if url:
            parsed = urlparse(url)
            host = parsed.hostname
            if host in {"localhost", "127.0.0.1"}:
                internal = os.getenv("COGNEE_INTERNAL_SERVICE_URL", "").rstrip("/")
                if internal:
                    url = internal
        if not url:
            raise ValueError(
                "Service URL not provided. Set COGNEE_SERVICE_URL or pass service_url argument."
            )
        return url

    def _alias_for_credentials(self, email: str, service_url: str) -> str:
        safe_url = service_url.lower().replace("://", "_").replace("/", "_")
        return f"user:{email.lower()}@{safe_url}"

    def _ensure_tenant_entry(
        self,
        *,
        tenant: Optional[str],
        email: Optional[str],
        password: Optional[str],
        service_url: Optional[str],
        dataset: Optional[str],
    ) -> tuple[str, TenantCredentials]:
        if tenant:
            alias = tenant.lower()
            if alias not in self.tenants:
                raise ValueError(
                    f"Unknown tenant alias '{alias}'. Available: {', '.join(self.tenants)}"
                )
            creds = self.tenants[alias]
            if service_url and service_url.rstrip("/") != creds.service_url:
                raise ValueError(
                    "service_url override does not match configured tenant service URL"
                )
            if dataset and not creds.dataset:
                creds.dataset = dataset
            return alias, creds

        if not (email and password):
            raise ValueError(
                "Provide either a tenant alias or user_email and user_password credentials."
            )

        base_url = self._service_url(service_url)
        alias = self._alias_for_credentials(email, base_url)
        creds = self.tenants.get(alias)
        if not creds:
            creds = TenantCredentials(alias, base_url, email, password, dataset)
            self.tenants[alias] = creds
        else:
            if dataset and not creds.dataset:
                creds.dataset = dataset
            # Update password if changed to avoid stale credential reuse
            if password != creds.password:
                creds.password = password
        return alias, creds

    async def prepare_client(
        self,
        *,
        tenant: Optional[str],
        email: Optional[str],
        password: Optional[str],
        service_url: Optional[str],
    ) -> tuple[str, CogneeAPIClient]:
        alias, _ = self._ensure_tenant_entry(
            tenant=tenant,
            email=email,
            password=password,
            service_url=service_url,
            dataset=None,
        )
        client = await self.get_client(alias)
        return alias, client

    async def prepare_dataset_request(
        self,
        *,
        tenant: Optional[str],
        dataset: Optional[str],
        email: Optional[str],
        password: Optional[str],
        service_url: Optional[str],
    ) -> tuple[str, CogneeAPIClient, str, str]:
        alias, _ = self._ensure_tenant_entry(
            tenant=tenant,
            email=email,
            password=password,
            service_url=service_url,
            dataset=dataset,
        )
        client = await self.get_client(alias)
        dataset_name, dataset_id = await self.resolve_dataset(alias, dataset)
        return alias, client, dataset_name, dataset_id

    async def start(self) -> None:
        for alias, creds in self.tenants.items():
            if creds.dataset:
                try:
                    await self.resolve_dataset(alias, creds.dataset)
                except Exception as exc:
                    logger.warning("Failed to preload dataset for tenant '%s': %s", alias, exc)
        logger.info("Tenants available: %s", ", ".join(sorted(self.tenants)))

    async def resolve_dataset(self, alias: str, dataset: Optional[str]) -> tuple[str, str]:
        alias = alias.lower()
        if alias not in self.tenants:
            raise ValueError(f"Unknown tenant alias '{alias}'. Available: {', '.join(self.tenants)}")
        tenant = self.tenants[alias]
        dataset_name = dataset or tenant.dataset
        if not dataset_name:
            env_alias = alias.upper().replace(":", "_").replace("@", "_").replace("/", "_")
            raise ValueError(
                "Dataset not specified. Provide the dataset argument or set "
                f"COGNEE_TENANT_{env_alias}_DATASET."
            )
        cache_key = (alias, dataset_name)
        if cache_key in self.dataset_cache:
            return dataset_name, self.dataset_cache[cache_key]
        client = await self.get_client(alias)
        info = await client.ensure_dataset(dataset_name)
        dataset_id = info.get("id")
        if not dataset_id:
            raise RuntimeError(f"Failed to resolve dataset id for '{dataset_name}'")
        self.dataset_cache[cache_key] = dataset_id
        return dataset_name, dataset_id

    def list_tenants(self) -> List[str]:
        return sorted(self.tenants.keys())


CONTEXT = CogneeMCPContext()


def _text(message: str) -> List[types.TextContent]:
    return [types.TextContent(type="text", text=message)]


@mcp.tool()
async def list_tenants() -> List[types.TextContent]:
    """List known tenant aliases (configured or dynamically added)."""

    aliases = CONTEXT.list_tenants()
    return _text(json.dumps({"tenants": aliases}, indent=2))


@mcp.tool()
async def ingest_file(
    path: str,
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Upload a local file into the Cognee dataset for a tenant or explicit user."""

    file_path = Path(path).expanduser().resolve()
    if not file_path.is_file():
        raise ValueError(f"File not found: {file_path}")

    alias, client, dataset_name, _ = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    result = await client.ingest_files(dataset_name, [file_path])
    success = result.get("success", 0)
    failed = result.get("failed", 0)
    errors = result.get("errors", [])

    message = [
        f"Tenant: {tenant or alias}",
        f"Dataset: {dataset_name}",
        f"Uploaded: {success} file(s)",
        f"Failed: {failed}",
    ]
    if errors:
        message.append("Errors:\n" + "\n".join(errors))

    return _text("\n".join(message))


@mcp.tool()
async def cognify(
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    run_in_background: bool = False,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
    custom_prompt: Optional[str] = None,
) -> List[types.TextContent]:
    """Trigger the Cognify pipeline for the dataset."""

    alias, client, dataset_name, _ = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    response = await client.cognify(
        dataset_name,
        run_in_background=run_in_background,
        custom_prompt=custom_prompt,
    )
    payload = {
        "tenant": tenant or alias,
        "dataset": dataset_name,
        "response": response,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def cognify_status(
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Return the status of recent Cognify runs."""

    alias, client, _, dataset_id = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    status = await client.status()
    payload = {
        "tenant": tenant or alias,
        "dataset_id": dataset_id,
        "status": status,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def search_chunks(
    query: str,
    tenant: Optional[str] = None,
    top_k: int = 5,
    dataset: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Search dataset for relevant document chunks."""

    alias, client, dataset_name, _ = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    result = await client.search(
        query,
        datasets=[dataset_name],
        search_type="CHUNKS",
        top_k=top_k,
    )
    payload = {
        "tenant": tenant or alias,
        "dataset": dataset_name,
        "results": result,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def search_insights(
    query: str,
    tenant: Optional[str] = None,
    top_k: int = 5,
    dataset: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Search dataset for graph insights."""

    alias, client, dataset_name, _ = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    result = await client.search(
        query,
        datasets=[dataset_name],
        search_type="INSIGHTS",
        top_k=top_k,
    )
    payload = {
        "tenant": tenant or alias,
        "dataset": dataset_name,
        "results": result,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def search(
    query: str,
    search_type: str = "CHUNKS",
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    top_k: Optional[int] = None,
    only_context: Optional[bool] = None,
    node_name: Optional[List[str]] = None,
    system_prompt: Optional[str] = None,
    use_combined_context: Optional[bool] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Execute a Cognee semantic search with custom modes."""

    alias, client, dataset_name, _ = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    search_type_upper = search_type.upper()
    if search_type_upper not in CONTEXT.SUPPORTED_SEARCH_TYPES:
        raise ValueError(
            "Unsupported search_type. Choose one of: "
            + ", ".join(sorted(CONTEXT.SUPPORTED_SEARCH_TYPES))
        )

    node_iter: Optional[Iterable[str]] = node_name if node_name is not None else None

    result = await client.search(
        query,
        datasets=[dataset_name],
        search_type=search_type_upper,
        top_k=top_k,
        only_context=only_context,
        node_name=node_iter,
        system_prompt=system_prompt,
        use_combined_context=use_combined_context,
    )
    payload = {
        "tenant": tenant or alias,
        "dataset": dataset_name,
        "search_type": search_type_upper,
        "results": result,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def memify(
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    extraction_tasks: Optional[List[str]] = None,
    enrichment_tasks: Optional[List[str]] = None,
    data: Optional[str] = None,
    node_name: Optional[List[str]] = None,
    run_in_background: bool = False,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Run the Memify enrichment pipeline for a dataset."""

    alias, client, dataset_name, dataset_id = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )

    response = await client.memify(
        dataset_name=dataset_name,
        dataset_id=dataset_id,
        extraction_tasks=extraction_tasks,
        enrichment_tasks=enrichment_tasks,
        data=data,
        node_name=node_name,
        run_in_background=run_in_background,
    )

    payload = {
        "tenant": tenant or alias,
        "dataset": dataset_name,
        "response": response,
    }
    return _text(json.dumps(payload, indent=2))


@mcp.tool()
async def list_datasets(
    tenant: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """List datasets accessible to configured tenants or provided user credentials."""

    result: dict[str, List[dict[str, object]]] = {}

    if tenant or user_email:
        alias, client = await CONTEXT.prepare_client(
            tenant=tenant,
            email=user_email,
            password=user_password,
            service_url=service_url,
        )
        result[tenant or alias] = await client.list_datasets()
    else:
        for alias in CONTEXT.list_tenants():
            client = await CONTEXT.get_client(alias)
            datasets = await client.list_datasets()
            result[alias] = datasets
    return _text(json.dumps(result, indent=2))


@mcp.tool()
async def prune_dataset(
    tenant: Optional[str] = None,
    dataset: Optional[str] = None,
    user_email: Optional[str] = None,
    user_password: Optional[str] = None,
    service_url: Optional[str] = None,
) -> List[types.TextContent]:
    """Delete the dataset and recreate it empty."""

    alias, client, dataset_name, dataset_id = await CONTEXT.prepare_dataset_request(
        tenant=tenant,
        dataset=dataset,
        email=user_email,
        password=user_password,
        service_url=service_url,
    )
    await client.delete_dataset(dataset_id)
    dataset_info = await client.ensure_dataset(dataset_name)
    CONTEXT.dataset_cache[(alias, dataset_name)] = dataset_info.get("id")
    return _text(
        f"Dataset '{dataset_name}' (tenant {tenant or alias}) has been pruned and recreated."
    )


async def _async_main(args: argparse.Namespace) -> None:
    try:
        await CONTEXT.start()
    except CogneeAPIError as exc:
        raise SystemExit(f"Failed to initialise Cognee MCP: {exc}") from exc

    transport = args.transport
    host = args.host
    port = args.port
    path = args.path

    # Configure FastMCP server settings before launch
    mcp.settings.host = host
    mcp.settings.port = port
    if transport == "http":
        mcp.settings.streamable_http_path = path
    elif transport == "sse":
        mcp.settings.sse_path = path

    logger.info(
        "Starting Cognee MCP server",
        extra={
            "transport": transport,
            "host": host,
            "port": port,
            "path": path,
            "tenants": CONTEXT.list_tenants(),
        },
    )

    try:
        if transport == "http":
            await mcp.run_streamable_http_async()
        elif transport == "sse":
            await mcp.run_sse_async(path)
        else:
            await mcp.run_stdio_async()
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
