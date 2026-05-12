#!/usr/bin/env python3
# Copyright (c) 2026, Salesforce, Inc.
# SPDX-License-Identifier: Apache-2.0
#
# Cross-platform installer for the Data 360 MCP Server.
#
# Works on macOS, Linux, and Windows. Uses the Python standard library only.
#
# Usage:
#   python3 install.py                 # install / upgrade
#   ./install.py uninstall             # remove JAR and data360 entry from MCP configs
#   python3 install.py --help
#   python3 install.py --version
#
# Windows one-liner (PowerShell):
#   irm https://raw.githubusercontent.com/forcedotcom/d360-mcp-server/refs/heads/main/install.py -OutFile install.py; python install.py

from __future__ import annotations

import argparse
import getpass
import json
import os
import re
import shutil
import stat
import subprocess
import sys
import tempfile
import time
import urllib.request
from pathlib import Path

# ── Constants ────────────────────────────────────────────────────────────────

INSTALLER_VERSION = "1.1.0"
INSTALL_DIR = Path.home() / ".data360-mcp-server"
REPO_URL = "https://github.com/forcedotcom/d360-mcp-server"
REPO_BRANCH = "main"
DEFAULT_API_VERSION = "66.0"

MCP_CONFIG_FILE = INSTALL_DIR / ".mcp_config.json"

# Set by build_jar()
JAR_NAME = ""

# ── Colors ───────────────────────────────────────────────────────────────────

def _colors_enabled() -> bool:
    if os.environ.get("NO_COLOR"):
        return False
    if not sys.stdout.isatty():
        return False
    return True

if _colors_enabled():
    # On Windows 10+, enable VT processing so ANSI codes render in cmd/PowerShell.
    if sys.platform == "win32":
        os.system("")
    RED = "\033[0;31m"
    GREEN = "\033[0;32m"
    YELLOW = "\033[1;33m"
    BLUE = "\033[0;34m"
    BOLD = "\033[1m"
    NC = "\033[0m"
else:
    RED = GREEN = YELLOW = BLUE = BOLD = NC = ""


def info(msg: str) -> None:
    print(f"{BLUE}[info]{NC}  {msg}")


def ok(msg: str) -> None:
    print(f"{GREEN}[ok]{NC}    {msg}")


def warn(msg: str) -> None:
    print(f"{YELLOW}[warn]{NC}  {msg}")


def fail(msg: str) -> None:
    print(f"{RED}[error]{NC} {msg}", file=sys.stderr)
    sys.exit(1)


# ── Prompts ──────────────────────────────────────────────────────────────────

def prompt(text: str) -> str:
    try:
        return input(text)
    except EOFError:
        return ""


def prompt_secret(text: str) -> str:
    if not sys.stdin.isatty():
        return prompt(text)
    try:
        return getpass.getpass(text)
    except (EOFError, KeyboardInterrupt):
        print()
        return ""


# ── Retry helper ─────────────────────────────────────────────────────────────

def retry(fn, *, max_attempts: int = 3, initial_delay: int = 2):
    delay = initial_delay
    for attempt in range(1, max_attempts + 1):
        try:
            return fn()
        except subprocess.CalledProcessError:
            if attempt >= max_attempts:
                raise
            warn(f"Command failed (attempt {attempt}/{max_attempts}), retrying in {delay}s...")
            time.sleep(delay)
            delay *= 2


# ── OS detection ─────────────────────────────────────────────────────────────

def detect_os() -> str:
    if sys.platform == "darwin":
        return "macos"
    if sys.platform.startswith("linux"):
        return "linux"
    if sys.platform == "win32":
        return "windows"
    fail(f"Unsupported OS: {sys.platform}. This installer supports macOS, Linux, and Windows.")


OS_TYPE = detect_os()


# ── Subprocess helpers ───────────────────────────────────────────────────────

def run(cmd, *, check: bool = True, capture: bool = False, stdin_null: bool = True, **kwargs):
    """Run a subprocess. Uses a list — never shell=True — to avoid quoting issues
    with paths that contain spaces on Windows."""
    stdin = subprocess.DEVNULL if stdin_null else None
    if capture:
        return subprocess.run(cmd, check=check, stdin=stdin,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                              text=True, **kwargs)
    return subprocess.run(cmd, check=check, stdin=stdin, **kwargs)


def which(binary: str) -> str | None:
    """shutil.which with Windows' PATHEXT handled automatically (finds .cmd/.exe)."""
    return shutil.which(binary)


