package app.QRventure.route

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSiteRoutes() {
    routing {
        get("/") { call.respondRedirect("/qrventure", permanent = false) }
        get("/qrventure") { call.respondRedirect("/qrventure/index.html", permanent = false) }

        listOf("attractions", "attraction-detail", "dining", "dining-detail", "services", "service-detail", "navigation").forEach { page ->
            get("/qrventure/$page") { call.respondRedirect("/qrventure/$page.html", permanent = false) }
        }

        staticResources("/qrventure", "static/qrventure")
    }
}
