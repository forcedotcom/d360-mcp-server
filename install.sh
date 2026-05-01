#!/usr/bin/env bash
# Copyright (c) 2026, Salesforce, Inc.
# SPDX-License-Identifier: Apache-2.0
#
# One-command installer for the Data 360 MCP Server.
#
# Usage (remote — clones, builds, and configures everything):
#   curl -fsSL https://raw.githubusercontent.com/forcedotcom/d360-mcp-server/refs/heads/main/install.sh | bash
#
# Usage (local — run from inside a cloned repo):
#   ./install.sh                  # install / upgrade
#   ./install.sh uninstall        # remove JAR and data360 entry from MCP configs
#   ./install.sh --help           # show help
#   ./install.sh --version        # show installer version
#
# What it does:
#   1. Checks for / installs Java 17+, Maven, Git, and python3
#   2. Clones the repo (if not already in it) and builds the fat JAR
#   3. Prompts for Salesforce credentials (secrets are masked)
#   4. Configures your MCP client (Claude Desktop, Claude Code, Cursor) atomically

set -euo pipefail

# Read prompts from the controlling terminal when available so `curl | bash`
# installs can still ask for credentials after stdin has carried the script.
if ! { exec 3</dev/tty; } 2>/dev/null; then
    exec 3<&0
fi

# ── Constants ────────────────────────────────────────────────────────────────

INSTALLER_VERSION="1.1.0"
INSTALL_DIR="${HOME}/.data360-mcp-server"
REPO_URL="https://github.com/forcedotcom/d360-mcp-server"
REPO_BRANCH="main"
DEFAULT_API_VERSION="66.0"

# Populated by build_jar
JAR_NAME=""
# Well-known path — set early so load_existing_credentials can find it on re-runs,
# before build_mcp_config has had a chance to run.
MCP_CONFIG_FILE="${INSTALL_DIR}/.mcp_config.json"

# ── Colors ───────────────────────────────────────────────────────────────────

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

info()  { printf "${BLUE}[info]${NC}  %s\n" "$1"; }
ok()    { printf "${GREEN}[ok]${NC}    %s\n" "$1"; }
warn()  { printf "${YELLOW}[warn]${NC}  %s\n" "$1"; }
fail()  { printf "${RED}[error]${NC} %s\n" "$1"; exit 1; }

# Visible prompt (read -rp echoes input)
prompt() { read -rp "$1" "$2" <&3; }

# Hidden prompt for secrets (does not echo)
prompt_secret() {
    read -rsp "$1" "$2" <&3
    printf '\n'
}

# Retry a command up to 3 times with brief backoff. Preserves args.
retry() {
    local n=1 max=3 delay=2
    while true; do
        if "$@"; then return 0; fi
        if [ "$n" -ge "$max" ]; then return 1; fi
        warn "Command failed (attempt ${n}/${max}), retrying in ${delay}s..."
        sleep "$delay"
        n=$((n + 1))
        delay=$((delay * 2))
    done
}

# ── CLI parsing ──────────────────────────────────────────────────────────────

show_help() {
    cat <<EOF
Data 360 MCP Server installer v${INSTALLER_VERSION}

Usage:
  ./install.sh                  Install or upgrade
  ./install.sh uninstall        Remove JAR and data360 entry from MCP configs
  ./install.sh --help           Show this help
  ./install.sh --version        Show installer version

The installer will check for and install Java 17+, Maven, Git, and python3 as
needed, build the server JAR, collect Salesforce credentials, and merge a
data360 entry into your chosen MCP client config files.
EOF
}

# ── Detect OS ────────────────────────────────────────────────────────────────

detect_os() {
    OS="$(uname -s)"
    case "${OS}" in
        Darwin) OS_TYPE="macos" ;;
        Linux)  OS_TYPE="linux" ;;
        *)      fail "Unsupported OS: ${OS}. This installer supports macOS and Linux." ;;
    esac
}

# ── Check for Homebrew (macOS) ───────────────────────────────────────────────

ensure_homebrew() {
    if [ "${OS_TYPE}" != "macos" ]; then
        return 0
    fi
    if command -v brew &>/dev/null; then
        return 0
    fi
    info "Homebrew not found. Installing..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" </dev/null
    # Add to PATH for Apple Silicon and Intel Macs
    if [ -f "/opt/homebrew/bin/brew" ]; then
        eval "$(/opt/homebrew/bin/brew shellenv)"
    elif [ -f "/usr/local/bin/brew" ]; then
        eval "$(/usr/local/bin/brew shellenv)"
    fi
    ok "Homebrew installed"
}

