"""
Cognee Integration Module for FuzzForge
Provides standardized access to project-specific knowledge graphs
Can be reused by external agents and other components
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
import asyncio
import json
from typing import Dict, List, Any, Optional, Union
from pathlib import Path

from .config_bridge import ProjectConfigManager
from .mcp.cognee_mcp_client import CogneeMCPTools
from .utils.project_env import load_project_env


class CogneeProjectIntegration:
    """
    Standardized Cognee integration that can be reused across agents
    Automatically detects project context and provides knowledge graph access
    """
    
    def __init__(self, project_dir: Optional[str] = None):
        """
        Initialize with project directory (defaults to current working directory)
        
        Args:
            project_dir: Path to project directory (optional, defaults to cwd)
        """
        self.project_dir = Path(project_dir) if project_dir else Path.cwd()
        self.config_file = self.project_dir / ".fuzzforge" / "config.yaml"
        self.project_context = None
        self._cognee = None
        self._initialized = False
        
    async def initialize(self) -> bool:
        """
        Initialize Cognee with project context
        
        Returns:
            bool: True if initialization successful
        """
        try:
            # Import Cognee
            import cognee
            self._cognee = cognee
            
            # Load project context
            if not self._load_project_context():
                return False
                
            # Configure Cognee for this project
            await self._setup_cognee_config()
            
            self._initialized = True
            return True
            
        except ImportError:
            print("Cognee not installed. Install with: pip install cognee")
            return False
        except Exception as e:
            print(f"Failed to initialize Cognee: {e}")
            return False
    
    def _load_project_context(self) -> bool:
        """Load project context from FuzzForge config"""
        try:
            if not self.config_file.exists():
                print(f"No FuzzForge config found at {self.config_file}")
                return False
                
            import yaml
            with open(self.config_file, 'r') as f:
                config = yaml.safe_load(f)
                
            self.project_context = {
                "project_name": config.get("project", {}).get("name", "default"),
                "project_id": config.get("project", {}).get("id", "default"),
                "tenant_id": config.get("cognee", {}).get("tenant", "default")
            }
            return True
            
        except Exception as e:
            print(f"Error loading project context: {e}")
            return False
    
    async def _setup_cognee_config(self):
        """Configure Cognee for project-specific access"""
        # Set API key and model
        api_key = os.getenv('OPENAI_API_KEY')
        model = os.getenv('LITELLM_MODEL', 'gpt-4o-mini')
        
        if not api_key:
            raise ValueError("OPENAI_API_KEY required for Cognee operations")
            
        # Configure Cognee
        self._cognee.config.set_llm_api_key(api_key)
        self._cognee.config.set_llm_model(model)
        self._cognee.config.set_llm_provider("openai")
        
        # Set project-specific directories
        project_cognee_dir = self.project_dir / ".fuzzforge" / "cognee" / f"project_{self.project_context['project_id']}"
        
        self._cognee.config.data_root_directory(str(project_cognee_dir / "data"))
        self._cognee.config.system_root_directory(str(project_cognee_dir / "system"))
        
        # Ensure directories exist
        project_cognee_dir.mkdir(parents=True, exist_ok=True)
        (project_cognee_dir / "data").mkdir(exist_ok=True)
        (project_cognee_dir / "system").mkdir(exist_ok=True)
    
    async def search_knowledge_graph(self, query: str, search_type: str = "GRAPH_COMPLETION", dataset: str = None) -> Dict[str, Any]:
        """
        Search the project's knowledge graph
        
        Args:
            query: Search query
            search_type: Type of search ("GRAPH_COMPLETION", "INSIGHTS", "CHUNKS", etc.)
            dataset: Specific dataset to search (optional)
            
        Returns:
            Dict containing search results
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        try:
            from cognee.modules.search.types import SearchType

            # Resolve search type dynamically; fallback to GRAPH_COMPLETION
            try:
                search_type_enum = getattr(SearchType, search_type.upper())
            except AttributeError:
                search_type_enum = SearchType.GRAPH_COMPLETION
                search_type = "GRAPH_COMPLETION"

            # Prepare search kwargs
            search_kwargs = {
                "query_type": search_type_enum,
                "query_text": query
            }
            
            # Add dataset filter if specified
            if dataset:
                search_kwargs["datasets"] = [dataset]
                
            results = await self._cognee.search(**search_kwargs)
            
            return {
                "query": query,
                "search_type": search_type,
                "dataset": dataset,
                "results": results,
                "project": self.project_context["project_name"]
            }
        except Exception as e:
            return {"error": f"Search failed: {e}"}
    
    async def list_knowledge_data(self) -> Dict[str, Any]:
        """
        List available data in the knowledge graph
        
        Returns:
            Dict containing available data
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        try:
            data = await self._cognee.list_data()
            return {
                "project": self.project_context["project_name"],
                "available_data": data
            }
        except Exception as e:
            return {"error": f"Failed to list data: {e}"}
    
    async def ingest_text_to_dataset(self, text: str, dataset: str = None) -> Dict[str, Any]:
        """
        Ingest text content into a specific dataset
        
        Args:
            text: Text to ingest
            dataset: Dataset name (defaults to project_name_codebase)
            
        Returns:
            Dict containing ingest results
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        if not dataset:
            dataset = f"{self.project_context['project_name']}_codebase"
            
        try:
            # Add text to dataset
            await self._cognee.add([text], dataset_name=dataset)
            
            # Process (cognify) the dataset
            await self._cognee.cognify([dataset])
            
            return {
                "text_length": len(text),
                "dataset": dataset,
                "project": self.project_context["project_name"],
                "status": "success"
            }
        except Exception as e:
            return {"error": f"Ingest failed: {e}"}
    
    async def ingest_files_to_dataset(self, file_paths: list, dataset: str = None) -> Dict[str, Any]:
        """
        Ingest multiple files into a specific dataset
        
        Args:
            file_paths: List of file paths to ingest
            dataset: Dataset name (defaults to project_name_codebase)
            
        Returns:
            Dict containing ingest results
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        if not dataset:
            dataset = f"{self.project_context['project_name']}_codebase"
            
        try:
            # Validate and filter readable files
            valid_files = []
            for file_path in file_paths:
                try:
                    path = Path(file_path)
                    if path.exists() and path.is_file():
                        # Test if file is readable
                        with open(path, 'r', encoding='utf-8') as f:
                            f.read(1)
                        valid_files.append(str(path))
                except (UnicodeDecodeError, PermissionError, OSError):
                    continue
            
            if not valid_files:
                return {"error": "No valid files found to ingest"}
            
            # Add files to dataset
            await self._cognee.add(valid_files, dataset_name=dataset)
            
            # Process (cognify) the dataset
            await self._cognee.cognify([dataset])
            
            return {
                "files_processed": len(valid_files),
                "total_files_requested": len(file_paths),
                "dataset": dataset,
                "project": self.project_context["project_name"],
                "status": "success"
            }
        except Exception as e:
            return {"error": f"Ingest failed: {e}"}
    
    async def list_datasets(self) -> Dict[str, Any]:
        """
        List all datasets available in the project
        
        Returns:
            Dict containing available datasets
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        try:
            # Get available datasets by searching for data
            data = await self._cognee.list_data()
            
            # Extract unique dataset names from the data
            datasets = set()
            if isinstance(data, list):
                for item in data:
                    if isinstance(item, dict) and 'dataset_name' in item:
                        datasets.add(item['dataset_name'])
            
            return {
                "project": self.project_context["project_name"],
                "datasets": list(datasets),
                "total_datasets": len(datasets)
            }
        except Exception as e:
            return {"error": f"Failed to list datasets: {e}"}
    
    async def create_dataset(self, dataset: str) -> Dict[str, Any]:
        """
        Create a new dataset (dataset is created automatically when data is added)
        
        Args:
            dataset: Dataset name to create
            
        Returns:
            Dict containing creation result
        """
        if not self._initialized:
            await self.initialize()
            
        if not self._initialized:
            return {"error": "Cognee not initialized"}
            
        try:
            # In Cognee, datasets are created implicitly when data is added
            # We'll add empty content to create the dataset
            await self._cognee.add([f"Dataset {dataset} initialized for project {self.project_context['project_name']}"], 
                                  dataset_name=dataset)
            
            return {
                "dataset": dataset,
                "project": self.project_context["project_name"],
                "status": "created"
            }
        except Exception as e:
            return {"error": f"Failed to create dataset: {e}"}
    
    def get_project_context(self) -> Optional[Dict[str, str]]:
        """Get current project context"""
        return self.project_context
    
    def is_initialized(self) -> bool:
        """Check if Cognee is initialized"""
        return self._initialized


