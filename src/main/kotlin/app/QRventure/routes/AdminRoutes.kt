package app.QRventure.routes

import app.QRventure.auth.AdminCredentials
import app.QRventure.auth.AdminSession
import app.QRventure.auth.loadAdminCredentials
import app.QRventure.auth.verifyPassword
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
private data class AdminLoginRequest(val username: String, val password: String)

fun Application.configureAdminRoutes() {
    val adminCredentials = loadAdminCredentials()

    routing {
        get("/admin") {
            val destination = if (call.sessions.get<AdminSession>() == null) "/admin/login" else "/qrventure/admin/index.html"
            call.respondRedirect(destination, permanent = false)
        }

        get("/admin/login") {
            if (call.sessions.get<AdminSession>() != null) {
                call.respondRedirect("/admin", permanent = false)
                return@get
            }
            call.respondRedirect("/qrventure/admin/login.html", permanent = false)
        }

        route("/qrventure/admin") {
            intercept(ApplicationCallPipeline.Call) {
                val path = call.request.path().removePrefix("/qrventure/admin")
                val isLoginPage = path == "/login.html"
                if (!isLoginPage && call.sessions.get<AdminSession>() == null) {
                    call.respondRedirect("/admin/login", permanent = false)
                    finish()
                }
            }
            staticResources("", "static/qrventure/admin")
        }

        route("/api/admin") {
            post("/login") {
                val request = runCatching { call.receive<AdminLoginRequest>() }.getOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "Provide a valid username and password.")
                    )

                if (!request.matches(adminCredentials)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid username or password."))
                    return@post
                }

                call.sessions.set(AdminSession(username = adminCredentials.username))
                call.respond(HttpStatusCode.OK, mapOf("username" to adminCredentials.username))
            }

            post("/logout") {
                call.sessions.clear<AdminSession>()
                call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out."))
            }

            get("/me") {
                val session = call.sessions.get<AdminSession>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Unauthorized."))

                call.respond(HttpStatusCode.OK, mapOf("username" to session.username))
            }

            get("/health") {
                call.respond(mapOf("status" to "ok", "module" to "admin-auth"))
            }
        }
    }
}

private fun AdminLoginRequest.matches(adminCredentials: AdminCredentials): Boolean {
    if (username != adminCredentials.username) return false
    return verifyPassword(password, adminCredentials)
}
