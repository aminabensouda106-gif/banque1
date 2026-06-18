#!/usr/bin/env python3
"""Capture application screenshots for the Overleaf report."""
from __future__ import annotations

import sys
import time
from pathlib import Path

BASE = "http://localhost:8081"
OUT = Path(__file__).resolve().parent / "images"
VIEWPORT = {"width": 1280, "height": 900}


def wait_for_app(timeout: int = 120) -> None:
    import urllib.request

    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(f"{BASE}/login", timeout=3) as resp:
                if resp.status == 200:
                    print("App ready")
                    return
        except Exception:
            time.sleep(2)
    raise RuntimeError(f"App not ready at {BASE} after {timeout}s")


def login(page, username: str, password: str) -> None:
    page.goto(f"{BASE}/login", wait_until="networkidle")
    page.fill("#username", username)
    page.fill("#password", password)
    page.click('button[type="submit"]')
    page.wait_for_load_state("networkidle")


def logout(page) -> None:
    page.goto(f"{BASE}/logout", wait_until="networkidle")


def shot(page, name: str, url: str | None = None, full_page: bool = False) -> None:
    if url:
        page.goto(url, wait_until="networkidle")
    path = OUT / name
    page.screenshot(path=str(path), full_page=full_page)
    print(f"  {name}")


def main() -> int:
    try:
        from playwright.sync_api import sync_playwright
    except ImportError:
        print("Install: pip install playwright && playwright install chromium")
        return 1

    OUT.mkdir(parents=True, exist_ok=True)
    wait_for_app()

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(viewport=VIEWPORT)

        print("Public pages")
        shot(page, "landing.png", BASE + "/", full_page=False)
        page.evaluate("document.querySelector('#services')?.scrollIntoView()")
        page.wait_for_timeout(500)
        shot(page, "landing-services.png", full_page=False)
        shot(page, "login.png", BASE + "/login")

        print("Agency — agent")
        login(page, "agent", "agent123")
        shot(page, "clients-liste.png", BASE + "/clients")
        shot(page, "compte-detail.png", BASE + "/accounts/1")
        shot(page, "operations-depot.png", BASE + "/operations/deposit")
        shot(page, "paiement-facture.png", BASE + "/operations/bill-payment")
        shot(page, "chequier-liste.png", BASE + "/checkbook-orders")
        shot(page, "recu-pdf.png", BASE + "/transactions/1/receipt")
        page.context.clear_cookies()

        print("Agency — chef")
        login(page, "chef", "chef123")
        shot(page, "dashboard-agent.png", BASE + "/dashboard")
        shot(page, "notifications.png", BASE + "/notifications")
        page.context.clear_cookies()

        print("Agency — admin")
        login(page, "admin", "admin123")
        shot(page, "admin-users.png", BASE + "/admin/users")
        page.context.clear_cookies()

        print("Portal — client")
        login(page, "MB654321", "client123")
        shot(page, "portal-dashboard.png", BASE + "/portal/dashboard")
        shot(page, "portal-notifications.png", BASE + "/portal/notifications")

        browser.close()

    print(f"Done — {len(list(OUT.glob('*.png')))} PNG in {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