# ── Check / Install Git ─────────────────────────────────────────────────────

check_git() {
    if command -v git &>/dev/null; then
        return 0
    fi
    info "Git not found. Installing..."
    if [ "${OS_TYPE}" = "macos" ]; then
        ensure_homebrew
        brew install git
    else
        if command -v apt-get &>/dev/null; then
            retry sudo apt-get update -qq && sudo apt-get install -y -qq git
        elif command -v yum &>/dev/null; then
            sudo yum install -y -q git
        else
            fail "Cannot auto-install git. Please install it manually and re-run."
        fi
    fi
    ok "Git installed"
}

# ── Check / Install Java 17+ ────────────────────────────────────────────────

# Parse a Java major version from `java -version` output.
# Handles both modern ("17.0.5") and legacy ("1.8.0_xxx") formats.
# Echoes the major version, or "0" if unknown.
java_major_version() {
    local raw major
    raw=$(java -version 2>&1 | awk -F'"' '/version/ {print $2; exit}')
    [ -z "${raw}" ] && { echo 0; return; }
    # Take first dotted component
    major="${raw%%.*}"
    # Legacy "1.8.0_xxx" → use second component
    if [ "${major}" = "1" ]; then
        major=$(printf '%s' "${raw}" | awk -F'.' '{print $2}')
    fi
    [[ "${major}" =~ ^[0-9]+$ ]] || major=0
    echo "${major}"
}

check_java() {
    info "Checking for Java 17+..."

    if command -v java &>/dev/null; then
        local java_version
        java_version=$(java_major_version)
        if [ "${java_version}" -ge 17 ] 2>/dev/null; then
            ok "Java ${java_version} found"
            return 0
        else
            warn "Java ${java_version} found, but 17+ is required"
        fi
    else
        warn "Java not found"
    fi

    info "Installing Java 17..."
    if [ "${OS_TYPE}" = "macos" ]; then
        ensure_homebrew
        brew install openjdk@17
        if [ -d "$(brew --prefix)/opt/openjdk@17/bin" ]; then
            export PATH="$(brew --prefix)/opt/openjdk@17/bin:${PATH}"
        fi
    else
        if command -v apt-get &>/dev/null; then
            retry sudo apt-get update -qq && sudo apt-get install -y -qq openjdk-17-jdk
        elif command -v yum &>/dev/null; then
            sudo yum install -y -q java-17-openjdk-devel
        else
            fail "Cannot auto-install Java. Please install Java 17+ manually and re-run."
        fi
    fi
    ok "Java 17 installed"
}

# ── Check / Install Maven ───────────────────────────────────────────────────

check_maven() {
    info "Checking for Maven..."

    if command -v mvn &>/dev/null; then
        local mvn_version
        mvn_version=$(mvn -v 2>&1 | awk '/Apache Maven/ {print $3; exit}')
        ok "Maven found (${mvn_version})"
        return 0
    fi

    info "Installing Maven..."
    if [ "${OS_TYPE}" = "macos" ]; then
        ensure_homebrew
        brew install maven
    else
        if command -v apt-get &>/dev/null; then
            retry sudo apt-get update -qq && sudo apt-get install -y -qq maven
        elif command -v yum &>/dev/null; then
            sudo yum install -y -q maven
        else
            fail "Cannot auto-install Maven. Please install it manually and re-run."
        fi
    fi
    ok "Maven installed"
}

# ── Check / Install python3 ─────────────────────────────────────────────────

check_python3() {
    if command -v python3 &>/dev/null; then
        return 0
    fi
    info "python3 not found. Installing..."
    if [ "${OS_TYPE}" = "macos" ]; then
        ensure_homebrew
        brew install python3
    else
        if command -v apt-get &>/dev/null; then
            retry sudo apt-get update -qq && sudo apt-get install -y -qq python3
        elif command -v yum &>/dev/null; then
            sudo yum install -y -q python3
        else
            fail "Cannot auto-install python3. Please install it manually and re-run."
        fi
    fi
    ok "python3 installed"
}

# ── Build the JAR ───────────────────────────────────────────────────────────

