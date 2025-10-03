"""
Android Static Analysis Workflow - Analyze APKs using Jadx, MobSF, and OpenGrep
"""

import sys
import os
import logging
import subprocess
import time
import signal
from pathlib import Path
from typing import Dict, Any

from prefect import flow, task

# S'assurer que /app est dans le PYTHONPATH (exécutions Docker)
sys.path.insert(0, "/app")

# Import des modules internes
from toolbox.modules.android.jadx import JadxModule
from toolbox.modules.android.opengrep import OpenGrepModule
from toolbox.modules.reporter import SARIFReporter
from toolbox.modules.android.mobsf import MobSFModule

# Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# ---------------------- TASKS ---------------------- #

@task(name="jadx_decompilation")
async def run_jadx_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    print("Running Jadx APK decompilation")
    print(f"   APK file: {config.get('apk_path')}")
    print(f"   Output dir: {config.get('output_dir')}")
    module = JadxModule()
    result = await module.execute(config, workspace)
    print(f"Jadx completed: {result.status}")
    if result.error:
        print(f"Jadx error: {result.error}")
    if result.status == "success":
        print(f"Jadx decompiled {result.summary.get('java_files', 0)} Java files")
        print(f"Source dir: {result.summary.get('source_dir')}")
    return result.dict()

@task(name="opengrep_analysis")
async def run_opengrep_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    print("Running OpenGrep static analysis")
    print(f"   Workspace: {workspace}")
    print(f"   Config: {config}")
    module = OpenGrepModule()
    result = await module.execute(config, workspace)
    print(f"OpenGrep completed: {result.status}")
    print(f"OpenGrep findings count: {len(result.findings)}")
    print(f"OpenGrep summary: {result.summary}")
    return result.dict()

@task(name="mobsf_analysis")
async def run_mobsf_task(workspace: Path, config: Dict[str, Any]) -> Dict[str, Any]:
    print("Running MobSF static analysis")
    print(f"   APK file: {config.get('file_path')}")
    print(f"   MobSF URL: {config.get('mobsf_url')}")

    module = MobSFModule()
    result = await module.execute(config, workspace)

    print(f"MobSF scan completed: {result.status}")
    print(f"MobSF findings count: {len(result.findings)}")
    return result.dict()

@task(name="android_report_generation")
async def generate_android_sarif_report(
    opengrep_result: Dict[str, Any],
    mobsf_result: Dict[str, Any],
    config: Dict[str, Any],
    workspace: Path
) -> Dict[str, Any]:
    logger.info("Generating SARIF report for Android scan")
    reporter = SARIFReporter()

    all_findings = []
    all_findings.extend(opengrep_result.get("findings", []))

    # Add MobSF findings if available
    if mobsf_result:
        all_findings.extend(mobsf_result.get("findings", []))

    reporter_config = {
        **(config or {}),
        "findings": all_findings,
        "tool_name": "FuzzForge Android Static Analysis",
        "tool_version": "1.0.0",
    }

    result = await reporter.execute(reporter_config, workspace)
    # Le reporter renvoie typiquement {"sarif": {...}} dans result.dict()
    return result.dict().get("sarif", {})


# ---------------------- FLOW ---------------------- #

