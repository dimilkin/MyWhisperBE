# Spring_Backend

## Stack

- Spring Boot 4.1.1, Java 21, Maven — use `./mvnw`, not a system `mvn`, so the wrapper-pinned version is what actually runs.
- Root package: `com.dentalwhisper.backend`

## Structure

```
src/main/java/com/dentalwhisper/backend/
  DentalWhisperApplication.java   entry point
  config/                         Spring configuration (e.g. WebConfig for CORS)
  health/                         health-check endpoint
  patient/                        Patient, PatientController, PatientService, PatientDatabase, PatientRequest, PatientMcpTools
  voice/                           VoiceSessionController, VoiceSessionService, VoiceSessionResponse, OpenAiClientSecretResponse
```

`patient/` is the reference shape for feature packages: a `@RestController`, a `@Service`, and (for now, in place of a real datastore) an in-memory `@Component` "database" holding a `List`. `PatientDatabase` seeds two patients on startup and is not persisted — restarting the app resets it. It also exposes `GET /api/patients/search?name=` (case-insensitive partial match) — an independent REST endpoint, not currently used by the voice assistant (see `PatientMcpTools` below), but kept because it's useful on its own and curl-testable without any of the OpenAI machinery.

`PatientMcpTools` exposes `create_patient` and `get_patient_by_name` as MCP tools (`@McpTool`/`@McpToolParam` from `spring-ai-starter-mcp-server-webmvc`, Streamable-HTTP transport, default endpoint `POST /mcp`) by delegating straight to `PatientService` — no separate business logic. Both tools carry explicit `@McpTool.McpAnnotations` (`readOnlyHint`/`destructiveHint`/`idempotentHint`) since the framework's defaults mislabel a plain lookup as destructive.

`voice/` mints ephemeral OpenAI Realtime session tokens for the browser (`POST /api/voice/session`) via Spring's built-in `RestClient` — no OpenAI SDK dependency. The OpenAI API key never leaves the server; the browser only ever receives a short-lived client secret. `VoiceSessionService` attaches the MCP server above as a **remote** `type: "mcp"` tool in the Realtime session config — OpenAI's own cloud infrastructure calls `/mcp` directly, server-to-server; the browser's data channel is no longer involved in executing these tool calls at all. This only works when `mcp.server.public-url` points at a URL OpenAI's servers can actually reach — see Config below. **No auth (Spring Security/JWT) exists anywhere in this backend, including `/mcp`, which is reachable from the public internet once tunneled** — a deliberate, explicitly-accepted gap for this POC stage, not an oversight; don't assume request identity is verified, and don't add auth here without it being asked for.

New code is grouped by feature/domain under `com.dentalwhisper.backend.<feature>`, not by technical layer — avoid generic top-level `controller/`, `service/`, `repository/` packages.

## Config

- `server.port` — `${PORT:8080}`. Railway injects `PORT` at runtime and routes traffic to whatever the app listens on; the `8080` fallback is for local dev only.
- CORS allowed origin — `app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}`. Set `CORS_ALLOWED_ORIGINS` (on Railway) to the deployed Cloudflare Worker's origin, e.g. `https://mywhisper.<subdomain>.workers.dev` — see `Angular_Frontend/CLAUDE.md`. A single origin string only; this isn't parsed as a list.
- `openai.api-key` — read from the `OPENAI_API_KEY` env var (`${OPENAI_API_KEY:}` in `application.properties`). Never hardcode or commit a real key.
- `openai.realtime.model` — defaults to `gpt-realtime-mini` (cheaper; start here for command-and-control style tool calls, per the voice assistant architecture doc)
- `spring.ai.mcp.server.*` — `name`/`version` (server identity), `protocol=STREAMABLE`, `type=SYNC`, `annotation-scanner.enabled=true` (required for `@McpTool` methods to be picked up)
- `mcp.server.public-url` — from the `MCP_SERVER_PUBLIC_URL` env var. Locally, an ngrok/Cloudflare Tunnel URL pointed at `localhost:8080` (**changes every time a free-tier tunnel restarts** — update the env var each dev session). On Railway, set it to `https://${{ RAILWAY_PUBLIC_DOMAIN }}` using Railway's reference-variable syntax — it resolves to this service's own stable public domain, so it never needs touching again across redeploys. `VoiceSessionService` appends `/mcp` and only includes the MCP tool in the session config when this is set (so a session can still be started without it, just without patient tools).

## Deployment (Railway)

- `Dockerfile` — multi-stage build using the project's own `./mvnw` (not a Maven base image, so the wrapper-pinned Maven version is what actually builds), `eclipse-temurin:21-jdk-alpine` to build, `eclipse-temurin:21-jre-alpine` to run. Needs `apk add curl` in the build stage — the Maven wrapper script downloads its distribution with curl/wget, and the base image has neither by default.
- `railway.toml` — `builder = "dockerfile"`; health check hits the existing `GET /api/health` (no Actuator dependency needed/added). Required Railway dashboard env vars are documented in the file's comments: `OPENAI_API_KEY`, `MCP_SERVER_PUBLIC_URL` (see above), optional `CORS_ALLOWED_ORIGINS`.
- Both the Dockerfile and a built image were actually built and run locally (`docker build` / `docker run`) to confirm the jar name matches, `PORT`/`CORS_ALLOWED_ORIGINS` env vars are honored, and `/api/health` + `/mcp` respond correctly inside the container — not just written and assumed to work.
- The Angular frontend deploys separately, to Cloudflare Workers (static assets, not this Dockerfile) — see `Angular_Frontend/CLAUDE.md`. The two deploys are independent; each needs to know the other's URL (`CORS_ALLOWED_ORIGINS` here, `environment.prod.ts`'s `apiBaseUrl` there) and neither auto-discovers it.

## Rules

- No comments unless the WHY is non-obvious — see root `CLAUDE.md`.
- Don't add dependencies, starters, or endpoints beyond what's been explicitly requested.
- Update this file whenever the package structure, port, or key config changes.
