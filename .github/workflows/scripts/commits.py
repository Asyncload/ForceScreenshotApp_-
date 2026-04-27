#!/usr/bin/env python3
import subprocess
import os
import base64
import sys

last_commit = sys.argv[1] if len(sys.argv) > 1 else ''
if not last_commit:
    print("has_commits=false")
    sys.exit(0)

commits = subprocess.check_output(
    ["git", "log", "--reverse", "--format=%H", f"{last_commit}..HEAD"],
    text=True
).strip().split()

entries = []
for c in commits:
    msg = subprocess.check_output(["git", "show", "-s", "--format=%B", c], text=True).strip()
    lines = msg.splitlines()
    if len(lines) <= 1:
        continue
    title = lines[0].strip()
    body_lines = [line.rstrip() for line in lines[1:] if line.strip()]
    if not body_lines:
        continue
    entry = f"- {title}"
    for line in body_lines:
        entry += f"\n  {line}"
    entries.append(entry)

if entries:
    print("has_commits=true")
    delim = base64.b64encode(os.urandom(15)).decode()
    print(f"commit_messages<<{delim}")
    print("\n\n".join(entries))
    print(delim)
else:
    print("has_commits=false")
