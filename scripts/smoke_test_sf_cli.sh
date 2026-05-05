#!/usr/bin/env bash
# Smoke test: drive the d360 MCP server over stdio with SF_ORG_ALIAS auth and run a SQL query.
#
# Usage: SF_ORG_ALIAS=sftutor ./scripts/smoke_test_sf_cli.sh
set -euo pipefail

ALIAS="${SF_ORG_ALIAS:?SF_ORG_ALIAS must be set (and sf must be authenticated for that alias)}"
JAR="${JAR:-target/data360-mcp-server-1.0.0.jar}"

if [[ ! -f "$JAR" ]]; then
  echo "Jar not found at $JAR. Build with: mvn -DskipTests package" >&2
  exit 1
fi

# AuthModeResolver prefers DATA360_ACCESS_TOKEN over SF_ORG_ALIAS when both are set; unset any
# leftover explicit token so the SF CLI path is actually exercised.
unset DATA360_ACCESS_TOKEN DATA360_INSTANCE_URL

# JSON-RPC messages: init, initialized notification, list tools, call execute(d360_query_sql).
INIT='{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"smoke","version":"0.0.1"}}}'
INITED='{"jsonrpc":"2.0","method":"notifications/initialized","params":{}}'
LIST='{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
CALL='{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"execute","arguments":{"toolName":"d360_query_sql","paramsJson":"{\"sql\":\"SELECT 1 AS one\"}"}}}'

{
  printf '%s\n' "$INIT"
  printf '%s\n' "$INITED"
  printf '%s\n' "$LIST"
  printf '%s\n' "$CALL"
  # Give the server a moment to respond before EOF closes stdin.
  sleep 30
} | SF_ORG_ALIAS="$ALIAS" \
    java -jar "$JAR" 2> /tmp/d360-mcp.stderr
