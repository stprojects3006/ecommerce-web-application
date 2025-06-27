# Microservices Observability Stack

## Prerequisites
- Docker & Docker Compose
- Maven (to build JARs)
- Node.js & npm (for frontend)

## How to Run

1. Build all microservice JARs and place them in `./jars/`.
2. Build the React frontend (`npm run build` in `frontend/`).
3. Run the stack:
   ```bash
   docker-compose up -d --build
   ```
4. Access:
   - Frontend: http://localhost/
   - API Gateway: http://localhost:8080/
   - Prometheus: http://localhost:9090/
   - Grafana: http://localhost:3000/ (admin/admin)
   - Loki: http://localhost:3100/
   - Nginx Exporter: http://localhost:9113/metrics
   - Blackbox Exporter: http://localhost:9115/
   - Node Exporter: http://localhost:9100/metrics

5. In Grafana, add Prometheus and Loki as data sources, and import the provided dashboards.

## Observability Features

- **Latency, error rate, and health** for all services
- **Up/down timeline and downtime analysis**
- **Log drill-downs with Loki/Promtail**
- **Nginx, Node, and Blackbox Exporter metrics**
