package app.QRventure

import app.QRventure.config.AppConfig
import app.QRventure.db.DatabaseFactory
import app.QRventure.repositories.TourismRepository
import app.QRventure.routes.configurePublicApiRoutes
import app.QRventure.routes.configurePublicSiteRoutes
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
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

fun Application.module(repositoryOverride: TourismRepository? = null, enableSelfPing: Boolean = true) {
    try {
        println("Starting application")

        configureHTTP()
        configureSerialization()

        val repository = repositoryOverride ?: run {
            println("Initializing database")
            DatabaseFactory.createRepository(AppConfig.postgres(environment.config)).also {
                println("Database initialized")
            }
        }

        if (repository is AutoCloseable) {
            monitor.subscribe(ApplicationStopping) {
                repository.close()
            }
        }

        configurePublicApiRoutes(repository)
        configurePublicSiteRoutes()
        if (enableSelfPing) {
            startSelfPingScheduler()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

private fun Application.startSelfPingScheduler() {
    val pingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val client = HttpClient(CIO) {
        expectSuccess = true
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
    }

    monitor.subscribe(ApplicationStarted) {
        pingScope.launch {
            while (true) {
                try {
                    val url = System.getenv("RENDER_EXTERNAL_URL")
                    if (!url.isNullOrBlank()) {
                        val response = client.get("${url.trimEnd('/')}/health")
                        if (response.status.isSuccess()) {
                            println("Self ping success")
                        }
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

    monitor.subscribe(ApplicationStopping) {
        pingScope.cancel()
        client.close()
    }
}
