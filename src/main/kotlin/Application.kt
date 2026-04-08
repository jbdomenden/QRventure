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

    val connection = runCatching { DatabaseFactory.connect(environment.config) }
        .onFailure { log.error("Database connection failed. Site pages will still be served, but APIs will return 503.", it) }
        .getOrNull()

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
