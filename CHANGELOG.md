# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `d360_datastream_create_third_party_connectors` tool — creates DCF data
  streams from third-party connectors (e.g. Airtable, HubSpot, Marketo,
  Google Ads). Calls `POST /ssot/data-streams`. Plus payload examples.
- `d360_transform_prepare` tool — validates a transform definition and
  enriches it with output schema. Recommended before `d360_transform_create`.
  Calls `POST /ssot/data-transforms-validation`.
- Payload examples for `BATCH` + `DCSQL` and `STREAMING` + `SQL` data
  transforms, covering both `d360_transform_prepare` and
  `d360_transform_create`.
- `d360_segment_deactivate` tool — inverse of `d360_segment_publish`. Calls
  `POST /ssot/segments/{segmentApiName}/actions/deactivate`.
  ([#9](https://github.com/forcedotcom/d360-mcp-server/issues/9))

### Changed

- New `@ApiEndpoint` annotation on every tool method declares its HTTP
  method and path. A drift-check test (`ToolMetadataDriftTest`) fails the
  build if a tool's annotation disagrees with the matching `FamilyCatalog`
  entry, keeping `search` and `payload_examples` results in lock-step with
  the actual implementations. Contributors adding new tools must annotate
  them — see `CONTRIBUTING.md` for the convention.
- JaCoCo coverage is now produced by `mvn test`
  (`target/site/jacoco/jacoco.xml`) for local coverage checks.

### Fixed

- `d360_transform_create` — fixed transform-creation failures and clarified
  the supported pairings: `BATCH` + `DCSQL` (manifest-based) and
  `STREAMING` + `SQL` (expression-based).
- `FamilyCatalog` — corrected 12 endpoint paths that disagreed with the
  actual tool implementations, so `search` and `payload_examples` return
  accurate routing hints.
- `SearchIndex` family — `FamilyCatalog` listed `/ssot/search-indexes`
  (plural) and a nested `/{id}/config`; `SearchIndexTools` actually calls
  `/ssot/search-index` (singular) with a top-level `/ssot/search-index/config`.
  Catalog now matches the tool.
- `d360_segment_delete` now uses the segment API name (developer name) in the
  path; the underlying endpoint is `DELETE /ssot/segments/{segmentApiName}`.
  ([#8](https://github.com/forcedotcom/d360-mcp-server/issues/8))
- `d360_query_sql`, `d360_query_sql_status`, `d360_query_sql_rows`, and
  `d360_query_sql_cancel` now call `/services/data/vNN.0/ssot/query-sql*`
  instead of `/services/data/vNN.0/query-sql*`. The previous paths returned
  404 on live orgs.
- `buildQueryPath` no longer double-prepends `/ssot/` now that callers pass
  the full path.
- Retriever and search-index tools now use `/ssot/machine-learning/retrievers`
  and `/ssot/search-index` respectively (were missing the prefix after the
  CdpClient→Data360Client consolidation).
- `/ssot/` is no longer misplaced inside the calculated-insights sub-path of
  `queryProfile`.

### Removed

- **Breaking:** `d360_query` (V1 ANSI SQL) and `d360_query_v2` are removed.
  Use `d360_query_sql` instead.

## [1.0.0] - 2026-04-21

### Added

- Initial public release of the Data 360 MCP Server.
- Three facade MCP tools — `search`, `payload_examples`, `execute` — consolidating
  187 underlying Data 360 operations across 21 families.
- STDIO transport for desktop MCP clients (Claude Code, Cursor, etc.).
- Auth flows: access token and client credentials — auto-detected from
  environment variables, with 1-hour token caching.
- Search strategies: keyword (default, no OpenAI key required), vector, and
  hybrid. `vector` and `hybrid` require both `OPENAI_API_KEY` and
  `SPRING_AI_MODEL_EMBEDDING=openai`; missing either variable fails fast at
  startup with a message naming both.
- Full test suite covering tool dispatch, services, and integration behavior.
