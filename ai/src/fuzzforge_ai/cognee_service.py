"""
Cognee Service for FuzzForge
Provides integrated Cognee functionality for codebase analysis and knowledge graphs
"""
# Copyright (c) 2025 FuzzingLabs
#
# Licensed under the Business Source License 1.1 (BSL). See the LICENSE file
# at the root of this repository for details.
#
# After the Change Date (four years from publication), this version of the
# Licensed Work will be made available under the Apache License, Version 2.0.
# See the LICENSE-APACHE file or http://www.apache.org/licenses/LICENSE-2.0
#
# Additional attribution and requirements are provided in the NOTICE file.


import os
import json
import logging
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple
from datetime import datetime

logger = logging.getLogger(__name__)

from .cognee_api_client import CogneeAPIClient, CogneeAPIError


class CogneeService:
    """
    Service for managing Cognee integration with FuzzForge
    Handles multi-tenant isolation and project-specific knowledge graphs
    """
    
    def __init__(self, config):
        """Initialize with FuzzForge config"""
        self.config = config
        self._cognee = None
        self._user = None
        self._initialized = False
        self._s3_client = None
        self._api_client: Optional[CogneeAPIClient] = None
        self._dataset_id: Optional[str] = None
        self._sync_config_state()

    def _sync_config_state(self) -> None:
        """Refresh cached configuration from the project manager."""
        self.cognee_config = self.config.get_cognee_config()
        self.project_context = self.config.get_project_context()
        self.project_dir = Path(self.project_context.get("project_dir", Path.cwd()))
        self.storage_backend = self.cognee_config.get("storage_backend", "filesystem")
        mode_env = os.getenv("COGNEE_STORAGE_MODE")
        if mode_env:
            self.service_mode = mode_env
        else:
            self.service_mode = self.cognee_config.get("mode", "embedded")

        bucket_env = os.getenv("COGNEE_S3_BUCKET") or os.getenv("S3_BUCKET")
        prefix_env = os.getenv("COGNEE_S3_PREFIX")
        self.s3_bucket = bucket_env or self.cognee_config.get("s3_bucket")
        self.s3_prefix = prefix_env or self.cognee_config.get("s3_prefix")

        email_env = os.getenv("COGNEE_SERVICE_USER_EMAIL") or os.getenv("DEFAULT_USER_EMAIL")
        self.service_user_email = email_env or self.cognee_config.get("service_user_email")
        self.service_env_dir = self.cognee_config.get("service_env_dir")

        url_env = os.getenv("COGNEE_SERVICE_URL") or os.getenv("COGNEE_API_URL")
        self.service_url = url_env or self.cognee_config.get("service_url")

    def _using_service(self) -> bool:
        return (self.service_mode or "").lower() == "service" and bool(self.service_url)

    def _get_service_credentials(self) -> Tuple[str, str]:
        email = (
            self.service_user_email
            or os.getenv("COGNEE_SERVICE_USER_EMAIL")
            or os.getenv("DEFAULT_USER_EMAIL")
            or f"project_{self.project_context['project_id']}@cognee.dev"
        )
        password = (
            os.getenv("COGNEE_SERVICE_USER_PASSWORD")
            or os.getenv("DEFAULT_USER_PASSWORD")
            or f"{self.project_context['project_id'][:8]}_C0gn33!"
        )
        os.environ.setdefault("COGNEE_SERVICE_USER_EMAIL", email)
        os.environ.setdefault("DEFAULT_USER_EMAIL", email)
        os.environ.setdefault("COGNEE_SERVICE_USER_PASSWORD", password)
        os.environ.setdefault("DEFAULT_USER_PASSWORD", password)
        return email, password

    async def _initialize_remote(self) -> None:
        if not self.service_url:
            raise ValueError(
                "COGNEE_SERVICE_URL must be configured when Cognee mode is set to 'service'"
            )

        # Ensure env variables are populated so downstream components (LLM config etc.) stay aligned
        self.config.setup_cognee_environment()
        self._sync_config_state()

        email, password = self._get_service_credentials()
        tenant_name = self.project_context.get("tenant_id")

        if self._api_client:
            await self._api_client.close()

        self._api_client = CogneeAPIClient(self.service_url)

        try:
            profile = await self._api_client.ensure_user(email, password, tenant_name)
            self._user = profile
            dataset_name = self.get_project_dataset_name()
            dataset_info = await self._api_client.ensure_dataset(dataset_name)
            self._dataset_id = dataset_info.get("id")
            logger.info(
                "Connected to Cognee service",
                extra={
                    "service_url": self.service_url,
                    "project": self.project_context.get("project_name"),
                    "dataset": dataset_name,
                },
            )
        except Exception:
            if self._api_client:
                await self._api_client.close()
            self._api_client = None
            raise
    
    async def initialize(self):
        """Initialize Cognee with project-specific configuration"""
        try:
            self.config.refresh()
            self.config.setup_cognee_environment()
            self._sync_config_state()
            if self._using_service():
                await self._initialize_remote()
                self._initialized = True
                return

            logger.debug(
                "Cognee environment configured",
                extra={
                    "data": self.cognee_config.get("data_directory"),
                    "system": self.cognee_config.get("system_directory"),
                },
            )

            import cognee
            self._cognee = cognee
            
            # Configure LLM with API key BEFORE any other cognee operations
            provider = os.getenv("LLM_PROVIDER", "openai")
            model = os.getenv("LLM_MODEL") or os.getenv("LITELLM_MODEL", "gpt-4o-mini")
            api_key = os.getenv("LLM_API_KEY") or os.getenv("OPENAI_API_KEY")
            endpoint = os.getenv("LLM_ENDPOINT")
            api_version = os.getenv("LLM_API_VERSION")
            max_tokens = os.getenv("LLM_MAX_TOKENS")

            if provider.lower() in {"openai", "azure_openai", "custom"} and not api_key:
                raise ValueError(
                    "OpenAI-compatible API key is required for Cognee LLM operations. "
                    "Set OPENAI_API_KEY, LLM_API_KEY, or COGNEE_LLM_API_KEY in your .env"
                )

            # Expose environment variables for downstream libraries
            os.environ["LLM_PROVIDER"] = provider
            os.environ["LITELLM_MODEL"] = model
            os.environ["LLM_MODEL"] = model
            if api_key:
                os.environ["LLM_API_KEY"] = api_key
                # Maintain compatibility with components still expecting OPENAI_API_KEY
                if provider.lower() in {"openai", "azure_openai", "custom"}:
                    os.environ.setdefault("OPENAI_API_KEY", api_key)
            if endpoint:
                os.environ["LLM_ENDPOINT"] = endpoint
            if api_version:
                os.environ["LLM_API_VERSION"] = api_version
            if max_tokens:
                os.environ["LLM_MAX_TOKENS"] = str(max_tokens)

            # Configure Cognee's runtime using its configuration helpers when available
            if hasattr(cognee.config, "set_llm_provider"):
                cognee.config.set_llm_provider(provider)
            if hasattr(cognee.config, "set_llm_model"):
                cognee.config.set_llm_model(model)
            if api_key and hasattr(cognee.config, "set_llm_api_key"):
                cognee.config.set_llm_api_key(api_key)
            if endpoint and hasattr(cognee.config, "set_llm_endpoint"):
                cognee.config.set_llm_endpoint(endpoint)
            if api_version and hasattr(cognee.config, "set_llm_api_version"):
                cognee.config.set_llm_api_version(api_version)
            if max_tokens and hasattr(cognee.config, "set_llm_max_tokens"):
                cognee.config.set_llm_max_tokens(int(max_tokens))
            
            # Configure graph database
            cognee.config.set_graph_db_config({
                "graph_database_provider": self.cognee_config.get("graph_database_provider", "kuzu"),
            })
            
            # Set data directories
            data_dir = self.cognee_config.get("data_directory")
            system_dir = self.cognee_config.get("system_directory")
            
            if data_dir:
                logger.debug("Setting cognee data root", extra={"path": data_dir})
                cognee.config.data_root_directory(data_dir)
            if system_dir:
                logger.debug("Setting cognee system root", extra={"path": system_dir})
                cognee.config.system_root_directory(system_dir)
            
            # Setup multi-tenant user context
            await self._setup_user_context()
            
            self._initialized = True
            logger.info(
                "Cognee initialized",
                extra={
                    "project": self.project_context.get("project_name"),
                    "storage": self.storage_backend,
                    "data_root": data_dir,
                },
            )
            
        except ImportError:
            logger.error("Cognee not installed. Install with: pip install cognee")
            raise
        except Exception as e:
            logger.error(f"Failed to initialize Cognee: {e}")
            raise
    
    async def create_dataset(self):
        """Create dataset for this project if it doesn't exist"""
        if not self._initialized:
            await self.initialize()
        
        try:
            # Dataset creation is handled automatically by Cognee when adding files
            # We just ensure we have the right context set up
            dataset_name = self.get_project_dataset_name()
            logger.info(f"Dataset {dataset_name} ready for project {self.project_context['project_name']}")
            return dataset_name
        except Exception as e:
            logger.error(f"Failed to create dataset: {e}")
            raise
    
    async def _setup_user_context(self):
        """Setup user context for multi-tenant isolation"""
        if self._using_service():
            return
        try:
            from cognee.modules.users.methods import create_user, get_user
            from cognee.modules.users.methods.get_user_by_email import get_user_by_email
            from cognee.modules.users.tenants.methods import create_tenant
            from cognee.infrastructure.databases.exceptions import EntityAlreadyExistsError

            fallback_email = self.service_user_email or f"project_{self.project_context['project_id']}@cognee.dev"
            fallback_password = (
                os.getenv("COGNEE_SERVICE_USER_PASSWORD")
                or os.getenv("DEFAULT_USER_PASSWORD")
                or f"{self.project_context['project_id'][:8]}_C0gn33!"
            )

            os.environ.setdefault("DEFAULT_USER_EMAIL", fallback_email)
            os.environ.setdefault("DEFAULT_USER_PASSWORD", fallback_password)

            try:
                user = await get_user_by_email(fallback_email)
                logger.info("Using existing Cognee service user", extra={"email": fallback_email})
            except Exception:
                user = None

            if user is None:
                try:
                    user = await create_user(
                        email=fallback_email,
                        password=fallback_password,
                        is_superuser=True,
                        is_active=True,
                        is_verified=True,
                        auto_login=True,
                    )
                    logger.info(
                        "Created Cognee service user",
                        extra={"email": fallback_email, "tenant": self.project_context.get("tenant_id")},
                    )
                except Exception as exc:
                    logger.warning("Failed to create Cognee service user", exc_info=True)
                    self._user = None
                    return

            tenant_name = self.project_context.get("tenant_id") or f"fuzzforge_project_{self.project_context['project_id']}"
            if not getattr(user, "tenant_id", None):
                try:
                    await create_tenant(tenant_name, user.id)
                except EntityAlreadyExistsError:
                    logger.debug("Tenant already exists for project", extra={"tenant": tenant_name})
                except Exception as exc:
                    logger.warning("Failed to ensure Cognee tenant", exc_info=True)

                try:
                    user = await get_user(user.id)
                except Exception:
                    pass

            self._user = user
            self.service_user_email = fallback_email

        except Exception as e:
            logger.warning(f"Could not setup multi-tenant user context: {e}")
            logger.info("Proceeding with default context")
            self._user = None

    def _uses_s3_storage(self) -> bool:
        if (self.storage_backend or "").lower() == "s3":
            return True
        if os.getenv("STORAGE_BACKEND", "").lower() == "s3":
            return True

        data_dir = (self.cognee_config.get("data_directory") or "").lower()
        system_dir = (self.cognee_config.get("system_directory") or "").lower()
        return data_dir.startswith("s3://") or system_dir.startswith("s3://")

    def _relative_ingest_path(self, file_path: Path) -> str:
        try:
            relative = file_path.relative_to(self.project_dir)
        except ValueError:
            relative = file_path.name
        return str(relative).replace(os.sep, "/")

    def _resolve_s3_paths(self) -> Tuple[str, str]:
        bucket = (
            self.s3_bucket
            or os.getenv("STORAGE_BUCKET_NAME")
            or os.getenv("COGNEE_S3_BUCKET")
            or os.getenv("S3_BUCKET")
        )
        if not bucket:
            raise ValueError("S3 bucket not configured for Cognee service storage")

        prefix = (
            self.s3_prefix
            or os.getenv("COGNEE_S3_PREFIX")
            or f"cognee/projects/{self.project_context['project_id']}"
        )
        return bucket, prefix.rstrip("/")

    def _get_s3_client(self):
        if self._s3_client is not None:
            return self._s3_client

        try:
            import boto3
        except ImportError as exc:
            raise RuntimeError("boto3 is required for Cognee S3 ingestion") from exc

        endpoint = os.getenv("AWS_ENDPOINT_URL") or os.getenv("S3_ENDPOINT")
        access_key = os.getenv("AWS_ACCESS_KEY_ID") or os.getenv("S3_ACCESS_KEY")
        secret_key = os.getenv("AWS_SECRET_ACCESS_KEY") or os.getenv("S3_SECRET_KEY")
        region = (
            os.getenv("AWS_REGION")
            or os.getenv("AWS_DEFAULT_REGION")
            or os.getenv("S3_REGION")
            or "us-east-1"
        )
        use_ssl = os.getenv("S3_USE_SSL", "false").lower() == "true"

        if not (access_key and secret_key):
            raise ValueError("AWS/S3 credentials missing for Cognee service ingestion")

        self._s3_client = boto3.client(
            "s3",
            endpoint_url=endpoint,
            aws_access_key_id=access_key,
            aws_secret_access_key=secret_key,
            region_name=region,
            use_ssl=use_ssl,
        )
        return self._s3_client

    def _upload_files_to_s3(self, file_paths: List[Path]) -> Tuple[List[str], Dict[str, str]]:
        bucket, prefix = self._resolve_s3_paths()
        client = self._get_s3_client()

        timestamp = datetime.utcnow().strftime("%Y%m%dT%H%M%S")
        try:
            from uuid import uuid4
        except ImportError:  # pragma: no cover - standard lib always available
            uuid4 = None  # type: ignore

        batch_suffix = uuid4().hex[:8] if uuid4 else "batch"
        base_prefix = f"{prefix}/uploads/{self.project_context['project_id']}/{timestamp}_{batch_suffix}"

        uploaded: Dict[str, str] = {}
        uris: List[str] = []
        for file_path in file_paths:
            key = f"{base_prefix}/{self._relative_ingest_path(file_path)}"
            with open(file_path, "rb") as handle:
                client.upload_fileobj(handle, bucket, key)
            uri = f"s3://{bucket}/{key}"
            uploaded[str(file_path)] = uri
            uris.append(uri)

        return uris, uploaded

    def _prepare_ingest_payload(self, file_paths: List[Path]) -> Tuple[List[str], Dict[str, str]]:
        if self._uses_s3_storage():
            return self._upload_files_to_s3(file_paths)
        return [str(path) for path in file_paths], {}
    
    def get_project_dataset_name(self, dataset_suffix: str = "codebase") -> str:
        """Get project-specific dataset name"""
        return f"{self.project_context['project_name']}_{dataset_suffix}"
    
    async def ingest_text(self, content: str, dataset: str = None) -> bool:
        """Ingest text content into knowledge graph"""
        if not self._initialized:
            await self.initialize()
        dataset = dataset or self.get_project_dataset_name()
        
        try:
            if self._using_service():
                if not self._api_client:
                    raise RuntimeError("Cognee API client not initialized")
                await self._api_client.ensure_dataset(dataset)
                await self._api_client.ingest_text(dataset, content)
                await self._api_client.cognify(dataset)
                return True

            await self._cognee.add([content], dataset)
            await self._cognee.cognify([dataset])
            return True
        except Exception as e:
            logger.error(f"Failed to ingest text: {e}")
            return False
    
    async def ingest_files(self, file_paths: List[Path], dataset: str = None) -> Dict[str, Any]:
        """Ingest multiple files into knowledge graph"""
        if not self._initialized:
            await self.initialize()
        self._sync_config_state()
        dataset = dataset or self.get_project_dataset_name()
        
        results = {
            "success": 0,
            "failed": 0,
            "errors": [],
            "uploaded": {},
        }
        readable_files: List[Path] = []

        for file_path in file_paths:
            try:
                with open(file_path, "r", encoding="utf-8"):
                    pass
                readable_files.append(Path(file_path))
            except (UnicodeDecodeError, PermissionError) as exc:
                results["failed"] += 1
                results["errors"].append(f"{file_path}: {exc}")
                logger.warning("Skipping %s: %s", file_path, exc)

        if not readable_files:
            return results

        try:
            if self._using_service():
                if not self._api_client:
                    raise RuntimeError("Cognee API client not initialized")
                await self._api_client.ensure_dataset(dataset)
                service_result = await self._api_client.ingest_files(dataset, readable_files)
                await self._api_client.cognify(dataset)
                results["success"] = len(readable_files)
                results["service"] = service_result
            else:
                ingest_payload, uploaded = self._prepare_ingest_payload(readable_files)
                await self._cognee.add(ingest_payload, dataset_name=dataset)
                await self._cognee.cognify([dataset])
                results["success"] = len(readable_files)
                if uploaded:
                    results["uploaded"] = uploaded
        except Exception as e:
            logger.error("Failed to ingest files", exc_info=True)
            results["errors"].append(f"Cognify error: {str(e)}")
            results["failed"] += len(readable_files)
            results["success"] = 0
            return results

        return results
    
    async def search_insights(self, query: str, dataset: str = None) -> List[str]:
        """Search for insights in the knowledge graph"""
        if not self._initialized:
            await self.initialize()

        try:
            if self._using_service():
                if not self._api_client:
                    raise RuntimeError("Cognee API client not initialized")
                dataset_name = dataset or self.get_project_dataset_name()
                results = await self._api_client.search(
                    query,
                    datasets=[dataset_name] if dataset_name else None,
                    search_type="INSIGHTS",
                )
                return self._normalise_service_results(results)

            from cognee.modules.search.types import SearchType
            
            kwargs = {
                "query_type": SearchType.INSIGHTS,
                "query_text": query
            }
            
            if dataset:
                kwargs["datasets"] = [dataset]
            
            results = await self._cognee.search(**kwargs)
            return results if isinstance(results, list) else []
            
        except Exception as e:
            logger.error(f"Failed to search insights: {e}")
            return []
    
    async def search_chunks(self, query: str, dataset: str = None) -> List[str]:
        """Search for relevant text chunks"""
        if not self._initialized:
            await self.initialize()
        
        try:
            if self._using_service():
                if not self._api_client:
                    raise RuntimeError("Cognee API client not initialized")
                dataset_name = dataset or self.get_project_dataset_name()
                results = await self._api_client.search(
                    query,
                    datasets=[dataset_name] if dataset_name else None,
                    search_type="CHUNKS",
                )
                return self._normalise_service_results(results)

            from cognee.modules.search.types import SearchType
            
            kwargs = {
                "query_type": SearchType.CHUNKS,
                "query_text": query
            }
            
            if dataset:
                kwargs["datasets"] = [dataset]
            
            results = await self._cognee.search(**kwargs)
            return results if isinstance(results, list) else []
            
        except Exception as e:
            logger.error(f"Failed to search chunks: {e}")
            return []
    
    async def search_graph_completion(self, query: str) -> List[str]:
        """Search for graph completion (relationships)"""
        if not self._initialized:
            await self.initialize()
        
        try:
            if self._using_service():
                if not self._api_client:
                    raise RuntimeError("Cognee API client not initialized")
                results = await self._api_client.search(
                    query,
                    datasets=[self.get_project_dataset_name()],
                    search_type="GRAPH_COMPLETION",
                )
                return self._normalise_service_results(results)

            from cognee.modules.search.types import SearchType
            
            results = await self._cognee.search(
                query_type=SearchType.GRAPH_COMPLETION,
                query_text=query
            )
            return results if isinstance(results, list) else []
            
        except Exception as e:
            logger.error(f"Failed to search graph completion: {e}")
            return []

    async def get_status(self) -> Dict[str, Any]:
        """Get service status and statistics"""
        self._sync_config_state()
        status = {
            "initialized": self._initialized,
            "enabled": self.cognee_config.get("enabled", True),
            "provider": self.cognee_config.get("graph_database_provider", "kuzu"),
            "data_directory": self.cognee_config.get("data_directory"),
            "system_directory": self.cognee_config.get("system_directory"),
            "storage_backend": self.storage_backend,
            "service_mode": self.service_mode,
            "s3_bucket": self.s3_bucket,
            "s3_prefix": self.s3_prefix,
            "dataset_name": self.get_project_dataset_name(),
        }
        if self._dataset_id:
            status["dataset_id"] = self._dataset_id

        if self._initialized and self._using_service() and self._api_client:
            try:
                status.update(await self._api_client.status())
            except CogneeAPIError as exc:
                status["service_error"] = str(exc)

        if self._initialized and not self._uses_s3_storage() and not self._using_service():
            try:
                data_dir = Path(status["data_directory"])
                system_dir = Path(status["system_directory"])

                status.update(
                    {
                        "data_dir_exists": data_dir.exists(),
                        "system_dir_exists": system_dir.exists(),
                        "kuzu_db_exists": (system_dir / "kuzu_db").exists(),
                        "lancedb_exists": (system_dir / "lancedb").exists(),
                    }
                )

            except Exception as e:
                status["status_error"] = str(e)
        else:
            status["s3_storage_active"] = self._uses_s3_storage()

        return status

    def _normalise_service_results(self, payload: Any) -> List[str]:
        if payload is None:
            return []
        if isinstance(payload, list):
            return [self._stringify_service_item(item) for item in payload]
        if isinstance(payload, dict):
            if "results" in payload and isinstance(payload["results"], list):
                return [self._stringify_service_item(item) for item in payload["results"]]
            if "data" in payload and isinstance(payload["data"], list):
                return [self._stringify_service_item(item) for item in payload["data"]]
        return [self._stringify_service_item(payload)]

    @staticmethod
    def _stringify_service_item(item: Any) -> str:
        if isinstance(item, str):
            return item
        try:
            return json.dumps(item, ensure_ascii=False)
        except Exception:
            return str(item)

    async def close(self) -> None:
        if self._api_client:
            await self._api_client.close()
            self._api_client = None
    
    async def clear_data(self, confirm: bool = False):
        """Clear all ingested data (dangerous!)"""
        if not confirm:
            raise ValueError("Must confirm data clearing with confirm=True")
        
        if not self._initialized:
            await self.initialize()
        
        if self._using_service():
            if not self._api_client:
                raise RuntimeError("Cognee API client not initialized")
            dataset_name = self.get_project_dataset_name()
            try:
                datasets = await self._api_client.list_datasets()
                target = next((d for d in datasets if d.get("name") == dataset_name), None)
                if target and target.get("id"):
                    await self._api_client.delete_dataset(target["id"])
                    logger.info("Deleted Cognee dataset via service", extra={"dataset": dataset_name})
                else:
                    logger.info(
                        "No matching Cognee dataset found to delete", extra={"dataset": dataset_name}
                    )
            except Exception as exc:
                logger.error("Failed to clear Cognee dataset via service", exc_info=True)
                raise
            return

        try:
            await self._cognee.prune.prune_data()
            await self._cognee.prune.prune_system(metadata=True)
            logger.info("Cognee data cleared")
        except Exception as e:
            logger.error(f"Failed to clear data: {e}")
            raise