build_jar() {
    mkdir -p "${INSTALL_DIR}"
    chmod 700 "${INSTALL_DIR}"
    local build_dir=""
    local cloned_tmpdir=""

    if [ -f "pom.xml" ] && grep -q "data360-mcp-server" pom.xml 2>/dev/null; then
        info "Building from local repo..."
        build_dir="."
    else
        check_git
        cloned_tmpdir=$(mktemp -d)
        info "Cloning repo..."
        if ! git clone --depth 1 --branch "${REPO_BRANCH}" "${REPO_URL}" "${cloned_tmpdir}/d360-mcp-server" </dev/null 2>&1 | tail -5; then
            printf "\n"
            fail "Failed to clone ${REPO_URL}.
    Possible causes:
      • GitHub credentials not cached — try: gh auth login
      • Repository access not granted — check github.com/forcedotcom/d360-mcp-server
      • Network connectivity issue
    Fix the above and re-run."
        fi
        build_dir="${cloned_tmpdir}/d360-mcp-server"
    fi

    info "Building JAR (this may take a minute on first run)..."
    local build_log="${INSTALL_DIR}/build.log"
    if ! (cd "${build_dir}" && mvn clean package -DskipTests) </dev/null > "${build_log}" 2>&1; then
        printf "\n--- Last 40 lines of build log ---\n"
        tail -40 "${build_log}"
        printf "\n"
        fail "Maven build failed. Full log: ${build_log}"
    fi

    # Glob the produced JAR — avoids hardcoding a version that drifts from pom.xml.
    local jar=""
    for candidate in "${build_dir}/target/"data360-mcp-server-*.jar; do
        case "${candidate}" in
            *.original) continue ;;
        esac
        [ -f "${candidate}" ] && jar="${candidate}" && break
    done
    [ -n "${jar}" ] || fail "Build succeeded but no JAR found in ${build_dir}/target/"

    JAR_NAME=$(basename "${jar}")
    cp "${jar}" "${INSTALL_DIR}/"
    chmod 644 "${INSTALL_DIR}/${JAR_NAME}"

    # Clean up temp dir if we cloned
    if [ -n "${cloned_tmpdir}" ]; then
        rm -rf "${cloned_tmpdir}"
    fi

    ok "JAR installed to ${INSTALL_DIR}/${JAR_NAME}"
}

# ── Credential reuse ────────────────────────────────────────────────────────

# Try to load credentials from an existing MCP_CONFIG_FILE into the shell vars.
# Returns 0 on success (AUTH_FLOW and relevant fields populated), 1 otherwise.
load_existing_credentials() {
    [ -f "${MCP_CONFIG_FILE}" ] || return 1

    local parsed
    parsed=$(python3 - "${MCP_CONFIG_FILE}" <<'PYEOF' 2>/dev/null
import json, sys
try:
    with open(sys.argv[1]) as f:
        cfg = json.load(f)
    env = cfg["mcpServers"]["data360"]["env"]
except Exception:
    sys.exit(1)
flow = env.get("DATA360_AUTH_FLOW", "access_token")
print(f"FLOW={flow}")
for k in ("DATA360_INSTANCE_URL", "DATA360_ACCESS_TOKEN",
          "DATA360_CLIENT_ID", "DATA360_CLIENT_SECRET",
          "DATA360_LOGIN_URL", "DATA360_API_VERSION"):
    v = env.get(k, "")
    # Shell-single-quote escape
    v_esc = v.replace("'", "'\"'\"'")
    print(f"{k}='{v_esc}'")
PYEOF
    ) || return 1

    eval "${parsed}"
    AUTH_FLOW="${FLOW:-access_token}"
    return 0
}

# ── Collect credentials ─────────────────────────────────────────────────────

