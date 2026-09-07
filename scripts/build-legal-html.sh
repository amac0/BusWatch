#!/usr/bin/env bash
# ABOUTME: Renders PrivacyPolicy.md and TermsOfService.md to docs/ for GitHub Pages.
# ABOUTME: The Play Store listing links to docs/privacy-policy.html, so keep that name.

set -euo pipefail
here="$(cd "$(dirname "$0")/.." && pwd)"

render() {
  local source="$1" target="$2" title="$3"
  {
    cat <<HTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
               line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; color: #333; }
        h1 { color: #6e1423; }
        h2 { color: #444; margin-top: 2em; }
        table { border-collapse: collapse; margin: 1em 0; font-size: 0.95em; }
        th, td { border: 1px solid #ddd; padding: 6px 10px; text-align: left; vertical-align: top; }
        hr { border: 0; border-top: 1px solid #ddd; margin: 2em 0; }
    </style>
</head>
<body>
HTML
    # Cross-links between the two markdown files become links between the pages.
    sed -e 's#(PrivacyPolicy\.md)#(privacy-policy.html)#g' \
        -e 's#(TermsOfService\.md)#(terms-of-service.html)#g' "$source" \
      | npx -y marked --gfm | python3 "$here/scripts/add_heading_ids.py"
    printf '</body>\n</html>\n'
  } > "$target"
}

render "$here/PrivacyPolicy.md" "$here/docs/privacy-policy.html" "BusWatch Privacy Policy"
render "$here/TermsOfService.md" "$here/docs/terms-of-service.html" "BusWatch Terms of Service"
echo "Rendered legal pages into $here/docs"