@flow(name="android_static_analysis", log_prints=True)
async def main_flow(
    target_path: str = os.getenv("FF_TARGET_PATH", "/workspace/android_test"),
    volume_mode: str = "ro",
    apk_path: str = "",
    opengrep_config: Dict[str, Any] = {},
    custom_rules_path: str = None,
    reporter_config: Dict[str, Any] = {},
) -> Dict[str, Any]:
    """
    Android static analysis workflow using OpenGrep and MobSF.

    Args:
        target_path: Path to decompiled source code (for OpenGrep analysis)
        volume_mode: Volume mount mode (ro/rw)
        apk_path: Path to APK file for MobSF analysis (relative to workspace or absolute)
        opengrep_config: Configuration for OpenGrep module
        custom_rules_path: Path to custom OpenGrep rules
        reporter_config: Configuration for SARIF reporter
    """
    print("📱 Starting Android Static Analysis Workflow")
    print(f"Workspace: {target_path} (mode: {volume_mode})")
    workspace = Path(target_path)

    # Start MobSF server in background if APK analysis is needed
    mobsf_process = None
    if apk_path:
        print("🚀 Starting MobSF server in background...")
        try:
            mobsf_process = subprocess.Popen(
                ["bash", "-c", "cd /app/mobsf && ./run.sh 127.0.0.1:8877"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )
            print("⏳ Waiting for MobSF to initialize (45 seconds)...")
            time.sleep(45)
            print("✅ MobSF should be ready now")

            # Retrieve MobSF API key from secret file
            print("🔑 Retrieving MobSF API key...")
            try:
                secret_file = Path("/root/.MobSF/secret")
                if secret_file.exists():
                    secret = secret_file.read_text().strip()
                    if secret:
                        # API key is SHA256 hash of the secret file contents
                        import hashlib
                        api_key = hashlib.sha256(secret.encode()).hexdigest()
                        os.environ["MOBSF_API_KEY"] = api_key
                        print(f"✅ MobSF API key retrieved")
                    else:
                        print("⚠️  API key file is empty")
                else:
                    print(f"⚠️  API key file not found at {secret_file}")
            except Exception as e:
                print(f"⚠️  Error retrieving API key: {e}")
        except Exception as e:
            print(f"⚠️  Failed to start MobSF: {e}")
            mobsf_process = None

    # Resolve APK path if provided
    # Note: target_path gets mounted as /workspace/ in the execution container
    # So all paths should be relative to /workspace/
    apk_file_path = None
    if apk_path:
        apk_path_obj = Path(apk_path)
        if apk_path_obj.is_absolute():
            apk_file_path = str(apk_path_obj)
        else:
            # Relative paths are relative to /workspace/ (the mounted target directory)
            apk_file_path = f"/workspace/{apk_path}"
        print(f"APK path resolved to: {apk_file_path}")
        print(f"Checking if APK exists in target: {(Path(target_path) / apk_path).exists()}")

    # Set default Android-specific configuration if not provided
    if not opengrep_config:
        opengrep_config = {
            "languages": ["java", "kotlin"],  # Focus on Android languages
        }

    # Use custom Android rules if available, otherwise use custom_rules_path param
    if custom_rules_path:
        opengrep_config["custom_rules_path"] = custom_rules_path
    elif "custom_rules_path" not in opengrep_config:
        # Default to custom Android security rules
        opengrep_config["custom_rules_path"] = "/app/custom_opengrep_rules"

    try:
        # --- Phase 1 : Jadx Decompilation ---
        jadx_result = None
        actual_workspace = workspace
        if apk_file_path:
            print(f"Phase 1: Jadx decompilation of APK: {apk_file_path}")
            jadx_config = {
                "apk_path": apk_file_path,
                "output_dir": "jadx_output",
                "overwrite": True,
                "threads": 4,
            }
            jadx_result = await run_jadx_task(workspace, jadx_config)

            if jadx_result.get("status") == "success":
                # Use Jadx source output as workspace for OpenGrep
                source_dir = jadx_result.get("summary", {}).get("source_dir")
                if source_dir:
                    actual_workspace = Path(source_dir)
                    print(f"✅ Jadx decompiled {jadx_result.get('summary', {}).get('java_files', 0)} Java files")
                    print(f"   OpenGrep will analyze: {source_dir}")
            else:
                print(f"⚠️  Jadx failed: {jadx_result.get('error', 'unknown error')}")
        else:
            print("Phase 1: Jadx decompilation skipped (no APK provided)")

        # --- Phase 2 : OpenGrep ---
        print("Phase 2: OpenGrep analysis on source code")
        print(f"Using config: {opengrep_config}")
        opengrep_result = await run_opengrep_task(actual_workspace, opengrep_config)

        # --- Phase 3 : MobSF ---
        mobsf_result = None
        if apk_file_path:
            print(f"Phase 3: MobSF analysis on APK: {apk_file_path}")
            mobsf_config = {
                "mobsf_url": "http://localhost:8877",
                "file_path": apk_file_path,
                "api_key": os.environ.get("MOBSF_API_KEY", "")
            }
            print(f"Using MobSF config (api_key={mobsf_config['api_key'][:10]}...): {mobsf_config}")
            mobsf_result = await run_mobsf_task(workspace, mobsf_config)
            print(f"MobSF result: {mobsf_result}")
        else:
            print(f"Phase 3: MobSF analysis skipped (apk_path='{apk_path}' empty)")

        # --- Phase 4 : Rapport SARIF ---
        print("Phase 4: SARIF report generation")
        sarif_report = await generate_android_sarif_report(
            opengrep_result, mobsf_result, reporter_config or {}, workspace
        )

        findings = sarif_report.get("runs", [{}])[0].get("results", []) if sarif_report else []
        print(f"✅ Workflow complete with {len(findings)} findings")
        return sarif_report

    except Exception as e:
        logger.error(f"Workflow failed: {e}")
        print(f"❌ Workflow failed: {e}")
        # Retourner un squelette SARIF minimal en cas d'échec
        return {
            "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
            "version": "2.1.0",
            "runs": [
                {
                    "tool": {"driver": {"name": "FuzzForge Android Static Analysis"}},
                    "results": [],
                    "invocations": [
                        {
                            "executionSuccessful": False,
                            "exitCode": 1,
                            "exitCodeDescription": str(e),
                        }
                    ],
                }
            ],
        }
    finally:
        # Cleanup: Stop MobSF if it was started
        if mobsf_process:
            print("🛑 Stopping MobSF server...")
            try:
                mobsf_process.terminate()
                mobsf_process.wait(timeout=5)
                print("✅ MobSF stopped")
            except Exception as e:
                print(f"⚠️  Error stopping MobSF: {e}")
                try:
                    mobsf_process.kill()
                except:
                    pass