# Normalize and validate a Salesforce instance URL.
# Echoes the normalized value on success. Fails with a clear message otherwise.
validate_instance_url() {
    local url="$1"
    # Strip trailing slashes
    url="${url%/}"
    if [[ ! "${url}" =~ ^https:// ]]; then
        fail "Instance URL must start with https:// (got: '${url}')"
    fi
    # Reject anything with a path segment beyond host
    local after_scheme="${url#*://}"
    if [[ "${after_scheme}" == */* ]]; then
        fail "Instance URL should be host-only, with no path (got: '${url}')"
    fi
    echo "${url}"
}

collect_credentials() {
    # Offer to reuse stored credentials on re-run.
    if load_existing_credentials; then
        printf "\n${BOLD}Existing credentials detected${NC} for flow '%s'.\n" "${AUTH_FLOW}"
        local reuse_choice=""
        prompt "Reuse them? [Y/n]: " reuse_choice
        case "${reuse_choice:-Y}" in
            Y|y|Yes|yes|"") ok "Reusing stored credentials"; return 0 ;;
        esac
        # Fall through to fresh collection
        unset DATA360_INSTANCE_URL DATA360_ACCESS_TOKEN
        unset DATA360_CLIENT_ID DATA360_CLIENT_SECRET DATA360_LOGIN_URL
        unset DATA360_API_VERSION AUTH_FLOW
    fi

    printf "\n"
    printf "${BOLD}Salesforce Data 360 Credentials${NC}\n"
    printf "Choose an auth method:\n"
    printf "  1) Access token  — quick setup, tokens expire\n"
    printf "  2) Client credentials  — auto-refreshing, recommended\n"
    printf "\n"
    prompt "Choose [1/2]: " auth_choice

    case "${auth_choice}" in
        1)
            AUTH_FLOW="access_token"
            local raw_url=""
            prompt "DATA360_INSTANCE_URL (e.g. https://your-org.my.salesforce.com): " raw_url
            [ -n "${raw_url}" ] || fail "Instance URL is required"
            DATA360_INSTANCE_URL=$(validate_instance_url "${raw_url}")
            prompt_secret "DATA360_ACCESS_TOKEN (hidden): " DATA360_ACCESS_TOKEN
            [ -n "${DATA360_ACCESS_TOKEN}" ] || fail "Access token is required"
            ;;
        2)
            AUTH_FLOW="client_credentials"
            prompt "DATA360_CLIENT_ID: " DATA360_CLIENT_ID
            [ -n "${DATA360_CLIENT_ID}" ] || fail "Client ID is required"
            prompt_secret "DATA360_CLIENT_SECRET (hidden): " DATA360_CLIENT_SECRET
            [ -n "${DATA360_CLIENT_SECRET}" ] || fail "Client secret is required"
            local default_login="https://login.salesforce.com"
            local input_login_url=""
            prompt "DATA360_LOGIN_URL [${default_login}]: " input_login_url || true
            DATA360_LOGIN_URL="${input_login_url:-${default_login}}"
            ;;
        *)
            fail "Invalid choice"
            ;;
    esac

    # Optional API version override
    local input_api=""
    prompt "DATA360_API_VERSION [${DEFAULT_API_VERSION}]: " input_api || true
    DATA360_API_VERSION="${input_api:-${DEFAULT_API_VERSION}}"
}

# ── Build MCP server config snippet ─────────────────────────────────────────
# Uses python3 for JSON construction so special characters in credentials
# ($, ", backticks, !) are properly escaped.

build_mcp_config() {
    local jar_path="${INSTALL_DIR}/${JAR_NAME}"

    if [ "${AUTH_FLOW}" = "access_token" ]; then
        python3 - "${jar_path}" "${DATA360_INSTANCE_URL}" "${DATA360_ACCESS_TOKEN}" \
                  "${DATA360_API_VERSION}" "${MCP_CONFIG_FILE}" <<'PYEOF'
import json, sys
jar, url, token, api_version, out = sys.argv[1:6]
cfg = {"mcpServers": {"data360": {"command": "java", "args": ["-jar", jar], "env": {
    "DATA360_AUTH_FLOW": "access_token",
    "DATA360_INSTANCE_URL": url,
    "DATA360_ACCESS_TOKEN": token,
    "DATA360_API_VERSION": api_version,
}}}}
with open(out, "w") as f:
    json.dump(cfg, f, indent=2)
    f.write("\n")
PYEOF
    else
        python3 - "${jar_path}" "${DATA360_CLIENT_ID}" "${DATA360_CLIENT_SECRET}" \
                  "${DATA360_LOGIN_URL}" "${DATA360_API_VERSION}" "${MCP_CONFIG_FILE}" <<'PYEOF'
import json, sys
jar, cid, secret, login, api_version, out = sys.argv[1:7]
cfg = {"mcpServers": {"data360": {"command": "java", "args": ["-jar", jar], "env": {
    "DATA360_AUTH_FLOW": "client_credentials",
    "DATA360_CLIENT_ID": cid,
    "DATA360_CLIENT_SECRET": secret,
    "DATA360_LOGIN_URL": login,
    "DATA360_API_VERSION": api_version,
}}}}
with open(out, "w") as f:
    json.dump(cfg, f, indent=2)
    f.write("\n")
PYEOF
    fi

    chmod 600 "${MCP_CONFIG_FILE}"
    ok "MCP config generated"
}

# ── Configure MCP clients ───────────────────────────────────────────────────

# Atomically merge the data360 entry into a JSON config file.
# Takes a .bak snapshot before the first modification, writes to a tmp, then mv.
merge_mcp_config() {
    local config_file="$1"
    local dir
    dir=$(dirname "${config_file}")
    mkdir -p "${dir}"

    if [ -f "${config_file}" ]; then
        # One-time backup, never overwrite an existing .bak to preserve original.
        [ -f "${config_file}.bak" ] || cp -p "${config_file}" "${config_file}.bak"

        local tmp="${config_file}.tmp.$$"
        python3 - "${config_file}" "${MCP_CONFIG_FILE}" "${tmp}" <<'PYEOF'
import json, sys
config_file, new_file, tmp = sys.argv[1], sys.argv[2], sys.argv[3]
with open(config_file) as f:
    existing = json.load(f)
with open(new_file) as f:
    new_server = json.load(f)
existing.setdefault("mcpServers", {})
existing["mcpServers"]["data360"] = new_server["mcpServers"]["data360"]
with open(tmp, "w") as f:
    json.dump(existing, f, indent=2)
    f.write("\n")
PYEOF
        mv "${tmp}" "${config_file}"
        chmod 600 "${config_file}"
        ok "  Updated data360 in ${config_file} (backup: ${config_file}.bak)"
    else
        cp "${MCP_CONFIG_FILE}" "${config_file}"
        chmod 600 "${config_file}"
        ok "  Created ${config_file}"
    fi
}

# Returns 0 if the given client is detected on this machine.
client_present_claude_desktop() {
    if [ "$(uname)" = "Darwin" ]; then
        [ -d "${HOME}/Library/Application Support/Claude" ] || \
        [ -d "/Applications/Claude.app" ]
    else
        [ -d "${HOME}/.config/Claude" ]
    fi
}
client_present_claude_code() {
    command -v claude &>/dev/null || [ -f "${HOME}/.claude.json" ]
}
client_present_cursor() {
    [ -d "${HOME}/.cursor" ] || \
    ([ "$(uname)" = "Darwin" ] && [ -d "/Applications/Cursor.app" ]) || \
    command -v cursor &>/dev/null
}

configure_clients() {
    printf "\n"
    printf "${BOLD}Configure MCP Clients${NC}\n"

    local cd_note="" cc_note="" cu_note=""
    client_present_claude_desktop || cd_note="  (not detected)"
    client_present_claude_code    || cc_note="  (not detected)"
    client_present_cursor         || cu_note="  (not detected)"

    printf "Which clients would you like to configure?\n"
    printf "  1) Claude Desktop%s\n" "${cd_note}"
    printf "  2) Claude Code%s\n" "${cc_note}"
    printf "  3) Cursor%s\n" "${cu_note}"
    printf "  4) All detected\n"
    printf "  5) None — I'll configure manually\n"
    printf "\n"
    prompt "Choose [1/2/3/4/5]: " client_choice

    case "${client_choice}" in
        1) configure_claude_desktop ;;
        2) configure_claude_code ;;
        3) configure_cursor ;;
        4)
            client_present_claude_desktop && configure_claude_desktop || warn "Skipping Claude Desktop (not detected)"
            client_present_claude_code    && configure_claude_code    || warn "Skipping Claude Code (not detected)"
            client_present_cursor         && configure_cursor         || warn "Skipping Cursor (not detected)"
            ;;
        5)
            printf "\n${BOLD}Manual configuration:${NC}\n"
            printf "Your MCP server config was saved (with secrets) to:\n"
            printf "  ${BOLD}%s${NC}\n\n" "${MCP_CONFIG_FILE}"
            printf "Copy its contents into your MCP client's configuration.\n"
            printf "(File permissions are 600 — do not print it in shared terminals.)\n"
            ;;
        *)
            fail "Invalid choice"
            ;;
    esac
}

configure_claude_desktop() {
    info "Configuring Claude Desktop..."
    local config_file
    if [ "$(uname)" = "Darwin" ]; then
        config_file="${HOME}/Library/Application Support/Claude/claude_desktop_config.json"
    else
        config_file="${HOME}/.config/Claude/claude_desktop_config.json"
    fi
    merge_mcp_config "${config_file}"
}

configure_claude_code() {
    info "Configuring Claude Code..."
    # Prefer the supported CLI when available — Claude Code owns ~/.claude.json
    # and may rewrite unrelated fields on its own.
    if command -v claude &>/dev/null; then
        local server_json
        server_json=$(python3 - "${MCP_CONFIG_FILE}" <<'PYEOF'
import json, sys
with open(sys.argv[1]) as f:
    cfg = json.load(f)
print(json.dumps(cfg["mcpServers"]["data360"]))
PYEOF
        )
        # Remove existing entry first to keep this idempotent
        claude mcp remove data360 &>/dev/null || true
        if claude mcp add-json --scope user data360 "${server_json}" </dev/null; then
            ok "  Registered data360 via 'claude mcp add-json --scope user'"
            return 0
        fi
        warn "'claude mcp add-json' failed — falling back to direct merge"
    fi
    local config_file="${HOME}/.claude.json"
    merge_mcp_config "${config_file}"
}

configure_cursor() {
    info "Configuring Cursor..."
    local config_file="${HOME}/.cursor/mcp.json"
    merge_mcp_config "${config_file}"
}

# ── Uninstall ────────────────────────────────────────────────────────────────

remove_from_config() {
    local config_file="$1"
    [ -f "${config_file}" ] || return 0

    local tmp="${config_file}.tmp.$$"
    if python3 - "${config_file}" "${tmp}" <<'PYEOF'
import json, sys
config_file, tmp = sys.argv[1], sys.argv[2]
try:
    with open(config_file) as f:
        cfg = json.load(f)
except Exception:
    sys.exit(2)
servers = cfg.get("mcpServers", {})
if "data360" in servers:
    del servers["data360"]
    with open(tmp, "w") as f:
        json.dump(cfg, f, indent=2)
        f.write("\n")
    sys.exit(0)
sys.exit(3)  # nothing to remove
PYEOF
    then
        mv "${tmp}" "${config_file}"
        chmod 600 "${config_file}"
        ok "  Removed data360 from ${config_file}"
    else
        rm -f "${tmp}"
        info "  No data360 entry in ${config_file}"
    fi
}

uninstall() {
    printf "\n${BOLD}Uninstalling Data 360 MCP Server${NC}\n\n"

    # Try claude CLI first for Claude Code
    if command -v claude &>/dev/null; then
        info "Removing from Claude Code (via CLI)..."
        claude mcp remove data360 &>/dev/null && ok "  Removed via 'claude mcp remove'" || true
    fi

    local desktop_config
    if [ "$(uname)" = "Darwin" ]; then
        desktop_config="${HOME}/Library/Application Support/Claude/claude_desktop_config.json"
    else
        desktop_config="${HOME}/.config/Claude/claude_desktop_config.json"
    fi

    remove_from_config "${desktop_config}"
    remove_from_config "${HOME}/.claude.json"
    remove_from_config "${HOME}/.cursor/mcp.json"

    if [ -d "${INSTALL_DIR}" ]; then
        info "Removing ${INSTALL_DIR}..."
        rm -rf "${INSTALL_DIR}"
        ok "Removed install directory"
    fi

    printf "\n${GREEN}Uninstall complete.${NC}\n"
    printf "Config backups (if any) remain at <config>.bak — remove them manually if desired.\n\n"
}

# ── Main ─────────────────────────────────────────────────────────────────────

main_install() {
    printf "\n"
    printf "${BOLD}Data 360 MCP Server Installer${NC} (v${INSTALLER_VERSION})\n"
    printf "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"

    detect_os
    check_java
    check_maven
    check_python3
    build_jar
    collect_credentials
    build_mcp_config
    configure_clients

    printf "\n"
    printf "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    printf "${GREEN}${BOLD}Installation complete!${NC}\n"
    printf "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    printf "\n"
    printf "  JAR location:  ${INSTALL_DIR}/${JAR_NAME}\n"
    printf "\n"
    printf "  To test manually:\n"
    printf "    java -jar ${INSTALL_DIR}/${JAR_NAME}\n"
    printf "\n"
    printf "  To update later:\n"
    printf "    Re-run this script — it will rebuild from the current source\n"
    printf "    and reuse your stored credentials.\n"
    printf "\n"
    printf "  To uninstall:\n"
    printf "    ./install.sh uninstall\n"
    printf "\n"
}

main() {
    case "${1:-}" in
        -h|--help)
            show_help
            ;;
        -V|--version)
            printf "install.sh v%s\n" "${INSTALLER_VERSION}"
            ;;
        uninstall)
            uninstall
            ;;
        ""|install)
            main_install
            ;;
        *)
            printf "Unknown argument: %s\n\n" "$1" >&2
            show_help
            exit 2
            ;;
    esac
}

main "$@"
