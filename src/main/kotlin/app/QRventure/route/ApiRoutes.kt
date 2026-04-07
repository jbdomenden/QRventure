package app.QRventure.route

import app.QRventure.dto.ErrorResponse
import app.QRventure.service.TourismService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.configureApiRoutes(connection: Connection?) {
    routing {
        route("/api") {
            if (connection == null) {
                get("/{...}") {
                    call.respond(
                        HttpStatusCode.ServiceUnavailable,
                        ErrorResponse("Database is unavailable. Check postgres settings and restart the server.")
                    )
                }
                return@route
            }

            val service = TourismService(connection)

            get("/featured") { call.respond(service.featured()) }

            get("/attractions") {
                call.respond(service.attractions(call.request.queryParameters["q"], call.request.queryParameters["category"]))
            }
            get("/attractions/{key}") {
                val key = call.parameters["key"]
                if (key.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing attraction key"))
                    return@get
                }
                val item = service.attractionBySlugOrId(key)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Attraction not found")) else call.respond(item)
            }

            get("/dining") { call.respond(service.dining(call.request.queryParameters["type"])) }
            get("/dining/{key}") {
                val key = call.parameters["key"]
                if (key.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing dining key"))
                    return@get
                }
                val item = service.diningBySlugOrId(key)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Dining place not found")) else call.respond(item)
            }

            get("/services") { call.respond(service.services(call.request.queryParameters["type"])) }
            get("/services/{key}") {
                val key = call.parameters["key"]
                if (key.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing service key"))
                    return@get
                }
                val item = service.serviceBySlugOrId(key)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Service not found")) else call.respond(item)
            }

            get("/search") {
                val query = call.request.queryParameters["q"]?.trim().orEmpty()
                if (query.length < 2) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Query must be at least 2 characters"))
                    return@get
                }
                call.respond(service.search(query))
            }
        }
    }
}
