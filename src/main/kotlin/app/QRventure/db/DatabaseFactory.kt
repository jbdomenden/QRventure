package app.QRventure.db

import io.ktor.server.config.*
import java.sql.Connection
import java.sql.DriverManager

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
        }
    }

    fun seedData(connection: Connection) {
        connection.createStatement().use { countStatement ->
            val rs = countStatement.executeQuery("SELECT COUNT(*) FROM attractions")
            rs.next()
            if (rs.getInt(1) > 0) return
        }

        connection.prepareStatement(
            """
            INSERT INTO attractions
            (slug, name, short_description, full_description, category, location_text, opening_hours, entrance_fee, contact_details, latitude, longitude, image_path, status, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("fort-santiago", "Fort Santiago", "Historic citadel with river views and Rizal museum.", "Fort Santiago is a 16th-century defensive fortress where visitors can walk stone ramparts, see preserved ruins, and explore memorial spaces dedicated to Jose Rizal.", "Historical Site", "Santa Clara St, Intramuros", "8:00 AM - 10:00 PM", "PHP 75 (regular)", "(02) 8527 2961", 14.5953, 120.9701, "/qrventure/images/fort-santiago.svg", "open", true),
                listOf("san-agustin-church", "San Agustin Church", "UNESCO church known for baroque interiors.", "San Agustin Church offers ornate trompe l'oeil ceilings, centuries-old religious artifacts, and peaceful courtyards that reflect Manila's colonial period.", "Religious Heritage", "General Luna St, Intramuros", "8:00 AM - 5:00 PM", "PHP 200 (museum)", "(02) 8527 4064", 14.5898, 120.9747, "/qrventure/images/san-agustin.svg", "open", true),
                listOf("baluarte-de-san-diego", "Baluarte de San Diego", "Gardened bastion with museum galleries.", "Baluarte de San Diego combines archaeological remains with landscaped gardens and rotating exhibits that explain Intramuros military architecture.", "Museum", "Sta. Lucia St, Intramuros", "8:00 AM - 5:00 PM", "PHP 75", "(02) 8527 4084", 14.5888, 120.9753, "/qrventure/images/baluarte.svg", "open", false)
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

        connection.prepareStatement(
            """
            INSERT INTO dining_places
            (slug, name, description, cuisine_or_type, location_text, opening_hours, price_range, contact_details, latitude, longitude, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("barbara-s-heritage", "Barbara's Heritage Restaurant", "Classic Filipino cuisine with cultural dance nights in a colonial house setting.", "Filipino Heritage Dining", "General Luna St, Intramuros", "10:00 AM - 9:00 PM", "PHP 500-1200", "(02) 8527 3893", 14.5894, 120.9750, "/qrventure/images/dining-heritage.svg", true),
                listOf("la-picara", "La Picara", "Spanish-inspired tapas and paella with alfresco seats near key landmarks.", "Spanish Tapas", "Anda St, Intramuros", "11:00 AM - 10:00 PM", "PHP 400-1000", "0917 100 2211", 14.5951, 120.9714, "/qrventure/images/dining-tapas.svg", true),
                listOf("intramuros-cafe", "Intramuros Cafe", "Casual all-day cafe for breakfast plates and coffee breaks between tours.", "Cafe", "Near Plaza San Luis, Intramuros", "8:00 AM - 8:00 PM", "PHP 200-500", "(02) 8536 1120", 14.5899, 120.9740, "/qrventure/images/dining-cafe.svg", false)
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

        connection.prepareStatement(
            """
            INSERT INTO local_services
            (slug, name, description, service_type, location_text, operating_hours, contact_details, latitude, longitude, nearby_landmark_notes, travel_tips, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            listOf(
                listOf("intramuros-visitor-center", "Intramuros Visitor Information Center", "Official tourist information desk for maps, events, and walking route suggestions.", "Information Center", "Near Fort Santiago Gate", "8:00 AM - 6:00 PM", "(02) 8527 3120", 14.5950, 120.9709, "Beside Fort Santiago ticket booth", "Ask for the current event calendar and suggested walking loop before starting your day.", "/qrventure/images/service-info.svg", true),
                listOf("plaza-moriones-atm", "Plaza Moriones ATM Cluster", "Multiple ATM kiosks for cash withdrawal near main heritage stops.", "ATM", "Plaza Moriones, Intramuros", "24/7", "Bank hotlines posted onsite", 14.5957, 120.9698, "Across the open plaza gardens", "Carry small denominations for ticket counters and pedicab fares.", "/qrventure/images/service-atm.svg", false),
                listOf("intra-first-aid", "Intramuros First-Aid and Help Desk", "Basic first-aid support and visitor safety assistance.", "First-Aid / Help Desk", "Anda Circle, Intramuros", "8:00 AM - 8:00 PM", "911 or onsite desk", 14.5941, 120.9722, "Near transport drop-off bays", "Hydrate often and seek shade during midday walking tours.", "/qrventure/images/service-firstaid.svg", true)
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
}
