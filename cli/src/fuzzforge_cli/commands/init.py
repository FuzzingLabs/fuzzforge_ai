"""Project initialization commands."""
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


from __future__ import annotations

from pathlib import Path
import os
from textwrap import dedent
from typing import Optional

import typer
from rich.console import Console
from rich.prompt import Confirm, Prompt

from ..config import ensure_project_config, provision_cognee_service_for_project, get_project_config
from ..database import ensure_project_db

console = Console()
app = typer.Typer()


@app.command()
def project(
    name: Optional[str] = typer.Option(
        None, "--name", "-n",
        help="Project name (defaults to current directory name)"
    ),
    api_url: Optional[str] = typer.Option(
        None, "--api-url", "-u",
        help="FuzzForge API URL (defaults to http://localhost:8000)"
    ),
    force: bool = typer.Option(
        False, "--force", "-f",
        help="Force initialization even if project already exists"
    )
):
    """
    📁 Initialize a new FuzzForge project in the current directory.

    This creates a .fuzzforge directory with:
    • SQLite database for storing runs, findings, and crashes
    • Configuration file with project settings
    • Default ignore patterns and preferences
    """
    current_dir = Path.cwd()
    fuzzforge_dir = current_dir / ".fuzzforge"

    # Check if project already exists
    if fuzzforge_dir.exists() and not force:
        if fuzzforge_dir.is_dir() and any(fuzzforge_dir.iterdir()):
            console.print("❌ FuzzForge project already exists in this directory", style="red")
            console.print("Use --force to reinitialize", style="dim")
            raise typer.Exit(1)

    # Get project name
    if not name:
        name = Prompt.ask(
            "Project name",
            default=current_dir.name,
            console=console
        )

    # Get API URL
    if not api_url:
        api_url = Prompt.ask(
            "FuzzForge API URL",
            default="http://localhost:8000",
            console=console
        )

    # Confirm initialization
    console.print(f"\n📁 Initializing FuzzForge project: [bold cyan]{name}[/bold cyan]")
    console.print(f"📍 Location: [dim]{current_dir}[/dim]")
    console.print(f"🔗 API URL: [dim]{api_url}[/dim]")

    if not Confirm.ask("\nProceed with initialization?", default=True, console=console):
        console.print("❌ Initialization cancelled", style="yellow")
        raise typer.Exit(0)

    try:
        # Create .fuzzforge directory
        console.print("\n🔨 Creating project structure...")
        fuzzforge_dir.mkdir(exist_ok=True)

        # Initialize configuration
        console.print("⚙️  Setting up configuration...")
        ensure_project_config(
            project_dir=current_dir,
            project_name=name,
            api_url=api_url,
        )

        # Initialize database
        console.print("🗄️  Initializing database...")
        ensure_project_db(current_dir)

        _ensure_env_file(fuzzforge_dir, force)
        _ensure_agents_registry(fuzzforge_dir, force)

        # Provision Cognee service user/tenant/dataset if service mode is enabled
        console.print("🧠 Provisioning Cognee service credentials...")
        provision_result = provision_cognee_service_for_project(current_dir)

        if provision_result["status"] == "success":
            console.print(f"   ✅ Created user: {provision_result.get('user', 'N/A')}", style="green")
            console.print(f"   ✅ Created tenant: {provision_result.get('tenant', 'N/A')}", style="green")
            console.print(f"   ✅ Created dataset: {provision_result.get('dataset', 'N/A')}", style="green")
        elif provision_result["status"] == "skipped":
            console.print(f"   ⏭️  Skipped: {provision_result.get('message', 'N/A')}", style="dim")
        elif provision_result["status"] == "error":
            console.print(f"   ⚠️  Warning: {provision_result.get('message', 'N/A')}", style="yellow")
            console.print("   💡 You can provision later when the service is available", style="dim")

        # Create .gitignore if needed
        gitignore_path = current_dir / ".gitignore"
        gitignore_entries = [
            "# FuzzForge CLI",
            ".fuzzforge/findings.db-*",  # SQLite temp files
            ".fuzzforge/cache/",
            ".fuzzforge/temp/",
        ]

        if gitignore_path.exists():
            with open(gitignore_path, 'r') as f:
                existing_content = f.read()

            if "# FuzzForge CLI" not in existing_content:
                with open(gitignore_path, 'a') as f:
                    f.write(f"\n{chr(10).join(gitignore_entries)}\n")
                console.print("📝 Updated .gitignore with FuzzForge entries")
        else:
            with open(gitignore_path, 'w') as f:
                f.write(f"{chr(10).join(gitignore_entries)}\n")
            console.print("📝 Created .gitignore")

        # Create README if it doesn't exist
        readme_path = current_dir / "README.md"
        if not readme_path.exists():
            readme_content = f"""# {name}

FuzzForge security testing project.

## Quick Start

```bash
# List available workflows
fuzzforge workflows

# Submit a workflow for analysis
fuzzforge workflow <workflow-name> /path/to/target

# Monitor run progress
fuzzforge monitor live <run-id>

# View findings
fuzzforge finding <run-id>
```

## Project Structure

- `.fuzzforge/` - Project data and configuration
- `.fuzzforge/config.yaml` - Project configuration
- `.fuzzforge/findings.db` - Local database for runs and findings
"""

            with open(readme_path, 'w') as f:
                f.write(readme_content)
            console.print("📚 Created README.md")

        console.print("\n✅ FuzzForge project initialized successfully!", style="green")
        console.print(f"\n🎯 Next steps:")
        console.print("   • ff workflows - See available workflows")
        console.print("   • ff status - Check API connectivity")
        console.print("   • ff workflow <workflow> <path> - Start your first analysis")
        console.print("   • edit .fuzzforge/.env with API keys & provider settings")

    except Exception as e:
        console.print(f"\n❌ Initialization failed: {e}", style="red")
        raise typer.Exit(1)