# Convenience functions for easy integration
async def search_project_codebase(query: str, project_dir: Optional[str] = None, dataset: str = None, search_type: str = "GRAPH_COMPLETION") -> str:
    """
    Convenience function to search project codebase
    
    Args:
        query: Search query
        project_dir: Project directory (optional, defaults to cwd)
        dataset: Specific dataset to search (optional)
        search_type: Type of search ("GRAPH_COMPLETION", "INSIGHTS", "CHUNKS")
        
    Returns:
        Formatted search results as string
    """
    cognee_integration = create_cognee_integration(project_dir)
    result = await cognee_integration.search_knowledge_graph(query, search_type, dataset)
    
    if "error" in result:
        return f"Error searching codebase: {result['error']}"
    
    project_name = result.get("project", "Unknown")
    results = result.get("results", [])
    
    if not results:
        return f"No results found for '{query}' in project {project_name}"
    
    output = f"Search results for '{query}' in project {project_name}:\n\n"
    
    # Format results
    if isinstance(results, list):
        for i, item in enumerate(results, 1):
            if isinstance(item, dict):
                # Handle structured results
                output += f"{i}. "
                if "search_result" in item:
                    output += f"Dataset: {item.get('dataset_name', 'Unknown')}\n"
                    for result_item in item["search_result"]:
                        if isinstance(result_item, dict):
                            if "name" in result_item:
                                output += f"   - {result_item['name']}: {result_item.get('description', '')}\n"
                            elif "text" in result_item:
                                text = result_item["text"][:200] + "..." if len(result_item["text"]) > 200 else result_item["text"]
                                output += f"   - {text}\n"
                            else:
                                output += f"   - {str(result_item)[:200]}...\n"
                else:
                    output += f"{str(item)[:200]}...\n"
                output += "\n"
            else:
                output += f"{i}. {str(item)[:200]}...\n\n"
    else:
        output += f"{str(results)[:500]}..."
    
        return output


