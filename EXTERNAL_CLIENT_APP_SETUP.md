# External Client App setup (Client Credentials flow)

This guide walks through creating a Salesforce **External Client App (ECA)**
that the Data 360 MCP server can use to authenticate via the OAuth 2.0
**Client Credentials** flow. At the end you will have a `DATA360_CLIENT_ID`
and `DATA360_CLIENT_SECRET` that the server uses to obtain and refresh access
tokens automatically — no user login required at runtime.

## Overview

The MCP server needs a long-lived way to call Data 360 APIs. Pasting a raw
access token works for quick experiments, but tokens expire after ~2 hours.
For anything sustained, configure an External Client App and use the Client
Credentials flow. The server exchanges the ECA's **Consumer Key** and
**Consumer Secret** for an access token, caches it, and refreshes it as
needed. All API calls run as the **run-as user** you configure on the ECA.

## Prerequisites

- A Salesforce org with Data 360 provisioned.
- A user with permission to manage External Client Apps (typically
  "Customize Application" plus the ability to edit ECA settings).
- A dedicated integration / API user to act as the **run-as user**. This
  user will need a permission set granting the required Data 360 access
  (see Step 4). Using a dedicated integration user is strongly recommended
  over reusing a human user's account.

## Step 1 — Create the External Client App

1. In Salesforce Setup, search for **External Client App Manager** and
   open it.
2. Click **New External Client App**.
3. Fill in:
   - **External Client App Name** — e.g. `Data 360 MCP Server`.
   - **API Name** — auto-populated from the name.
   - **Contact Email** — your contact email.
   - **Distribution State** — `Local`.
4. Enable **API (Enable OAuth Settings)**.
5. **Callback URL** — the Client Credentials flow does not use the callback,
   but the form requires a value. Any valid URL works, e.g.
   `https://login.salesforce.com/`.
6. Under **Selected OAuth Scopes**, add:
   - `Manage Data Cloud profile data (cdp_api)`
   - `Perform ANSI SQL queries on Data Cloud data (cdp_query_api)`
   - `Manage user data via APIs (api)`

   > Scope labels have drifted across releases (e.g. "Data Cloud" vs
   > "Data 360"). If you don't see the exact label above, search by the
   > scope key in parentheses — the keys are stable.
7. In **Flow Enablement**, click **Enable Client Credentials Flow**.
8. Click **Create**.

## Step 2 — Enable the Client Credentials flow

1. Open the newly created External Client App.
2. Go to **Policies** → **OAuth Policies** (or the **Policies** tab,
   depending on your release).
3. Click on **Edit**
4. Enable **Client Credentials Flow**.
5. Set **Run As** to your integration user from the Prerequisites.
6. Save.

## Step 3 — Retrieve the Consumer Key and Consumer Secret

1. From the External Client App detail page, open **Settings** →
   **OAuth Settings** 
2. Under **App Settings** click on **Consumer Key and Secret**
3. Copy the **Consumer Key** — this becomes `DATA360_CLIENT_ID`.
4. Copy the **Consumer Secret** — this becomes `DATA360_CLIENT_SECRET`.

Treat the Consumer Secret like a password. You can rotate it from the same
screen if it is ever exposed; rotating invalidates the old secret
immediately.

## Step 4 — Grant Data 360 access to the run-as user

The access token the ECA mints runs under the run-as user, so that user must
have the Data 360 permissions needed for the operations you plan to drive
through the MCP server.

1. Create or reuse a permission set that includes the Data 360 system
   permissions you need (a broad choice is the "Data Cloud Admin" /
   "Data Cloud Architect" permission set; prefer the minimum permission set
   required for your use case).
2. Assign the permission set to the run-as user.

If you later see scope / permission errors at runtime, revisit this step —
missing permissions on the run-as user are the most common cause.

## Step 5 — Configure the MCP server

Set the following environment variables before starting the server:

```bash
export DATA360_CLIENT_ID="<Consumer Key from Step 3>"
export DATA360_CLIENT_SECRET="<Consumer Secret from Step 3>"
export DATA360_AUTH_FLOW="client_credentials"
# Defaults to the production login endpoint.
# Sandbox / scratch orgs: https://test.salesforce.com
# My Domain logins: https://<mydomain>.my.salesforce.com
export DATA360_LOGIN_URL="https://login.salesforce.com"
```

## Step 6 — Verify

Start the server:

```bash
java -jar target/data360-mcp-server-1.0.0.jar
```

In the startup logs, look for:

```
Initialized with client_credentials auth mode
```

On the first tool call that requires the Data 360 API, the server will mint
a token and log:

```
OAuth authentication successful, token cached for 3600 seconds
```

### Common errors

| Error from token endpoint | Likely cause |
|---------------------------|--------------|
| `invalid_client_id` | `DATA360_CLIENT_ID` doesn't match the Consumer Key. |
| `invalid_client` | `DATA360_CLIENT_SECRET` is wrong or has been rotated. |
| `unsupported_grant_type` | Client Credentials flow is not enabled on the ECA (Step 2). |
| `inactive_user` / `user_deleted` | Run-as user is frozen or deactivated. |
| Permission / scope errors on API calls (not the token endpoint) | Run-as user is missing the Data 360 permission set (Step 4). |

## References

- [External Client Apps overview](https://help.salesforce.com/s/articleView?id=xcloud.external_client_apps.htm&type=5)
- [Build Integrations with External Client Apps (Trailhead)](https://trailhead.salesforce.com/content/learn/projects/build-integrations-with-external-client-apps)
- [OAuth 2.0 Client Credentials Flow for External Client Apps](https://help.salesforce.com/s/articleView?id=sf.remoteaccess_oauth_client_credentials_flow.htm&type=5)
