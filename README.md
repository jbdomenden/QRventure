# QRventure Intramuros Tourism Website

A production-oriented, mobile-first tourism web platform for Intramuros, Manila using:

- Kotlin
- Ktor
- PostgreSQL
- Plain HTML, CSS, JavaScript

No frontend frameworks are used.

---

## Phase 10 — Final Validation

This project now includes:

- Public tourism website with real API-backed content rendering.
- Admin CMS with authenticated session login.
- CRUD APIs for attractions, dining, services, and walking routes.
- Image upload endpoint for CMS content.
- PostgreSQL schema initialization + seed data.
- Search APIs consumed by public pages and admin list views.

### Final validation checklist mapping

1. `/` loads homepage
   - `GET /` serves `static/qrventure/index.html` through `configurePublicSiteRoutes()`.
2. No Hello World
   - No `Hello World` content in Kotlin routes/static pages.
3. Assets load correctly
   - `staticResources("/qrventure", "static/qrventure")` exposes CSS/JS/images/uploads.
4. Admin login works
   - `POST /api/admin/login` validates credentials and sets session cookie.
5. CRUD works
   - Admin routes expose create/read/update/delete for attractions, dining, services, and routes.
6. Upload works
   - `POST /api/admin/upload` saves validated images into `static/qrventure/uploads`.
7. Data reflects DB
   - Public + admin APIs read/write through JDBC-backed service layer into PostgreSQL tables.
8. Search works
   - `GET /api/search?q=...`, plus list filters for attractions/dining/services/routes.

---

## Full File Changes (Phase 10)

- Updated documentation for final delivery/validation in this `README.md`.

---

## Final Project Tree

```text
.
├── README.md
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── kotlin/
    │   │   ├── Application.kt
    │   │   ├── Databases.kt
    │   │   ├── HTTP.kt
    │   │   ├── Routing.kt
    │   │   ├── Security.kt
    │   │   ├── Serialization.kt
    │   │   └── app/QRventure/
    │   │       ├── auth/
    │   │       │   └── AdminAuth.kt
    │   │       ├── config/
    │   │       │   └── AppConfig.kt
    │   │       ├── db/
    │   │       │   └── DatabaseFactory.kt
    │   │       ├── dto/
    │   │       │   ├── Requests.kt
    │   │       │   ├── Responses.kt
    │   │       │   └── Validation.kt
    │   │       ├── model/
    │   │       │   └── Models.kt
    │   │       ├── models/
    │   │       │   └── Models.kt
    │   │       ├── route/
    │   │       │   ├── ApiRoutes.kt
    │   │       │   └── SiteRoutes.kt
    │   │       ├── routes/
    │   │       │   ├── AdminRoutes.kt
    │   │       │   ├── PublicApiRoutes.kt
    │   │       │   └── PublicSiteRoutes.kt
    │   │       ├── service/
    │   │       │   └── TourismService.kt
    │   │       ├── services/
    │   │       │   └── TourismService.kt
    │   │       └── utils/
    │   │           └── ResourceUtils.kt
    │   └── resources/
    │       ├── application.yaml
    │       ├── logback.xml
    │       └── static/
    │           ├── index.html
    │           └── qrventure/
    │               ├── index.html
    │               ├── attractions.html
    │               ├── attraction-detail.html
    │               ├── dining.html
    │               ├── dining-detail.html
    │               ├── services.html
    │               ├── service-detail.html
    │               ├── navigation.html
    │               ├── routes.html
    │               ├── admin/
    │               │   ├── index.html
    │               │   └── login.html
    │               ├── css/
    │               │   └── styles.css
    │               ├── js/
    │               │   ├── admin-api.js
    │               │   ├── admin-cms.js
    │               │   ├── admin-login.js
    │               │   ├── api.js
    │               │   ├── detail.js
    │               │   ├── home.js
    │               │   ├── listing.js
    │               │   └── navigation.js
    │               ├── images/
    │               │   └── *.svg
    │               └── uploads/
    │                   └── .gitkeep
    └── test/
        └── kotlin/
            └── ApplicationTest.kt
```

---

## PostgreSQL Setup Steps

1. Install PostgreSQL 14+ and ensure the service is running.
2. Create database + user credentials:

```sql
CREATE DATABASE qrventure_db;
```

3. Confirm your `src/main/resources/application.yaml` values match your local PostgreSQL instance:

```yaml
postgres:
  url: "jdbc:postgresql://localhost:5434/qrventure_db"
  user: "postgres"
  password: "root"
```

> If your PostgreSQL uses port `5432` (default), update the JDBC URL accordingly.

4. Start the app once; schema creation + seed data are executed automatically by `DatabaseFactory` at startup.

---

## IntelliJ IDEA Run Steps

1. Open the repository in IntelliJ IDEA.
2. Set the Project SDK to Java 21.
3. Wait for Gradle sync to complete.
4. Open `src/main/kotlin/Application.kt`.
5. Run `main()` in `ApplicationKt`.
6. Open:
   - Public site: `http://localhost:8020/`
   - Admin login: `http://localhost:8020/admin/login`

Default admin credentials are configured from `application.yaml` under `adminAuth`.

---

## Suggested Manual Validation Script

After launching the server, validate in order:

1. Visit `/` and confirm homepage renders (no plain text placeholder output).
2. Confirm CSS, JS, and image assets load from `/qrventure/...`.
3. Log in at `/admin/login`.
4. In admin CMS, create/update/delete one record from each module.
5. Upload a JPG/PNG/WEBP image and verify path resolves in browser.
6. Refresh public pages and verify changes persist from DB.
7. Test search from homepage and `/qrventure/navigation.html`.

