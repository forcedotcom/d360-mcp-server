# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
