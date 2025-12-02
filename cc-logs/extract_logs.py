#!/usr/bin/env python3
# ABOUTME: Extracts Claude Code logs from cc-logs web interface between two timestamps
# ABOUTME: and generates a standalone HTML file with the conversation.

import argparse
import json
import os
import requests
import sys
from datetime import datetime
from typing import List, Dict, Any, Optional


def get_project_name() -> str:
    """Convert current directory path to cc-logs project format."""
    cwd = os.getcwd()
    # Replace / with - to match cc-logs format
    project_name = cwd.replace('/', '-')
    return project_name


def parse_timestamp(timestamp_str: str) -> datetime:
    """Parse timestamp in format 'MM/DD/YYYY, HH:MM:SS AM/PM' or ISO 8601."""
    # Try ISO 8601 format first (what's stored in the data)
    try:
        # Remove 'Z' and parse as UTC, then convert to local time
        if timestamp_str.endswith('Z'):
            dt = datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
            # Convert to local time for comparison
            import time
            utc_timestamp = dt.timestamp()
            local_dt = datetime.fromtimestamp(utc_timestamp)
            return local_dt
        return datetime.fromisoformat(timestamp_str)
    except (ValueError, AttributeError):
        # Fall back to display format for user input
        return datetime.strptime(timestamp_str, "%m/%d/%Y, %I:%M:%S %p")


def fetch_sessions(project_name: str, base_url: str = "http://localhost:2006") -> List[Dict[str, Any]]:
    """Fetch all sessions for a project."""
    url = f"{base_url}/api/projects/{requests.utils.quote(project_name)}/sessions"
    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.json()
    except requests.RequestException as e:
        print(f"Error fetching sessions: {e}", file=sys.stderr)
        sys.exit(1)


def fetch_session_entries(project_name: str, session_id: str, base_url: str = "http://localhost:2006") -> List[Dict[str, Any]]:
    """Fetch all entries for a specific session."""
    url = f"{base_url}/api/projects/{requests.utils.quote(project_name)}/sessions/{requests.utils.quote(session_id)}"
    try:
        response = requests.get(url)
        response.raise_for_status()
        return response.json()
    except requests.RequestException as e:
        print(f"Error fetching session {session_id}: {e}", file=sys.stderr)
        return []


def filter_entries_by_timestamp(entries: List[Dict[str, Any]], start_time: datetime, end_time: datetime) -> List[Dict[str, Any]]:
    """Filter entries to only include those within the timestamp range."""
    filtered = []
    for entry in entries:
        if 'timestamp' in entry and entry['timestamp']:
            try:
                entry_time = parse_timestamp(entry['timestamp'])
                if start_time <= entry_time <= end_time:
                    filtered.append(entry)
            except (ValueError, KeyError, TypeError):
                # Skip entries with invalid timestamps
                continue
    return filtered


def escape_html(text: str) -> str:
    """Escape HTML special characters."""
    return (text
            .replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;')
            .replace("'", '&#39;'))


def render_message_content(content: Any) -> str:
    """Render message content (can be string or list of content blocks)."""
    if isinstance(content, str):
        return f'<div class="message-text">{escape_html(content)}</div>'

    if isinstance(content, list):
        html_parts = []
        for block in content:
            if isinstance(block, dict):
                block_type = block.get('type', '')

                # Skip thinking blocks - they're internal and not displayed
                if block_type == 'thinking':
                    continue

                if block_type == 'text':
                    text = block.get('text', '')
                    html_parts.append(f'<div class="message-text">{escape_html(text)}</div>')

                elif block_type == 'tool_use':
                    tool_name = block.get('name', 'unknown')
                    tool_id = block.get('id', '')
                    tool_input = block.get('input', {})
                    input_json = json.dumps(tool_input, indent=2)
                    html_parts.append(f'''
                        <div class="tool-use-container">
                            <div class="tool-use-header">
                                <span>🔧</span>
                                <span>{escape_html(tool_name)}</span>
                            </div>
                            <div class="tool-use-content">
                                <pre>{escape_html(input_json)}</pre>
                            </div>
                        </div>
                    ''')

                elif block_type == 'tool_result':
                    tool_id = block.get('tool_use_id', '')
                    result_content = block.get('content', '')
                    if isinstance(result_content, list):
                        result_text = '\n'.join(str(c.get('text', '')) if isinstance(c, dict) else str(c) for c in result_content)
                    else:
                        result_text = str(result_content)

                    html_parts.append(f'''
                        <div class="tool-result-container">
                            <div class="tool-result-header">
                                <span>📋</span>
                                <span>Tool Result</span>
                            </div>
                            <div class="tool-result-content">{escape_html(result_text)}</div>
                        </div>
                    ''')
            elif isinstance(block, str):
                html_parts.append(f'<div class="message-text">{escape_html(block)}</div>')

        return ''.join(html_parts)

    return f'<div class="message-text">{escape_html(str(content))}</div>'


