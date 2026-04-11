package app.QRventure

import app.QRventure.auth.configureAdminAuth
import app.QRventure.db.DatabaseFactory
import app.QRventure.routes.configureAdminRoutes
import app.QRventure.routes.configurePublicApiRoutes
import app.QRventure.routes.configurePublicSiteRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureAdminAuth()

    val isDatabaseRequired = environment.config.propertyOrNull("postgres.required")?.getString()?.toBooleanStrictOrNull() ?: true

    val connection = runCatching { DatabaseFactory.connect(environment.config) }
        .onFailure { throwable ->
            if (isDatabaseRequired) {
                log.error("Database connection failed and postgres.required=true. Server startup is aborted.", throwable)
            } else {
                log.error("Database connection failed and postgres.required=false. Site pages will still be served, but APIs will return 503.", throwable)
            }
        }
        .getOrNull()

    if (connection == null && isDatabaseRequired) {
        throw IllegalStateException(
            "Unable to connect to PostgreSQL. Update postgres.url/user/password (and optional postgres.required) in application config and restart."
        )
    }

    if (connection != null) {
        DatabaseFactory.initializeSchema(connection)
        DatabaseFactory.seedData(connection)
        monitor.subscribe(ApplicationStopping) {
            connection.close()
        }
    }

    configurePublicApiRoutes(connection)
    configureAdminRoutes(connection)
    configurePublicSiteRoutes()
}
