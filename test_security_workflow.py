#!/usr/bin/env python3
"""
Test security_assessment workflow with vulnerable_app test project
"""

import asyncio
import shutil
import sys
import uuid
from pathlib import Path

import boto3
from temporalio.client import Client


async def main():
    # Configuration
    temporal_address = "localhost:7233"
    s3_endpoint = "http://localhost:9000"
    s3_access_key = "fuzzforge"
    s3_secret_key = "fuzzforge123"

    # Initialize S3 client
    s3_client = boto3.client(
        's3',
        endpoint_url=s3_endpoint,
        aws_access_key_id=s3_access_key,
        aws_secret_access_key=s3_secret_key,
        region_name='us-east-1',
        use_ssl=False
    )

    print("=" * 70)
    print("Testing security_assessment workflow with vulnerable_app")
    print("=" * 70)

    # Step 1: Create tarball of vulnerable_app
    print("\n[1/5] Creating tarball of test_projects/vulnerable_app...")
    vulnerable_app_dir = Path("test_projects/vulnerable_app")

    if not vulnerable_app_dir.exists():
        print(f"❌ Error: {vulnerable_app_dir} not found")
        return 1

    target_id = str(uuid.uuid4())
    tarball_path = f"/tmp/{target_id}.tar.gz"

    # Create tarball
    shutil.make_archive(
        tarball_path.replace('.tar.gz', ''),
        'gztar',
        root_dir=vulnerable_app_dir.parent,
        base_dir=vulnerable_app_dir.name
    )

    tarball_size = Path(tarball_path).stat().st_size
    print(f"✓ Created tarball: {tarball_path} ({tarball_size / 1024:.2f} KB)")

    # Step 2: Upload to MinIO
    print(f"\n[2/5] Uploading target to MinIO (target_id={target_id})...")
    try:
        s3_key = f'{target_id}/target'
        s3_client.upload_file(
            Filename=tarball_path,
            Bucket='targets',
            Key=s3_key
        )
        print(f"✓ Uploaded to s3://targets/{s3_key}")
    except Exception as e:
        print(f"❌ Failed to upload: {e}")
        return 1
    finally:
        # Cleanup local tarball
        Path(tarball_path).unlink(missing_ok=True)

    # Step 3: Connect to Temporal
    print(f"\n[3/5] Connecting to Temporal at {temporal_address}...")
    try:
        client = await Client.connect(temporal_address)
        print("✓ Connected to Temporal")
    except Exception as e:
        print(f"❌ Failed to connect to Temporal: {e}")
        return 1

    # Step 4: Execute workflow
    print("\n[4/5] Executing security_assessment workflow...")
    workflow_id = f"security-assessment-{target_id}"

    try:
        result = await client.execute_workflow(
            "SecurityAssessmentWorkflow",
            args=[target_id],
            id=workflow_id,
            task_queue="rust-queue"
        )

        print(f"✓ Workflow completed successfully: {workflow_id}")

    except Exception as e:
        print(f"❌ Workflow execution failed: {e}")
        return 1

    # Step 5: Display results
    print("\n[5/5] Results Summary:")
    print("=" * 70)

    if result.get("status") == "success":
        summary = result.get("summary", {})
        print(f"Total findings: {summary.get('total_findings', 0)}")
        print(f"Files scanned: {summary.get('files_scanned', 0)}")

        # Display SARIF results URL if available
        if result.get("results_url"):
            print(f"Results URL: {result['results_url']}")

        # Show workflow steps
        print("\nWorkflow steps:")
        for step in result.get("steps", []):
            status_icon = "✓" if step["status"] == "success" else "✗"
            print(f"  {status_icon} {step['step']}")

        print("\n" + "=" * 70)
        print("✅ Security assessment workflow test PASSED")
        print("=" * 70)
        return 0
    else:
        print(f"❌ Workflow failed: {result.get('error', 'Unknown error')}")
        return 1


if __name__ == "__main__":
    try:
        exit_code = asyncio.run(main())
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Fatal error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
