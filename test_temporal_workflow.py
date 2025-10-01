#!/usr/bin/env python3
"""
Test script for Temporal workflow execution.

This script:
1. Creates a test target file
2. Uploads it to MinIO
3. Executes the rust_test workflow
4. Prints the results
"""

import asyncio
import uuid
from pathlib import Path

import boto3
from temporalio.client import Client


async def main():
    print("=" * 60)
    print("Testing Temporal Workflow Execution")
    print("=" * 60)

    # Step 1: Create a test target file
    print("\n[1/4] Creating test target file...")
    test_file = Path("/tmp/test_target.txt")
    test_file.write_text("This is a test target file for FuzzForge Temporal architecture.")
    print(f"✓ Created test file: {test_file} ({test_file.stat().st_size} bytes)")

    # Step 2: Upload to MinIO
    print("\n[2/4] Uploading target to MinIO...")
    s3_client = boto3.client(
        's3',
        endpoint_url='http://localhost:9000',
        aws_access_key_id='fuzzforge',
        aws_secret_access_key='fuzzforge123',
        region_name='us-east-1',
        use_ssl=False
    )

    # Generate target ID
    target_id = str(uuid.uuid4())
    s3_key = f'{target_id}/target'

    # Upload file
    s3_client.upload_file(
        str(test_file),
        'targets',
        s3_key,
        ExtraArgs={
            'Metadata': {
                'test': 'true',
                'uploaded_by': 'test_script'
            }
        }
    )
    print(f"✓ Uploaded to MinIO: s3://targets/{s3_key}")
    print(f"  Target ID: {target_id}")

    # Step 3: Execute workflow
    print("\n[3/4] Connecting to Temporal...")
    client = await Client.connect("localhost:7233")
    print("✓ Connected to Temporal")

    print("\n[4/4] Starting workflow execution...")
    workflow_id = f"test-workflow-{uuid.uuid4().hex[:8]}"

    # Start workflow
    handle = await client.start_workflow(
        "RustTestWorkflow",  # Workflow name (class name)
        args=[target_id],  # Arguments: target_id
        id=workflow_id,
        task_queue="rust-queue",  # Route to rust worker
    )

    print(f"✓ Workflow started!")
    print(f"  Workflow ID: {workflow_id}")
    print(f"  Run ID: {handle.first_execution_run_id}")
    print(f"\n  View in UI: http://localhost:8080/namespaces/default/workflows/{workflow_id}")

    print("\nWaiting for workflow to complete...")
    result = await handle.result()

    print("\n" + "=" * 60)
    print("✓ WORKFLOW COMPLETED SUCCESSFULLY!")
    print("=" * 60)
    print(f"\nResults:")
    print(f"  Status: {result.get('status')}")
    print(f"  Workflow ID: {result.get('workflow_id')}")
    print(f"  Target ID: {result.get('target_id')}")
    print(f"  Message: {result.get('message')}")
    print(f"  Results URL: {result.get('results_url')}")

    print(f"\nSteps executed:")
    for i, step in enumerate(result.get('steps', []), 1):
        print(f"  {i}. {step.get('step')}: {step.get('status')}")

    print("\n" + "=" * 60)
    print("Test completed successfully! 🎉")
    print("=" * 60)


if __name__ == "__main__":
    asyncio.run(main())