# ── Homebrew (macOS) ─────────────────────────────────────────────────────────

def ensure_homebrew() -> None:
    if OS_TYPE != "macos":
        return
    if which("brew"):
        return
    info("Homebrew not found. Installing...")
    installer_url = "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh"
    installer_path: Path | None = None
    try:
        with urllib.request.urlopen(installer_url) as response:
            script = response.read()
        with tempfile.NamedTemporaryFile(delete=False, suffix=".sh") as tmp:
            tmp.write(script)
            installer_path = Path(tmp.name)
        run(["/bin/bash", str(installer_path)])
    finally:
        if installer_path and installer_path.exists():
            installer_path.unlink()
    for candidate in ("/opt/homebrew/bin/brew", "/usr/local/bin/brew"):
        if Path(candidate).is_file():
            # Prepend brew's bin to PATH for the rest of this process
            brew_prefix = str(Path(candidate).parent)
            os.environ["PATH"] = brew_prefix + os.pathsep + os.environ.get("PATH", "")
            break
    ok("Homebrew installed")


# ── Package-manager dispatch ─────────────────────────────────────────────────

def install_package(mac_pkg: str, apt_pkg: str, yum_pkg: str, winget_id: str | None) -> None:
    """Install a package using the native package manager for OS_TYPE."""
    if OS_TYPE == "macos":
        ensure_homebrew()
        run(["brew", "install", mac_pkg])
        return

    if OS_TYPE == "linux":
        if which("apt-get"):
            retry(lambda: run(["sudo", "apt-get", "update", "-qq"]))
            run(["sudo", "apt-get", "install", "-y", "-qq", apt_pkg])
            return
        if which("yum"):
            run(["sudo", "yum", "install", "-y", "-q", yum_pkg])
            return
        fail(f"Cannot auto-install {apt_pkg}. Please install it manually and re-run.")

    if OS_TYPE == "windows":
        if winget_id and which("winget"):
            run(["winget", "install", "--id", winget_id, "-e", "--silent",
                 "--accept-package-agreements", "--accept-source-agreements"])
            return
        if which("choco"):
            run(["choco", "install", mac_pkg, "-y"])
            return
        fail(f"Cannot auto-install {winget_id or mac_pkg}. "
             "Install winget (App Installer from Microsoft Store) or Chocolatey, then re-run.")


# ── Git ──────────────────────────────────────────────────────────────────────

def check_git() -> None:
    if which("git"):
        return
    info("Git not found. Installing...")
    install_package("git", "git", "git", "Git.Git")
    # Fresh winget installs don't show up on PATH until a new shell; warn and bail.
    if not which("git"):
        fail("Git installed but not on PATH. Open a new terminal and re-run this installer.")
    ok("Git installed")


# ── Java 17+ ────────────────────────────────────────────────────────────────

def java_major_version() -> int:
    """Parse major version from `java -version` output. Handles modern ('17.0.5')
    and legacy ('1.8.0_xxx') formats. Returns 0 if unknown."""
    try:
        result = subprocess.run(["java", "-version"], capture_output=True, text=True)
    except FileNotFoundError:
        return 0
    # `java -version` prints to stderr
    output = (result.stderr or "") + (result.stdout or "")
    match = re.search(r'version "([^"]+)"', output)
    if not match:
        return 0
    raw = match.group(1)
    parts = raw.split(".")
    try:
        major = int(parts[0])
    except ValueError:
        return 0
    if major == 1 and len(parts) > 1:
        try:
            return int(parts[1])
        except ValueError:
            return 0
    return major


def check_java() -> None:
    info("Checking for Java 17+...")
    if which("java"):
        version = java_major_version()
        if version >= 17:
            ok(f"Java {version} found")
            return
        warn(f"Java {version} found, but 17+ is required")
    else:
        warn("Java not found")

    info("Installing Java 17...")
    if OS_TYPE == "macos":
        ensure_homebrew()
        run(["brew", "install", "openjdk@17"])
        brew_prefix = subprocess.run(["brew", "--prefix"], capture_output=True, text=True,
                                     check=True).stdout.strip()
        bin_dir = Path(brew_prefix) / "opt" / "openjdk@17" / "bin"
        if bin_dir.is_dir():
            os.environ["PATH"] = str(bin_dir) + os.pathsep + os.environ.get("PATH", "")
    elif OS_TYPE == "linux":
        install_package("openjdk@17", "openjdk-17-jdk", "java-17-openjdk-devel", None)
    else:  # windows
        install_package("openjdk17", "openjdk-17-jdk", "java-17-openjdk-devel",
                        "EclipseAdoptium.Temurin.17.JDK")
        if not which("java"):
            fail("Java installed but not on PATH. Open a new terminal and re-run this installer.")
    ok("Java 17 installed")


