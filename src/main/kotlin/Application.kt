package app.QRventure

import app.QRventure.db.DatabaseFactory
import app.QRventure.routes.configurePublicApiRoutes
import app.QRventure.routes.configurePublicSiteRoutes
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*

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
        startSelfPingScheduler()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

private fun Application.startSelfPingScheduler() {
    val pingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val client = HttpClient()

    environment.monitor.subscribe(ApplicationStarted) {
        pingScope.launch {
            while (true) {
                try {
                    val url = System.getenv("RENDER_EXTERNAL_URL")
                    if (!url.isNullOrBlank()) {
                        client.get("${url.trimEnd('/')}/health")
                        println("Self ping success")
                    } else {
                        println("RENDER_EXTERNAL_URL not set")
                    }
                } catch (e: Exception) {
                    println("Self ping failed")
                    e.printStackTrace()
                }
                delay(600_000)
            }
        }
    }

    environment.monitor.subscribe(ApplicationStopping) {
        pingScope.cancel()
        client.close()
    }
}
