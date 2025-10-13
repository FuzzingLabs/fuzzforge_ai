"""
FuzzForge Agent Definition
The core agent that combines all components
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
import threading
import time
import socket
import asyncio
from pathlib import Path
from typing import Dict, Any, List, Optional
from google.adk import Agent
from google.adk.models.lite_llm import LiteLlm
from .agent_card import get_fuzzforge_agent_card
from .agent_executor import FuzzForgeExecutor
from .memory_service import FuzzForgeMemoryService, HybridMemoryManager

# Load environment variables from the AI module's .env file
try:
    from dotenv import load_dotenv
    _ai_dir = Path(__file__).parent
    _env_file = _ai_dir / ".env"
    if _env_file.exists():
        load_dotenv(_env_file, override=False)  # Don't override existing env vars
except ImportError:
    # dotenv not available, skip loading
    pass


class FuzzForgeAgent:
    """The main FuzzForge agent that combines card, executor, and ADK agent"""
    
    def __init__(
        self,
        model: str = None,
        cognee_url: str = None,
        port: int = 10100,
        auto_start_server: Optional[bool] = None,
    ):
        """Initialize FuzzForge agent with configuration"""
        self.model = model or os.getenv('LITELLM_MODEL', 'gpt-4o-mini')
        self.cognee_url = cognee_url or os.getenv('COGNEE_MCP_URL')
        self.port = int(os.getenv('FUZZFORGE_PORT', port))
        self._auto_start_server = (
            auto_start_server
            if auto_start_server is not None
            else os.getenv('FUZZFORGE_AUTO_A2A_SERVER', '1') not in {'0', 'false', 'False'}
        )
        self._uvicorn_server = None
        self._a2a_server_thread: Optional[threading.Thread] = None

        # Initialize ADK Memory Service for conversational memory
        memory_type = os.getenv('MEMORY_SERVICE', 'inmemory')
        self.memory_service = FuzzForgeMemoryService(memory_type=memory_type)
        
        # Create the executor (the brain) with memory and session services
        self.executor = FuzzForgeExecutor(
            model=self.model,
            cognee_url=self.cognee_url,
            debug=os.getenv('FUZZFORGE_DEBUG', '0') == '1',
            memory_service=self.memory_service,
            session_persistence=os.getenv('SESSION_PERSISTENCE', 'inmemory'),
            fuzzforge_mcp_url=os.getenv('FUZZFORGE_MCP_URL'),
        )
        
        # Create Hybrid Memory Manager (ADK + Cognee direct integration)
        # MCP tools removed - using direct Cognee integration only
        self.memory_manager = HybridMemoryManager(
            memory_service=self.memory_service,
            cognee_tools=None  # No MCP tools, direct integration used instead
        )
        
        # Get the agent card (the identity)
        self.agent_card = get_fuzzforge_agent_card(f"http://localhost:{self.port}")
        
        # Create the ADK agent (for A2A server mode)
        self.adk_agent = self._create_adk_agent()

        if self._auto_start_server:
            self._ensure_a2a_server_running()
        
    def _create_adk_agent(self) -> Agent:
        """Create the ADK agent for A2A server mode"""
        # Build instruction
        instruction = f"""You are {self.agent_card.name}, {self.agent_card.description}

Your capabilities include:
"""
        for skill in self.agent_card.skills:
            instruction += f"\n- {skill.name}: {skill.description}"
        
        instruction += """

When responding to requests:
1. Use your registered agents when appropriate
2. Use Cognee memory tools when available
3. Provide helpful, concise responses
4. Maintain context across conversations
"""
        
        # Create ADK agent
        return Agent(
            model=LiteLlm(model=self.model),
            name=self.agent_card.name,
            description=self.agent_card.description,
            instruction=instruction,
            tools=self.executor.agent.tools if hasattr(self.executor.agent, 'tools') else []
        )
    
    async def process_message(self, message: str, context_id: str = None) -> str:
        """Process a message using the executor"""
        result = await self.executor.execute(message, context_id or "default")
        return result.get("response", "No response generated")
    
    async def register_agent(self, url: str) -> Dict[str, Any]:
        """Register a new agent"""
        return await self.executor.register_agent(url)
    
    def list_agents(self) -> List[Dict[str, Any]]:
        """List registered agents"""
        return self.executor.list_agents()
    
    async def cleanup(self):
        """Clean up resources"""
        await self._stop_a2a_server()
        await self.executor.cleanup()

    def _ensure_a2a_server_running(self):
        """Start the A2A server in the background if it's not already running."""
        if self._a2a_server_thread and self._a2a_server_thread.is_alive():
            return

        try:
            from uvicorn import Config, Server
            from .a2a_server import create_a2a_app as create_custom_a2a_app
        except ImportError as exc:
            if os.getenv('FUZZFORGE_DEBUG', '0') == '1':
                print(f"[DEBUG] Unable to start A2A server automatically: {exc}")
            return

        app = create_custom_a2a_app(
            self.adk_agent,
            port=self.port,
            executor=self.executor,
        )

        log_level = os.getenv('FUZZFORGE_UVICORN_LOG_LEVEL', 'error')
        config = Config(app=app, host='127.0.0.1', port=self.port, log_level=log_level, loop='asyncio')
        server = Server(config=config)
        self._uvicorn_server = server

        def _run_server():
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)

            async def _serve():
                await server.serve()

            try:
                loop.run_until_complete(_serve())
            finally:
                loop.close()

        thread = threading.Thread(target=_run_server, name='FuzzForgeA2AServer', daemon=True)
        thread.start()
        self._a2a_server_thread = thread

        # Give the server a moment to bind to the port for downstream agents
        for _ in range(50):
            if server.should_exit:
                break
            try:
                with socket.create_connection(('127.0.0.1', self.port), timeout=0.1):
                    if os.getenv('FUZZFORGE_DEBUG', '0') == '1':
                        print(f"[DEBUG] Auto-started A2A server on http://127.0.0.1:{self.port}")
                    break
            except OSError:
                time.sleep(0.1)

    async def _stop_a2a_server(self):
        """Shut down the background A2A server if we started one."""
        server = self._uvicorn_server
        if server is None:
            return

        server.should_exit = True
        if self._a2a_server_thread and self._a2a_server_thread.is_alive():
            # Allow server loop to exit gracefully without blocking event loop
            try:
                await asyncio.wait_for(asyncio.to_thread(self._a2a_server_thread.join, 5), timeout=6)
            except (asyncio.TimeoutError, RuntimeError):
                pass

        self._uvicorn_server = None
        self._a2a_server_thread = None


# Create a singleton instance for import
_instance = None

def get_fuzzforge_agent(auto_start_server: Optional[bool] = None) -> FuzzForgeAgent:
    """Get the singleton FuzzForge agent instance"""
    global _instance
    if _instance is None:
        _instance = FuzzForgeAgent(auto_start_server=auto_start_server)
    return _instance