@app.callback()
def init_callback():
    """
    📁 Initialize FuzzForge projects and components
    """


def _ensure_env_file(fuzzforge_dir: Path, force: bool) -> None:
    """Create or update the .fuzzforge/.env file with AI defaults."""

    env_path = fuzzforge_dir / ".env"
    if env_path.exists() and not force:
        console.print("🧪 Using existing .fuzzforge/.env (use --force to regenerate)")
        return

    console.print("🧠 Configuring AI environment...")
    console.print("   • Default LLM provider: openai")
    console.print("   • Default LLM model: gpt-5-mini")
    console.print("   • To customise provider/model later, edit .fuzzforge/.env")

    llm_provider = "openai"
    llm_model = "gpt-5-mini"

    api_key = Prompt.ask(
        "OpenAI API key (leave blank to fill manually)",
        default="",
        show_default=False,
        console=console,
    )

    enable_cognee = False
    cognee_url = ""

    session_db_path = fuzzforge_dir / "fuzzforge_sessions.db"
    session_db_rel = session_db_path.relative_to(fuzzforge_dir.parent)

    project_config = get_project_config(fuzzforge_dir.parent)
    cognee_cfg = project_config.cognee if project_config else None

    service_url_default = "http://localhost:18000"
    service_url = os.getenv("COGNEE_SERVICE_URL") or (cognee_cfg.service_url if cognee_cfg and cognee_cfg.service_url else service_url_default)
    service_port = os.getenv("COGNEE_SERVICE_PORT") or "18000"
    s3_bucket = os.getenv("COGNEE_S3_BUCKET") or (cognee_cfg.s3_bucket if cognee_cfg and cognee_cfg.s3_bucket else "cognee-bucket")
    s3_prefix = os.getenv("COGNEE_S3_PREFIX") or (cognee_cfg.s3_prefix if cognee_cfg and cognee_cfg.s3_prefix else "cognee/projects")
    service_email = os.getenv("COGNEE_SERVICE_USER_EMAIL") or (cognee_cfg.service_user_email if cognee_cfg and cognee_cfg.service_user_email else "")
    service_password = os.getenv("COGNEE_SERVICE_USER_PASSWORD") or (cognee_cfg.service_user_password if cognee_cfg and cognee_cfg.service_user_password else "")
    aws_endpoint = os.getenv("COGNEE_AWS_ENDPOINT_URL") or ""
    aws_region = os.getenv("COGNEE_AWS_REGION") or ""
    aws_access = os.getenv("COGNEE_AWS_ACCESS_KEY_ID") or ""
    aws_secret = os.getenv("COGNEE_AWS_SECRET_ACCESS_KEY") or ""

    env_lines = [
        "# FuzzForge AI configuration",
        "# Populate the API key(s) that match your LLM provider",
        "",
        f"LLM_PROVIDER={llm_provider}",
        f"LLM_MODEL={llm_model}",
        f"LITELLM_MODEL={llm_model}",
        f"OPENAI_API_KEY={api_key}",
        f"FUZZFORGE_MCP_URL={os.getenv('FUZZFORGE_MCP_URL', 'http://localhost:8010/mcp')}",
        "",
        "# Cognee configuration mirrors the primary LLM by default",
        f"LLM_COGNEE_PROVIDER={llm_provider}",
        f"LLM_COGNEE_MODEL={llm_model}",
        f"LLM_COGNEE_API_KEY={api_key}",
        "LLM_COGNEE_ENDPOINT=",
        "COGNEE_MCP_URL=",
        "",
        "# Cognee service configuration",
        "COGNEE_STORAGE_MODE=service",
        f"COGNEE_SERVICE_URL={service_url}",
        f"COGNEE_SERVICE_PORT={service_port}",
        f"COGNEE_S3_BUCKET={s3_bucket}",
        f"COGNEE_S3_PREFIX={s3_prefix}",
        f"COGNEE_SERVICE_USER_EMAIL={service_email}",
        f"COGNEE_SERVICE_USER_PASSWORD={service_password}",
        f"COGNEE_AWS_ENDPOINT_URL={aws_endpoint}",
        f"COGNEE_AWS_REGION={aws_region}",
        f"COGNEE_AWS_ACCESS_KEY_ID={aws_access}",
        f"COGNEE_AWS_SECRET_ACCESS_KEY={aws_secret}",
        "",
        "# Session persistence options: inmemory | sqlite",
        "SESSION_PERSISTENCE=sqlite",
        f"SESSION_DB_PATH={session_db_rel}",
        "",
        "# Optional integrations",
        "AGENTOPS_API_KEY=",
        "FUZZFORGE_DEBUG=0",
        "",
    ]

    env_path.write_text("\n".join(env_lines), encoding="utf-8")
    console.print(f"📝 Created {env_path.relative_to(fuzzforge_dir.parent)}")

    template_path = fuzzforge_dir / ".env.template"
    if not template_path.exists() or force:
        template_lines = []
        for line in env_lines:
            if line.startswith("OPENAI_API_KEY="):
                template_lines.append("OPENAI_API_KEY=")
            elif line.startswith("LLM_COGNEE_API_KEY="):
                template_lines.append("LLM_COGNEE_API_KEY=")
            else:
                template_lines.append(line)
        template_path.write_text("\n".join(template_lines), encoding="utf-8")
        console.print(f"📝 Created {template_path.relative_to(fuzzforge_dir.parent)}")

    # SQLite session DB will be created automatically when first used by the AI agent


def _ensure_agents_registry(fuzzforge_dir: Path, force: bool) -> None:
    """Create a starter agents.yaml registry if needed."""

    agents_path = fuzzforge_dir / "agents.yaml"
    if agents_path.exists() and not force:
        return

    template = dedent(
        """\
        # FuzzForge Registered Agents
        # Populate this list to auto-register remote agents when the AI CLI starts
        registered_agents: []

        # Example:
        # registered_agents:
        #   - name: Calculator
        #     url: http://localhost:10201
        #     description: Sample math agent
        """.strip()
    )

    agents_path.write_text(template + "\n", encoding="utf-8")
    console.print(f"📝 Created {agents_path.relative_to(fuzzforge_dir.parent)}")
