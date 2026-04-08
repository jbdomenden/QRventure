package app.QRventure.config

import io.ktor.server.config.*

data class PostgresConfig(
    val url: String,
    val user: String,
    val password: String
)

object AppConfig {
    fun postgres(config: ApplicationConfig): PostgresConfig = PostgresConfig(
        url = config.property("postgres.url").getString(),
        user = config.property("postgres.user").getString(),
        password = config.property("postgres.password").getString()
    )
}
