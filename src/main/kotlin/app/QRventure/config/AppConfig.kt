package app.QRventure.config

import io.ktor.server.config.ApplicationConfig

data class PostgresConfig(
    val jdbcUrl: String,
    val databaseUrl: String,
    val user: String,
    val password: String,
    val poolMaxSize: Int
)

object AppConfig {
    fun postgres(config: ApplicationConfig): PostgresConfig = PostgresConfig(
        jdbcUrl = config.propertyOrNull("postgres.jdbcUrl")?.getString()?.trim().orEmpty(),
        databaseUrl = config.propertyOrNull("postgres.databaseUrl")?.getString()?.trim().orEmpty(),
        user = config.propertyOrNull("postgres.user")?.getString()?.trim().orEmpty(),
        password = config.propertyOrNull("postgres.password")?.getString()?.trim().orEmpty(),
        poolMaxSize = config.propertyOrNull("postgres.poolMaxSize")?.getString()?.toIntOrNull() ?: 5
    )
}
