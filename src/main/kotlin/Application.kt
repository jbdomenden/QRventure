package app.QRventure

import app.QRventure.db.DatabaseFactory
import app.QRventure.route.configureApiRoutes
import app.QRventure.route.configureSiteRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()

    val connection = DatabaseFactory.connect(environment.config)
    DatabaseFactory.initializeSchema(connection)
    DatabaseFactory.seedData(connection)

    monitor.subscribe(ApplicationStopping) {
        connection.close()
    }

    configureApiRoutes(connection)
    configureSiteRoutes()
}
