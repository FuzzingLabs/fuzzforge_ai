"""Async helper for interacting with the Cognee MCP mirror."""

from __future__ import annotations

import asyncio
import json
import os
from dataclasses import dataclass
from typing import Any, Dict, Iterable, Optional

from mcp.client import streamable_http
from mcp.client.session import ClientSession
from mcp.types import Implementation, CallToolResult

from fuzzforge_ai import __version__


@dataclass
class MCPCallResult:
    raw: CallToolResult
    payload: Any


class CogneeMCPTools:
    """Minimal client for invoking Cognee MCP tools."""

    def __init__(
        self,
        base_url: str,
        *,
        default_dataset: Optional[str] = None,
        default_email: Optional[str] = None,
        default_password: Optional[str] = None,
        default_service_url: Optional[str] = None,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.default_dataset = default_dataset
        self.default_email = default_email
        self.default_password = default_password
        self.default_service_url = default_service_url
        self._client_info = Implementation(name="FuzzForge-Agent", version=__version__)
        self._lock = asyncio.Lock()

    async def _call_tool(self, name: str, arguments: Dict[str, Any]) -> MCPCallResult:
        async with streamable_http.streamablehttp_client(self.base_url) as (read_stream, write_stream, _):
            session = ClientSession(read_stream, write_stream, client_info=self._client_info)
            await session.initialize()
            result = await session.call_tool(name, arguments)

        if result.isError:
            message = getattr(result, "message", None) or "Unknown MCP tool error"
            raise RuntimeError(f"Tool '{name}' failed: {message}")

        payload = self._extract_payload(result)
        return MCPCallResult(raw=result, payload=payload)

    def _with_defaults(self, overrides: Dict[str, Any]) -> Dict[str, Any]:
        merged: Dict[str, Any] = {}
        if self.default_email and "user_email" not in overrides:
            merged["user_email"] = self.default_email
        if self.default_password and "user_password" not in overrides:
            merged["user_password"] = self.default_password
        if self.default_service_url and "service_url" not in overrides:
            merged["service_url"] = self.default_service_url
        if self.default_dataset and "dataset" not in overrides and "dataset" not in merged:
            merged["dataset"] = self.default_dataset
        merged.update({k: v for k, v in overrides.items() if v is not None})
        return merged

    @staticmethod
    def _extract_payload(result: CallToolResult) -> Any:
        if result.content:
            texts: list[str] = []
            for item in result.content:
                text = getattr(item, "text", None)
                if text:
                    texts.append(text)
            if texts:
                combined = "\n".join(texts).strip()
                if combined:
                    try:
                        return json.loads(combined)
                    except json.JSONDecodeError:
                        return combined
        return None

    async def list_datasets(self, **overrides: Any) -> Any:
        args = self._with_defaults(overrides)
        result = await self._call_tool("list_datasets", args)
        return result.payload

    async def search(
        self,
        *,
        query: str,
        search_type: str = "CHUNKS",
        top_k: Optional[int] = None,
        only_context: Optional[bool] = None,
        node_name: Optional[Iterable[str]] = None,
        system_prompt: Optional[str] = None,
        use_combined_context: Optional[bool] = None,
        dataset: Optional[str] = None,
        **overrides: Any,
    ) -> Any:
        args = {
            "query": query,
            "search_type": search_type,
            "top_k": top_k,
            "only_context": only_context,
            "node_name": list(node_name) if node_name is not None else None,
            "system_prompt": system_prompt,
            "use_combined_context": use_combined_context,
            "dataset": dataset,
        }
        payload = self._with_defaults({**args, **overrides})
        result = await self._call_tool("search", payload)
        return result.payload

    async def cognify(
        self,
        *,
        dataset: Optional[str] = None,
        run_in_background: bool = False,
        custom_prompt: Optional[str] = None,
        **overrides: Any,
    ) -> Any:
        args = {
            "dataset": dataset,
            "run_in_background": run_in_background,
            "custom_prompt": custom_prompt,
        }
        payload = self._with_defaults({**args, **overrides})
        result = await self._call_tool("cognify", payload)
        return result.payload

    async def memify(
        self,
        *,
        dataset: Optional[str] = None,
        extraction_tasks: Optional[Iterable[str]] = None,
        enrichment_tasks: Optional[Iterable[str]] = None,
        data: Optional[str] = None,
        node_name: Optional[Iterable[str]] = None,
        run_in_background: bool = False,
        **overrides: Any,
    ) -> Any:
        args = {
            "dataset": dataset,
            "extraction_tasks": list(extraction_tasks) if extraction_tasks is not None else None,
            "enrichment_tasks": list(enrichment_tasks) if enrichment_tasks is not None else None,
            "data": data,
            "node_name": list(node_name) if node_name is not None else None,
            "run_in_background": run_in_background,
        }
        payload = self._with_defaults({**args, **overrides})
        result = await self._call_tool("memify", payload)
        return result.payload

    async def search_chunks(self, **kwargs: Any) -> Any:
        return await self.search(search_type="CHUNKS", **kwargs)

    async def search_insights(self, **kwargs: Any) -> Any:
        return await self.search(search_type="INSIGHTS", **kwargs)


__all__ = ["CogneeMCPTools"]