# ── Maven ────────────────────────────────────────────────────────────────────

def check_maven() -> None:
    info("Checking for Maven...")
    if which("mvn"):
        result = subprocess.run(["mvn", "-v"], capture_output=True, text=True)
        match = re.search(r"Apache Maven (\S+)", (result.stdout or "") + (result.stderr or ""))
        version = match.group(1) if match else "unknown"
        ok(f"Maven found ({version})")
        return
    info("Installing Maven...")
    install_package("maven", "maven", "maven", "Apache.Maven")
    if not which("mvn"):
        fail("Maven installed but not on PATH. Open a new terminal and re-run this installer.")
    ok("Maven installed")


# ── Build the JAR ───────────────────────────────────────────────────────────

def _rmtree_force(path: Path) -> None:
    """shutil.rmtree that copes with Windows read-only files (git pack indexes)."""
    def onerror(func, target, exc_info):
        try:
            os.chmod(target, stat.S_IWRITE)
            func(target)
        except Exception:
            pass
    shutil.rmtree(path, onerror=onerror)


def _find_built_jar(build_dir: Path) -> Path | None:
    target = build_dir / "target"
    if not target.is_dir():
        return None
    for candidate in sorted(target.glob("data360-mcp-server-*.jar")):
        if candidate.name.endswith(".original"):
            continue
        if candidate.is_file():
            return candidate
    return None


def build_jar() -> None:
    global JAR_NAME

    INSTALL_DIR.mkdir(parents=True, exist_ok=True)
    try:
        os.chmod(INSTALL_DIR, 0o700)
    except OSError:
        pass  # Windows: chmod is a no-op for dirs; user profile ACLs still apply

    cloned_tmp: Path | None = None
    pom = Path("pom.xml")
    if pom.is_file() and "data360-mcp-server" in pom.read_text(errors="ignore"):
        info("Building from local repo...")
        build_dir = Path(".")
    else:
        check_git()
        cloned_tmp = Path(tempfile.mkdtemp(prefix="d360-mcp-"))
        clone_target = cloned_tmp / "d360-mcp-server"
        info("Cloning repo...")
        try:
            run(["git", "clone", "--depth", "1", "--branch", REPO_BRANCH,
                 REPO_URL, str(clone_target)])
        except subprocess.CalledProcessError:
            fail(
                f"Failed to clone {REPO_URL}.\n"
                "    Possible causes:\n"
                "      • GitHub credentials not cached — try: gh auth login\n"
                "      • Repository access not granted — check github.com/forcedotcom/d360-mcp-server\n"
                "      • Network connectivity issue\n"
                "    Fix the above and re-run."
            )
        build_dir = clone_target

    info("Building JAR (this may take a minute on first run)...")
    build_log = INSTALL_DIR / "build.log"
    mvn_cmd = "mvn.cmd" if OS_TYPE == "windows" and which("mvn.cmd") else "mvn"
    with open(build_log, "w") as log:
        result = subprocess.run(
            [mvn_cmd, "clean", "package", "-DskipTests"],
            cwd=str(build_dir), stdin=subprocess.DEVNULL,
            stdout=log, stderr=subprocess.STDOUT,
        )
    if result.returncode != 0:
        print("\n--- Last 40 lines of build log ---")
        tail = build_log.read_text(errors="ignore").splitlines()[-40:]
        print("\n".join(tail))
        print()
        fail(f"Maven build failed. Full log: {build_log}")

    jar = _find_built_jar(build_dir)
    if not jar:
        fail(f"Build succeeded but no JAR found in {build_dir}/target/")

    JAR_NAME = jar.name
    dest = INSTALL_DIR / JAR_NAME
    shutil.copy2(jar, dest)
    try:
        os.chmod(dest, 0o644)
    except OSError:
        pass

    if cloned_tmp and cloned_tmp.exists():
        _rmtree_force(cloned_tmp)

    ok(f"JAR installed to {dest}")


# ── Credential reuse ────────────────────────────────────────────────────────

# Populated by collect_credentials() / load_existing_credentials()
CREDS: dict[str, str] = {}


