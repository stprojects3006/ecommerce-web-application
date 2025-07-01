#!/bin/bash

GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"
DASHBOARD_DIR="testing-projects/queueit-functional-testing/config/grafana_dashboards"

for file in "$DASHBOARD_DIR"/*.json; do
  echo "Importing $file..."
  dashboard_json=$(cat "$file")
  payload=$(jq -n --argjson dash "$dashboard_json" '{dashboard: $dash, overwrite: true, folderId: 0}')
  curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" -H "Content-Type: application/json" \
    -X POST "$GRAFANA_URL/api/dashboards/import" \
    -d "$payload"
  echo -e "\n"
done 