# QRventure Intramuros Tourism Website

A mobile-first tourism website for Intramuros built with **Kotlin + Ktor + PostgreSQL + plain HTML/CSS/JS**.

## Architecture Summary
- **Backend (Ktor)**
  - `DatabaseFactory`: JDBC connection, schema creation, and Intramuros seed data.
  - `TourismService`: safe prepared statements and query logic.
  - API routes under `/api` for featured content, module listings/details, and global search.
  - Site routes for `/`, `/qrventure`, and direct page entry points.
- **Frontend (static under `src/main/resources/static/qrventure`)**
  - Multi-page app (MPA), mobile-first responsive pages.
  - Shared design token CSS aligned to the provided warm editorial style.
  - Modular JavaScript for home, listing, detail, and navigation/search pages.

## API Endpoints
- `GET /api/featured`
- `GET /api/attractions?q=&category=`
- `GET /api/attractions/{slug-or-id}`
- `GET /api/dining?type=`
- `GET /api/dining/{slug-or-id}`
- `GET /api/services?type=`
- `GET /api/services/{slug-or-id}`
- `GET /api/search?q=`

## IntelliJ + PostgreSQL Run Instructions
1. **Create database**
   ```sql
   CREATE DATABASE qrventure_db;
   ```
2. **Set credentials** in `src/main/resources/application.yaml`:
   - `postgres.url: jdbc:postgresql://localhost:5434/qrventure_db`
   - `postgres.user`
   - `postgres.password`
3. **Open in IntelliJ** as a Gradle project.
4. Run `ApplicationKt`.
5. Open `http://localhost:6000/qrventure`.

On startup, schema + Intramuros seed data are automatically created if tables are empty.

## Project Tree (key files)
```
src/main/kotlin/
  Application.kt
  HTTP.kt
  Serialization.kt
  app/QRventure/db/DatabaseFactory.kt
  app/QRventure/model/Models.kt
  app/QRventure/dto/Responses.kt
  app/QRventure/service/TourismService.kt
  app/QRventure/route/ApiRoutes.kt
  app/QRventure/route/SiteRoutes.kt
src/main/resources/
  application.yaml
  static/qrventure/
    index.html
    attractions.html
    attraction-detail.html
    dining.html
    dining-detail.html
    services.html
    service-detail.html
    navigation.html
    css/styles.css
    js/api.js
    js/home.js
    js/listing.js
    js/detail.js
    js/navigation.js
    images/*.svg
```
