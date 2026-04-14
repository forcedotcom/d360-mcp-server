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
