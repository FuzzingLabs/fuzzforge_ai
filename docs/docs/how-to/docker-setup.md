# How to Configure Docker for FuzzForge

Getting Docker set up correctly is essential for running FuzzForge workflows. This guide will walk you through the process, explain why each step matters, and help you troubleshoot common issues—so you can get up and running with confidence.

---

## Why Does FuzzForge Need Special Docker Configuration?

FuzzForge builds and runs custom workflow images using a local Docker registry at `localhost:5001`. By default, Docker only trusts secure (HTTPS) registries, so you need to explicitly allow this local, insecure registry for development. Without this, workflows that build or pull images will fail.

---

## Quick Setup: The One-Liner

Add this to your Docker daemon configuration:

```json
{
  "insecure-registries": ["localhost:5001"]
}
```

After editing, **restart Docker** for changes to take effect.

---

## Step-by-Step: Platform-Specific Instructions

### Docker Desktop (macOS & Windows)

#### Using the Docker Desktop UI

1. Open Docker Desktop.
2. Go to **Settings** (Windows) or **Preferences** (macOS).
3. Navigate to **Docker Engine**.
4. Add or update the `"insecure-registries"` section as shown above.
5. Click **Apply & Restart**.
6. Wait for Docker to restart (this may take a minute).

#### Editing the Config File Directly

- **macOS:** Edit `~/.docker/daemon.json`
- **Windows:** Edit `%USERPROFILE%\.docker\daemon.json`

Add or update the `"insecure-registries"` entry, then restart Docker Desktop.

---

### Docker Engine (Linux)

1. Edit (or create) `/etc/docker/daemon.json`:
   ```bash
   sudo nano /etc/docker/daemon.json
   ```
2. Add:
   ```json
   {
     "insecure-registries": ["localhost:5001"]
   }
   ```
3. Restart Docker:
   ```bash
   sudo systemctl restart docker
   ```
4. Confirm Docker is running:
   ```bash
   sudo systemctl status docker
   ```

#### Alternative: Systemd Drop-in (Advanced)

If you prefer, you can use a systemd override to add the registry flag. See the original guide for details.

---

## Verifying Your Configuration

1. **Check Docker’s registry settings:**
   ```bash
   docker info | grep -i "insecure registries"
   ```
   You should see `localhost:5001` listed.

2. **Test the registry:**
   ```bash
   curl -f http://localhost:5001/v2/ && echo "✅ Registry accessible" || echo "❌ Registry not accessible"
   ```

3. **Try pushing and pulling an image:**
   ```bash
   docker pull hello-world
   docker tag hello-world localhost:5001/hello-world:test
   docker push localhost:5001/hello-world:test
   docker rmi localhost:5001/hello-world:test
   docker pull localhost:5001/hello-world:test
   ```

---

## Worker Profiles (Resource Optimization - v0.7.0)

FuzzForge workers use Docker Compose profiles to prevent auto-startup:

```yaml
# docker-compose.yml
worker-ossfuzz:
  profiles:
    - workers    # For starting all workers
    - ossfuzz    # For starting just this worker
  restart: "no"  # Don't auto-restart
```

### Behavior

- **`docker-compose up -d`**: Workers DON'T start (saves ~6-7GB RAM)
- **CLI workflows**: Workers start automatically on-demand
- **Manual start**: `docker start fuzzforge-worker-ossfuzz`

### Resource Savings

| Command | Workers Started | RAM Usage |
|---------|----------------|-----------|
| `docker-compose up -d` | None (core only) | ~1.2 GB |
| `ff workflow run ossfuzz_campaign .` | ossfuzz worker only | ~3-5 GB |
| `docker-compose --profile workers up -d` | All workers | ~8 GB |

### Starting All Workers (Legacy Behavior)

If you prefer the old behavior where all workers start:

```bash
docker-compose --profile workers up -d
```

---

## Common Issues & How to Fix Them

### "x509: certificate signed by unknown authority"

- **What’s happening?** Docker is trying to use HTTPS for the registry.
- **How to fix:** Double-check your `"insecure-registries"` config and restart Docker.

### "connection refused" to localhost:5001

- **What’s happening?** The registry isn’t running or the port is blocked.
- **How to fix:** Make sure FuzzForge services are up (`docker compose ps`), and that nothing else is using port 5001.

### Docker Desktop doesn’t apply settings

- **How to fix:** Fully quit and restart Docker Desktop. Check for typos in your JSON config.

### "permission denied" on Linux

- **How to fix:** Add your user to the `docker` group:
  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

---

## Security Notes

- Using an insecure registry on `localhost:5001` is safe for local development.
- For production, always use a secure (HTTPS) registry and proper authentication.

---

## Where Are Docker Config Files?

- **Docker Desktop (macOS):** `~/.docker/daemon.json`
- **Docker Desktop (Windows):** `%USERPROFILE%\.docker\daemon.json`
- **Docker Engine (Linux):** `/etc/docker/daemon.json`

**Tip:** Always back up your config before making changes.

---

## Advanced: Example Configurations

### Minimal

```json
{
  "insecure-registries": ["localhost:5001"]
}
```

### With Logging and Storage Options

```json
{
  "insecure-registries": ["localhost:5001"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
```

### Multiple Registries

```json
{
  "insecure-registries": [
    "localhost:5001",
    "192.168.1.100:5000",
    "registry.internal:5000"
  ]
}
```

---

## Next Steps

- [Getting Started Guide](../tutorial/getting-started.md): Continue with FuzzForge setup.
- [Troubleshooting](troubleshooting.md): For more help if things don’t work.

---

**Remember:**
After any Docker config change, always restart Docker and verify your settings with `docker info` before running FuzzForge.
