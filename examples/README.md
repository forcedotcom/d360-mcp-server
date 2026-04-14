# Example MCP client configs

Ready-to-paste configurations for running the Data 360 MCP server from
various clients. In every file, replace `/absolute/path/to/data360-mcp-server-1.0.0.jar`
with the path to the JAR you built via `mvn clean package`.

| File | Client | Auth flow | Search strategy |
|------|--------|-----------|-----------------|
| `claude-code-config.json` | Claude Code | Access token | `keyword` (default) |
| `cursor-config.json` | Cursor | Client credentials (auto-refreshing) | `keyword` (default) |
| `vector-search-config.json` | Claude Code / Cursor | Client credentials (auto-refreshing) | `vector` (OpenAI-backed) |

`DATA360_LOGIN_URL` in the client-credentials example defaults to the
login endpoint (`https://login.salesforce.com`). Change it
for sandboxes or My Domain logins (e.g. `https://test.salesforce.com`).

See the root [README](../README.md#authentication) for the full list of
supported environment variables.

## Optional: semantic search

`claude-code-config.json` and `cursor-config.json` use the default `keyword`
search strategy, which needs no extra configuration.

`vector-search-config.json` shows the opt-in for OpenAI-backed semantic
search. The three extra env vars it adds are:

```json
"DATA360_SEARCH_STRATEGY": "vector",
"SPRING_AI_MODEL_EMBEDDING": "openai",
"OPENAI_API_KEY": "YOUR_OPENAI_API_KEY"
```

Use `"hybrid"` in place of `"vector"` to combine keyword and embedding
scores. See the root [README](../README.md#optional-enable-semantic-tool-search)
for details.
