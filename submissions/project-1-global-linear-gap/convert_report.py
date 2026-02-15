#!/usr/bin/env python3
"""Convert report.md to report.html for printing to PDF."""
import markdown
import pathlib

script_dir = pathlib.Path(__file__).resolve().parent
md_path = script_dir / "report.md"
html_path = script_dir / "report.html"

md_text = md_path.read_text(encoding="utf-8")

style = """
body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; line-height: 1.6; font-size: 14px; }
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
code { background: #f4f4f4; padding: 2px 4px; }
pre { background: #f4f4f4; padding: 10px; overflow-x: auto; }
h1 { font-size: 22px; }
h2 { font-size: 18px; }
h3 { font-size: 15px; }
"""

html_body = markdown.markdown(md_text, extensions=["tables", "fenced_code"])
html = f"<html><head><meta charset='utf-8'><style>{style}</style></head><body>{html_body}</body></html>"

html_path.write_text(html, encoding="utf-8")
print(f"Done: {html_path}")
