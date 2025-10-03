"""Async HTTP client for interacting with a remote Cognee service."""

from __future__ import annotations

import json
import logging
import mimetypes
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence

import httpx

logger = logging.getLogger(__name__)


class CogneeAPIError(RuntimeError):
    """Raised when the Cognee service returns an unexpected response."""


class CogneeAPIClient:
    """Thin async wrapper around Cognee's HTTP API."""

    def __init__(self, base_url: str, timeout: float = 180.0, verify_ssl: Optional[bool] = None):
        if not base_url:
            raise ValueError("Cognee service URL must be provided")
        self.base_url = base_url.rstrip("/")
        kwargs: Dict[str, Any] = {"timeout": timeout, "base_url": self.base_url, "follow_redirects": True}
        if verify_ssl is not None:
            kwargs["verify"] = verify_ssl
        self._client = httpx.AsyncClient(**kwargs)
        self._token: Optional[str] = None

    async def close(self) -> None:
        await self._client.aclose()

    # ------------------------------------------------------------------
    # Authentication helpers
    # ------------------------------------------------------------------
    async def login(self, email: str, password: str) -> bool:
        """Attempt to authenticate the user and store the JWT token."""
        form = {"username": email, "password": password}
        try:
            response = await self._client.post("/api/v1/auth/login", data=form)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee login request failed: {exc}") from exc

        if response.status_code == 200:
            data = response.json()
            self._token = data.get("access_token")
            if self._token:
                # Update client headers with Bearer token
                self._client.headers.update({"Authorization": f"Bearer {self._token}"})
            return True
        if response.status_code in {400, 401}:  # Invalid credentials / user not found
            return False
        raise CogneeAPIError(
            f"Unexpected login status {response.status_code}: {response.text.strip()}"
        )

    async def register(self, email: str, password: str) -> bool:
        """Register a new user; returns True when created, False if it already exists."""
        payload = {"email": email, "password": password}
        try:
            response = await self._client.post("/api/v1/auth/register", json=payload)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee register request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            return True
        if response.status_code in {400, 409}:  # already exists / validation error
            logger.debug("Cognee register response %s: %s", response.status_code, response.text)
            return False
        raise CogneeAPIError(
            f"Unexpected register status {response.status_code}: {response.text.strip()}"
        )

    async def ensure_user(self, email: str, password: str, tenant_name: str) -> Dict[str, Any]:
        """Ensure a user exists, authenticate, and provision tenant if needed."""
        logged_in = await self.login(email, password)
        if not logged_in:
            created = await self.register(email, password)
            if not created:
                # User may already exist with different password; try login again to surface error
                logged_in = await self.login(email, password)
                if not logged_in:
                    raise CogneeAPIError("Unable to authenticate with Cognee service")
            else:
                # Newly created -> login to capture cookies
                if not await self.login(email, password):
                    raise CogneeAPIError("Login failed after registering Cognee service user")

        # Fetch user profile - if this fails with 401, auth may not be enforced
        try:
            profile = await self._get_me()
        except CogneeAPIError as exc:
            if "401" in str(exc):
                # Auth endpoints exist but may not be enforced - create minimal profile
                logger.debug("Auth endpoints returned 401, using minimal profile")
                profile = {"email": email, "id": None}
            else:
                raise

        # Ensure tenant exists and is associated with the user
        if tenant_name:
            await self._create_tenant_if_needed(tenant_name)
            profile["tenant_name"] = tenant_name

        return profile

    async def _get_me(self) -> Dict[str, Any]:
        try:
            response = await self._client.get("/api/v1/auth/me")
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee profile request failed: {exc}") from exc

        if response.status_code == 200:
            return response.json()
        raise CogneeAPIError(
            f"Failed to fetch Cognee profile ({response.status_code}): {response.text.strip()}"
        )

    async def _create_tenant_if_needed(self, tenant_name: str) -> None:
        try:
            response = await self._client.post(
                "/api/v1/permissions/tenants", params={"tenant_name": tenant_name}
            )
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee tenant request failed: {exc}") from exc

        if response.status_code in {200, 409}:  # created or already exists
            return
        # FastAPI may surface validation errors as 400/422; treat as already exists
        if response.status_code in {400, 422}:
            logger.debug("Tenant creation returned %s: %s", response.status_code, response.text)
            return
        raise CogneeAPIError(
            f"Failed to ensure Cognee tenant ({response.status_code}): {response.text.strip()}"
        )

    # ------------------------------------------------------------------
    # Dataset helpers
    # ------------------------------------------------------------------
    async def ensure_dataset(self, dataset_name: str) -> Dict[str, Any]:
        payload = {"name": dataset_name}
        try:
            response = await self._client.post("/api/v1/datasets", json=payload)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee dataset request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            return response.json()
        raise CogneeAPIError(
            f"Failed to ensure dataset {dataset_name} ({response.status_code}): {response.text.strip()}"
        )

    async def delete_dataset(self, dataset_id: str) -> None:
        try:
            response = await self._client.delete(f"/api/v1/datasets/{dataset_id}")
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee dataset delete failed: {exc}") from exc

        if response.status_code in {200, 204}:
            return
        if response.status_code == 404:
            return
        raise CogneeAPIError(
            f"Failed to delete dataset ({response.status_code}): {response.text.strip()}"
        )

    async def list_datasets(self) -> List[Dict[str, Any]]:
        try:
            response = await self._client.get("/api/v1/datasets")
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee datasets list failed: {exc}") from exc

        if response.status_code == 200:
            return response.json()
        raise CogneeAPIError(
            f"Failed to list datasets ({response.status_code}): {response.text.strip()}"
        )

    # ------------------------------------------------------------------
    # Ingestion and processing
    # ------------------------------------------------------------------
    async def ingest_files(self, dataset_name: str, files: Sequence[Path]) -> Dict[str, Any]:
        form_data = {"datasetName": dataset_name}
        multipart_files: List[tuple[str, tuple[str, Any, Optional[str]]]] = []
        for path in files:
            mime_type, _ = mimetypes.guess_type(path.name)
            try:
                content = path.read_bytes()
            except OSError as exc:
                raise CogneeAPIError(f"Failed to read file for Cognee upload: {path}: {exc}") from exc
            multipart_files.append(
                (
                    "data",
                    (path.name, content, mime_type or "application/octet-stream"),
                )
            )

        try:
            response = await self._client.post(
                "/api/v1/add", data=form_data, files=multipart_files
            )
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee ingestion request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            result = response.json()
            result.setdefault("status", response.status_code)
            return result

        raise CogneeAPIError(
            f"Cognee ingestion failed ({response.status_code}): {response.text.strip()}"
        )

    async def cognify(
        self,
        dataset_name: str,
        *,
        run_in_background: bool = False,
        custom_prompt: Optional[str] = None,
    ) -> Dict[str, Any]:
        payload: Dict[str, Any] = {"datasets": [dataset_name], "run_in_background": run_in_background}
        if custom_prompt is not None:
            payload["custom_prompt"] = custom_prompt
        try:
            response = await self._client.post("/api/v1/cognify", json=payload)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee cognify request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            return response.json()
        raise CogneeAPIError(
            f"Cognee cognify failed ({response.status_code}): {response.text.strip()}"
        )

    async def ingest_text(self, dataset_name: str, content: str) -> Dict[str, Any]:
        data_path = Path("virtual.txt")
        multipart_files = [
            (
                "data",
                (data_path.name, content.encode("utf-8"), "text/plain"),
            )
        ]
        form_data = {"datasetName": dataset_name}
        try:
            response = await self._client.post(
                "/api/v1/add", data=form_data, files=multipart_files
            )
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee text ingestion request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            return response.json()
        raise CogneeAPIError(
            f"Cognee text ingestion failed ({response.status_code}): {response.text.strip()}"
        )

    # ------------------------------------------------------------------
    # Search / insights
    # ------------------------------------------------------------------
    async def search(
        self,
        query: str,
        datasets: Optional[Iterable[str]] = None,
        search_type: str = "INSIGHTS",
        top_k: Optional[int] = None,
        only_context: Optional[bool] = None,
        node_name: Optional[Iterable[str]] = None,
        system_prompt: Optional[str] = None,
        use_combined_context: Optional[bool] = None,
    ) -> Any:
        payload: Dict[str, Any] = {
            "query": query,
            "search_type": search_type,
        }
        if datasets:
            payload["datasets"] = list(datasets)
        if top_k is not None:
            payload["top_k"] = top_k
        if only_context is not None:
            payload["only_context"] = only_context
        if node_name is not None:
            payload["node_name"] = list(node_name)
        if system_prompt is not None:
            payload["system_prompt"] = system_prompt
        if use_combined_context is not None:
            payload["use_combined_context"] = use_combined_context

        try:
            response = await self._client.post("/api/v1/search", json=payload)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee search request failed: {exc}") from exc

        if response.status_code in {200, 201}:
            return response.json()
        raise CogneeAPIError(
            f"Cognee search failed ({response.status_code}): {response.text.strip()}"
        )

    async def memify(
        self,
        *,
        dataset_name: Optional[str] = None,
        dataset_id: Optional[str] = None,
        extraction_tasks: Optional[Iterable[str]] = None,
        enrichment_tasks: Optional[Iterable[str]] = None,
        data: Optional[str] = None,
        node_name: Optional[Iterable[str]] = None,
        run_in_background: bool = False,
    ) -> Dict[str, Any]:
        payload: Dict[str, Any] = {
            "datasetName": dataset_name,
            "datasetId": dataset_id,
            "runInBackground": run_in_background,
        }
        if extraction_tasks is not None:
            payload["extractionTasks"] = list(extraction_tasks)
        if enrichment_tasks is not None:
            payload["enrichmentTasks"] = list(enrichment_tasks)
        if data is not None:
            payload["data"] = data
        if node_name is not None:
            payload["nodeName"] = list(node_name)

        payload = {key: value for key, value in payload.items() if value is not None}

        try:
            response = await self._client.post("/api/v1/memify", json=payload)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee memify request failed: {exc}") from exc

        if response.status_code in {200, 202}:
            return response.json()
        raise CogneeAPIError(
            f"Cognee memify failed ({response.status_code}): {response.text.strip()}"
        )

    async def status(self) -> Dict[str, Any]:
        """Return health information along with dataset list."""
        info: Dict[str, Any] = {}
        try:
            response = await self._client.get("/health")
            if response.status_code == 200:
                info["health"] = response.json()
            else:
                info["health_error"] = {
                    "status_code": response.status_code,
                    "body": response.text.strip(),
                }
        except httpx.RequestError as exc:
            info["health_error"] = {"message": str(exc)}

        try:
            datasets = await self.list_datasets()
            info["datasets"] = datasets
        except CogneeAPIError as exc:
            info.setdefault("dataset_error", str(exc))

        return info

    # ------------------------------------------------------------------
    async def raw_request(self, method: str, url: str, **kwargs: Any) -> httpx.Response:
        """Expose a raw request helper for advanced scenarios."""
        try:
            response = await self._client.request(method, url, **kwargs)
        except httpx.RequestError as exc:
            raise CogneeAPIError(f"Cognee request failed: {exc}") from exc
        return response


__all__ = ["CogneeAPIClient", "CogneeAPIError"]
