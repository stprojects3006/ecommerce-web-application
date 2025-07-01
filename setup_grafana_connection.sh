#!/bin/bash

set -e

GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"
PROM_URL="http://prometheus:9090"
DASHBOARD_DIR="testing-projects/queueit-functional-testing/config/grafana_dashboards"

# 1. Check Grafana status
if curl -s "$GRAFANA_URL/api/health" >/dev/null 2>&1; then
    echo "✅ Grafana is running"
else
    echo "❌ Grafana is not running. Please start Grafana first."
    exit 1
fi

# 2. Check Prometheus status
if curl -s "$PROM_URL/api/v1/status/config" >/dev/null 2>&1; then
    echo "✅ Prometheus is running"
else
    echo "❌ Prometheus is not running. Please start Prometheus first."
    exit 1
fi

# 3. Add Prometheus data source (idempotent)
echo "🔗 Adding Prometheus data source to Grafana..."
payload=$(jq -n \
  --arg name "prometheus" \
  --arg type "prometheus" \
  --arg url "$PROM_URL" \
  '{name: $name, type: $type, access: "proxy", url: $url, isDefault: true, jsonData: {httpMethod: "POST"}}')
curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" -H "Content-Type: application/json" \
  -X POST "$GRAFANA_URL/api/datasources" \
  -d "$payload" | jq

# 4. Import dashboards
for file in "$DASHBOARD_DIR"/*.json; do
  echo "📊 Importing $file..."
  dashboard_json=$(cat "$file")
  payload=$(jq -n --argjson dash "$dashboard_json" '{dashboard: $dash, overwrite: true, folderId: 0}')
  curl -s -u "$GRAFANA_USER:$GRAFANA_PASS" -H "Content-Type: application/json" \
    -X POST "$GRAFANA_URL/api/dashboards/import" \
    -d "$payload" | jq
done

echo "✅ Grafana connection and dashboards setup complete!" 