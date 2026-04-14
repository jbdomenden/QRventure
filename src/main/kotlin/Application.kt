package app.QRventure

import app.QRventure.db.DatabaseFactory
import app.QRventure.routes.configurePublicApiRoutes
import app.QRventure.routes.configurePublicSiteRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()

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
    configurePublicSiteRoutes()
}
