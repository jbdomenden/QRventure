# QRventure Intramuros Tourism Website

A mobile-first tourism website for Intramuros built with **Kotlin + Ktor + PostgreSQL + plain HTML/CSS/JS**.

## Phase 1 Foundation (Current)
- Root endpoint `/` serves the real public homepage: `static/qrventure/index.html`.
- Structured backend packages established for:
  - `config`
  - `db`
  - `models`
  - `dto`
  - `services`
  - `routes`
  - `auth`
  - `utils`
- Route scaffolding in place for:
  - Public pages
  - Public APIs
  - Admin pages and admin API health scaffold
- Static structure created under `static/qrventure` including `admin/` and `uploads/`.

## IntelliJ + PostgreSQL Run Instructions
1. Create database:
   ```sql
   CREATE DATABASE qrventure_db;
   ```
2. Configure `src/main/resources/application.yaml`:
   - `postgres.url: jdbc:postgresql://localhost:5434/qrventure_db`
   - `postgres.user`
   - `postgres.password`
3. Run `ApplicationKt` in IntelliJ.
4. Open `http://localhost:8020/`.

## Updated File Tree (Phase 1)
```
src/main/kotlin/
  Application.kt
  app/QRventure/
    auth/
      AdminAuth.kt
    config/
      AppConfig.kt
    db/
      DatabaseFactory.kt
    dto/
      Responses.kt
    models/
      Models.kt
    services/
      TourismService.kt
    routes/
      PublicSiteRoutes.kt
      PublicApiRoutes.kt
      AdminRoutes.kt
    utils/
      ResourceUtils.kt
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
    admin/
      index.html
    uploads/
      .gitkeep
    css/
      styles.css
    js/
      api.js
      home.js
      listing.js
      detail.js
      navigation.js
    images/
      *.svg
```
