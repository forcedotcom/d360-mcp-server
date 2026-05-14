# Data 360 MCP Server

A [Model Context Protocol](https://modelcontextprotocol.io) server that exposes the
**Salesforce Data 360** Connect API to MCP-compatible clients such as
Claude Code, Cursor, and any other host that speaks MCP over STDIO.

Built on [Spring AI](https://spring.io/projects/spring-ai).

## Developer Preview

The Data 360 MCP server is provided as a developer preview running in a single user/org context per process. We strongly encourage use only via STDIO instead of making this server network-accessible.

The generally available version of this MCP server is slated to be provided as a [hosted and managed Salesforce Platform MCP server, alongside other existing Product Integration MCP servers](https://developer.salesforce.com/docs/platform/hosted-mcp-servers/guide/servers-reference.html).

During the developer preview please share feature requests, issues, success stories, or other feedback via github issues on this repo, the R&D team will be actively reviewing these on a regular basis. We're especially interested in feedback about how effective the fascade tools approach works across use cases and clients.

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
| Connection | 14 | Connector metadata, connection CRUD, Snowflake connections, schema/object/field discovery |
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

## Quick start (one command)

The installer checks for Java 17+ and Maven (installs them if missing),
clones the repo, builds the server JAR, prompts for your Salesforce
credentials, and configures your MCP client (Claude Desktop, Claude Code,
or Cursor):

### macOS / Linux

```bash
curl -fsSL https://raw.githubusercontent.com/forcedotcom/d360-mcp-server/refs/heads/main/install.sh | bash
```

That's it. The script handles everything — including installing Java, Maven,
and Git if you don't have them.

If you already cloned the repo and want to build the current checkout, run:

```bash
./install.sh
```

### Windows (PowerShell)

Install [Python 3.9+](https://www.python.org/downloads/windows/) first (the
installer uses only the Python standard library). Then:

```powershell
irm https://raw.githubusercontent.com/forcedotcom/d360-mcp-server/refs/heads/main/install.py -OutFile install.py
python install.py
```

The Python installer auto-installs Git, Java 17, and Maven via `winget`
(falls back to Chocolatey if present). You may see a UAC prompt.

### Cross-platform alternative

A Python installer is also available on macOS and Linux:

```bash
python3 install.py
```

## Requirements (manual setup)

If you prefer to set things up yourself:

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

If you're using the `sf` cli you can initialize these automatically and create a session for a specific org connection with a command like:

```bash
  eval "$(
    sf org display --target-org target-org-alias --json \
      | jq -r '.result | "export DATA360_INSTANCE_URL=\(.instanceUrl)\nexport DATA360_ACCESS_TOKEN=\(.accessToken)"'
  )"
  ```

Replacing `target-org-alias` with the `sf` cli's alias for your connected org.

**Client credentials (auto-refreshing):**

This requires you to [create an External Client Application](https://help.salesforce.com/s/articleView?id=xcloud.create_a_local_external_client_app.htm&type=5) to obtain a client id and client secret to support OAuth based refreshing of credentials as needed. 

- Enable OAuth, set https://login.salesforce.com (this is unused but required in this flow).
- Add scopes: cdp_api, cdp_query_api, api.
- Under Flow Enablement, Enable Client Credentials Flow.

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

## Uninstall

If you used the installer, use its uninstall command to remove the installed
JAR and the `"data360"` entries it added to supported MCP clients:

```bash
./install.sh uninstall
```

On Windows, run:

```powershell
python install.py uninstall
```

Manual cleanup is also possible: remove `~/.data360-mcp-server`, then remove
the `"data360"` entry from your MCP client config file:
- Claude Desktop: `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) or `~/.config/Claude/claude_desktop_config.json` (Linux)
- Claude Code: `~/.claude.json`
- Cursor: `~/.cursor/mcp.json`

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for the process,
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community guidelines, and the
[Salesforce CLA](https://cla.salesforce.com/sign-cla).

## License

[Apache License 2.0](LICENSE.txt). See [LICENSE.txt](LICENSE.txt) for the full text.

## Security

Please report security issues per [SECURITY.md](SECURITY.md) — do **not** open a public
GitHub issue for security reports.