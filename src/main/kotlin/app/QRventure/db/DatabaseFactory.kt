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
                        opening_hours TEXT NOT NULL DEFAULT '',
                        entrance_fee TEXT NOT NULL DEFAULT '',
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
                        opening_hours TEXT NOT NULL DEFAULT '',
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
                        hours TEXT NOT NULL DEFAULT '',
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
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS opening_hours TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ALTER COLUMN opening_hours TYPE TEXT")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS entrance_fee TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ALTER COLUMN entrance_fee TYPE TEXT")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS visitor_tips TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS best_time_to_visit VARCHAR(120) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS dining_type VARCHAR(100) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS cuisine VARCHAR(100) NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS opening_hours TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ALTER COLUMN opening_hours TYPE TEXT")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT '[]'")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
                st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS hours TEXT NOT NULL DEFAULT ''")
                st.execute("ALTER TABLE local_services ALTER COLUMN hours TYPE TEXT")
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
            insertBaselineAttractions(connection)
            insertBaselineServices(connection)
            insertBaselineDining(connection)
            insertBaselineRoutes(connection)
        }
    }

    private fun insertBaselineAttractions(connection: Connection) {
        connection.prepareStatement(
            """
            INSERT INTO attractions (
                slug,
                name,
                alternate_names,
                short_description,
                full_description,
                category,
                historical_period,
                location_text,
                opening_hours,
                entrance_fee,
                contact_details,
                visitor_tips,
                best_time_to_visit,
                latitude,
                longitude,
                image_path,
                image_urls,
                is_featured,
                status,
                sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (slug) DO UPDATE SET
                name = EXCLUDED.name,
                alternate_names = EXCLUDED.alternate_names,
                short_description = EXCLUDED.short_description,
                full_description = EXCLUDED.full_description,
                category = EXCLUDED.category,
                historical_period = EXCLUDED.historical_period,
                location_text = EXCLUDED.location_text,
                opening_hours = EXCLUDED.opening_hours,
                entrance_fee = EXCLUDED.entrance_fee,
                contact_details = EXCLUDED.contact_details,
                visitor_tips = EXCLUDED.visitor_tips,
                best_time_to_visit = EXCLUDED.best_time_to_visit,
                latitude = EXCLUDED.latitude,
                longitude = EXCLUDED.longitude,
                image_path = EXCLUDED.image_path,
                image_urls = EXCLUDED.image_urls,
                is_featured = EXCLUDED.is_featured,
                status = EXCLUDED.status,
                sort_order = EXCLUDED.sort_order
            """.trimIndent()
        ).use { statement ->
            baselineAttractions.forEach { item ->
                statement.bindAttraction(item)
                statement.addBatch()
            }
            statement.executeBatch()
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
                name = EXCLUDED.name,
                dining_type = EXCLUDED.dining_type,
                cuisine = EXCLUDED.cuisine,
                location_text = EXCLUDED.location_text,
                sort_order = EXCLUDED.sort_order,
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

    private fun insertBaselineRoutes(connection: Connection) {
        connection.prepareStatement(
            """
            INSERT INTO tour_routes (
                slug,
                name,
                short_description,
                full_description,
                route_type,
                starting_point,
                estimated_duration,
                travel_tips,
                distance_text,
                map_link,
                image_url,
                image_urls,
                is_featured,
                status,
                sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (slug) DO UPDATE SET
                name = EXCLUDED.name,
                short_description = EXCLUDED.short_description,
                full_description = EXCLUDED.full_description,
                route_type = EXCLUDED.route_type,
                starting_point = EXCLUDED.starting_point,
                estimated_duration = EXCLUDED.estimated_duration,
                travel_tips = EXCLUDED.travel_tips,
                distance_text = EXCLUDED.distance_text,
                map_link = EXCLUDED.map_link,
                image_url = EXCLUDED.image_url,
                image_urls = EXCLUDED.image_urls,
                is_featured = EXCLUDED.is_featured,
                status = EXCLUDED.status,
                sort_order = EXCLUDED.sort_order
            """.trimIndent()
        ).use { statement ->
            baselineRoutes.forEach { item ->
                statement.bindTourRoute(item)
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

private val baselineAttractions = listOf(
    SeedAttraction(
        slug = "fort-santiago",
        name = "Fort Santiago",
        shortDescription = "Iconic citadel at the northern tip of Intramuros and one of Manila's defining Spanish-era landmarks.",
        fullDescription = "Fort Santiago stands at the mouth of the Pasig River where the Spanish established the main citadel of Intramuros. The stone fort rebuilt in 1590 later served as a military headquarters, prison, memorial landscape, and one of the most visited heritage sites in Manila.",
        category = "Fortress / heritage site",
        historicalPeriod = "Spanish Colonial",
        locationText = "Santa Clara St., Intramuros, Manila",
        openingHours = "Mon-Fri 8:00 AM - 10:00 PM (last entry 8:00 PM); Sat-Sun 6:00 AM - 10:00 PM (last entry 8:30 PM). Late entry may be limited to partner bookings.",
        entranceFee = "PHP 75 regular; PHP 50 student, senior citizen, PWD",
        contactDetails = "Intramuros Administration: 8527-1572 | intramuros.gov.ph/hours",
        visitorTips = "Start here early if you want the ramparts, Rizal-related stops, and gardens before the heaviest foot traffic and midday heat.",
        bestTimeToVisit = "Early morning or late afternoon",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/20-1.png",
        isFeatured = true,
        sortOrder = 801
    ),
    SeedAttraction(
        slug = "museo-ni-rizal-fort-santiago",
        name = "Museo ni Rizal - Fort Santiago",
        alternateNames = listOf("Rizal Shrine"),
        shortDescription = "Memorial museum in the old barracks where Jose Rizal spent his final days before execution.",
        fullDescription = "Museo ni Rizal, also widely known as the Rizal Shrine in Fort Santiago, occupies the old barracks where Jose Rizal was imprisoned from November 3 to December 29, 1896. It works best as a historical stop paired with Rizal's final footsteps markers and the wider Fort Santiago grounds.",
        category = "Museum / shrine",
        historicalPeriod = "Spanish Colonial / Rizal Era",
        locationText = "Inside Fort Santiago, Santa Clara St., Intramuros, Manila",
        openingHours = "Tue-Sun 9:00 AM - 4:00 PM",
        entranceFee = "Included with Fort Santiago admission",
        contactDetails = "Museo ni Rizal: 09178519548 | fb.com/mjrfsofficial",
        visitorTips = "Visit after entering Fort Santiago while the museum rooms are cooler and quieter, then continue to the fort's memorial markers and gardens.",
        bestTimeToVisit = "Morning, before the fort grounds get busier",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/33-1.png",
        isFeatured = true,
        sortOrder = 802
    ),
    SeedAttraction(
        slug = "san-agustin-church",
        name = "San Agustin Church",
        shortDescription = "UNESCO World Heritage church and the oldest surviving stone church structure in the Philippines.",
        fullDescription = "San Agustin Church was founded by the Augustinians in the 16th century and completed in 1607. It survived earthquakes and the destruction of World War II, and remains the strongest ecclesiastical anchor inside Intramuros as part of the Baroque Churches of the Philippines UNESCO inscription.",
        category = "Church / UNESCO site",
        historicalPeriod = "Spanish Colonial",
        locationText = "Gen. Luna St., Intramuros, Manila",
        openingHours = "Church access changes around masses, weddings, and religious events; verify the current church schedule before visiting.",
        entranceFee = "Church entry is generally free; museum admission is separate",
        contactDetails = "Check the San Agustin Church and Museum complex on site for current worship and visitor access details.",
        visitorTips = "If you want quiet architectural viewing, avoid peak ceremony windows and pair the stop with the museum next door.",
        bestTimeToVisit = "Weekday morning outside liturgical schedules",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-san-agustin-nave.jpg",
        isFeatured = true,
        sortOrder = 803
    ),
    SeedAttraction(
        slug = "san-agustin-museum",
        name = "San Agustin Museum",
        shortDescription = "Cloister museum beside San Agustin Church with religious art, archives, and colonial-era artifacts.",
        fullDescription = "San Agustin Museum occupies the adjoining monastery complex of San Agustin Church and is one of the strongest indoor cultural stops in Intramuros. Its galleries cover centuries of religious art, archival material, stonework, and monastic spaces that give more depth than a quick church-only visit.",
        category = "Museum",
        historicalPeriod = "Spanish Colonial / Religious Collection",
        locationText = "San Agustin complex, General Luna St., Intramuros, Manila",
        openingHours = "Mon-Sun 8:00 AM - 12:00 PM; 1:00 PM - 5:00 PM",
        entranceFee = "Approx. PHP 200; verify current museum ticketing on site",
        contactDetails = "San Agustin Museum: 8714-6889 / 8714-7470 | fb.com/sanagustinmuseum",
        visitorTips = "Give this more time than a quick photo stop. The cloister galleries and upper levels reward a slower museum visit.",
        bestTimeToVisit = "Morning, before church and group traffic build up",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-san-agustin-museum.jpg",
        isFeatured = true,
        sortOrder = 804
    ),
    SeedAttraction(
        slug = "manila-cathedral",
        name = "Manila Cathedral",
        alternateNames = listOf("Cathedral of Manila", "Minor Basilica of the Immaculate Conception"),
        shortDescription = "Major Catholic basilica beside Plaza Roma and one of the most recognizable landmarks in Intramuros.",
        fullDescription = "The Manila Cathedral is the historic metropolitan cathedral of Manila and the first cathedral seat in the Philippines. The current structure, completed in 1958 after repeated destruction and reconstruction, remains one of the district's strongest civic and religious landmarks.",
        category = "Church / historical landmark",
        historicalPeriod = "Spanish Colonial / Modern Reconstruction",
        locationText = "Beaterio St. corner Cabildo St., Plaza Roma, Intramuros, Manila",
        openingHours = "Masses: Mon-Fri 7:30 AM and 12:10 PM; Sat 7:30 AM; Sun 8:00 AM, 10:00 AM, and 6:00 PM. Office hours: Tue-Sat 8:00 AM - 4:30 PM; Sun 8:00 AM - 11:30 AM; closed Mondays and holidays.",
        entranceFee = "Free church entry",
        contactDetails = "Manila Cathedral: (02) 8527-1796 / (02) 8527-3093 / 0917-541-2103 | manilacathedral.com.ph",
        visitorTips = "If you want unobstructed photos, avoid wedding blocks and peak mass times. If you are visiting for worship, plan around the published mass schedule instead.",
        bestTimeToVisit = "Early weekday morning or late afternoon",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-manila-cathedral.jpg",
        isFeatured = true,
        sortOrder = 805
    ),
    SeedAttraction(
        slug = "casa-manila",
        name = "Casa Manila Museum",
        shortDescription = "Lifestyle museum recreating the domestic world of an affluent late-Spanish-colonial Manila household.",
        fullDescription = "Casa Manila is a reconstructed bahay na bato museum inside Plaza San Luis that introduces visitors to elite domestic life in late Spanish Manila. It is a compact and approachable stop for period interiors, household objects, and heritage-house interpretation within the core Intramuros museum zone.",
        category = "Museum / heritage house",
        historicalPeriod = "Spanish Colonial Lifestyle Reconstruction",
        locationText = "Plaza San Luis Complex, General Luna St., Intramuros, Manila",
        openingHours = "Tue-Sun 9:00 AM - 6:00 PM (last entry 5:00 PM); closed Mondays",
        entranceFee = "PHP 75 regular; PHP 50 discounted",
        contactDetails = "Intramuros Administration: 8527-3108 / 8527-9012 / 8527-3096 | intramuros.gov.ph/hours",
        visitorTips = "Best paired with Plaza San Luis and San Agustin Museum. It is a shorter stop than Fort Santiago, so budget your museum time accordingly.",
        bestTimeToVisit = "Weekday morning or mid-afternoon",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/21-1.png",
        isFeatured = true,
        sortOrder = 806
    ),
    SeedAttraction(
        slug = "plaza-san-luis-complex",
        name = "Plaza San Luis Complex",
        shortDescription = "Reconstructed heritage block anchored by Casa Manila and several active cultural and commercial tenants.",
        fullDescription = "Plaza San Luis Complex is a cluster of reconstructed 19th-century-style houses across from San Agustin Church. It functions as both a heritage streetscape and a practical visitor zone with Casa Manila, restaurants, craft shops, and small commercial tenants in one walkable block.",
        category = "Heritage complex",
        historicalPeriod = "Spanish Colonial / Heritage Reconstruction",
        locationText = "General Luna St. corner Real St., Intramuros, Manila",
        openingHours = "Open-air heritage block; individual museums, restaurants, and shops keep their own operating hours",
        entranceFee = "Free to walk through; tenant and museum charges vary",
        contactDetails = "Schedules vary by tenant; Casa Manila inside the complex follows the current Intramuros Administration museum schedule.",
        visitorTips = "Treat this as a short but worthwhile streetscape stop rather than a long standalone attraction unless you are also dining, shopping, or entering Casa Manila.",
        bestTimeToVisit = "Late afternoon, when the block is active and easier to photograph",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-plaza-san-luis.jpg",
        sortOrder = 807
    ),
    SeedAttraction(
        slug = "baluarte-de-san-diego",
        name = "Baluarte de San Diego",
        shortDescription = "Oldest major fortification in Intramuros, combining a circular ruin, spade-shaped bulwark, and garden setting.",
        fullDescription = "Baluarte de San Diego began as the circular Fort Nuestra Senora de Guia in the late 16th century and later became part of the larger San Diego bulwark. Its excavated ruins, layered military history, and garden setting make it one of the most visually distinctive fortification stops in Intramuros.",
        category = "Fortification / garden",
        historicalPeriod = "Spanish Colonial",
        locationText = "Santa Lucia St. cor. Muralla St., Intramuros, Manila",
        openingHours = "Mon-Sun 8:00 AM - 5:00 PM (last entry 4:00 PM)",
        entranceFee = "Check the current ticket rate on intramuros.gov.ph/hours before visiting",
        contactDetails = "Intramuros Administration: 8527-3108 / 8527-9012 / 8527-3096 | intramuros.gov.ph",
        visitorTips = "This is one of the best late-afternoon photo stops in Intramuros. Pair it with Puerta Real Gardens and the Santa Lucia gate stretch.",
        bestTimeToVisit = "Late afternoon for softer light on the stone ruins",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/22-1.png",
        isFeatured = true,
        sortOrder = 808
    ),
    SeedAttraction(
        slug = "museo-de-intramuros",
        name = "Museo de Intramuros",
        shortDescription = "Museum inside the rebuilt San Ignacio complex focused on ecclesiastical art, evangelization, and religious heritage.",
        fullDescription = "Museo de Intramuros occupies the rebuilt San Ignacio and Mission House complex and holds one of the most substantial state-managed collections of ecclesiastical art in the country. It is one of the strongest museum stops for visitors who want context beyond fortifications and facades.",
        category = "Museum",
        historicalPeriod = "Spanish Colonial Heritage / Modern Reconstruction",
        locationText = "Arzobispo St. cor. Real St., Intramuros, Manila",
        openingHours = "Tue-Sun 9:00 AM - 6:00 PM (last entry 4:30 PM); closed Mondays",
        entranceFee = "Included in the current Centro de Turismo / Museo de Intramuros paid access ticket: PHP 150 regular; PHP 120 student, senior citizen, PWD",
        contactDetails = "Intramuros Administration: 8527-3108 | intramuros.gov.ph/hours",
        visitorTips = "Use this for a longer indoor stop. It rewards visitors who want labels, context, and religious heritage collections rather than only exterior sightseeing.",
        bestTimeToVisit = "Morning or early afternoon on a museum-focused day",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/23-1.png",
        isFeatured = true,
        sortOrder = 809
    ),
    SeedAttraction(
        slug = "centro-de-turismo-intramuros",
        name = "Centro de Turismo Intramuros",
        shortDescription = "New visitor hub and interpretive center with orientation exhibits, visitor services, and event-ready cultural space.",
        fullDescription = "Opened in 2024, Centro de Turismo Intramuros gives first-time visitors a practical orientation point before they move deeper into the walled city. It combines visitor services, introductory exhibits, and access to the reconstructed San Ignacio heritage complex.",
        category = "Tourist center / museum",
        historicalPeriod = "Modern Intramuros",
        locationText = "Arzobispo St. cor. Real St., Intramuros, Manila",
        openingHours = "Tue-Sun 9:00 AM - 6:00 PM (last entry 5:30 PM); visitor desk open daily 9:00 AM - 6:00 PM",
        entranceFee = "Free access to the visitor center and Centro museum; paid combo access with Museo de Intramuros and additional areas is PHP 150 regular / PHP 120 discounted",
        contactDetails = "Centro de Turismo visitor desk: cdti.intramuros.gov.ph",
        visitorTips = "Use this as your first stop if you want orientation before choosing between museums, churches, and fortification walks.",
        bestTimeToVisit = "Start-of-day orientation or midday museum break",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/24-1.png",
        isFeatured = true,
        sortOrder = 810
    ),
    SeedAttraction(
        slug = "bahay-tsinoy",
        name = "Bahay Tsinoy",
        alternateNames = listOf("Kaisa-Angelo King Heritage Center"),
        shortDescription = "Museum dedicated to Chinese-Filipino history, exchange, migration, and community contributions.",
        fullDescription = "Bahay Tsinoy presents the long history of exchanges between China and the Philippines and the development of the Chinese-Filipino community. It gives Intramuros visitors a museum narrative that is social, commercial, and community-centered rather than purely military or ecclesiastical.",
        category = "Museum",
        historicalPeriod = "Chinese-Filipino Heritage",
        locationText = "Anda St. cor. Cabildo St., Intramuros, Manila",
        openingHours = "Check the museum's current schedule before going",
        entranceFee = "Verify current museum admission on site",
        contactDetails = "Bahay Tsinoy: 8526-6796 / 8527-6083 / 8527-6085 | fb.com/bahaytsinoy",
        visitorTips = "A strong stop if you want a wider Manila story that connects trade, migration, family history, and identity beyond the Spanish walls alone.",
        bestTimeToVisit = "Late morning or early afternoon",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/35-1.png",
        sortOrder = 811
    ),
    SeedAttraction(
        slug = "puerta-real-gardens",
        name = "Puerta Real Gardens",
        shortDescription = "Garden and historic gate zone built around one of Intramuros' ceremonial southern approaches.",
        fullDescription = "Puerta Real Gardens sits around the Royal Gate used by governors-general during special occasions and provides a softer landscaped stop on the southern edge of Intramuros. It works well as a pause point between Baluarte de San Diego and the Santa Lucia side of the walls.",
        category = "Garden / fortification area",
        historicalPeriod = "Spanish Colonial",
        locationText = "Real St., Intramuros, Manila",
        openingHours = "Open-air garden area; access conditions may vary for events, shoots, or site management",
        entranceFee = "Free outdoor access",
        contactDetails = "Check current access conditions with the Intramuros Administration if you are planning a shoot or organized visit.",
        visitorTips = "Best treated as a scenic outdoor stop rather than a long standalone attraction. Pair it with Baluarte de San Diego or a southern wall walk.",
        bestTimeToVisit = "Late afternoon",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/30-1.png",
        sortOrder = 812
    ),
    SeedAttraction(
        slug = "plaza-roma",
        name = "Plaza Roma",
        alternateNames = listOf("Plaza Mayor"),
        shortDescription = "Historic main square of Intramuros and the easiest orientation point for the district's civic core.",
        fullDescription = "Plaza Roma, formerly Plaza Mayor, was the ceremonial and civic heart of Spanish Manila. It remains the easiest orientation point for visitors moving between Manila Cathedral, Ayuntamiento, Palacio del Gobernador, and the museum corridor around General Luna Street.",
        category = "Plaza / civic space",
        historicalPeriod = "Spanish Colonial Civic Core",
        locationText = "Cabildo St. area, Intramuros, Manila",
        openingHours = "Open-air public space",
        entranceFee = "Free public access",
        contactDetails = "No separate ticketing; this is part of the central open public zone of Intramuros.",
        visitorTips = "Use Plaza Roma as your navigation hub. It is the cleanest point for switching between the cathedral, civic buildings, and the museum quarter.",
        bestTimeToVisit = "Early morning or late afternoon",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-plaza-roma.jpg",
        sortOrder = 813
    ),
    SeedAttraction(
        slug = "ayuntamiento-de-manila",
        name = "Ayuntamiento de Manila",
        alternateNames = listOf("Cabildo", "Ayuntamiento Building"),
        shortDescription = "Reconstructed civic building once nicknamed the Marble Palace and now one of Intramuros' key colonial-government landmarks.",
        fullDescription = "Ayuntamiento de Manila served as the city hall and later housed major government functions in the colonial capital. The present reconstructed building remains a strong civic landmark on Plaza Roma and helps explain how government, church, and military power were arranged inside Intramuros.",
        category = "Government / heritage building",
        historicalPeriod = "Spanish Colonial / Modern Reconstruction",
        locationText = "Cabildo St. corner Andres Soriano Ave., Intramuros, Manila",
        openingHours = "Exterior viewing only unless you have official business or pre-arranged access",
        entranceFee = "Free exterior viewing",
        contactDetails = "Best treated as an exterior stop while walking the Plaza Roma civic core.",
        visitorTips = "Pair this with Plaza Roma and Manila Cathedral. It works best as a facade-and-context stop rather than a long visit.",
        bestTimeToVisit = "Morning for a lighter facade view from Plaza Roma",
        imageUrl = "https://filmphilippines.com/sites/default/files/Ayuntamiento-image-1.png",
        sortOrder = 814
    ),
    SeedAttraction(
        slug = "puerta-de-santa-lucia",
        name = "Puerta de Santa Lucia",
        shortDescription = "Historic gate linking the southern wall circuit, Baluarte de San Diego, and nearby garden spaces.",
        fullDescription = "Puerta de Santa Lucia was one of the original entrances to Intramuros and remains a useful marker on the southern fortification route. It is a practical walking-tour pin when moving between Baluarte de San Diego, Puerta Real Gardens, and the wall-side streets.",
        category = "Gate / fortification",
        historicalPeriod = "Spanish Colonial",
        locationText = "Santa Lucia St., Intramuros, Manila",
        openingHours = "Open-air heritage structure; best viewed in daylight",
        entranceFee = "Free outdoor viewing",
        contactDetails = "No separate ticketing; include it as part of a fortifications walk.",
        visitorTips = "Use this as a photo stop and directional marker rather than a standalone destination.",
        bestTimeToVisit = "Late afternoon on a fortifications route",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2017/07/2.jpg",
        sortOrder = 815
    ),
    SeedAttraction(
        slug = "puerta-del-parian",
        name = "Puerta del Parian",
        shortDescription = "Historic gate associated with the old Parian district and Chinese commercial history around Manila.",
        fullDescription = "Puerta del Parian marks one of the historic gate zones connected with the old Parian and the long commercial relationship between Intramuros and the Chinese community outside the walls. It is a useful stop for visitors tracing trade history and the northeastern edge of the walled city.",
        category = "Gate / fortification",
        historicalPeriod = "Spanish Colonial",
        locationText = "Muralla St., Intramuros, Manila",
        openingHours = "Open-air heritage structure; best viewed in daylight",
        entranceFee = "Free outdoor viewing",
        contactDetails = "No separate ticketing; pair it with Fort Santiago or Tiendas del Parian on a longer walk.",
        visitorTips = "Best combined with Fort Santiago or a food stop near Tiendas del Parian rather than visited on its own.",
        bestTimeToVisit = "Daylight hours on a north-side wall route",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2017/07/3.jpg",
        sortOrder = 816
    ),
    SeedAttraction(
        slug = "plazuela-de-santa-isabel",
        name = "Plazuela de Santa Isabel",
        shortDescription = "Quieter memorial plaza and open space for visitors who want a less crowded pause inside Intramuros.",
        fullDescription = "Plazuela de Santa Isabel is a smaller memorial-oriented plaza that works as a calm break between the larger museums, gates, and church landmarks of Intramuros. It is less of a headline attraction and more of a useful breathing space on foot.",
        category = "Memorial / open space",
        historicalPeriod = "Spanish Colonial / Civic Memorial",
        locationText = "Near General Luna St., Intramuros, Manila",
        openingHours = "Open-air public space",
        entranceFee = "Free public access",
        contactDetails = "No separate ticketing; best included as part of a walking route.",
        visitorTips = "Use this as a pause point, orientation stop, or quieter photo break rather than a destination that needs long dwell time.",
        bestTimeToVisit = "Morning or late afternoon",
        imageUrl = "https://www.phtourguide.com/wp-content/uploads/2010/10/Plazuela-de-Santa-Isabel.jpg",
        sortOrder = 817
    ),
    SeedAttraction(
        slug = "intendencia-building",
        name = "Intendencia Building",
        alternateNames = listOf("Aduana Building", "Aduana de Manila"),
        shortDescription = "Ruined customs and government complex tied to the old administrative and river-facing edge of Intramuros.",
        fullDescription = "The Intendencia Building, also known as the Aduana Building, once housed customs and other government offices on the river-facing side of old Manila. Today it is best understood as a historical exterior stop for visitors interested in the civilian and administrative geography of Intramuros beyond its churches and fort walls.",
        category = "Heritage building / ruins",
        historicalPeriod = "Spanish Colonial / American Era",
        locationText = "Near Plaza Mexico and Andres Soriano Ave., Intramuros, Manila",
        openingHours = "Exterior viewing only; access to the structure itself is restricted",
        entranceFee = "Free exterior viewing",
        contactDetails = "Treat this as an exterior stop near the river edge and old government quarter.",
        visitorTips = "Best paired with Plaza Mexico, Jones Bridge approaches, or a longer heritage walk that includes the civic side of Intramuros.",
        bestTimeToVisit = "Late afternoon, when the river-facing side is easier to walk and photograph",
        imageUrl = "https://gttp.images.tshiftcdn.com/495307/x/0/aduana-building?crop=1.91%3A1&fit=crop&width=1200",
        sortOrder = 818
    )
)

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
        name = "Jollibee",
        shortDescription = "Popular fast-food stop for burgers, Chickenjoy, and rice meals.",
        fullDescription = "Jollibee Aduana is a practical quick-meal option for visitors who want familiar Filipino fast food before or after exploring Intramuros landmarks.",
        diningType = "Fast food",
        cuisine = "Filipino fast food",
        locationText = "Aduana St. corner Muralla St., Brgy. 656, Intramuros, Manila",
        openingHours = "Mon-Thu 6:00 AM - 9:45 PM; Fri-Sun 6:00 AM - 10:45 PM",
        priceRange = "PHP 99-350",
        contactDetails = "09296293916",
        visitorNotes = "Useful for quick breakfast, lunch, or takeout near the Muralla side of Intramuros.",
        imageUrl = "https://www.jollibee.com.ph/sites/g/files/iojlck111/files/styles/webp_optimized/public/2025-07/footer-logo.png.webp?itok=o4UsIY1c",
        sortOrder = 901
    ),
    SeedDiningPlace(
        slug = "mcdonalds-muralla",
        name = "McDonald's",
        shortDescription = "Quick-service branch for burgers, breakfast meals, and coffee.",
        fullDescription = "McDonald's Muralla gives travelers a familiar fast-food option near key university and heritage areas inside Intramuros.",
        diningType = "Fast food",
        cuisine = "Burgers, fried chicken, and coffee",
        locationText = "Muralla St., Intramuros, Manila",
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
        name = "Chowking",
        shortDescription = "Fast-food chain for Chinese-style rice meals, noodles, and dim sum.",
        fullDescription = "Chowking Intramuros is a convenient option for visitors who want quick rice bowls, noodle soups, and snack-sized Chinese fast-food items.",
        diningType = "Fast food / Chinese QSR",
        cuisine = "Chinese QSR",
        locationText = "FEMII Bldg., A. Soriano Ave., Intramuros, Manila",
        openingHours = "Daily 6:15 AM - 9:30 PM",
        priceRange = "PHP 99-320",
        contactDetails = "Chowking delivery channels and in-store ordering",
        visitorNotes = "A practical stop for quick merienda, halo-halo, or rice meals while walking around the walled city.",
        latitude = 14.59304,
        longitude = 120.97331,
        imageUrl = "https://www.chowking.ph/wp-content/uploads/2023/08/logo.svg",
        sortOrder = 904
    ),
    SeedDiningPlace(
        slug = "greenwich-aduana",
        name = "Greenwich",
        shortDescription = "Casual fast-food branch for pizza, pasta, and chicken meals.",
        fullDescription = "Greenwich Aduana gives visitors an accessible fast-food choice for pizza, pasta, and group-friendly meals close to Intramuros gates and schools.",
        diningType = "Fast food / Pizza",
        cuisine = "Pizza and pasta",
        locationText = "G/F FEMII Bldg., A. Soriano Ave., Intramuros, Manila",
        openingHours = "Daily; verify current branch hours before visiting",
        priceRange = "PHP 129-499",
        contactDetails = "Greenwich delivery channels and in-store ordering",
        visitorNotes = "Useful for small groups looking for shareable meals without leaving the Intramuros area.",
        latitude = 14.59304,
        longitude = 120.97331,
        imageUrl = "https://www.greenwich.com.ph/images/banner/me-time-snack-active/1024.webp?version=1.38.8.1776238062348",
        sortOrder = 905
    ),
    SeedDiningPlace(
        slug = "kfc-intramuros",
        name = "KFC",
        shortDescription = "Fried chicken chain for buckets, rice meals, burgers, and quick combos.",
        fullDescription = "KFC Intramuros is a reliable fast-food stop near Plaza Roma and Manila Cathedral, making it a convenient option for visitors who want a familiar chicken meal without leaving the district.",
        diningType = "Fast food",
        cuisine = "Fried chicken",
        locationText = "Unit A & B, G/F Shipping Center Condo Bldg., Soriano Ave., Intramuros, Manila",
        openingHours = "Daily 7:00 AM - 9:30 PM",
        priceRange = "PHP 120-499",
        contactDetails = "+639176566929",
        visitorNotes = "Useful for quick group meals and takeout near the central heritage stops.",
        latitude = 14.589791,
        longitude = 120.975351,
        imageUrl = "https://cdn.tictuk.com/04d917c0-7bcd-6ced-acad-3e4db2bcbfd5/assets/logoDesktopHeader.svg",
        sortOrder = 903
    ),
    SeedDiningPlace(
        slug = "potato-corner-tiendas-del-parian",
        name = "Potato Corner",
        shortDescription = "Snack chain kiosk for flavored fries and quick add-on bites.",
        fullDescription = "Potato Corner is a convenient snack stop inside Intramuros for flavored fries and quick takeaway orders while walking between nearby heritage sites.",
        diningType = "Snack / QSR",
        cuisine = "Flavored fries and snacks",
        locationText = "Tiendas del Parian / Intramuros Wall area, Muralla St., Intramuros, Manila",
        openingHours = "Verify current kiosk hours before visiting",
        priceRange = "PHP 69-249",
        contactDetails = "In-store ordering",
        visitorNotes = "Useful for a quick snack stop near the wall area and Tiendas del Parian stalls.",
        imageUrl = "https://potatocorner.com/wp-content/uploads/2023/07/Logo-Stacked-Mascot.png",
        sortOrder = 906
    ),
    SeedDiningPlace(
        slug = "ate-ricas-bacsilog-intramuros",
        name = "Ate Rica's Bacsilog",
        shortDescription = "Popular silog chain for bacon rice meals, tapsilog-style plates, and quick comfort food.",
        fullDescription = "Ate Rica's Bacsilog Intramuros is a practical stop for affordable silog meals and rice bowls, especially for visitors who want a heavier snack or casual meal near Muralla.",
        diningType = "Chain silog / rice meals",
        cuisine = "Silog and rice meals",
        locationText = "Stall 8 Tiendas del Parian, Muralla St. corner San Francisco St., Intramuros, Manila",
        openingHours = "Daily 7:30 AM - 7:30 PM",
        priceRange = "PHP 99-249",
        contactDetails = "0917-678-2648",
        visitorNotes = "Useful for budget-friendly rice meals near Tiendas del Parian and the Muralla side of Intramuros.",
        imageUrl = "https://static.wixstatic.com/media/52e0bf_07f854d8165a4936b5794c55e3e4e1f6~mv2_d_1500_1500_s_2.jpg/v1/fit/w_2500,h_1330,al_c/52e0bf_07f854d8165a4936b5794c55e3e4e1f6~mv2_d_1500_1500_s_2.jpg",
        sortOrder = 907
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
        sortOrder = 910
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
        sortOrder = 911
    ),
    SeedDiningPlace(
        slug = "maxs-intramuros",
        name = "Max's Restaurant",
        shortDescription = "Established Filipino restaurant chain known for fried chicken and classic local dishes.",
        fullDescription = "Max's Restaurant Intramuros is a practical sit-down chain option for visitors who want familiar Filipino comfort food in a fuller restaurant setting after walking the heritage district.",
        diningType = "Chain casual dining",
        cuisine = "Filipino comfort food",
        locationText = "Commerce / Bank of Commerce Bldg., 409 A. Soriano Ave., Intramuros, Manila",
        openingHours = "Daily 9:00 AM - 7:00 PM",
        priceRange = "PHP 250-900",
        contactDetails = "(02) 8527-0532",
        visitorNotes = "Useful when you want a longer meal stop or need a group-friendly Filipino menu inside Intramuros.",
        imageUrl = "https://maxschicken.com/img/logo_04.png",
        sortOrder = 908
    ),
    SeedDiningPlace(
        slug = "teriyaki-boy-intramuros",
        name = "Teriyaki Boy",
        shortDescription = "Japanese chain restaurant for rice bowls, teppanyaki plates, ramen, and sushi-style sides.",
        fullDescription = "Teriyaki Boy Intramuros gives visitors a casual Japanese chain option inside the same Bank of Commerce area as Max's, making it useful for small groups with different meal preferences.",
        diningType = "Chain Japanese resto",
        cuisine = "Japanese casual dining",
        locationText = "Inside Max's Intramuros, Bank of Commerce Bldg., 409 A. Soriano Ave., Intramuros, Manila",
        openingHours = "Mon-Fri 8:00 AM - 8:30 PM; Sat-Sun 9:00 AM - 7:30 PM",
        priceRange = "PHP 199-599",
        contactDetails = "foodpanda and in-store ordering",
        visitorNotes = "Useful for rice bowls, teppanyaki, and Japanese comfort food within the same cluster as Max's.",
        imageUrl = "https://mgi-deliveryportal.s3.amazonaws.com/assets/TeriyakiBoy.png",
        sortOrder = 909
    )
)

private val baselineRoutes = listOf(
    SeedTourRoute(
        slug = "classic-intramuros-heritage-walk",
        name = "Classic Intramuros Heritage Walk",
        shortDescription = "Best for first-time visitors who want the strongest heritage landmarks in one practical loop.",
        fullDescription = "A first-time-visitor loop that starts at Fort Santiago, continues through Museo ni Rizal and the central civic core at Plaza Roma, then moves to Manila Cathedral, Ayuntamiento, Casa Manila, San Agustin Church, San Agustin Museum, and Bahay Tsinoy.",
        routeType = "Heritage landmark loop",
        startingPoint = "Fort Santiago",
        estimatedDuration = "2 to 2.5 hours",
        travelTips = "Start early if you want Fort Santiago and Museo ni Rizal before the central core gets busier. This route works best at a relaxed pace with time for at least one museum interior.",
        distanceText = "Approx. 2 km plus museum interiors",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Fort+Santiago,+Intramuros,+Manila&destination=Bahay+Tsinoy,+Intramuros,+Manila&travelmode=walking&waypoints=Museo+ni+Rizal,+Intramuros,+Manila%7CPlaza+Roma,+Intramuros,+Manila%7CManila+Cathedral,+Intramuros,+Manila%7CAyuntamiento+de+Manila,+Intramuros,+Manila%7CCasa+Manila,+Intramuros,+Manila%7CSan+Agustin+Church,+Intramuros,+Manila%7CSan+Agustin+Museum,+Intramuros,+Manila",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/20-1.png",
        isFeatured = true,
        sortOrder = 801
    ),
    SeedTourRoute(
        slug = "quick-intramuros-highlights",
        name = "Quick Intramuros Highlights",
        shortDescription = "Compact central route for visitors who want the most recognizable landmarks in limited time.",
        fullDescription = "A short core route covering Manila Cathedral, Plaza Roma, Ayuntamiento de Manila, Casa Manila and Plaza San Luis, San Agustin Church, and San Agustin Museum without pushing visitors to the far ends of the district.",
        routeType = "Compact central route",
        startingPoint = "Manila Cathedral",
        estimatedDuration = "45 minutes to 1.5 hours",
        travelTips = "Use this when your time is tight. It gives you the strongest central facades and one optional museum without requiring the longer Fort Santiago segment.",
        distanceText = "Approx. 1 km through the central core",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Manila+Cathedral,+Intramuros,+Manila&destination=San+Agustin+Museum,+Intramuros,+Manila&travelmode=walking&waypoints=Plaza+Roma,+Intramuros,+Manila%7CAyuntamiento+de+Manila,+Intramuros,+Manila%7CCasa+Manila,+Intramuros,+Manila%7CSan+Agustin+Church,+Intramuros,+Manila",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-manila-cathedral.jpg",
        isFeatured = true,
        sortOrder = 802
    ),
    SeedTourRoute(
        slug = "fort-santiago-to-casa-manila-walk",
        name = "Fort Santiago to Casa Manila Walk",
        shortDescription = "Straightforward short walk linking Fort Santiago to Casa Manila through the civic center.",
        fullDescription = "A practical directional route from Fort Santiago through Plaza Roma and Manila Cathedral to Casa Manila. This is useful when visitors want one clear museum transfer rather than a longer loop.",
        routeType = "Short directional route",
        startingPoint = "Fort Santiago",
        estimatedDuration = "Around 11 minutes walking, excluding stops",
        travelTips = "Use this when you want a clean point-to-point transfer from the fort area to the core museum zone without adding the longer wall or gate circuit.",
        distanceText = "Around 940 meters",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Fort+Santiago,+Intramuros,+Manila&destination=Casa+Manila,+Intramuros,+Manila&travelmode=walking&waypoints=Plaza+Roma,+Intramuros,+Manila%7CManila+Cathedral,+Intramuros,+Manila",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/21-1.png",
        isFeatured = true,
        sortOrder = 803
    ),
    SeedTourRoute(
        slug = "walls-and-fortifications-trail",
        name = "Walls and Fortifications Trail",
        shortDescription = "Outdoor route for visitors focused on bastions, gates, walls, and the military side of Intramuros.",
        fullDescription = "A fortifications-first route running from Baluarte de San Diego to Puerta Real Gardens, Puerta de Santa Lucia, Fort Santiago, Puerta del Parian, and the Tiendas del Parian side of Intramuros. It is built for visitors who care more about defenses, walls, and ruins than museum interiors.",
        routeType = "Military-history route",
        startingPoint = "Baluarte de San Diego",
        estimatedDuration = "2 to 3 hours",
        travelTips = "Best done outside the hottest part of the day. Bring water, expect more outdoor exposure than the central museum routes, and use gates as navigation anchors.",
        distanceText = "Approx. 2.5 km outdoor circuit",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Baluarte+de+San+Diego,+Intramuros,+Manila&destination=Tiendas+del+Parian,+Intramuros,+Manila&travelmode=walking&waypoints=Puerta+Real+Gardens,+Intramuros,+Manila%7CPuerta+de+Santa+Lucia,+Intramuros,+Manila%7CFort+Santiago,+Intramuros,+Manila%7CPuerta+del+Parian,+Intramuros,+Manila",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/22-1.png",
        isFeatured = true,
        sortOrder = 804
    ),
    SeedTourRoute(
        slug = "intramuros-museum-walk",
        name = "Intramuros Museum Walk",
        shortDescription = "Museum-heavy route for students, researchers, and visitors who want more interior interpretation than outdoor roaming.",
        fullDescription = "A museum-focused route that starts at Centro de Turismo Intramuros, continues to Museo de Intramuros, San Agustin Museum, Casa Manila Museum, Bahay Tsinoy, and ends at Museo ni Rizal inside Fort Santiago.",
        routeType = "Indoor-heavy museum route",
        startingPoint = "Centro de Turismo Intramuros",
        estimatedDuration = "3 to 4 hours",
        travelTips = "Use a Tuesday-to-Sunday window if you want the full route in one day. Monday closures and changing museum schedules matter more on this route than on the others.",
        distanceText = "Approx. 1.8 km plus museum interiors",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Centro+de+Turismo+Intramuros,+Intramuros,+Manila&destination=Museo+ni+Rizal,+Intramuros,+Manila&travelmode=walking&waypoints=Museo+de+Intramuros,+Intramuros,+Manila%7CSan+Agustin+Museum,+Intramuros,+Manila%7CCasa+Manila,+Intramuros,+Manila%7CBahay+Tsinoy,+Intramuros,+Manila",
        imageUrl = "https://intramuros.gov.ph/wp-content/uploads/2025/05/23-1.png",
        isFeatured = true,
        sortOrder = 805
    ),
    SeedTourRoute(
        slug = "best-photo-spots-walk",
        name = "Best Photo Spots Walk",
        shortDescription = "Scenic route for visitors prioritizing facades, courtyards, ruins, and the strongest camera-friendly stops.",
        fullDescription = "A photo-first route that starts at Fort Santiago Gate and moves through the fort courtyard, Plaza Roma, Manila Cathedral, Casa Manila and Plaza San Luis, San Agustin Church, Baluarte de San Diego, and Puerta Real Gardens.",
        routeType = "Scenic / photo-first",
        startingPoint = "Fort Santiago Gate",
        estimatedDuration = "1.5 to 2.5 hours",
        travelTips = "Go earlier or later in the day for softer light on stone facades and fewer harsh shadows. This route is strongest for casual tourists, students, and content creators.",
        distanceText = "Approx. 2 km with frequent photo stops",
        mapLink = "https://www.google.com/maps/dir/?api=1&origin=Fort+Santiago,+Intramuros,+Manila&destination=Puerta+Real+Gardens,+Intramuros,+Manila&travelmode=walking&waypoints=Plaza+Roma,+Intramuros,+Manila%7CManila+Cathedral,+Intramuros,+Manila%7CCasa+Manila,+Intramuros,+Manila%7CSan+Agustin+Church,+Intramuros,+Manila%7CBaluarte+de+San+Diego,+Intramuros,+Manila",
        imageUrl = "https://traveltomanila.net/wp-content/uploads/2026/04/intramuros-manila-baluarte-pan.jpg",
        isFeatured = true,
        sortOrder = 806
    )
)

private fun PreparedStatement.bindAttraction(item: SeedAttraction) {
    setString(1, item.slug)
    setString(2, item.name)
    setString(3, jsonStringArray(*item.alternateNames.toTypedArray()))
    setString(4, item.shortDescription)
    setString(5, item.fullDescription)
    setString(6, item.category)
    setString(7, item.historicalPeriod)
    setString(8, item.locationText)
    setString(9, item.openingHours)
    setString(10, item.entranceFee)
    setString(11, item.contactDetails)
    setString(12, item.visitorTips)
    setString(13, item.bestTimeToVisit)
    setDouble(14, item.latitude)
    setDouble(15, item.longitude)
    setString(16, item.imageUrl)
    setString(17, jsonStringArray(item.imageUrl))
    setBoolean(18, item.isFeatured)
    setString(19, "open")
    setInt(20, item.sortOrder)
}

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

private fun PreparedStatement.bindTourRoute(item: SeedTourRoute) {
    setString(1, item.slug)
    setString(2, item.name)
    setString(3, item.shortDescription)
    setString(4, item.fullDescription)
    setString(5, item.routeType)
    setString(6, item.startingPoint)
    setString(7, item.estimatedDuration)
    setString(8, item.travelTips)
    setString(9, item.distanceText)
    setString(10, item.mapLink)
    setString(11, item.imageUrl)
    setString(12, jsonStringArray(item.imageUrl))
    setBoolean(13, item.isFeatured)
    setString(14, "open")
    setInt(15, item.sortOrder)
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

private data class SeedAttraction(
    val slug: String,
    val name: String,
    val alternateNames: List<String> = emptyList(),
    val shortDescription: String,
    val fullDescription: String,
    val category: String,
    val historicalPeriod: String,
    val locationText: String,
    val openingHours: String = "",
    val entranceFee: String = "",
    val contactDetails: String = "",
    val visitorTips: String = "",
    val bestTimeToVisit: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val isFeatured: Boolean = false,
    val sortOrder: Int
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

private data class SeedTourRoute(
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val routeType: String,
    val startingPoint: String,
    val estimatedDuration: String,
    val travelTips: String,
    val distanceText: String = "",
    val mapLink: String = "",
    val imageUrl: String = "",
    val isFeatured: Boolean = false,
    val sortOrder: Int
)