def load_existing_credentials() -> bool:
    if not MCP_CONFIG_FILE.is_file():
        return False
    try:
        with open(MCP_CONFIG_FILE) as f:
            cfg = json.load(f)
        env = cfg["mcpServers"]["data360"]["env"]
    except (OSError, ValueError, KeyError):
        return False
    CREDS["AUTH_FLOW"] = env.get("DATA360_AUTH_FLOW", "access_token")
    for key in ("DATA360_INSTANCE_URL", "DATA360_ACCESS_TOKEN",
                "DATA360_CLIENT_ID", "DATA360_CLIENT_SECRET",
                "DATA360_LOGIN_URL", "DATA360_API_VERSION"):
        if key in env:
            CREDS[key] = env[key]
    return True


# ── Credentials ─────────────────────────────────────────────────────────────

def validate_instance_url(url: str) -> str:
    url = url.rstrip("/")
    if not re.match(r"^https://", url):
        fail(f"Instance URL must start with https:// (got: '{url}')")
    after_scheme = url.split("://", 1)[1]
    if "/" in after_scheme:
        fail(f"Instance URL should be host-only, with no path (got: '{url}')")
    return url


def collect_credentials() -> None:
    if load_existing_credentials():
        print()
        print(f"{BOLD}Existing credentials detected{NC} for flow '{CREDS['AUTH_FLOW']}'.")
        choice = prompt("Reuse them? [Y/n]: ").strip()
        if choice in ("", "Y", "y", "Yes", "yes"):
            ok("Reusing stored credentials")
            return
        CREDS.clear()

    print()
    print(f"{BOLD}Salesforce Data 360 Credentials{NC}")
    print("Choose an auth method:")
    print("  1) Access token  — quick setup, tokens expire")
    print("  2) Client credentials  — auto-refreshing, recommended")
    print()
    auth_choice = prompt("Choose [1/2]: ").strip()

    if auth_choice == "1":
        CREDS["AUTH_FLOW"] = "access_token"
        raw_url = prompt("DATA360_INSTANCE_URL (e.g. https://your-org.my.salesforce.com): ").strip()
        if not raw_url:
            fail("Instance URL is required")
        CREDS["DATA360_INSTANCE_URL"] = validate_instance_url(raw_url)
        token = prompt_secret("DATA360_ACCESS_TOKEN (hidden): ").strip()
        if not token:
            fail("Access token is required")
        CREDS["DATA360_ACCESS_TOKEN"] = token

    elif auth_choice == "2":
        CREDS["AUTH_FLOW"] = "client_credentials"
        client_id = prompt("DATA360_CLIENT_ID: ").strip()
        if not client_id:
            fail("Client ID is required")
        CREDS["DATA360_CLIENT_ID"] = client_id
        secret = prompt_secret("DATA360_CLIENT_SECRET (hidden): ").strip()
        if not secret:
            fail("Client secret is required")
        CREDS["DATA360_CLIENT_SECRET"] = secret
        default_login = "https://login.salesforce.com"
        login = prompt(f"DATA360_LOGIN_URL [{default_login}]: ").strip()
        CREDS["DATA360_LOGIN_URL"] = login or default_login

    else:
        fail("Invalid choice")

    api_version = prompt(f"DATA360_API_VERSION [{DEFAULT_API_VERSION}]: ").strip()
    CREDS["DATA360_API_VERSION"] = api_version or DEFAULT_API_VERSION


# ── Build MCP server config ─────────────────────────────────────────────────

