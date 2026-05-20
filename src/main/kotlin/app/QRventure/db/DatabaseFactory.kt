package app.QRventure.db

import app.QRventure.config.PostgresConfig
import app.QRventure.repositories.JdbcTourismRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.net.URI
import java.net.URLDecoder
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

object DatabaseFactory {
    fun createRepository(config: PostgresConfig): JdbcTourismRepository {
        val dataSource = createDataSource(config)
        initializeSchema(dataSource)
        seedReferenceContent(dataSource)
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

    private fun seedReferenceContent(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            insertBaselineServices(connection)
            insertBaselineDining(connection)
        }
    }

    private fun insertBaselineServices(connection: Connection) {
        connection.prepareStatement(
            """
            INSERT INTO local_services (
                slug,
                name,
                short_description,
                full_description,
                service_type,
                location_text,
                hours,
                contact_details,
                visitor_notes,
                latitude,
                longitude,
                image_path,
                image_urls,
                status,
                sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (slug) DO NOTHING
            """.trimIndent()
        ).use { statement ->
            baselineServices.forEach { item ->
                statement.bindLocalService(item)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun insertBaselineDining(connection: Connection) {
        connection.prepareStatement(
            """
            INSERT INTO dining_places (
                slug,
                name,
                short_description,
                full_description,
                dining_type,
                cuisine,
                location_text,
                opening_hours,
                price_range,
                contact_details,
                visitor_notes,
                latitude,
                longitude,
                image_path,
                image_urls,
                is_featured,
                status,
                sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (slug) DO UPDATE SET
                latitude = CASE
                    WHEN dining_places.latitude = 0 AND EXCLUDED.latitude <> 0 THEN EXCLUDED.latitude
                    ELSE dining_places.latitude
                END,
                longitude = CASE
                    WHEN dining_places.longitude = 0 AND EXCLUDED.longitude <> 0 THEN EXCLUDED.longitude
                    ELSE dining_places.longitude
                END,
                image_path = CASE
                    WHEN COALESCE(dining_places.image_path, '') = '' THEN EXCLUDED.image_path
                    ELSE dining_places.image_path
                END,
                image_urls = CASE
                    WHEN COALESCE(dining_places.image_urls, '') IN ('', '[]') THEN EXCLUDED.image_urls
                    ELSE dining_places.image_urls
                END
            """.trimIndent()
        ).use { statement ->
            baselineDining.forEach { item ->
                statement.bindDiningPlace(item)
                statement.addBatch()
            }
            statement.executeBatch()
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

private val baselineServices = listOf(
    SeedLocalService(
        slug = "national-emergency-hotline-911",
        name = "National Emergency Hotline 911",
        shortDescription = "24/7 emergency dispatch for police, fire, and medical help.",
        fullDescription = "Use 911 for urgent emergencies that need immediate police, fire, or ambulance response while visiting Intramuros or elsewhere in Manila.",
        serviceType = "Emergency Hotline",
        locationText = "Nationwide hotline for callers in Intramuros and Metro Manila",
        hours = "24/7",
        contactDetails = "911",
        visitorNotes = "Use for life-threatening emergencies and urgent incidents that require dispatch.",
        sortOrder = 901
    ),
    SeedLocalService(
        slug = "manila-mdrrmo-hotline",
        name = "Manila MDRRMO Emergency Hotline",
        shortDescription = "City disaster and emergency response hotline for Manila incidents.",
        fullDescription = "The Manila Disaster Risk Reduction and Management Office hotline can be used for storm response, flooding, rescue coordination, and urgent city-level emergencies.",
        serviceType = "Emergency Hotline",
        locationText = "Manila citywide response line serving Intramuros and nearby districts",
        hours = "24/7",
        contactDetails = "0932-662-2322",
        visitorNotes = "Useful backup contact during severe weather, flooding, or local rescue situations.",
        sortOrder = 902
    ),
    SeedLocalService(
        slug = "ospital-ng-maynila-emergency-contact",
        name = "Ospital ng Maynila Contact",
        shortDescription = "Public hospital contact for urgent medical guidance and hospital access.",
        fullDescription = "Ospital ng Maynila Lacson-Villegas Medical Center is one of the nearest major public hospitals for visitors needing urgent medical assistance beyond on-site first aid.",
        serviceType = "Emergency Medical Contact",
        locationText = "719 Quirino Avenue corner A. Mabini Street, Malate, Manila",
        hours = "24/7 emergency department; verify departments before visiting",
        contactDetails = "884-67629 / 0916-604-8881",
        visitorNotes = "Call ahead for urgent medical concerns and proceed to the nearest ER when necessary.",
        sortOrder = 903
    ),
    SeedLocalService(
        slug = "aleng-pulis-hotline",
        name = "Aleng Pulis Hotline",
        shortDescription = "Hotline for women and children protection concerns.",
        fullDescription = "The Aleng Pulis hotline is a dedicated support line for reporting abuse, harassment, and violence against women and children.",
        serviceType = "Emergency Support Hotline",
        locationText = "Metro Manila support line; accessible from Intramuros",
        hours = "24/7",
        contactDetails = "0919-777-7377",
        visitorNotes = "Use when you need help reporting abuse, harassment, or immediate protection concerns.",
        sortOrder = 904
    )
)

private val baselineDining = listOf(
    SeedDiningPlace(
        slug = "jollibee-aduana",
        name = "Jollibee Aduana",
        shortDescription = "Popular fast-food stop for burgers, Chickenjoy, and rice meals.",
        fullDescription = "Jollibee Aduana is a practical quick-meal option for visitors who want familiar Filipino fast food before or after exploring Intramuros landmarks.",
        diningType = "Fast Food",
        cuisine = "Filipino Fast Food",
        locationText = "Aduana corner Muralla Streets, Intramuros, Manila",
        openingHours = "Mon-Thu 6:00 AM - 9:45 PM; Fri-Sun 6:00 AM - 10:45 PM",
        priceRange = "PHP 99-350",
        contactDetails = "09296293916",
        visitorNotes = "Useful for quick breakfast, lunch, or takeout near the Muralla side of Intramuros.",
        imageUrl = "https://www.jollibee.com.ph/sites/g/files/iojlck111/files/styles/webp_optimized/public/2025-07/footer-logo.png.webp?itok=o4UsIY1c",
        sortOrder = 901
    ),
    SeedDiningPlace(
        slug = "mcdonalds-muralla",
        name = "McDonald's Muralla",
        shortDescription = "Quick-service branch for burgers, breakfast meals, and coffee.",
        fullDescription = "McDonald's Muralla gives travelers a familiar fast-food option near key university and heritage areas inside Intramuros.",
        diningType = "Fast Food",
        cuisine = "Burgers and Fried Chicken",
        locationText = "The Herald Building, 61 Muralla Street, Intramuros, Manila",
        openingHours = "Daily 5:30 AM - 10:00 PM",
        priceRange = "PHP 99-399",
        contactDetails = "McDonald's mobile app, GrabFood, and in-store ordering",
        visitorNotes = "Convenient if you need an early breakfast or a predictable quick meal during a packed itinerary.",
        latitude = 14.59294,
        longitude = 120.97769,
        imageUrl = "https://mcdonalds.com.ph/storage/imageable/setting/e4da3b7fbbce2345d7772b0674a318d5/dafault-McDelivery-Logo_Black-main.png",
        sortOrder = 902
    ),
    SeedDiningPlace(
        slug = "chowking-intramuros",
        name = "Chowking Intramuros",
        shortDescription = "Fast-food chain for Chinese-style rice meals, noodles, and dim sum.",
        fullDescription = "Chowking Intramuros is a convenient option for visitors who want quick rice bowls, noodle soups, and snack-sized Chinese fast-food items.",
        diningType = "Fast Food",
        cuisine = "Chinese Fast Food",
        locationText = "Ground Floor FEMII Building, A. Soriano Avenue, Intramuros, Manila",
        openingHours = "Daily 6:15 AM - 9:30 PM",
        priceRange = "PHP 99-320",
        contactDetails = "Chowking delivery channels and in-store ordering",
        visitorNotes = "A practical stop for quick merienda, halo-halo, or rice meals while walking around the walled city.",
        latitude = 14.59304,
        longitude = 120.97331,
        imageUrl = "https://www.chowking.ph/wp-content/uploads/2023/08/logo.svg",
        sortOrder = 903
    ),
    SeedDiningPlace(
        slug = "greenwich-aduana",
        name = "Greenwich Aduana",
        shortDescription = "Casual fast-food branch for pizza, pasta, and chicken meals.",
        fullDescription = "Greenwich Aduana gives visitors an accessible fast-food choice for pizza, pasta, and group-friendly meals close to Intramuros gates and schools.",
        diningType = "Fast Food",
        cuisine = "Pizza and Pasta",
        locationText = "Ground Floor FEMII Building, A. Soriano Avenue, Intramuros, Manila",
        openingHours = "Daily; verify current branch hours before visiting",
        priceRange = "PHP 129-499",
        contactDetails = "Greenwich delivery channels and in-store ordering",
        visitorNotes = "Useful for small groups looking for shareable meals without leaving the Intramuros area.",
        latitude = 14.59304,
        longitude = 120.97331,
        imageUrl = "https://www.greenwich.com.ph/images/banner/me-time-snack-active/1024.webp?version=1.38.8.1776238062348",
        sortOrder = 904
    ),
    SeedDiningPlace(
        slug = "kfc-intramuros",
        name = "KFC Intramuros",
        shortDescription = "Fried chicken chain for buckets, rice meals, burgers, and quick combos.",
        fullDescription = "KFC Intramuros is a reliable fast-food stop near Plaza Roma and Manila Cathedral, making it a convenient option for visitors who want a familiar chicken meal without leaving the district.",
        diningType = "Fast Food",
        cuisine = "Fried Chicken",
        locationText = "Unit A & B, G/F Shipping Center Condo Building, Soriano Avenue, Intramuros, Manila",
        openingHours = "Daily 7:00 AM - 9:30 PM",
        priceRange = "PHP 120-499",
        contactDetails = "+639176566929",
        visitorNotes = "Useful for quick group meals and takeout near the central heritage stops.",
        latitude = 14.589791,
        longitude = 120.975351,
        imageUrl = "https://cdn.tictuk.com/04d917c0-7bcd-6ced-acad-3e4db2bcbfd5/assets/logoDesktopHeader.svg",
        sortOrder = 905
    ),
    SeedDiningPlace(
        slug = "starbucks-muralla-herald",
        name = "Starbucks Muralla Herald",
        shortDescription = "Coffee chain branch for espresso drinks, pastries, and light cafe meals.",
        fullDescription = "Starbucks Muralla Herald offers a familiar coffee stop inside Intramuros for travelers needing a break, a pickup meeting point, or a light cafe meal near the Muralla side.",
        diningType = "Cafe Chain",
        cuisine = "Coffee and Light Meals",
        locationText = "61 Muralla Street, Herald Building, Intramuros, Manila",
        openingHours = "Delivery branch currently listed in Intramuros; verify in-store hours before visiting",
        priceRange = "PHP 130-300",
        contactDetails = "Starbucks app, foodpanda, GrabFood, and in-store ordering",
        visitorNotes = "A practical rest stop for coffee, pastries, and meeting up before a walking tour.",
        latitude = 14.59294,
        longitude = 120.97769,
        imageUrl = "https://starbucks.ph/images/sblogo.svg",
        sortOrder = 906
    ),
    SeedDiningPlace(
        slug = "figaro-intramuros",
        name = "Figaro Coffee Intramuros",
        shortDescription = "Philippine cafe chain serving coffee, breakfast plates, pasta, and pastries.",
        fullDescription = "Figaro Coffee Intramuros is a dependable sit-down cafe option for visitors who want coffee, light meals, and a calmer place to pause between heritage stops.",
        diningType = "Cafe Chain",
        cuisine = "Coffee and Casual Cafe Meals",
        locationText = "GF FEMII Building, Andres Soriano Jr. Avenue, Intramuros, Manila",
        openingHours = "Mon-Fri 7:00 AM - 9:00 PM; Sat-Sun 9:00 AM - 9:00 PM",
        priceRange = "PHP 150-450",
        contactDetails = "0906-500-5068",
        visitorNotes = "Commonly used as a meetup point for tours and a convenient coffee break near Plaza Roma.",
        latitude = 14.59304,
        longitude = 120.97331,
        imageUrl = "https://figarocoffee.com/wp-content/uploads/2023/06/logo-primary.png",
        sortOrder = 907
    )
)

private fun PreparedStatement.bindLocalService(item: SeedLocalService) {
    setString(1, item.slug)
    setString(2, item.name)
    setString(3, item.shortDescription)
    setString(4, item.fullDescription)
    setString(5, item.serviceType)
    setString(6, item.locationText)
    setString(7, item.hours)
    setString(8, item.contactDetails)
    setString(9, item.visitorNotes)
    setDouble(10, 0.0)
    setDouble(11, 0.0)
    setString(12, "")
    setString(13, "[]")
    setString(14, "open")
    setInt(15, item.sortOrder)
}

private fun PreparedStatement.bindDiningPlace(item: SeedDiningPlace) {
    setString(1, item.slug)
    setString(2, item.name)
    setString(3, item.shortDescription)
    setString(4, item.fullDescription)
    setString(5, item.diningType)
    setString(6, item.cuisine)
    setString(7, item.locationText)
    setString(8, item.openingHours)
    setString(9, item.priceRange)
    setString(10, item.contactDetails)
    setString(11, item.visitorNotes)
    setDouble(12, item.latitude)
    setDouble(13, item.longitude)
    setString(14, item.imageUrl)
    setString(15, jsonStringArray(item.imageUrl))
    setBoolean(16, false)
    setString(17, "open")
    setInt(18, item.sortOrder)
}

private fun jsonStringArray(vararg values: String): String {
    return values
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(prefix = "[", postfix = "]") { "\"${escapeJson(it)}\"" }
        .ifEmpty { "[]" }
}

private fun escapeJson(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

private data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String?,
    val password: String?
)

private data class SeedLocalService(
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val serviceType: String,
    val locationText: String,
    val hours: String,
    val contactDetails: String,
    val visitorNotes: String,
    val sortOrder: Int
)

private data class SeedDiningPlace(
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val diningType: String,
    val cuisine: String,
    val locationText: String,
    val openingHours: String,
    val priceRange: String,
    val contactDetails: String,
    val visitorNotes: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String,
    val sortOrder: Int
)
