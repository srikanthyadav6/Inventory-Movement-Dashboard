# Inventory Movement Dashboard

Monorepo for a take-home inventory movement dashboard. The backend is a Spring Boot API backed by an H2 in-memory database. The frontend is a Vite React app with filters, server-side pagination, charts, and CSV export.

## Project Structure

```text
.
|-- backend/              # Spring Boot API
|-- frontend/             # React + TypeScript UI
|-- mock_movements.json   # Startup seed data for H2
`-- README.md
```

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+ and npm

## Run the Backend

```powershell
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. H2 is in-memory and is rebuilt on each backend start from the root `mock_movements.json` file.

## Run the Frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173` and proxies `/api` requests to the backend.

## API Examples

Page-only JSON response with server-side pagination:

```text
GET http://localhost:8080/api/movements?from=2026-02-01&to=2026-05-31&page=0&size=10
```

Filter page rows by movement type:

```text
GET http://localhost:8080/api/movements?from=2026-02-01&to=2026-05-31&type=IN&page=0&size=10
```

Analytics response for charts:

```text
GET http://localhost:8080/api/movements/analytics?from=2026-02-01&to=2026-05-31&type=IN
```

Export all filtered rows as CSV:

```text
GET http://localhost:8080/api/movements?from=2026-02-01&to=2026-05-31&type=OUT&export=true
```

## Behavior Notes

- Table pagination is server-side and fixed to 10 rows per page in the UI.
- Page changes call only `/api/movements`; chart data is loaded separately from `/api/movements/analytics` when filters change.
- Chart aggregates and CSV export cover the full filtered dataset, not only the current table page.
- Date filtering is inclusive: the backend includes all movement timestamps from the `from` date through the end of the `to` date in UTC.
- Warehouse filtering is not implemented because `mock_movements.json` does not include a warehouse field.
- Automated tests are intentionally not included yet.

## Manual Verification

1. Start the backend and confirm it loads `mock_movements.json` into H2.
2. Open `http://localhost:8080/api/movements?from=2026-02-01&to=2026-05-31` and confirm the response includes 10 rows plus page metadata only.
3. Open `http://localhost:8080/api/movements/analytics?from=2026-02-01&to=2026-05-31` and confirm the response includes `summary` and `dailyTotals`.
4. Start the frontend and apply `IN` and `OUT` filters; the table and charts should update together.
5. Use Previous/Next pagination and confirm each action requests only a different server page.
6. Click Export CSV and confirm the downloaded file includes all filtered rows.