def render_entry(entry: Dict[str, Any]) -> Optional[str]:
    """Render a single log entry as HTML. Returns None if entry has no visible content."""
    entry_type = entry.get('type', 'unknown')
    timestamp = entry.get('timestamp', '')
    message = entry.get('message', {})

    if entry_type == 'user':
        content = message.get('content', '')
        content_html = render_message_content(content)
        # Skip if no visible content
        if not content_html.strip():
            return None
        return f'''
            <div class="message user">
                <div class="avatar user">U</div>
                <div class="message-content">
                    {content_html}
                    <div class="message-meta">{escape_html(timestamp)}</div>
                </div>
            </div>
        '''

    elif entry_type == 'assistant':
        content = message.get('content', '')
        content_html = render_message_content(content)
        # Skip if no visible content (e.g., only thinking blocks)
        if not content_html.strip():
            return None
        return f'''
            <div class="message assistant">
                <div class="avatar assistant">AI</div>
                <div class="message-content">
                    {content_html}
                    <div class="message-meta">{escape_html(timestamp)}</div>
                </div>
            </div>
        '''

    else:
        # Generic rendering for unknown types
        return f'''
            <div class="message">
                <div class="message-content">
                    <div class="message-text">{escape_html(json.dumps(entry, indent=2))}</div>
                    <div class="message-meta">{escape_html(timestamp)}</div>
                </div>
            </div>
        '''


def get_css_styles() -> str:
    """Return the CSS styles for the output HTML."""
    return '''
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
            padding: 2rem;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 8px 25px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        header {
            background: linear-gradient(90deg, #4a90e2, #357abd);
            color: white;
            padding: 2rem;
            text-align: center;
        }

        h1 {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }

        .subtitle {
            color: rgba(255,255,255,0.9);
            font-size: 1rem;
        }

        .conversation-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 2rem;
        }

        .message {
            display: flex;
            margin-bottom: 1.5rem;
            gap: 12px;
        }

        .message.user {
            flex-direction: row-reverse;
        }

        .message.assistant {
            flex-direction: row;
        }

        .avatar {
            width: 32px;
            height: 32px;
            border-radius: 6px;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 600;
        }

        .avatar.user {
            background: #007bff;
            color: white;
        }

        .avatar.assistant {
            background: #28a745;
            color: white;
        }

        .message-content {
            flex: 1;
            min-width: 0;
        }

        .message.user .message-content {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
        }

        .message-text {
            background: #f8f9fa;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            line-height: 1.5;
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .message.user .message-text {
            background: #007bff;
            color: white;
        }

        .message-meta {
            font-size: 0.75rem;
            color: #6c757d;
            margin-top: 0.25rem;
            padding: 0 0.5rem;
        }

        .tool-use-container,
        .tool-result-container {
            margin-top: 0.5rem;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            overflow: hidden;
        }

        .tool-use-header,
        .tool-result-header {
            background: #e9ecef;
            padding: 0.5rem 0.75rem;
            font-size: 0.875rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .tool-use-content,
        .tool-result-content {
            padding: 0.75rem;
            background: white;
        }

        .tool-use-content pre,
        .tool-result-content pre {
            margin: 0;
            font-family: 'Courier New', monospace;
            font-size: 0.875rem;
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .tool-result-content {
            white-space: pre-wrap;
            word-wrap: break-word;
            font-family: 'Courier New', monospace;
            font-size: 0.875rem;
        }

        .no-entries {
            text-align: center;
            padding: 3rem;
            color: #6c757d;
            font-size: 1.1rem;
        }
    '''


