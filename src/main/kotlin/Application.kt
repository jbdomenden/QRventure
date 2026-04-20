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
    try {
        println("Starting application")

        configureHTTP()
        configureSerialization()

        val skipDb = System.getenv("SKIP_DB") == "true"
        val connection = if (!skipDb) {
            println("Initializing database")
            val dbUrl = System.getenv("JDBC_DATABASE_URL")
            val dbUser = System.getenv("DB_USER")
            val dbPass = System.getenv("DB_PASSWORD")

            require(!dbUrl.isNullOrBlank()) { "JDBC_DATABASE_URL is missing" }
            require(!dbUser.isNullOrBlank()) { "DB_USER is missing" }
            require(!dbPass.isNullOrBlank()) { "DB_PASSWORD is missing" }

            val connected = DatabaseFactory.connect(dbUrl, dbUser, dbPass)
            DatabaseFactory.initializeSchema(connected)
            DatabaseFactory.seedData(connected)
            println("Database initialized")
            connected
        } else {
            println("Skipping database initialization because SKIP_DB=true")
            null
        }

        if (connection != null) {
            monitor.subscribe(ApplicationStopping) {
                connection.close()
            }
        }

        configurePublicApiRoutes(connection)
        configurePublicSiteRoutes()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
