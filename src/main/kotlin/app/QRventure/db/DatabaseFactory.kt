package app.QRventure.db

import app.QRventure.config.PostgresConfig
import app.QRventure.repositories.JdbcTourismRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.net.URI
import java.net.URLDecoder
import javax.sql.DataSource

object DatabaseFactory {
    fun createRepository(config: PostgresConfig): JdbcTourismRepository {
        val dataSource = createDataSource(config)
        initializeSchema(dataSource)
        return JdbcTourismRepository(dataSource)
    }

    private fun createDataSource(postgresConfig: PostgresConfig): HikariDataSource {
        val settings = databaseSettings(postgresConfig)
        val config = HikariConfig().apply {
            jdbcUrl = settings.jdbcUrl
            username = settings.username
            password = settings.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = postgresConfig.poolMaxSize.coerceAtLeast(1)
            minimumIdle = 0
            initializationFailTimeout = -1
            isAutoCommit = true
        }
        return HikariDataSource(config)
    }

    fun initializeSchema(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { st ->
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS attractions (
                        id SERIAL PRIMARY KEY,
                        slug VARCHAR(120) UNIQUE NOT NULL,
                        name VARCHAR(180) NOT NULL,
                        alternate_names TEXT NOT NULL DEFAULT '[]',
                        short_description TEXT NOT NULL DEFAULT '',
                        full_description TEXT NOT NULL DEFAULT '',
                        category VARCHAR(80) NOT NULL DEFAULT '',
                        historical_period VARCHAR(120) NOT NULL DEFAULT '',
                        location_text VARCHAR(220) NOT NULL DEFAULT '',
                        opening_hours VARCHAR(150) NOT NULL DEFAULT '',
                        entrance_fee VARCHAR(120) NOT NULL DEFAULT '',
                        contact_details VARCHAR(180) NOT NULL DEFAULT '',
                        visitor_tips TEXT NOT NULL DEFAULT '',
                        best_time_to_visit VARCHAR(120) NOT NULL DEFAULT '',
                        latitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        longitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        image_path TEXT NOT NULL DEFAULT '',
                        image_urls TEXT NOT NULL DEFAULT '[]',
                        is_featured BOOLEAN NOT NULL DEFAULT FALSE,
                        status VARCHAR(30) NOT NULL DEFAULT 'open',
                        sort_order INT NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS dining_places (
                        id SERIAL PRIMARY KEY,
                        slug VARCHAR(120) UNIQUE NOT NULL,
                        name VARCHAR(180) NOT NULL,
                        short_description TEXT NOT NULL DEFAULT '',
                        full_description TEXT NOT NULL DEFAULT '',
                        dining_type VARCHAR(100) NOT NULL DEFAULT '',
                        cuisine VARCHAR(100) NOT NULL DEFAULT '',
                        location_text VARCHAR(220) NOT NULL DEFAULT '',
                        opening_hours VARCHAR(150) NOT NULL DEFAULT '',
                        price_range VARCHAR(80) NOT NULL DEFAULT '',
                        contact_details VARCHAR(180) NOT NULL DEFAULT '',
                        visitor_notes TEXT NOT NULL DEFAULT '',
                        latitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        longitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        image_path TEXT NOT NULL DEFAULT '',
                        image_urls TEXT NOT NULL DEFAULT '[]',
                        is_featured BOOLEAN NOT NULL DEFAULT FALSE,
                        status VARCHAR(30) NOT NULL DEFAULT 'open',
                        sort_order INT NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS local_services (
                        id SERIAL PRIMARY KEY,
                        slug VARCHAR(120) UNIQUE NOT NULL,
                        name VARCHAR(180) NOT NULL,
                        short_description TEXT NOT NULL DEFAULT '',
                        full_description TEXT NOT NULL DEFAULT '',
                        service_type VARCHAR(100) NOT NULL DEFAULT '',
                        location_text VARCHAR(220) NOT NULL DEFAULT '',
                        hours VARCHAR(150) NOT NULL DEFAULT '',
                        contact_details VARCHAR(180) NOT NULL DEFAULT '',
                        visitor_notes TEXT NOT NULL DEFAULT '',
                        latitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        longitude DOUBLE PRECISION NOT NULL DEFAULT 0,
                        image_path TEXT NOT NULL DEFAULT '',
                        image_urls TEXT NOT NULL DEFAULT '[]',
                        status VARCHAR(30) NOT NULL DEFAULT 'open',
                        sort_order INT NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS tour_routes (
                        id SERIAL PRIMARY KEY,
                        slug VARCHAR(120) UNIQUE NOT NULL,
                        name VARCHAR(180) NOT NULL,
                        short_description TEXT NOT NULL DEFAULT '',
                        full_description TEXT NOT NULL DEFAULT '',
                        route_type VARCHAR(80) NOT NULL DEFAULT '',
                        starting_point VARCHAR(220) NOT NULL DEFAULT '',
                        estimated_duration VARCHAR(80) NOT NULL DEFAULT '',
                        travel_tips TEXT NOT NULL DEFAULT '',
                        distance_text VARCHAR(80) NOT NULL DEFAULT '',
                        map_link TEXT NOT NULL DEFAULT '',
                        image_url TEXT NOT NULL DEFAULT '',
                        image_urls TEXT NOT NULL DEFAULT '[]',
                        is_featured BOOLEAN NOT NULL DEFAULT FALSE,
                        status VARCHAR(30) NOT NULL DEFAULT 'open',
                        sort_order INT NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS alternate_names TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS category VARCHAR(80) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS historical_period VARCHAR(120) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS visitor_tips TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS best_time_to_visit VARCHAR(120) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS dining_type VARCHAR(100) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS cuisine VARCHAR(100) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS hours VARCHAR(150) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS route_type VARCHAR(80) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS starting_point VARCHAR(220) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS estimated_duration VARCHAR(80) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS travel_tips TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS distance_text VARCHAR(80) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS map_link TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS image_url TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")
            }
        }
    }

    private fun databaseSettings(config: PostgresConfig): DatabaseSettings {
        val explicitJdbcUrl = config.jdbcUrl.trim()
        val explicitUser = config.user.trim()
        val explicitPassword = config.password.trim()

        if (explicitJdbcUrl.isNotBlank()) {
            return DatabaseSettings(
                jdbcUrl = normalizeJdbcUrl(explicitJdbcUrl),
                username = explicitUser.ifBlank { null },
                password = explicitPassword.ifBlank { null }
            )
        }

        val rawDatabaseUrl = config.databaseUrl.trim()
        require(rawDatabaseUrl.isNotBlank()) {
            "Set postgres.jdbcUrl or postgres.databaseUrl in application config."
        }

        val uri = URI(rawDatabaseUrl)
        val credentials = parseUserInfo(uri.userInfo)
        val path = uri.path?.removePrefix("/").orEmpty().ifBlank { "postgres" }
        val query = uri.rawQuery?.takeIf { it.isNotBlank() }?.let { "?$it" }.orEmpty()
        val port = if (uri.port > 0) uri.port else 5432

        return DatabaseSettings(
            jdbcUrl = "jdbc:postgresql://${uri.host}:$port/$path$query",
            username = explicitUser.ifBlank { credentials.first },
            password = explicitPassword.ifBlank { credentials.second }
        )
    }

    private fun normalizeJdbcUrl(rawUrl: String): String {
        return when {
            rawUrl.startsWith("jdbc:postgresql://", ignoreCase = true) -> rawUrl
            rawUrl.startsWith("postgresql://", ignoreCase = true) -> "jdbc:$rawUrl"
            rawUrl.startsWith("postgres://", ignoreCase = true) -> "jdbc:postgresql://${rawUrl.removePrefix("postgres://")}"
            else -> throw IllegalArgumentException("Database URL must start with jdbc:postgresql://, postgresql://, or postgres://")
        }
    }

    private fun parseUserInfo(userInfo: String?): Pair<String?, String?> {
        if (userInfo.isNullOrBlank()) {
            return null to null
        }

        val parts = userInfo.split(":", limit = 2)
        val user = parts.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(::decode)
        val password = parts.getOrNull(1)?.takeIf { it.isNotBlank() }?.let(::decode)
        return user to password
    }

    private fun decode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8)
}

private data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String?,
    val password: String?
)
