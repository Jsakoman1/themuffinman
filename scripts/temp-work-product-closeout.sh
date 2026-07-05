#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: scripts/temp-work-product-closeout.sh <plan-file> [delete|archive]" >&2
  exit 1
fi

plan="$1"
action="${2:-delete}"

case "$action" in
  delete|archive) ;;
  *)
    echo "unsupported action: $action" >&2
    exit 1
    ;;
esac

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
(cd "$repo_root" && ruby -e 'require "./scripts/audits/local_tooling_extended_tools"; LocalToolingExtendedTools.run("temp-work-product-closeout", ARGV)' temp-work-product-closeout plan="$plan" action="$action")
