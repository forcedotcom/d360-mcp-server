# Data 360 MCP Server

A [Model Context Protocol](https://modelcontextprotocol.io) server that exposes the
**Salesforce Data 360** Connect API to MCP-compatible clients such as
Claude Code, Cursor, and any other host that speaks MCP over STDIO.

Built on [Spring AI](https://spring.io/projects/spring-ai).

## How it works

Data 360 exposes ~190 distinct REST operations. Rather than registering one MCP tool
per endpoint (which quickly overwhelms an LLM's tool list and context window), this
server consolidates everything behind **three facade tools**:

| Tool | Purpose |
|------|---------|
| `search` | Discover tools by intent, keyword, or family |
| `payload_examples` | Fetch working JSON payloads for complex tools |
| `execute` | Run any underlying tool by name with parameters |

**Typical workflow:** `search` → `payload_examples` → `execute`.

Under the hood, 187 operations are organized into 21 tool families:

| Family | Tools | Description |
|--------|-------|-------------|
| DLO | 5 | Data Lake Object CRUD |
| DMO | 5 | Data Model Object CRUD |
| Data Streams | 9 | Ingestion pipelines — generic, SFDC CRM, Snowflake, AWS S3 |
| Mappings | 7 | DLO-to-DMO field mappings |
| Data Transforms | 9 | SQL transforms — CRUD, run, validate, schedule |
| Identity Resolution | 8 | IR rulesets — create, run, publish |
| Calculated Insights | 10 | CI CRUD, run, validate, query |
| Segment | 6 | Segment CRUD + publish |
| Dataspace | 8 | Dataspace + member management |
| Connection | 11 | Connector metadata, connection CRUD, Snowflake connections |
| Query | 16 | SQL, profile, datagraph, and metadata queries |
| Activation | 10 | Activation + target CRUD |
| SDM | 38 | Semantic Data Models — dimensions, measures, relationships, queries |
| Retriever | 10 | RAG retriever + configuration CRUD |
| DataKit | 9 | Bundle deploy / undeploy |
| Data Action | 8 | Action + target CRUD |
| Search Index | 7 | Vector / hybrid search index lifecycle |
| Smart Tools | 4 | AI-assisted mapping suggestions, event-date recommendations |
| Standard Mappings | 2 | Pre-configured mapping bundles |
| Eventing | 2 | Single + batch event publish |
| GDPR | 3 | Data subject read / delete requests |

## Requirements

- Java 17+
- Maven 3.9+
- A Salesforce Data 360 org with Connect API access
- (Optional) An [OpenAI API key](https://platform.openai.com/api-keys) — only
  needed if you opt into `vector` or `hybrid` tool-search strategies

## Build

```bash
mvn clean package -DskipTests
```

This produces `target/data360-mcp-server-1.0.0.jar`.

## Authentication

The server auto-detects an auth flow from environment variables. Priority:
`access_token` → `client_credentials`.

### Getting credentials

The server needs credentials for a Salesforce org with Data 360 enabled.
You have two options, in order of convenience:

1. **Access token** — the quickest way to try the server. Run `sf org login web`
   (or the equivalent in your org tooling) and grab the access token from your
   session. Tokens expire; for anything beyond experimentation, use the
   option below.
2. **External Client App with client credentials** — create an External Client
   App in Setup, enable the Client Credentials flow, grant the `api`, `cdp_api` and
   `cdp_query_api` scopes, and use the resulting Consumer Key / Secret. The
   server will refresh tokens automatically. See
   [EXTERNAL_CLIENT_APP_SETUP.md](EXTERNAL_CLIENT_APP_SETUP.md)
   for a step-by-step walkthrough.

### Configuring the server

The default search strategy is `keyword`, which needs no extra configuration.
To enable semantic search, see
[Optional: enable semantic tool search](#optional-enable-semantic-tool-search)
below.

**Access token (default):**
```bash
export DATA360_INSTANCE_URL="https://your-instance.my.salesforce.com"
export DATA360_ACCESS_TOKEN="your_access_token"
```

**Client credentials (auto-refreshing):**
```bash
export DATA360_CLIENT_ID="your_client_id"
export DATA360_CLIENT_SECRET="your_client_secret"
export DATA360_AUTH_FLOW="client_credentials"
# Defaults to the production login endpoint; change only for sandboxes
# or My Domain logins (e.g. https://test.salesforce.com).
export DATA360_LOGIN_URL="https://login.salesforce.com"
```

**Optional:**
- `DATA360_API_VERSION` — defaults to `66.0`
- `DATA360_LOGIN_URL` — defaults to `https://login.salesforce.com`
- `DATA360_SEARCH_STRATEGY` — `keyword` (default), `vector`, or `hybrid`.
  `vector` and `hybrid` also require `OPENAI_API_KEY` and
  `SPRING_AI_MODEL_EMBEDDING=openai`.
- `OPENAI_API_KEY` — only used by `vector` / `hybrid`
- `SPRING_AI_MODEL_EMBEDDING` — set to `openai` to enable `vector` / `hybrid`;
  leave unset otherwise

### Optional: enable semantic tool search

`keyword` search is the default and works with no extra config. To use
OpenAI-backed semantic search instead, set all three of:

```bash
export DATA360_SEARCH_STRATEGY="vector"          # or "hybrid"
export SPRING_AI_MODEL_EMBEDDING="openai"
export OPENAI_API_KEY="your_openai_api_key"
```

## Running

### STDIO mode (default — for Claude Code, Cursor, etc.)

```bash
java -jar target/data360-mcp-server-1.0.0.jar
```

### Example MCP client configuration

Ready-to-paste configs for common clients live in [`examples/`](examples/).
A minimal Claude Code / Cursor config:

```json
{
  "mcpServers": {
    "data360": {
      "command": "java",
      "args": [
        "-jar", "/path/to/data360-mcp-server-1.0.0.jar"
      ],
      "env": {
        "DATA360_INSTANCE_URL": "https://your-instance.my.salesforce.com",
        "DATA360_ACCESS_TOKEN": "YOUR_TOKEN"
      }
    }
  }
}
```

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│  MCP Client (Claude Code / Cursor / etc.)                │
│  sees only: search, payload_examples, execute            │
└────────────────────┬─────────────────────────────────────┘
                     │ STDIO
┌────────────────────▼─────────────────────────────────────┐
│  Data360RuntimeConfiguration — registers 3 facade tools  │
├──────────────────────────────────────────────────────────┤
│  search            → FamilyCatalog (21 families)         │
│  payload_examples  → payload-examples.json               │
│  execute           → ToolCallbackRegistry                │
│                          ↓                               │
│                    28 concrete @McpTool classes           │
│                       (187 underlying tools)             │
│                          ↓                               │
│              Service Layer (Query, Smart,                │
│                       MappingLookup)                     │
│                          ↓                               │
│                    Data360Client                          │
│                          ↓                               │
│              Auth (AuthService, TokenCache)               │
│                   adds Bearer token                      │
│                          ↓                               │
│              Salesforce Data 360 REST API                 │
│       /ssot/*, /cdp/*, /a360/*, /connect/*, etc.         │
└──────────────────────────────────────────────────────────┘
```

## Project layout

```
src/main/java/com/salesforce/data360/mcp/
├── Application.java
├── config/          # Spring config, MCP tool registration
├── runtime/         # Tool catalog, registry, search strategies
├── service/         # Query, smart-mapping, mapping lookup
├── auth/            # Auth flows, token cache
├── client/          # Data360Client — HTTP client
├── tools/           # 25 @McpTool domain classes + 3 facade tools
│   └── datastream/  # Connector-specific data stream tools
├── model/           # Typed request/response DTOs
└── util/            # Helpers (JSON, tool utilities)

src/main/resources/
├── application.yml
├── mappings/                         # 500+ standard DLO-to-DMO mapping XMLs
└── metadata/payload-examples.json    # Curated example payloads
```

## Testing

```bash
mvn test
```

The test suite covers core tool dispatch, services, and request/response behavior.

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for the process,
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community guidelines, and the
[Salesforce CLA](https://cla.salesforce.com/sign-cla).

## License

[Apache License 2.0](LICENSE.txt). See [LICENSE.txt](LICENSE.txt) for the full text.

## Security

Please report security issues per [SECURITY.md](SECURITY.md) — do **not** open a public
GitHub issue for security reports.