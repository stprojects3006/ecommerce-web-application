#!/bin/bash

set -e

# Convert GRAFANA_QUICK_START.md to HTML
pandoc --wrap=none --from gfm -t html5 -s GRAFANA_QUICK_START.md -o GRAFANA_QUICK_START_full.html

# Convert GRAFANA_MONITORING_GUIDE.md to HTML
pandoc --wrap=none --from gfm -t html5 -s GRAFANA_MONITORING_GUIDE.md -o GRAFANA_MONITORING_GUIDE_full.html

echo "Conversion complete. All data from .md files is now in the .html files."

