# CC-Logs Extraction Tool

This tool extracts Claude Code conversation logs from the cc-logs web interface and generates standalone HTML files.

## Usage

```bash
python3 extract_logs.py --start "MM/DD/YYYY, HH:MM:SS AM/PM" --end "MM/DD/YYYY, HH:MM:SS AM/PM" [OPTIONS]
```

### Options

- `--start`: Start timestamp (required)
- `--end`: End timestamp (required)
- `--output`: Output HTML file path (default: `conversation_extract.html`)
- `--title`: Optional custom title (replaces project name and timestamps in header)
- `--base-url`: cc-logs server URL (default: `http://localhost:2006`)

### Examples

Extract logs between specific timestamps:
```bash
python3 extract_logs.py \
  --start "11/19/2025, 11:29:15 AM" \
  --end "11/19/2025, 11:29:32 AM" \
  --output my_conversation.html
```

Extract logs with a custom title:
```bash
python3 extract_logs.py \
  --start "11/19/2025, 11:29:15 AM" \
  --end "11/19/2025, 11:29:32 AM" \
  --title "Brainstorming Session: App Design" \
  --output brainstorming_session.html
```

## Requirements

- Python 3.6+
- `requests` library: `pip install requests`
- cc-logs web server running on localhost:2006

## How It Works

1. Detects the current project directory and converts it to cc-logs format
2. Fetches all sessions for the project from cc-logs
3. Filters messages within each session by timestamp
4. Generates a standalone HTML file with:
   - All CSS styles embedded
   - Complete conversation history
   - Same visual styling as cc-logs interface
   - No web server required to view

## Output

The generated HTML file is completely standalone and can be:
- Opened directly in any web browser
- Shared with others
- Archived for later reference
