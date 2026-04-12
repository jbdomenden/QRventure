# QRventure (Public Brochure)

QRventure is a Kotlin + Ktor brochure site for Intramuros.

## Runtime architecture

- Ktor serves static brochure pages and assets.
- Public content is loaded in-browser from Firebase Realtime Database.
- No PostgreSQL dependency is required for brochure runtime.

## Firebase setup used by the brochure

`src/main/resources/static/qrventure/js/api.js` initializes Firebase with:

- `initializeApp` from `firebase/app`
- `getDatabase` from `firebase/database`
- `databaseURL` set to `https://qrventure-b4e52-default-rtdb.firebaseio.com`

## Realtime Database structure

Use root path:

- `/qrventure/featured`
- `/qrventure/attractions`
- `/qrventure/routes`
- `/qrventure/dining`
- `/qrventure/services`

Sample import payload is included at:

- `firebase/qrventure-realtime-db.json`

## Realtime Database rules

Apply rules from:

- `firebase/realtime-database.rules.json`

Rules intent:

- public read allowed
- writes denied

## Import steps (Firebase console)

1. Open Firebase Console → Realtime Database for project `qrventure-b4e52`.
2. Use **Import JSON** and import `firebase/qrventure-realtime-db.json` at the database root.
3. Open Rules tab and paste/apply `firebase/realtime-database.rules.json`.

## IntelliJ run steps

1. Open the repo in IntelliJ.
2. Use JDK 21.
3. Run `main()` in `src/main/kotlin/Application.kt`.
4. Visit `http://localhost:8020/`.

## Optional npm dependency install

If you want local Firebase package installation in this repo:

```bash
npm install firebase
```

(If your environment blocks npm registry access, runtime is still served by Ktor and browser-loaded Firebase SDK URLs.)