def generate_html(entries: List[Dict[str, Any]], start_time: datetime, end_time: datetime, project_name: str, title: Optional[str] = None) -> str:
    """Generate the complete standalone HTML document."""
    # Filter out None entries (those with no visible content)
    rendered_entries = [render_entry(entry) for entry in entries]
    entries_html = ''.join(entry for entry in rendered_entries if entry is not None)

    if not entries_html:
        entries_html = '<div class="no-entries">No entries found in the specified time range.</div>'

    # Use custom title if provided, otherwise show project name and timestamps
    if title:
        subtitle_html = f'<div class="subtitle">{escape_html(title)}</div>'
        page_title = f"Claude Code Logs - {escape_html(title)}"
    else:
        subtitle_html = f'''<div class="subtitle">
                {escape_html(project_name)}<br>
                {escape_html(start_time.strftime("%m/%d/%Y %I:%M:%S %p"))} - {escape_html(end_time.strftime("%m/%d/%Y %I:%M:%S %p"))}
            </div>'''
        page_title = f"Claude Code Logs - {escape_html(start_time.strftime('%m/%d/%Y %I:%M:%S %p'))} to {escape_html(end_time.strftime('%m/%d/%Y %I:%M:%S %p'))}"

    return f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{page_title}</title>
    <style>
{get_css_styles()}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🤖 Claude Code Logs</h1>
            {subtitle_html}
        </header>
        <div class="conversation-container">
{entries_html}
        </div>
    </div>
</body>
</html>'''


def main():
    parser = argparse.ArgumentParser(
        description='Extract Claude Code logs from cc-logs web interface between two timestamps'
    )
    parser.add_argument('--start', required=True, help='Start timestamp (e.g., "11/19/2025, 11:29:15 AM")')
    parser.add_argument('--end', required=True, help='End timestamp (e.g., "11/19/2025, 11:29:32 AM")')
    parser.add_argument('--output', default='conversation_extract.html', help='Output HTML file (default: conversation_extract.html)')
    parser.add_argument('--title', help='Optional title for the HTML output (replaces project name and timestamps)')
    parser.add_argument('--base-url', default='http://localhost:2006', help='cc-logs server URL (default: http://localhost:2006)')

    args = parser.parse_args()

    # Parse timestamps
    try:
        start_time = parse_timestamp(args.start)
        end_time = parse_timestamp(args.end)
    except ValueError as e:
        print(f"Error parsing timestamp: {e}", file=sys.stderr)
        print('Expected format: "MM/DD/YYYY, HH:MM:SS AM/PM"', file=sys.stderr)
        sys.exit(1)

    if start_time > end_time:
        print("Error: Start time must be before end time", file=sys.stderr)
        sys.exit(1)

    # Get project name from current directory
    project_name = get_project_name()
    print(f"Project: {project_name}")
    print(f"Time range: {args.start} to {args.end}")

    # Fetch all sessions
    print("Fetching sessions...")
    sessions = fetch_sessions(project_name, args.base_url)
    print(f"Found {len(sessions)} sessions")

    # Collect all relevant entries
    all_entries = []
    for session in sessions:
        session_id = session['id']
        session_time = datetime.fromisoformat(session['timestamp'].replace('Z', '+00:00'))

        # Fetch this session if it might contain relevant entries
        # (sessions are ordered by time, and we want to check all that might overlap)
        print(f"Checking session {session_id}...")
        entries = fetch_session_entries(project_name, session_id, args.base_url)
        filtered = filter_entries_by_timestamp(entries, start_time, end_time)

        if filtered:
            print(f"  Found {len(filtered)} entries in range")
            all_entries.extend(filtered)

    # Sort entries by timestamp
    all_entries.sort(key=lambda e: parse_timestamp(e['timestamp']) if 'timestamp' in e else datetime.min)

    print(f"\nTotal entries found: {len(all_entries)}")

    # Generate HTML
    print(f"Generating HTML output to {args.output}...")
    html = generate_html(all_entries, start_time, end_time, project_name, args.title)

    # Write to file
    with open(args.output, 'w', encoding='utf-8') as f:
        f.write(html)

    print(f"✓ Successfully extracted logs to {args.output}")


if __name__ == '__main__':
    main()
