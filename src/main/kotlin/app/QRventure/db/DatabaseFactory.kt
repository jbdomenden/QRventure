package app.QRventure.db

import io.ktor.server.config.*
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.util.Base64

object DatabaseFactory {
    fun connect(config: ApplicationConfig): Connection {
        val url = config.property("postgres.url").getString()
        if (url.startsWith("jdbc:h2:")) Class.forName("org.h2.Driver") else Class.forName("org.postgresql.Driver")
        val user = config.property("postgres.user").getString()
        val password = config.property("postgres.password").getString()
        return DriverManager.getConnection(url, user, password)
    }

    fun initializeSchema(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS admins (
                  id SERIAL PRIMARY KEY,
                  email VARCHAR(180) UNIQUE NOT NULL,
                  password_hash VARCHAR(255) NOT NULL,
                  role VARCHAR(40) NOT NULL DEFAULT 'super_admin',
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS attractions (
                  id SERIAL PRIMARY KEY,
                  slug VARCHAR(120) UNIQUE NOT NULL,
                  name VARCHAR(180) NOT NULL,
                  short_description TEXT NOT NULL,
                  full_description TEXT NOT NULL,
                  category VARCHAR(80) NOT NULL,
                  location_text VARCHAR(220) NOT NULL,
                  opening_hours VARCHAR(150) NOT NULL,
                  entrance_fee VARCHAR(120) NOT NULL,
                  contact_details VARCHAR(180) NOT NULL,
                  latitude DOUBLE PRECISION NOT NULL,
                  longitude DOUBLE PRECISION NOT NULL,
                  image_path VARCHAR(240) NOT NULL,
                  status VARCHAR(30) NOT NULL,
                  is_featured BOOLEAN NOT NULL DEFAULT FALSE
                );
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS dining_places (
                  id SERIAL PRIMARY KEY,
                  slug VARCHAR(120) UNIQUE NOT NULL,
                  name VARCHAR(180) NOT NULL,
                  description TEXT NOT NULL,
                  cuisine_or_type VARCHAR(100) NOT NULL,
                  location_text VARCHAR(220) NOT NULL,
                  opening_hours VARCHAR(150) NOT NULL,
                  price_range VARCHAR(80) NOT NULL,
                  contact_details VARCHAR(180) NOT NULL,
                  latitude DOUBLE PRECISION NOT NULL,
                  longitude DOUBLE PRECISION NOT NULL,
                  image_path VARCHAR(240) NOT NULL,
                  is_featured BOOLEAN NOT NULL DEFAULT FALSE
                );
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS local_services (
                  id SERIAL PRIMARY KEY,
                  slug VARCHAR(120) UNIQUE NOT NULL,
                  name VARCHAR(180) NOT NULL,
                  description TEXT NOT NULL,
                  service_type VARCHAR(100) NOT NULL,
                  location_text VARCHAR(220) NOT NULL,
                  operating_hours VARCHAR(150) NOT NULL,
                  contact_details VARCHAR(180) NOT NULL,
                  latitude DOUBLE PRECISION NOT NULL,
                  longitude DOUBLE PRECISION NOT NULL,
                  nearby_landmark_notes VARCHAR(240) NOT NULL,
                  travel_tips TEXT NOT NULL,
                  image_path VARCHAR(240) NOT NULL,
                  is_featured BOOLEAN NOT NULL DEFAULT FALSE
                );
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS tour_routes (
                  id SERIAL PRIMARY KEY,
                  slug VARCHAR(120) UNIQUE NOT NULL,
                  name VARCHAR(180) NOT NULL,
                  duration_text VARCHAR(80) NOT NULL,
                  start_point VARCHAR(220) NOT NULL,
                  route_description TEXT NOT NULL,
                  distance_km DOUBLE PRECISION NOT NULL,
                  highlights TEXT NOT NULL,
                  is_featured BOOLEAN NOT NULL DEFAULT FALSE
                );
                """.trimIndent()
            )
        }
    }

    fun seedData(connection: Connection) {
        seedAdmin(connection)
        seedAttractions(connection)
        seedDining(connection)
        seedServices(connection)
        seedTourRoutes(connection)
    }

    private fun seedAdmin(connection: Connection) {
        if (tableHasData(connection, "admins")) return

        connection.prepareStatement(
            "INSERT INTO admins (email, password_hash, role) VALUES (?, ?, ?)"
        ).use { ps ->
            ps.setString(1, "admin@qrventure.local")
            ps.setString(2, hashPassword("Admin123!"))
            ps.setString(3, "super_admin")
            ps.executeUpdate()
        }
    }

    private fun seedAttractions(connection: Connection) {
        if (tableHasData(connection, "attractions")) return

        connection.prepareStatement(
            """
            INSERT INTO attractions
            (slug, name, short_description, full_description, category, location_text, opening_hours, entrance_fee, contact_details, latitude, longitude, image_path, status, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("fort-santiago", "Fort Santiago", "Historic citadel and Rizal memorial destination.", "Fort Santiago is one of Intramuros' most visited heritage landmarks, known for its stone gates, riverfront promenades, and galleries dedicated to Dr. Jose Rizal's final days.", "Historic Landmark", "Santa Clara Street, Intramuros", "8:00 AM - 11:00 PM", "PHP 75 regular / PHP 50 student", "(02) 8527 2961", 14.5953, 120.9701, "/qrventure/images/fort-santiago.svg", "open", true),
                listOf("san-agustin-church", "San Agustin Church", "UNESCO-listed baroque church and museum.", "Completed in 1607, San Agustin Church features ornate interiors, centuries-old religious artifacts, and a museum that narrates Manila's colonial and wartime history.", "Religious Heritage", "General Luna Street, Intramuros", "8:00 AM - 5:00 PM", "Church free / Museum PHP 200", "(02) 8527 4064", 14.5898, 120.9747, "/qrventure/images/san-agustin.svg", "open", true),
                listOf("casa-manila", "Casa Manila", "Recreation of a Spanish colonial bahay na bato.", "Casa Manila offers curated period rooms, decorative arts, and daily-life exhibits that help visitors experience elite Manila household culture during the Spanish period.", "Museum", "Plaza San Luis Complex, Intramuros", "9:00 AM - 6:00 PM", "PHP 75", "(02) 8527 4084", 14.5892, 120.9749, "/qrventure/images/baluarte.svg", "open", true),
                listOf("baluarte-de-san-diego", "Baluarte de San Diego", "Gardened bastion with panoramic wall views.", "Baluarte de San Diego combines restored stonework, a circular bastion, and open gardens ideal for short history walks and photo stops.", "Historic Fortification", "Sta. Lucia Street, Intramuros", "8:00 AM - 5:00 PM", "PHP 75", "(02) 8527 4084", 14.5888, 120.9753, "/qrventure/images/baluarte.svg", "open", false),
                listOf("manila-cathedral", "Manila Cathedral", "Neo-romanesque cathedral at the heart of Plaza Roma.", "The Minor Basilica and Metropolitan Cathedral of the Immaculate Conception has been rebuilt multiple times and remains a spiritual and architectural anchor in Intramuros.", "Religious Heritage", "Cabildo Street, Intramuros", "7:30 AM - 5:30 PM", "Free admission", "(02) 8527 1796", 14.5911, 120.9736, "/qrventure/images/san-agustin.svg", "open", true),
                listOf("plaza-roma", "Plaza Roma", "Historic civic square surrounded by key landmarks.", "Plaza Roma is the ceremonial center of Intramuros, located between Manila Cathedral and Palacio del Gobernador, and serves as a convenient orientation point.", "Plaza", "Cabildo Street, Intramuros", "Open 24 hours", "Free admission", "Intramuros Administration", 14.5906, 120.9734, "/qrventure/images/fort-santiago.svg", "open", false),
                listOf("puerta-del-parian", "Puerta del Parian", "Restored gate that once linked trade routes.", "Puerta del Parian is one of the traditional entrances into the walled city, highlighting old access control systems and urban defense planning.", "City Gate", "Muralla Street, Intramuros", "Open 24 hours", "Free admission", "Intramuros Administration", 14.5923, 120.9718, "/qrventure/images/fort-santiago.svg", "open", false),
                listOf("puerta-real-gardens", "Puerta Real Gardens", "Landscaped leisure space beside historic walls.", "Puerta Real Gardens offers shaded seating, open lawns, and wall views, making it a rest stop for walking tours around southern Intramuros.", "Park", "Real Street, Intramuros", "6:00 AM - 10:00 PM", "Free admission", "Intramuros Administration", 14.5868, 120.9758, "/qrventure/images/baluarte.svg", "open", false)
            ).forEach { row ->
                ps.setString(1, row[0] as String)
                ps.setString(2, row[1] as String)
                ps.setString(3, row[2] as String)
                ps.setString(4, row[3] as String)
                ps.setString(5, row[4] as String)
                ps.setString(6, row[5] as String)
                ps.setString(7, row[6] as String)
                ps.setString(8, row[7] as String)
                ps.setString(9, row[8] as String)
                ps.setDouble(10, row[9] as Double)
                ps.setDouble(11, row[10] as Double)
                ps.setString(12, row[11] as String)
                ps.setString(13, row[12] as String)
                ps.setBoolean(14, row[13] as Boolean)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun seedDining(connection: Connection) {
        if (tableHasData(connection, "dining_places")) return

        connection.prepareStatement(
            """
            INSERT INTO dining_places
            (slug, name, description, cuisine_or_type, location_text, opening_hours, price_range, contact_details, latitude, longitude, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("barbara-s-heritage", "Barbara's Heritage Restaurant", "Heritage Filipino dining with cultural dinner performances.", "Filipino Restaurant", "General Luna Street, Intramuros", "10:00 AM - 9:00 PM", "PHP 500-1,200", "(02) 8527 3893", 14.5894, 120.9750, "/qrventure/images/dining-heritage.svg", true),
                listOf("cafe-intramuros", "Cafe Intramuros", "Relaxed heritage cafe serving local comfort dishes and coffee.", "Heritage Cafe", "Plaza San Luis, Intramuros", "8:00 AM - 8:00 PM", "PHP 200-500", "(02) 8536 1120", 14.5899, 120.9740, "/qrventure/images/dining-cafe.svg", true),
                listOf("carta-filipina", "Carta Filipina", "Modern Filipino plates for quick lunches near major sites.", "Filipino Restaurant", "Anda Street, Intramuros", "10:30 AM - 9:30 PM", "PHP 300-800", "0917 100 2211", 14.5949, 120.9720, "/qrventure/images/dining-tapas.svg", false),
                listOf("muralla-coffee-stop", "Muralla Coffee Stop", "Grab-and-go coffee and pastries ideal for walkers.", "Coffee Stop", "Muralla Street, Intramuros", "7:00 AM - 7:00 PM", "PHP 120-300", "0920 889 2213", 14.5921, 120.9728, "/qrventure/images/dining-cafe.svg", false)
            ).forEach { row ->
                ps.setString(1, row[0] as String)
                ps.setString(2, row[1] as String)
                ps.setString(3, row[2] as String)
                ps.setString(4, row[3] as String)
                ps.setString(5, row[4] as String)
                ps.setString(6, row[5] as String)
                ps.setString(7, row[6] as String)
                ps.setString(8, row[7] as String)
                ps.setDouble(9, row[8] as Double)
                ps.setDouble(10, row[9] as Double)
                ps.setString(11, row[10] as String)
                ps.setBoolean(12, row[11] as Boolean)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun seedServices(connection: Connection) {
        if (tableHasData(connection, "local_services")) return

        connection.prepareStatement(
            """
            INSERT INTO local_services
            (slug, name, description, service_type, location_text, operating_hours, contact_details, latitude, longitude, nearby_landmark_notes, travel_tips, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("intra-restroom-plaza", "Plaza Roma Public Restroom", "Maintained comfort room for tourists near the cathedral area.", "Restroom", "Near Plaza Roma, Intramuros", "6:00 AM - 9:00 PM", "Onsite staff", 14.5904, 120.9732, "Beside Plaza Roma loading zone", "Carry small tissue and hand sanitizer for convenience.", "/qrventure/images/service-info.svg", true),
                listOf("plaza-moriones-atm", "Plaza Moriones ATM Cluster", "Multiple ATM kiosks for cash withdrawal near major stops.", "ATM", "Plaza Moriones, Intramuros", "24/7", "Bank hotlines onsite", 14.5957, 120.9698, "Across open plaza gardens", "Withdraw small denominations for pedicab and entrance fees.", "/qrventure/images/service-atm.svg", true),
                listOf("real-street-parking", "Real Street Visitor Parking", "Pay parking area for private vehicles and tour vans.", "Parking", "Real Street, Intramuros", "6:00 AM - 11:00 PM", "IA Parking Desk", 14.5872, 120.9759, "Near Puerta Real Gardens", "Arrive early on weekends to secure slots.", "/qrventure/images/service-info.svg", false),
                listOf("intra-police-assist", "Intramuros Police Assistance Desk", "Tourist police help desk for safety concerns and lost-and-found.", "Police", "Anda Circle, Intramuros", "24/7", "PNP Hotline 911", 14.5942, 120.9723, "Near transport drop-off bays", "Report incidents immediately and keep ID ready.", "/qrventure/images/service-firstaid.svg", true),
                listOf("intramuros-info-center", "Intramuros Information Center", "Visitor orientation, map assistance, and event updates.", "Info Center", "Fort Santiago Gate Area", "8:00 AM - 6:00 PM", "(02) 8527 3120", 14.5950, 120.9709, "Beside Fort Santiago ticketing zone", "Ask for current walking route advisories before starting.", "/qrventure/images/service-info.svg", true)
            ).forEach { row ->
                ps.setString(1, row[0] as String)
                ps.setString(2, row[1] as String)
                ps.setString(3, row[2] as String)
                ps.setString(4, row[3] as String)
                ps.setString(5, row[4] as String)
                ps.setString(6, row[5] as String)
                ps.setString(7, row[6] as String)
                ps.setDouble(8, row[7] as Double)
                ps.setDouble(9, row[8] as Double)
                ps.setString(10, row[9] as String)
                ps.setString(11, row[10] as String)
                ps.setString(12, row[11] as String)
                ps.setBoolean(13, row[12] as Boolean)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun seedTourRoutes(connection: Connection) {
        if (tableHasData(connection, "tour_routes")) return

        connection.prepareStatement(
            """
            INSERT INTO tour_routes
            (slug, name, duration_text, start_point, route_description, distance_km, highlights, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("historic-core", "Historic Core", "2.5 hours", "Plaza Roma", "A comprehensive first-time loop covering the civic and religious center before ending at the walls.", 2.6, "Plaza Roma, Manila Cathedral, San Agustin Church, Casa Manila, Baluarte de San Diego", true),
                listOf("fort-route", "Fort Route", "1.5 hours", "Fort Santiago Gate", "A focused walk through Fort Santiago and nearby wall sections with river viewpoints.", 1.4, "Fort Santiago, Rizal Shrine, Plaza Moriones, river promenade", true),
                listOf("church-route", "Church Route", "1 hour 45 minutes", "Manila Cathedral", "A heritage-faith route that links major worship spaces and museum areas in central Intramuros.", 1.8, "Manila Cathedral, San Agustin Church, Plaza San Luis, nearby chapels", false),
                listOf("one-hour-route", "1-Hour Route", "1 hour", "Puerta del Parian", "A short orientation route ideal for tight schedules and sunset photography.", 1.1, "Puerta del Parian, Plaza Roma, wallside viewpoints, Puerta Real Gardens", false)
            ).forEach { row ->
                ps.setString(1, row[0] as String)
                ps.setString(2, row[1] as String)
                ps.setString(3, row[2] as String)
                ps.setString(4, row[3] as String)
                ps.setString(5, row[4] as String)
                ps.setDouble(6, row[5] as Double)
                ps.setString(7, row[6] as String)
                ps.setBoolean(8, row[7] as Boolean)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun tableHasData(connection: Connection, table: String): Boolean {
        connection.createStatement().use { statement ->
            val rs = statement.executeQuery("SELECT COUNT(*) FROM $table")
            rs.next()
            return rs.getInt(1) > 0
        }
    }

    private fun hashPassword(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return "sha256:${Base64.getEncoder().encodeToString(digest)}"
    }
}
