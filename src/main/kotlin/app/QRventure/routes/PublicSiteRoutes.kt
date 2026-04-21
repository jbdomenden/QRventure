package app.QRventure.routes

import app.QRventure.utils.ResourceUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePublicSiteRoutes() {
    routing {
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
        }

        get("/") {
            val homepage = ResourceUtils.readResourceOrNull("static/qrventure/index.html")
                ?: return@get call.respond(HttpStatusCode.InternalServerError, "Homepage not found")
            call.respondText(homepage, ContentType.Text.Html)
        }

        get("/qrventure") { call.respondRedirect("/qrventure/index.html", permanent = false) }
        get("/qrventure/") { call.respondRedirect("/qrventure/index.html", permanent = false) }

        listOf("attractions", "attraction-detail", "dining", "dining-detail", "services", "service-detail", "navigation", "routes", "route-detail").forEach { page ->
            get("/qrventure/$page") { call.respondRedirect("/qrventure/$page.html", permanent = false) }
        }

        staticResources("/qrventure", "static/qrventure")
    }
}
