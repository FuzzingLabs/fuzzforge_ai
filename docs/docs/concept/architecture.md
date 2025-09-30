# Architecture

FuzzForge is a distributed, containerized platform for security analysis workflows. Its architecture is designed for scalability, isolation, and reliability, drawing on modern patterns like microservices and orchestration. This page explains the core architectural concepts behind FuzzForge, meaning what the main components are, how they interact, and why the system is structured this way.

:::warning

FuzzForge’s architecture is evolving. While the long-term goal is a hexagonal architecture, the current implementation is still in transition. Expect changes as the platform matures.

:::

---

## Why This Architecture?

FuzzForge’s architecture is shaped by several key goals:

- **Scalability:** Handle many workflows in parallel, scaling up or down as needed.
- **Isolation:** Run each workflow in its own secure environment, minimizing risk.
- **Reliability:** Ensure that failures in one part of the system don’t bring down the whole platform.
- **Extensibility:** Make it easy to add new workflows, tools, or integrations.

## High-Level System Overview

At a glance, FuzzForge is organized into several layers, each with a clear responsibility:

- **Client Layer:** Where users and external systems interact (CLI, API clients, MCP server).
- **API Layer:** The FastAPI backend, which exposes REST endpoints and manages requests.
- **Orchestration Layer:** Prefect server and workers, which schedule and execute workflows.
- **Execution Layer:** Docker Engine and containers, where workflows actually run.
- **Storage Layer:** PostgreSQL database, Docker volumes, and a result cache for persistence.

Here’s a simplified view of how these layers fit together:

```mermaid
graph TB
    subgraph "Client Layer"
        CLI[CLI Client]
        API_Client[API Client]
        MCP[MCP Server]
    end

    subgraph "API Layer"
        FastAPI[FastAPI Backend]
        Router[Route Handlers]
        Middleware[Middleware Stack]
    end

    subgraph "Orchestration Layer"
        Prefect[Prefect Server]
        Workers[Prefect Workers]
        Scheduler[Workflow Scheduler]
    end

    subgraph "Execution Layer"
        Docker[Docker Engine]
        Containers[Workflow Containers]
        Registry[Docker Registry]
    end

    subgraph "Storage Layer"
        PostgreSQL[PostgreSQL Database]
        Volumes[Docker Volumes]
        Cache[Result Cache]
    end

    CLI --> FastAPI
    API_Client --> FastAPI
    MCP --> FastAPI

    FastAPI --> Router
    Router --> Middleware
    Middleware --> Prefect

    Prefect --> Workers
    Workers --> Scheduler
    Scheduler --> Docker

    Docker --> Containers
    Docker --> Registry
    Containers --> Volumes

    FastAPI --> PostgreSQL
    Workers --> PostgreSQL
    Containers --> Cache
```

## What Are the Main Components?

### API Layer

- **FastAPI Backend:** The main entry point for users and clients. Handles authentication, request validation, and exposes endpoints for workflow management, results, and health checks.
- **Middleware Stack:** Manages API keys, user authentication, CORS, logging, and error handling.

### Orchestration Layer

- **Prefect Server:** Schedules and tracks workflows, backed by PostgreSQL.
- **Prefect Workers:** Execute workflows in Docker containers. Can be scaled horizontally.
- **Workflow Scheduler:** Balances load, manages priorities, and enforces resource limits.

### Execution Layer

- **Docker Engine:** Runs workflow containers, enforcing isolation and resource limits.
- **Workflow Containers:** Custom images with security tools, mounting code and results volumes.
- **Docker Registry:** Stores and distributes workflow images.

### Storage Layer

- **PostgreSQL Database:** Stores workflow metadata, state, and results.
- **Docker Volumes:** Persist workflow results and artifacts.
- **Result Cache:** Speeds up access to recent results, with in-memory and disk persistence.

## How Does Data Flow Through the System?

### Submitting a Workflow

