package app.QRventure.auth

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AdminSession(val username: String)

data class AdminCredentials(
    val username: String,
    val passwordHash: ByteArray,
    val passwordSalt: ByteArray,
    val iterations: Int
)

private const val DEFAULT_ADMIN_USERNAME = "admin"
private const val DEFAULT_PASSWORD_HASH = "uuwgBkpbBLDkauDW88ClZxZDarRvu9hsHzJUJCGI2VY="
private const val DEFAULT_PASSWORD_SALT = "7FEZnOhXPhFJr4JKFEtqQQ=="
private const val DEFAULT_PASSWORD_ITERATIONS = 120_000

fun Application.configureAdminAuth() {
    install(Sessions) {
        cookie<AdminSession>("qrventure_admin_session") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = false
            cookie.extensions["SameSite"] = "lax"
            cookie.maxAge = 60 * 60 * 12
        }
    }
}

fun Application.loadAdminCredentials(): AdminCredentials {
    val adminConfig = runCatching { environment.config.config("adminAuth") }.getOrNull()
    val username = adminConfig?.propertyOrNull("username")?.getString() ?: DEFAULT_ADMIN_USERNAME
    val passwordHash = adminConfig?.propertyOrNull("passwordHash")?.getString() ?: DEFAULT_PASSWORD_HASH
    val passwordSalt = adminConfig?.propertyOrNull("passwordSalt")?.getString() ?: DEFAULT_PASSWORD_SALT
    val iterations = adminConfig?.propertyOrNull("iterations")?.getString()?.toIntOrNull() ?: DEFAULT_PASSWORD_ITERATIONS

    return AdminCredentials(
        username = username,
        passwordHash = Base64.getDecoder().decode(passwordHash),
        passwordSalt = Base64.getDecoder().decode(passwordSalt),
        iterations = iterations
    )
}

fun verifyPassword(password: String, credentials: AdminCredentials): Boolean {
    val hashed = hashPassword(password, credentials.passwordSalt, credentials.iterations)
    return MessageDigest.isEqual(hashed, credentials.passwordHash)
}

private fun hashPassword(password: String, salt: ByteArray, iterations: Int): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
}