def _write_json_secure(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    try:
        os.chmod(path, 0o600)
    except OSError:
        # Windows: os.chmod only toggles readonly. The file lives in the user
        # profile, which already restricts access to other users via ACL.
        pass


def build_mcp_config() -> None:
    jar_path = str(INSTALL_DIR / JAR_NAME)
    env = {"DATA360_AUTH_FLOW": CREDS["AUTH_FLOW"]}
    if CREDS["AUTH_FLOW"] == "access_token":
        env["DATA360_INSTANCE_URL"] = CREDS["DATA360_INSTANCE_URL"]
        env["DATA360_ACCESS_TOKEN"] = CREDS["DATA360_ACCESS_TOKEN"]
    else:
        env["DATA360_CLIENT_ID"] = CREDS["DATA360_CLIENT_ID"]
        env["DATA360_CLIENT_SECRET"] = CREDS["DATA360_CLIENT_SECRET"]
        env["DATA360_LOGIN_URL"] = CREDS["DATA360_LOGIN_URL"]
    env["DATA360_API_VERSION"] = CREDS["DATA360_API_VERSION"]

    cfg = {"mcpServers": {"data360": {
        "command": "java", "args": ["-jar", jar_path], "env": env
    }}}
    _write_json_secure(MCP_CONFIG_FILE, cfg)
    ok("MCP config generated")


# ── Merge into client configs ───────────────────────────────────────────────

def merge_mcp_config(config_file: Path) -> None:
    """Atomically merge the data360 entry into a JSON config file."""
    config_file.parent.mkdir(parents=True, exist_ok=True)

    with open(MCP_CONFIG_FILE) as f:
        new_cfg = json.load(f)
    new_server = new_cfg["mcpServers"]["data360"]

    if config_file.is_file():
        backup = config_file.with_suffix(config_file.suffix + ".bak")
        if not backup.exists():
            shutil.copy2(config_file, backup)
        with open(config_file) as f:
            existing = json.load(f)
        existing.setdefault("mcpServers", {})["data360"] = new_server
        tmp = config_file.with_name(f"{config_file.name}.tmp.{os.getpid()}")
        with open(tmp, "w") as f:
            json.dump(existing, f, indent=2)
            f.write("\n")
        try:
            os.chmod(tmp, 0o600)
        except OSError:
            pass
        os.replace(tmp, config_file)
        ok(f"  Updated data360 in {config_file} (backup: {backup})")
    else:
        shutil.copy2(MCP_CONFIG_FILE, config_file)
        try:
            os.chmod(config_file, 0o600)
        except OSError:
            pass
        ok(f"  Created {config_file}")


# ── Client presence detection ───────────────────────────────────────────────

def claude_desktop_config_path() -> Path:
    if OS_TYPE == "macos":
        return Path.home() / "Library" / "Application Support" / "Claude" / "claude_desktop_config.json"
    if OS_TYPE == "windows":
        appdata = os.environ.get("APPDATA") or str(Path.home() / "AppData" / "Roaming")
        return Path(appdata) / "Claude" / "claude_desktop_config.json"
    return Path.home() / ".config" / "Claude" / "claude_desktop_config.json"


def client_present_claude_desktop() -> bool:
    if OS_TYPE == "macos":
        return (Path.home() / "Library" / "Application Support" / "Claude").is_dir() or \
               Path("/Applications/Claude.app").is_dir()
    if OS_TYPE == "windows":
        appdata = os.environ.get("APPDATA") or str(Path.home() / "AppData" / "Roaming")
        local = os.environ.get("LOCALAPPDATA") or str(Path.home() / "AppData" / "Local")
        return (Path(appdata) / "Claude").is_dir() or (Path(local) / "AnthropicClaude").is_dir()
    return (Path.home() / ".config" / "Claude").is_dir()


def client_present_claude_code() -> bool:
    return bool(which("claude")) or (Path.home() / ".claude.json").is_file()


def cursor_config_path() -> Path:
    return Path.home() / ".cursor" / "mcp.json"


def client_present_cursor() -> bool:
    if (Path.home() / ".cursor").is_dir():
        return True
    if OS_TYPE == "macos" and Path("/Applications/Cursor.app").is_dir():
        return True
    return bool(which("cursor"))


# ── Configure clients ───────────────────────────────────────────────────────

def configure_claude_desktop() -> None:
    info("Configuring Claude Desktop...")
    merge_mcp_config(claude_desktop_config_path())


def configure_claude_code() -> None:
    info("Configuring Claude Code...")
    if which("claude"):
        with open(MCP_CONFIG_FILE) as f:
            cfg = json.load(f)
        server_json = json.dumps(cfg["mcpServers"]["data360"])
        # Keep idempotent: best-effort remove before add
        subprocess.run(["claude", "mcp", "remove", "data360"],
                       stdin=subprocess.DEVNULL,
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        add = subprocess.run(["claude", "mcp", "add-json", "--scope", "user",
                              "data360", server_json],
                             stdin=subprocess.DEVNULL)
        if add.returncode == 0:
            ok("  Registered data360 via 'claude mcp add-json --scope user'")
            return
        warn("'claude mcp add-json' failed — falling back to direct merge")
    merge_mcp_config(Path.home() / ".claude.json")


def configure_cursor() -> None:
    info("Configuring Cursor...")
    merge_mcp_config(cursor_config_path())


def configure_clients() -> None:
    print()
    print(f"{BOLD}Configure MCP Clients{NC}")

    cd_note = "" if client_present_claude_desktop() else "  (not detected)"
    cc_note = "" if client_present_claude_code() else "  (not detected)"
    cu_note = "" if client_present_cursor() else "  (not detected)"

    print("Which clients would you like to configure?")
    print(f"  1) Claude Desktop{cd_note}")
    print(f"  2) Claude Code{cc_note}")
    print(f"  3) Cursor{cu_note}")
    print("  4) All detected")
    print("  5) None — I'll configure manually")
    print()
    choice = prompt("Choose [1/2/3/4/5]: ").strip()

    if choice == "1":
        configure_claude_desktop()
    elif choice == "2":
        configure_claude_code()
    elif choice == "3":
        configure_cursor()
    elif choice == "4":
        if client_present_claude_desktop():
            configure_claude_desktop()
        else:
            warn("Skipping Claude Desktop (not detected)")
        if client_present_claude_code():
            configure_claude_code()
        else:
            warn("Skipping Claude Code (not detected)")
        if client_present_cursor():
            configure_cursor()
        else:
            warn("Skipping Cursor (not detected)")
    elif choice == "5":
        print()
        print(f"{BOLD}Manual configuration:{NC}")
        print("Your MCP server config was saved (with secrets) to:")
        print(f"  {BOLD}{MCP_CONFIG_FILE}{NC}")
        print()
        print("Copy its contents into your MCP client's configuration.")
        print("(File permissions are 600 — do not print it in shared terminals.)")
    else:
        fail("Invalid choice")


# ── Uninstall ───────────────────────────────────────────────────────────────

def remove_from_config(config_file: Path) -> None:
    if not config_file.is_file():
        return
    try:
        with open(config_file) as f:
            cfg = json.load(f)
    except (OSError, ValueError):
        return
    servers = cfg.get("mcpServers", {})
    if "data360" not in servers:
        info(f"  No data360 entry in {config_file}")
        return
    del servers["data360"]
    tmp = config_file.with_name(f"{config_file.name}.tmp.{os.getpid()}")
    with open(tmp, "w") as f:
        json.dump(cfg, f, indent=2)
        f.write("\n")
    try:
        os.chmod(tmp, 0o600)
    except OSError:
        pass
    os.replace(tmp, config_file)
    ok(f"  Removed data360 from {config_file}")


def uninstall() -> None:
    print()
    print(f"{BOLD}Uninstalling Data 360 MCP Server{NC}")
    print()

    if which("claude"):
        info("Removing from Claude Code (via CLI)...")
        result = subprocess.run(["claude", "mcp", "remove", "data360"],
                                stdin=subprocess.DEVNULL,
                                stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if result.returncode == 0:
            ok("  Removed via 'claude mcp remove'")

    remove_from_config(claude_desktop_config_path())
    remove_from_config(Path.home() / ".claude.json")
    remove_from_config(cursor_config_path())

    if INSTALL_DIR.is_dir():
        info(f"Removing {INSTALL_DIR}...")
        _rmtree_force(INSTALL_DIR)
        ok("Removed install directory")

    print()
    print(f"{GREEN}Uninstall complete.{NC}")
    print("Config backups (if any) remain at <config>.bak — remove them manually if desired.")
    print()


# ── Main install flow ───────────────────────────────────────────────────────

def main_install() -> None:
    print()
    print(f"{BOLD}Data 360 MCP Server Installer{NC} (v{INSTALLER_VERSION})")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print()

    check_java()
    check_maven()
    build_jar()
    collect_credentials()
    build_mcp_config()
    configure_clients()

    print()
    print(f"{GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{GREEN}{BOLD}Installation complete!{NC}")
    print(f"{GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print()
    print(f"  JAR location:  {INSTALL_DIR / JAR_NAME}")
    print()
    print("  To test manually:")
    print(f"    java -jar {INSTALL_DIR / JAR_NAME}")
    print()
    print("  To update later:")
    print("    Re-run this script — it will rebuild from the current source")
    print("    and reuse your stored credentials.")
    print()
    print(f"  To uninstall:")
    print(f"    ./install.py uninstall" if OS_TYPE != "windows"
          else "    python install.py uninstall")
    print()


# ── CLI ──────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        prog="install.py",
        description=f"Data 360 MCP Server installer v{INSTALLER_VERSION}",
        add_help=True,
    )
    parser.add_argument("-V", "--version", action="version",
                        version=f"install.py v{INSTALLER_VERSION}")
    parser.add_argument("action", nargs="?", default="install",
                        choices=["install", "uninstall"],
                        help="install (default) or uninstall")
    args = parser.parse_args()

    if args.action == "uninstall":
        uninstall()
    else:
        main_install()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print()
        sys.exit(130)