1. **User submits a workflow** via CLI or API client.
2. **API validates** the request and creates a deployment in Prefect.
3. **Prefect schedules** the workflow and assigns it to a worker.
4. **Worker launches a container** to run the workflow.
5. **Results are stored** in Docker volumes and the database.
6. **Status updates** flow back through Prefect and the API to the user.

```mermaid
sequenceDiagram
    participant User
    participant API
    participant Prefect
    participant Worker
    participant Container
    participant Storage

    User->>API: Submit workflow
    API->>API: Validate parameters
    API->>Prefect: Create deployment
    Prefect->>Worker: Schedule execution
    Worker->>Container: Create and start
    Container->>Container: Execute security tools
    Container->>Storage: Store SARIF results
    Worker->>Prefect: Update status
    Prefect->>API: Workflow complete
    API->>User: Return results
```

### Retrieving Results

1. **User requests status or results** via the API.
2. **API queries the database** for workflow metadata.
3. **If complete,** results are fetched from storage and returned to the user.

## How Do Services Communicate?

- **Internally:** FastAPI talks to Prefect via REST; Prefect coordinates with workers over HTTP; workers manage containers via the Docker Engine API. All core services use pooled connections to PostgreSQL.
- **Externally:** Users interact via CLI or API clients (HTTP REST). The MCP server can automate workflows via its own protocol.

## How Is Security Enforced?

- **Container Isolation:** Each workflow runs in its own Docker network, as a non-root user, with strict resource limits and only necessary volumes mounted.
- **Volume Security:** Source code is mounted read-only; results are written to dedicated, temporary volumes.
- **API Security:** All endpoints require API keys, validate inputs, enforce rate limits, and log requests for auditing.

## How Does FuzzForge Scale?

- **Horizontally:** Add more Prefect workers to handle more workflows in parallel. Scale the database with read replicas and connection pooling.
- **Vertically:** Adjust CPU and memory limits for containers and services as needed.

Example Docker Compose scaling:
```yaml
services:
  prefect-worker:
    deploy:
      resources:
        limits:
          memory: 4G
          cpus: '2.0'
        reservations:
          memory: 1G
          cpus: '0.5'
```

## How Is It Deployed?

- **Development:** All services run via Docker Compose—backend, Prefect, workers, database, and registry.
- **Production:** Add load balancers, database clustering, and multiple worker instances for high availability. Health checks, metrics, and centralized logging support monitoring and troubleshooting.

## How Is Configuration Managed?

- **Environment Variables:** Control core settings like database URLs, registry location, and Prefect API endpoints.
- **Service Discovery:** Docker Compose’s internal DNS lets services find each other by name, with consistent port mapping and health check endpoints.

Example configuration:
```bash
COMPOSE_PROJECT_NAME=fuzzforge
DATABASE_URL=postgresql://postgres:postgres@postgres:5432/fuzzforge
PREFECT_API_URL=http://prefect-server:4200/api
DOCKER_REGISTRY=localhost:5001
DOCKER_INSECURE_REGISTRY=true
```

## How Are Failures Handled?

- **Failure Isolation:** Each service is independent; failures don’t cascade. Circuit breakers and graceful degradation keep the system stable.
- **Recovery:** Automatic retries with backoff for transient errors, dead letter queues for persistent failures, and workflow state recovery after restarts.

## Implementation Details

- **Tech Stack:** FastAPI (Python async), Prefect 3.x, Docker, Docker Compose, PostgreSQL (asyncpg), and Docker networking.
- **Performance:** Workflows start in 2–5 seconds; results are retrieved quickly thanks to caching and database indexing.
- **Extensibility:** Add new workflows by deploying new Docker images; extend the API with new endpoints; configure storage backends as needed.

---

## In Summary

FuzzForge’s architecture is designed to be robust, scalable, and secure—ready to handle demanding security analysis workflows in a modern, distributed environment. As the platform evolves, expect even more modularity and flexibility, making it easier to adapt to new requirements and technologies.
