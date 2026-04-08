package app.QRventure.routes

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAdminRoutes() {
    routing {
        get("/admin") { call.respondRedirect("/qrventure/admin/index.html", permanent = false) }
        staticResources("/qrventure/admin", "static/qrventure/admin")

        route("/api/admin") {
            get("/health") {
                call.respond(mapOf("status" to "ok", "module" to "admin-scaffold"))
            }
        }
    }
}
