# QRventure

Ktor-based tourism site for Intramuros with static pages plus a public JSON API backed by PostgreSQL. Supabase is a supported deployment target because the app reads its database settings from Ktor config in `application.yaml`, with values typically injected by environment variables.

## Runtime

- Ktor serves the public pages from `src/main/resources/static/qrventure`.
- Public content is loaded from PostgreSQL through server-side routes under `/api`.
- The backend uses JDBC with a small Hikari connection pool.

## Database layout

Use four tables:

- `attractions`
- `dining_places`
- `local_services`
- `tour_routes`

Rows should usually be keyed by `slug`. Stored fields match the JSON already consumed by the frontend, such as `name`, `short_description`, `image_urls`, `status`, and `sort_order`.

## Environment

`application.yaml` defines the database config keys:

```yaml
postgres:
  jdbcUrl: ${JDBC_DATABASE_URL:}
  databaseUrl: ${DATABASE_URL:}
  user: ${DB_USER:}
  password: ${DB_PASSWORD:}
  poolMaxSize: ${DB_POOL_MAX_SIZE:5}
```

Required:

- `JDBC_DATABASE_URL`
  - or `DATABASE_URL`

Optional:

- `DB_USER`
- `DB_PASSWORD`
- `DB_POOL_MAX_SIZE`
- `RENDER_EXTERNAL_URL=https://your-service.onrender.com`

If you use Supabase, point those env vars at your Supabase Postgres connection. The app will create missing tables and bootstrap a small baseline set of emergency hotlines and fast-food dining entries when their slugs are missing. Broader tourism content still comes from your database.

## Local run

1. Use JDK 21.
2. Set `JDBC_DATABASE_URL` or `DATABASE_URL` in the environment.
3. If your URL does not embed credentials, also set `DB_USER` and `DB_PASSWORD`.
4. Ensure your database contains tourism records.
5. Start the app:

```powershell
.\gradlew.bat run
```

6. Open `http://localhost:8080/`.

## Validation

- `GET /health`
- `GET /api/featured`
- `GET /api/attractions`
- `GET /api/dining`
- `GET /api/services`
- `GET /api/routes`
- `GET /api/search?q=fort`

## Notes

- The public API contract is preserved, but the only runtime data source is PostgreSQL.
- Filtering and search are performed in the app layer after sorted table reads, which keeps the schema simple for this content dataset.