class FuzzForgeCogneeIntegration:
    """
    Main integration class for FuzzForge + Cognee
    Provides high-level operations for security analysis
    """
    
    def __init__(self, config):
        self.service = CogneeService(config)
    
    async def analyze_codebase(self, path: Path, recursive: bool = True) -> Dict[str, Any]:
        """
        Analyze a codebase and extract security-relevant insights
        """
        # Collect code files
        from fuzzforge_ai.ingest_utils import collect_ingest_files

        files = collect_ingest_files(path, recursive, None, [])
        
        if not files:
            return {"error": "No files found to analyze"}
        
        # Ingest files
        results = await self.service.ingest_files(files, "security_analysis")
        
        if results["success"] == 0:
            return {"error": "Failed to ingest any files", "details": results}
        
        # Extract security insights
        security_queries = [
            "vulnerabilities security risks",
            "authentication authorization",
            "input validation sanitization", 
            "encryption cryptography",
            "error handling exceptions",
            "logging sensitive data"
        ]
        
        insights = {}
        for query in security_queries:
            insight_results = await self.service.search_insights(query, "security_analysis")
            if insight_results:
                insights[query.replace(" ", "_")] = insight_results
        
        return {
            "files_processed": results["success"],
            "files_failed": results["failed"],
            "errors": results["errors"],
            "security_insights": insights
        }
    
    async def query_codebase(self, query: str, search_type: str = "insights") -> List[str]:
        """Query the ingested codebase"""
        if search_type == "insights":
            return await self.service.search_insights(query)
        elif search_type == "chunks":
            return await self.service.search_chunks(query)
        elif search_type == "graph":
            return await self.service.search_graph_completion(query)
        else:
            raise ValueError(f"Unknown search type: {search_type}")
    
    async def get_project_summary(self) -> Dict[str, Any]:
        """Get a summary of the analyzed project"""
        # Search for general project insights
        summary_queries = [
            "project structure components",
            "main functionality features",
            "programming languages frameworks",
            "dependencies libraries"
        ]
        
        summary = {}
        for query in summary_queries:
            results = await self.service.search_insights(query)
            if results:
                summary[query.replace(" ", "_")] = results[:3]  # Top 3 results
        
        return summary
