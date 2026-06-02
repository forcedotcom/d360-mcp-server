# Contributing to Data 360 MCP Server

Thanks for your interest in contributing to the Data 360 MCP Server.

We welcome bug fixes, tests, documentation improvements, and focused enhancements that improve the reliability and usability of the project.

## Before you start

- Search existing issues and pull requests before starting new work.
- For anything larger than a small fix or documentation change, open an issue first so maintainers can confirm the change is in scope.
- Keep proposed changes focused and easy to review.

## Reporting bugs and requesting features

- Use GitHub Issues to report bugs, request features, or ask questions about behavior.
- When reporting a bug, include clear reproduction steps, expected behavior, actual behavior, and relevant logs or stack traces when available.
- For feature requests, explain the problem you are trying to solve and why the change would be broadly useful.

## Development guidelines

- Use Java 17 or newer.
- Use Maven 3.9 or newer.
- Keep code changes small and targeted.
- Add or update tests when changing behavior.
- Update documentation when user-facing behavior, configuration, or tool semantics change.

## Build and test

Run the full test suite before submitting a pull request:

```bash
mvn test
```

To build the project locally:

```bash
mvn clean package -DskipTests
```

## Pull requests

- Open pull requests against `main` unless maintainers ask otherwise.
- Write clear commit messages and pull request descriptions.
- Reference related issues when applicable.
- Make sure tests pass before requesting review.
- Avoid mixing unrelated refactors with functional changes.

## Adding or modifying an MCP tool

`ToolMetadataDriftTest` (under `src/test/java/.../runtime/`) enforces that every `@McpTool` method has a corresponding row in `FamilyCatalog`, and that every catalog row has a backing method. No orphans, no ghosts. It also enforces that every `@McpTool` method declares an `@ApiEndpoint`, and that the declared path and verb match the corresponding `FamilyCatalog` row. The test runs as part of `mvn install`, so failures show up locally.

Every `@McpTool` method also carries an `@ApiEndpoint` annotation declaring the Connect API path and HTTP verb the method calls:

```java
@ApiEndpoint(path = "/ssot/segments/{id}", verb = "PATCH")
```

Always include `@ApiEndpoint` alongside `@McpTool` when adding a new tool. The path and verb you declare must match the corresponding row in `FamilyCatalog.java`. Tools that have no single deterministic API path (composed flows or local helpers) are listed in `NO_ENDPOINT_TOOLS` inside `ToolMetadataDriftTest` and skip the annotation.

Every new write tool (POST/PUT/PATCH) must also ship with at least one entry in `src/main/resources/metadata/payload-examples.json`, keyed by tool name. Agents rely on these examples to construct valid request bodies. A snapshot of pre-existing write tools without examples is captured in `KNOWN_MISSING_PAYLOAD_EXAMPLES` inside `ToolMetadataDriftTest`; that set may only shrink. Backfilling an example for a snapshotted tool requires removing the name from the set in the same PR.

`@McpTool(description = "...")` text affects how LLMs select tools. If you significantly change a description, flag it in the PR description so reviewers know downstream tool-selection behavior may change.

## Contributor License Agreement

In order for Salesforce to accept your contribution, you must sign the Salesforce Contributor License Agreement (CLA). You only need to do this once for Salesforce open source projects.

Complete your CLA here:

<https://cla.salesforce.com/sign-cla>

## Security

If you discover a security issue, do not open a public issue. Follow the process described in [SECURITY.md](SECURITY.md).

## Code of Conduct

Please follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## License

By contributing to this project, you agree that your contributions will be licensed under the terms of [LICENSE.txt](LICENSE.txt).
