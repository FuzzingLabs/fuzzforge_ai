# Troubleshooting FuzzForge

Running into issues with FuzzForge? This guide will help you diagnose and resolve the most common problems—whether you’re just getting started or running complex workflows. Each section is focused on a specific area, with actionable steps and explanations.

---

## Quick Checks: Is Everything Running?

Before diving into specific errors, let’s check the basics:

```bash
# Check all FuzzForge services
docker compose ps

# Verify Docker registry config
docker info | grep -i "insecure registries"

# Test service health endpoints
curl http://localhost:8000/health
curl http://localhost:4200
curl http://localhost:5001/v2/
```

If any of these commands fail, note the error message and continue below.

---

## Docker Registry Problems

### "x509: certificate signed by unknown authority"

**What’s happening?**
Docker is trying to use HTTPS for the local registry, but it’s set up for HTTP.

**How to fix:**
1. Add this to your Docker daemon config (e.g., `/etc/docker/daemon.json`):
   ```json
   {
     "insecure-registries": ["localhost:5001"]
   }
   ```
2. Restart Docker.
3. Confirm with:
   ```bash
   docker info | grep -A 5 -i "insecure registries"
   ```

### "connection refused" to localhost:5001

**What’s happening?**
The registry isn’t running or the port is blocked.

**How to fix:**
- Make sure the registry container is up:
  ```bash
  docker compose ps registry
  ```
- Check logs for errors:
  ```bash
  docker compose logs registry
  ```
- If port 5001 is in use, change it in `docker-compose.yaml` and your Docker config.

### "no such host" error

**What’s happening?**
Docker can’t resolve `localhost`.

**How to fix:**
- Try using `127.0.0.1` instead of `localhost` in your Docker config.
- Check your `/etc/hosts` file for a correct `127.0.0.1 localhost` entry.

---

## Workflow Execution Issues

### "mounts denied" or volume errors

**What’s happening?**
Docker can’t access the path you provided.

**How to fix:**
- Always use absolute paths.
- On Docker Desktop, add your project directory to File Sharing.
- Confirm the path exists and is readable.

### Workflow status is "Crashed" or "Late"

**What’s happening?**
- "Crashed": Usually a registry, path, or tool error.
- "Late": Worker is overloaded or system is slow.

**How to fix:**
- Check logs for details:
  ```bash
  docker compose logs prefect-worker | tail -50
  ```
- Restart services:
  ```bash
  docker compose down
  docker compose up -d
  ```
- Reduce the number of concurrent workflows if your system is resource-constrained.

---

## Service Connectivity Issues

### Backend (port 8000) or Prefect UI (port 4200) not responding

**How to fix:**
- Check if the service is running:
  ```bash
  docker compose ps fuzzforge-backend
  docker compose ps prefect-server
  ```
- View logs for errors:
  ```bash
  docker compose logs fuzzforge-backend --tail 50
  docker compose logs prefect-server --tail 20
  ```
- Restart the affected service:
  ```bash
  docker compose restart fuzzforge-backend
  ```

---

## CLI Issues

### "fuzzforge: command not found"

**How to fix:**
- Install the CLI:
  ```bash
  cd cli
  pip install -e .
  ```
  or
  ```bash
  uv tool install .
  ```
- Check your PATH:
  ```bash
  which fuzzforge
  echo $PATH
  ```
- As a fallback:
  ```bash
  python -m fuzzforge_cli --help
  ```

### CLI connection errors

**How to fix:**
- Make sure the backend is running and healthy.
- Check your CLI config:
  ```bash
  fuzzforge config show
  ```
- Update the server URL if needed:
  ```bash
  fuzzforge config set-server http://localhost:8000
  ```

---

## System Resource Issues

### Out of disk space

**How to fix:**
- Clean up Docker:
  ```bash
  docker system prune -f
  docker image prune -f
  ```
- Remove old workflow images:
  ```bash
  docker images | grep localhost:5001 | awk '{print $3}' | xargs docker rmi -f
  ```

### High memory usage

**How to fix:**
- Limit the number of concurrent workflows.
- Add swap space if possible.
- Restart services to free up memory.

---

## Network Issues

### Services can’t communicate

**How to fix:**
- Check Docker network configuration:
  ```bash
  docker network ls
  docker network inspect fuzzforge_default
  ```
- Recreate the network:
  ```bash
  docker compose down
  docker network prune -f
  docker compose up -d
  ```

---

## Workflow-Specific Issues

### Static analysis or secret detection finds no issues

**What’s happening?**
- Your code may be clean, or the workflow isn’t scanning the right files.

**How to fix:**
- Make sure your target contains files to analyze:
  ```bash
  find /path/to/target -name "*.py" -o -name "*.js" -o -name "*.java" | head -10
  ```
- Test with a known-vulnerable project or file.

---

## Getting Help and Diagnostics

### Enable debug logging

```bash
export PREFECT_LOGGING_LEVEL=DEBUG
docker compose down
docker compose up -d
docker compose logs fuzzforge-backend -f
```

### Collect diagnostic info

Save and run this script to gather info for support:

```bash
#!/bin/bash
echo "=== FuzzForge Diagnostics ==="
date
docker compose ps
docker info | grep -A 5 -i "insecure registries"
curl -s http://localhost:8000/health || echo "Backend unhealthy"
curl -s http://localhost:4200 >/dev/null && echo "Prefect UI healthy" || echo "Prefect UI unhealthy"
curl -s http://localhost:5001/v2/ >/dev/null && echo "Registry healthy" || echo "Registry unhealthy"
docker compose logs --tail 10
```

### Still stuck?

- Check the [FAQ](#) (not yet available)
- Review the [Getting Started guide](../tutorial/getting-started.md)
- Submit an issue with your diagnostics output
- Join the community or check for similar issues

---

## Prevention & Maintenance Tips

- Regularly clean up Docker images and containers:
  ```bash
  docker system prune -f
  ```
- Monitor disk space and memory usage.
- Back up your configuration files (`docker-compose.yaml`, `.env`, `daemon.json`).
- Add health checks to your monitoring scripts.

---

If you have a persistent or unusual issue, don’t hesitate to reach out with logs and details. FuzzForge is designed to be robust, but every environment is unique—and your feedback helps make it better!