class CogneeServiceIntegration:
    """Cognee integration backed by the MCP service."""

    def __init__(self, project_dir: Optional[str] = None):
        self.project_dir = Path(project_dir) if project_dir else Path.cwd()
        self.project_manager: Optional[ProjectConfigManager] = None
        self.project_context: Dict[str, Any] = {}
        self.cognee_tools: Optional[CogneeMCPTools] = None
        self.dataset: Optional[str] = None
        self._initialized = False

    async def initialize(self) -> bool:
        if self._initialized:
            return True

        try:
            load_project_env(self.project_dir)
            self.project_manager = ProjectConfigManager(self.project_dir)
            self.project_manager.setup_cognee_environment()
            self.project_context = self.project_manager.get_project_context()
        except Exception:
            return False

        dataset = os.getenv("COGNEE_DEFAULT_DATASET") or os.getenv("COGNEE_DATASET_NAME")
        if not dataset:
            project_name = self.project_context.get("project_name") or "fuzzforge"
            dataset = f"{project_name}_codebase"
        self.dataset = dataset

        mcp_url = os.getenv("COGNEE_MCP_URL") or "http://localhost:18001/mcp"
        service_url = os.getenv("COGNEE_SERVICE_URL") or os.getenv("COGNEE_API_URL")
        user_email = os.getenv("COGNEE_SERVICE_USER_EMAIL") or os.getenv("DEFAULT_USER_EMAIL")
        user_password = os.getenv("COGNEE_SERVICE_USER_PASSWORD") or os.getenv("DEFAULT_USER_PASSWORD")

        self.cognee_tools = CogneeMCPTools(
            base_url=mcp_url,
            default_dataset=self.dataset,
            default_email=user_email,
            default_password=user_password,
            default_service_url=service_url,
        )

        os.environ.setdefault("COGNEE_MCP_URL", mcp_url)
        self._initialized = True
        return True

    async def search_knowledge_graph(
        self,
        query: str,
        search_type: str = "GRAPH_COMPLETION",
        dataset: Optional[str] = None,
        **kwargs: Any,
    ) -> Dict[str, Any]:
        if not self._initialized and not await self.initialize():
            return {"error": "Cognee service not initialised"}
        if not self.cognee_tools:
            return {"error": "Cognee tools unavailable"}

        target_dataset = dataset or self.dataset
        result = await self.cognee_tools.search(
            query=query,
            search_type=search_type,
            dataset=target_dataset,
            **kwargs,
        )
        return {
            "tenant": result.get("tenant") if isinstance(result, dict) else None,
            "dataset": target_dataset,
            "search_type": search_type,
            "results": result,
        }

    async def list_knowledge_data(self) -> Dict[str, Any]:
        if not self._initialized and not await self.initialize():
            return {"error": "Cognee service not initialised"}
        if not self.cognee_tools:
            return {"error": "Cognee tools unavailable"}

        datasets = await self.cognee_tools.list_datasets()
        return {
            "project": self.project_context.get("project_name"),
            "available_data": datasets,
        }

    async def ingest_text_to_dataset(self, text: str, dataset: str = None) -> Dict[str, Any]:
        if not self._initialized and not await self.initialize():
            return {"error": "Cognee service not initialised"}

        target_dataset = dataset or self.dataset
        try:
            from .cognee_service import CogneeService

            service = CogneeService(self.project_manager)
            await service.initialize()
            success = await service.ingest_text(text, target_dataset)
            if success:
                await self.cognee_tools.cognify(dataset=target_dataset, run_in_background=True)
                return {"status": "queued", "dataset": target_dataset}
            return {"error": "Cognee ingestion failed"}
        except Exception as exc:
            return {"error": f"Service ingestion failed: {exc}"}


def create_cognee_integration(project_dir: Optional[str] = None):
    storage_mode = (os.getenv("COGNEE_STORAGE_MODE") or "").lower()
    service_url = os.getenv("COGNEE_SERVICE_URL") or os.getenv("COGNEE_API_URL")
    if storage_mode == "service" or service_url:
        return CogneeServiceIntegration(project_dir)
    return CogneeProjectIntegration(project_dir)


async def list_project_knowledge(project_dir: Optional[str] = None) -> str:
    """
    Convenience function to list project knowledge
    
    Args:
        project_dir: Project directory (optional, defaults to cwd)
        
    Returns:
        Formatted list of available data
    """
    cognee_integration = create_cognee_integration(project_dir)
    result = await cognee_integration.list_knowledge_data()
    
    if "error" in result:
        return f"Error listing knowledge: {result['error']}"
    
    project_name = result.get("project", "Unknown")
    data = result.get("available_data", [])
    
    output = f"Available knowledge in project {project_name}:\n\n"
    
    if not data:
        output += "No data available in knowledge graph"
    else:
        for i, item in enumerate(data, 1):
            output += f"{i}. {item}\n"
    
    return output
