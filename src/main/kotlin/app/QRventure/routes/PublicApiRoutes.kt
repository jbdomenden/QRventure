package app.QRventure.routes

import app.QRventure.dto.ErrorResponse
import app.QRventure.services.TourismService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.configurePublicApiRoutes(connection: Connection?) {
    routing {
        route("/api") {
            if (connection == null) {
                get("/{...}") {
                    call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("Database is unavailable. Check postgres settings and restart the server."))
                }
                return@route
            }

            val service = TourismService(connection)
            get("/featured") { call.respond(service.featured()) }
            get("/attractions") { call.respond(service.attractions(call.request.queryParameters["q"], call.request.queryParameters["category"])) }
            get("/attractions/{idOrSlug}") {
                val idOrSlug = call.parameters["idOrSlug"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing attraction key"))
                val item = service.attractionBySlugOrId(idOrSlug)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Attraction not found")) else call.respond(item)
            }
            get("/dining") { call.respond(service.dining(call.request.queryParameters["type"])) }
            get("/dining/{key}") {
                val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing dining key"))
                val item = service.diningBySlugOrId(key)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Dining place not found")) else call.respond(item)
            }
            get("/services") { call.respond(service.services(call.request.queryParameters["type"])) }
            get("/services/{key}") {
                val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing service key"))
                val item = service.serviceBySlugOrId(key)
                if (item == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Service not found")) else call.respond(item)
            }
            get("/search") {
                val query = call.request.queryParameters["q"]?.trim().orEmpty()
                if (query.length < 2) return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Query must be at least 2 characters"))
                call.respond(service.search(query))
            }
            get("/routes") { call.respond(service.tourRoutes(call.request.queryParameters["q"])) }
        }
    }
}
