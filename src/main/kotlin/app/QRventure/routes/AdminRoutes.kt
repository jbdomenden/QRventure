package app.QRventure.routes

import app.QRventure.auth.AdminCredentials
import app.QRventure.auth.AdminSession
import app.QRventure.auth.loadAdminCredentials
import app.QRventure.auth.verifyPassword
import app.QRventure.dto.*
import app.QRventure.service.TourismService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Serializable
private data class AdminLoginRequest(val username: String, val password: String)

private const val maxUploadBytes = 5L * 1024L * 1024L
private val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")

fun Application.configureAdminRoutes(connection: java.sql.Connection?) {
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

            if (connection == null) {
                get("/{...}") {
                    call.respond(HttpStatusCode.ServiceUnavailable, ErrorResponse("Database is unavailable."))
                }
            } else {
                val service = TourismService(connection)

                post("/upload") {
                    requireAdminSession() ?: return@post

                    val multipart = call.receiveMultipart()
                    var uploadResult: String? = null

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem && part.name == "file") {
                            val saved = saveUpload(part)
                            if (saved.isFailure) {
                                part.dispose()
                                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(saved.exceptionOrNull()?.message ?: "Invalid file upload."))
                            }
                            uploadResult = saved.getOrNull()
                        }
                        part.dispose()
                    }

                    val path = uploadResult
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("No file was uploaded."))

                    call.respond(HttpStatusCode.Created, mapOf("imagePath" to path))
                }

                route("/attractions") {
                    get {
                        requireAdminSession() ?: return@get
                        call.respond(service.attractions(search = call.request.queryParameters["q"], category = call.request.queryParameters["category"]))
                    }
                    get("/{key}") {
                        requireAdminSession() ?: return@get
                        val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing attraction key"))
                        val found = service.attractionBySlugOrId(key)
                            ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Attraction not found"))
                        call.respond(found)
                    }
                    post {
                        requireAdminSession() ?: return@post
                        val payload = parseBody<AttractionUpsertDto>() ?: return@post
                        validateAttraction(payload)?.let { return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        call.respond(HttpStatusCode.Created, service.createAttraction(payload))
                    }
                    put("/{key}") {
                        requireAdminSession() ?: return@put
                        val key = call.parameters["key"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing attraction key"))
                        val payload = parseBody<AttractionUpsertDto>() ?: return@put
                        validateAttraction(payload)?.let { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        val updated = service.updateAttraction(key, payload)
                            ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Attraction not found"))
                        call.respond(updated)
                    }
                    delete("/{key}") {
                        requireAdminSession() ?: return@delete
                        val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing attraction key"))
                        if (!service.deleteAttraction(key)) return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Attraction not found"))
                        call.respond(HttpStatusCode.NoContent)
                    }
                }

                route("/dining") {
                    get {
                        requireAdminSession() ?: return@get
                        call.respond(service.dining(type = call.request.queryParameters["type"], search = call.request.queryParameters["q"]))
                    }
                    get("/{key}") {
                        requireAdminSession() ?: return@get
                        val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing dining key"))
                        val found = service.diningBySlugOrId(key)
                            ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Dining place not found"))
                        call.respond(found)
                    }
                    post {
                        requireAdminSession() ?: return@post
                        val payload = parseBody<DiningUpsertDto>() ?: return@post
                        validateDining(payload)?.let { return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        call.respond(HttpStatusCode.Created, service.createDining(payload))
                    }
                    put("/{key}") {
                        requireAdminSession() ?: return@put
                        val key = call.parameters["key"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing dining key"))
                        val payload = parseBody<DiningUpsertDto>() ?: return@put
                        validateDining(payload)?.let { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        val updated = service.updateDining(key, payload)
                            ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Dining place not found"))
                        call.respond(updated)
                    }
                    delete("/{key}") {
                        requireAdminSession() ?: return@delete
                        val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing dining key"))
                        if (!service.deleteDining(key)) return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Dining place not found"))
                        call.respond(HttpStatusCode.NoContent)
                    }
                }

                route("/services") {
                    get {
                        requireAdminSession() ?: return@get
                        call.respond(service.services(type = call.request.queryParameters["type"], search = call.request.queryParameters["q"]))
                    }
                    get("/{key}") {
                        requireAdminSession() ?: return@get
                        val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing service key"))
                        val found = service.serviceBySlugOrId(key)
                            ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Service not found"))
                        call.respond(found)
                    }
                    post {
                        requireAdminSession() ?: return@post
                        val payload = parseBody<ServiceUpsertDto>() ?: return@post
                        validateService(payload)?.let { return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        call.respond(HttpStatusCode.Created, service.createService(payload))
                    }
                    put("/{key}") {
                        requireAdminSession() ?: return@put
                        val key = call.parameters["key"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing service key"))
                        val payload = parseBody<ServiceUpsertDto>() ?: return@put
                        validateService(payload)?.let { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        val updated = service.updateService(key, payload)
                            ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Service not found"))
                        call.respond(updated)
                    }
                    delete("/{key}") {
                        requireAdminSession() ?: return@delete
                        val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing service key"))
                        if (!service.deleteService(key)) return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Service not found"))
                        call.respond(HttpStatusCode.NoContent)
                    }
                }

                route("/routes") {
                    get {
                        requireAdminSession() ?: return@get
                        call.respond(service.tourRoutes(search = call.request.queryParameters["q"]))
                    }
                    get("/{key}") {
                        requireAdminSession() ?: return@get
                        val key = call.parameters["key"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing route key"))
                        val found = service.tourRouteBySlugOrId(key)
                            ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Route not found"))
                        call.respond(found)
                    }
                    post {
                        requireAdminSession() ?: return@post
                        val payload = parseBody<TourRouteUpsertDto>() ?: return@post
                        validateTourRoute(payload)?.let { return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        call.respond(HttpStatusCode.Created, service.createTourRoute(payload))
                    }
                    put("/{key}") {
                        requireAdminSession() ?: return@put
                        val key = call.parameters["key"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing route key"))
                        val payload = parseBody<TourRouteUpsertDto>() ?: return@put
                        validateTourRoute(payload)?.let { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(it)) }
                        val updated = service.updateTourRoute(key, payload)
                            ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Route not found"))
                        call.respond(updated)
                    }
                    delete("/{key}") {
                        requireAdminSession() ?: return@delete
                        val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing route key"))
                        if (!service.deleteTourRoute(key)) return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("Route not found"))
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}

private suspend inline fun <reified T> RoutingCall.parseBody(): T? =
    runCatching { receive<T>() }.getOrElse {
        respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body."))
        null
    }

private suspend fun RoutingCall.requireAdminSession(): AdminSession? {
    val session = sessions.get<AdminSession>()
    if (session == null) respond(HttpStatusCode.Unauthorized, ErrorResponse("Unauthorized."))
    return session
}

private fun saveUpload(filePart: PartData.FileItem): Result<String> = runCatching {
    val originalName = filePart.originalFileName?.trim().orEmpty()
    require(originalName.isNotBlank()) { "Uploaded file must have a name." }

    val extension = originalName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    require(extension in allowedExtensions) { "Unsupported file extension. Allowed: jpg, jpeg, png, webp." }

    val contentType = filePart.contentType?.toString()?.lowercase().orEmpty()
    require(contentType in allowedMimeTypes) { "Unsupported file type. Allowed: image/jpeg, image/png, image/webp." }

    val uploadsDir = Paths.get("src/main/resources/static/qrventure/uploads")
    Files.createDirectories(uploadsDir)

    val generatedName = "${UUID.randomUUID()}.$extension"
    val targetFile = uploadsDir.resolve(generatedName)

    try {
        filePart.streamProvider().use { input ->
            Files.newOutputStream(targetFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    total += read
                    require(total <= maxUploadBytes) { "File exceeds 5MB limit." }
                    output.write(buffer, 0, read)
                }
            }
        }
    } catch (ex: Exception) {
        Files.deleteIfExists(targetFile)
        throw ex
    }

    "/qrventure/uploads/$generatedName"
}

private fun AdminLoginRequest.matches(adminCredentials: AdminCredentials): Boolean {
    if (username != adminCredentials.username) return false
    return verifyPassword(password, adminCredentials)
}
