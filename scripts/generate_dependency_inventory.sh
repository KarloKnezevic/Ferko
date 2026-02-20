#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

INVENTORY_DIR="target/dependency-inventory"
TREES_DIR="$INVENTORY_DIR/trees"

rm -rf "$INVENTORY_DIR"
mkdir -p "$TREES_DIR"

./mvnw -B -ntp -DskipTests dependency:tree -DoutputFile=target/dependency-tree.txt

./mvnw -B -ntp -DskipTests \
  org.cyclonedx:cyclonedx-maven-plugin:2.8.1:makeAggregateBom \
  -DoutputFormat=json \
  -DoutputName=ferko-sbom \
  -DoutputDirectory="$INVENTORY_DIR"

if [[ -f "target/dependency-tree.txt" ]]; then
  cp "target/dependency-tree.txt" "$TREES_DIR/ferko-parent.txt"
fi

while IFS= read -r tree_file; do
  module_dir="${tree_file%/target/dependency-tree.txt}"
  module_dir="${module_dir#./}"

  if [[ "$module_dir" == "." || "$module_dir" == "target" ]]; then
    continue
  fi

  safe_name="${module_dir//\//-}"
  cp "$tree_file" "$TREES_DIR/${safe_name}.txt"
done < <(find . -type f -path '*/target/dependency-tree.txt' | sort)

{
  echo "generated_at_utc=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "sbom_json=$INVENTORY_DIR/ferko-sbom.json"
  echo "tree_files=$(find "$TREES_DIR" -type f | wc -l | tr -d ' ')"
} > "$INVENTORY_DIR/metadata.txt"

echo "Dependency inventory written to $INVENTORY_DIR"
