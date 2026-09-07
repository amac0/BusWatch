# ABOUTME: Adds GitHub-style id attributes to <h1>-<h6> tags read from stdin.
# ABOUTME: Keeps the markdown's in-page anchor links working after rendering with marked.

import html
import re
import sys

HEADING = re.compile(r"<h([1-6])>(.*?)</h\1>", re.S)


def slug(text: str) -> str:
    plain = html.unescape(re.sub(r"<[^>]+>", "", text)).lower()
    plain = re.sub(r"[^\w\s-]", "", plain)
    return re.sub(r"\s+", "-", plain.strip())


def add_ids(document: str) -> str:
    return HEADING.sub(
        lambda m: f'<h{m.group(1)} id="{slug(m.group(2))}">{m.group(2)}</h{m.group(1)}>',
        document,
    )


if __name__ == "__main__":
    sys.stdout.write(add_ids(sys.stdin.read()))
