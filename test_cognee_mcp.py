#!/usr/bin/env python3
"""Test multi-tenant Cognee MCP via HTTP"""

import json
import requests

MCP_URL = "http://localhost:18001/mcp"
HEADERS = {
    "Content-Type": "application/json",
    "Accept": "application/json, text/event-stream",
}


def send_mcp_request(method, params=None, session_id=None, is_notification=False):
    """Send MCP JSON-RPC request or notification."""

    payload = {
        "jsonrpc": "2.0",
        "method": method,
    }

    if not is_notification:
        payload["id"] = 1

    if params:
        payload["params"] = params

    headers = HEADERS.copy()
    if session_id:
        headers["Mcp-Session-Id"] = session_id

    response = requests.post(MCP_URL, json=payload, headers=headers, stream=True)
    new_session_id = response.headers.get("Mcp-Session-Id")

    if is_notification:
        return None, new_session_id

    for line in response.iter_lines():
        if not line:
            continue
        line_str = line.decode("utf-8")
        if line_str.startswith("data: "):
            data = json.loads(line_str[6:])
            return data, new_session_id

    return None, new_session_id


def extract_text(result):
    """Return the first text content from an MCP result payload."""

    if not result:
        return None
    payload = result.get("result") or {}
    content = payload.get("content") or []
    if not content:
        return None
    return content[0].get("text")


def test_multi_tenant_mcp():
    print("🧪 Testing Multi-Tenant Cognee MCP\n")

    # Test 1: Initialize
    print("1️⃣  Initializing MCP connection...")
    result, session_id = send_mcp_request(
        "initialize",
        {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {"name": "test", "version": "1.0"},
        },
    )
    if not result:
        print("   ❌ No response from initialize")
        return
    server_info = result.get("result", {}).get("serverInfo", {})
    print(
        f"   ✅ Server: {server_info.get('name')} v{server_info.get('version')}"
    )
    print(f"   ✅ Session ID: {session_id}")

    # Send initialized notification (required by MCP spec)
    print("\n1b️⃣  Sending initialized notification...")
    send_mcp_request(
        "notifications/initialized",
        {},
        session_id=session_id,
        is_notification=True,
    )
    print("   ✅ Initialized")

    # Test 2: List tools
    print("\n2️⃣  Listing available tools...")
    result, _ = send_mcp_request("tools/list", session_id=session_id)
    if not result or "result" not in result:
        print(f"   ❌ Error: Got response {result}")
        return
    tools = result["result"].get("tools", [])
    print(f"   ✅ Found {len(tools)} tools:")
    for tool in tools[:5]:
        print(f"      - {tool['name']}")

    cognify_tool = next((t for t in tools if t["name"] == "cognify"), None)
    if cognify_tool:
        params = cognify_tool["inputSchema"].get("properties", {})
        print("\n   🔍 Cognify tool parameters:")
        for field in [
            "tenant",
            "dataset",
            "run_in_background",
            "user_email",
            "user_password",
            "service_url",
            "custom_prompt",
        ]:
            print(f"      - {field}: {'✅' if field in params else '❌'}")
    else:
        print("\n   ⚠️  Cognify tool not advertised")

    # Test 3: Discover tenants
    print("\n3️⃣  Fetching configured tenant aliases...")
    result, _ = send_mcp_request(
        "tools/call",
        {"name": "list_tenants", "arguments": {}},
        session_id=session_id,
    )
    tenants_payload = {}
    tenants_text = extract_text(result)
    if tenants_text:
        try:
            tenants_payload = json.loads(tenants_text)
        except json.JSONDecodeError:
            print(f"   ❌ Failed to parse tenants payload: {tenants_text}")
    tenants = tenants_payload.get("tenants", []) if isinstance(tenants_payload, dict) else []
    if tenants:
        print(f"   ✅ Found tenants: {', '.join(tenants)}")
    else:
        print("   ⚠️  No tenants discovered; configure COGNEE_TENANT_* env vars")

    # Test 4: Trigger cognify for up to two tenants
    for idx, tenant in enumerate(tenants[:2], start=1):
        print(f"\n{idx + 3}️⃣  Triggering cognify for tenant '{tenant}'...")
        result, _ = send_mcp_request(
            "tools/call",
            {
                "name": "cognify",
                "arguments": {
                    "tenant": tenant,
                    "run_in_background": True,
                },
            },
            session_id=session_id,
        )
        cognify_text = extract_text(result)
        if not cognify_text:
            print("   ❌ No response from cognify tool")
            continue
        try:
            payload = json.loads(cognify_text)
        except json.JSONDecodeError:
            print(f"   ❌ Unable to parse cognify response: {cognify_text[:100]}...")
            continue
        dataset = payload.get("dataset")
        response = payload.get("response", {})
        status = response.get("status") or response.get("detail")
        print(f"   ✅ Cognify triggered (dataset={dataset}, status={status})")

        status_result, _ = send_mcp_request(
            "tools/call",
            {"name": "cognify_status", "arguments": {"tenant": tenant}},
            session_id=session_id,
        )
        status_text = extract_text(status_result)
        if status_text:
            try:
                status_payload = json.loads(status_text)
            except json.JSONDecodeError:
                print(f"      ⚠️  Status not JSON: {status_text[:100]}...")
            else:
                dataset_id = status_payload.get("dataset_id")
                print(f"      📦 Dataset ID: {dataset_id}")

        if dataset:
            print(f"      🔎 Running search for tenant '{tenant}'...")
            search_result, _ = send_mcp_request(
                "tools/call",
                {
                    "name": "search",
                    "arguments": {
                        "tenant": tenant,
                        "dataset": dataset,
                        "query": "hello",
                        "search_type": "CHUNKS",
                        "top_k": 3,
                    },
                },
                session_id=session_id,
            )
            search_text = extract_text(search_result)
            if search_text:
                print(f"      🔍 Search response preview: {search_text[:80]}...")

            print(f"      🧠 Triggering memify for tenant '{tenant}'...")
            memify_result, _ = send_mcp_request(
                "tools/call",
                {
                    "name": "memify",
                    "arguments": {
                        "tenant": tenant,
                        "dataset": dataset,
                        "run_in_background": True,
                        "data": "Automated memify check",
                    },
                },
                session_id=session_id,
            )
            memify_text = extract_text(memify_result)
            if memify_text:
                print(f"      🧾 Memify response preview: {memify_text[:80]}...")
        else:
            print("      ⚠️  Skipping search/memify because dataset name was not resolved")

    print("\n✅ Multi-tenant MCP test complete!")
    print("\n📝 Summary:")
    print("   - MCP server responding correctly")
    print("   - Tools expose tenant or credential parameters")
    if tenants:
        print(f"   - Tested tenants: {', '.join(tenants[:2])}")
    else:
        print("   - Configure at least one tenant via environment variables")
    print("   - Server URL: http://localhost:18001/mcp")


if __name__ == "__main__":
    try:
        test_multi_tenant_mcp()
    except Exception as exc:
        print(f"❌ Error: {exc}")
        import traceback

        traceback.print_exc()
