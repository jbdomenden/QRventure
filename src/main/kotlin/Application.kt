package app.QRventure

import app.QRventure.routes.configurePublicSiteRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configurePublicSiteRoutes()
}
