package app.QRventure.db

import io.ktor.server.config.*
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {
    fun connect(config: ApplicationConfig): Connection {
        val url = config.property("postgres.url").getString()
        if (url.startsWith("jdbc:h2:")) Class.forName("org.h2.Driver") else Class.forName("org.postgresql.Driver")
        return DriverManager.getConnection(url, config.property("postgres.user").getString(), config.property("postgres.password").getString())
    }

    fun initializeSchema(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS attractions (
                    id SERIAL PRIMARY KEY,
                    slug VARCHAR(120) UNIQUE NOT NULL,
                    name VARCHAR(180) NOT NULL,
                    short_description TEXT NOT NULL,
                    full_description TEXT NOT NULL,
                    category VARCHAR(80) NOT NULL,
                    historical_period VARCHAR(120) NOT NULL DEFAULT '',
                    location_text VARCHAR(220) NOT NULL,
                    opening_hours VARCHAR(150) NOT NULL,
                    entrance_fee VARCHAR(120) NOT NULL,
                    contact_details VARCHAR(180) NOT NULL,
                    visitor_tips TEXT NOT NULL DEFAULT '',
                    best_time_to_visit VARCHAR(120) NOT NULL DEFAULT '',
                    latitude DOUBLE PRECISION NOT NULL,
                    longitude DOUBLE PRECISION NOT NULL,
                    image_path VARCHAR(240) NOT NULL,
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
                    location_text VARCHAR(220) NOT NULL,
                    opening_hours VARCHAR(150) NOT NULL,
                    price_range VARCHAR(80) NOT NULL,
                    contact_details VARCHAR(180) NOT NULL,
                    visitor_notes TEXT NOT NULL DEFAULT '',
                    latitude DOUBLE PRECISION NOT NULL,
                    longitude DOUBLE PRECISION NOT NULL,
                    image_path VARCHAR(240) NOT NULL,
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
                    service_type VARCHAR(100) NOT NULL,
                    location_text VARCHAR(220) NOT NULL,
                    hours VARCHAR(150) NOT NULL DEFAULT '',
                    contact_details VARCHAR(180) NOT NULL,
                    visitor_notes TEXT NOT NULL DEFAULT '',
                    latitude DOUBLE PRECISION NOT NULL,
                    longitude DOUBLE PRECISION NOT NULL,
                    image_path VARCHAR(240) NOT NULL,
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
                    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
                    status VARCHAR(30) NOT NULL DEFAULT 'open',
                    sort_order INT NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )

            // compatibility columns for previously seeded DBs
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS historical_period VARCHAR(120) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS visitor_tips TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS best_time_to_visit VARCHAR(120) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS dining_type VARCHAR(100) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS cuisine VARCHAR(100) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")
            if (columnExists(connection, "dining_places", "description")) {
                st.execute("UPDATE dining_places SET short_description = COALESCE(NULLIF(short_description,''), description)")
                st.execute("UPDATE dining_places SET full_description = COALESCE(NULLIF(full_description,''), description)")
            }
            if (columnExists(connection, "dining_places", "cuisine_or_type")) {
                st.execute("UPDATE dining_places SET dining_type = COALESCE(NULLIF(dining_type,''), cuisine_or_type)")
                st.execute("UPDATE dining_places SET cuisine = COALESCE(NULLIF(cuisine,''), cuisine_or_type)")
            }

            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS hours VARCHAR(150) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")
            if (columnExists(connection, "local_services", "description")) {
                st.execute("UPDATE local_services SET short_description = COALESCE(NULLIF(short_description,''), description)")
                st.execute("UPDATE local_services SET full_description = COALESCE(NULLIF(full_description,''), description)")
            }
            if (columnExists(connection, "local_services", "operating_hours")) {
                st.execute("UPDATE local_services SET hours = COALESCE(NULLIF(hours,''), operating_hours)")
            }
            if (columnExists(connection, "local_services", "travel_tips")) {
                st.execute("UPDATE local_services SET visitor_notes = COALESCE(NULLIF(visitor_notes,''), travel_tips)")
            }

            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS route_type VARCHAR(80) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS starting_point VARCHAR(220) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS estimated_duration VARCHAR(80) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS travel_tips TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS distance_text VARCHAR(80) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS map_link TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'open'")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")
            if (columnExists(connection, "tour_routes", "route_description")) {
                st.execute("UPDATE tour_routes SET full_description = COALESCE(NULLIF(full_description,''), route_description)")
                st.execute("UPDATE tour_routes SET short_description = COALESCE(NULLIF(short_description,''), SUBSTRING(route_description FROM 1 FOR 150))")
            }
            if (columnExists(connection, "tour_routes", "start_point")) {
                st.execute("UPDATE tour_routes SET starting_point = COALESCE(NULLIF(starting_point,''), start_point)")
            }
            if (columnExists(connection, "tour_routes", "duration_text")) {
                st.execute("UPDATE tour_routes SET estimated_duration = COALESCE(NULLIF(estimated_duration,''), duration_text)")
            }
            if (columnExists(connection, "tour_routes", "distance_km")) {
                st.execute("UPDATE tour_routes SET distance_text = COALESCE(NULLIF(distance_text,''), CONCAT(distance_km, ' km'))")
            }
            if (columnExists(connection, "tour_routes", "highlights")) {
                st.execute("UPDATE tour_routes SET travel_tips = COALESCE(NULLIF(travel_tips,''), highlights)")
            }

            st.execute("CREATE TABLE IF NOT EXISTS admins (id SERIAL PRIMARY KEY, email VARCHAR(180) UNIQUE NOT NULL, password_hash VARCHAR(255) NOT NULL, role VARCHAR(40) NOT NULL DEFAULT 'super_admin', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)")
        }
    }

    fun seedData(connection: Connection) {
        if (!tableHasData(connection, "admins")) {
            connection.createStatement().executeUpdate("INSERT INTO admins (email, password_hash, role) VALUES ('admin@qrventure.local', 'seed-managed', 'super_admin')")
        }
        if (!tableHasData(connection, "attractions")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO attractions (slug,name,short_description,full_description,category,historical_period,location_text,opening_hours,entrance_fee,contact_details,visitor_tips,best_time_to_visit,latitude,longitude,image_path,is_featured,status,sort_order) VALUES
                ('fort-santiago','Fort Santiago','Historic citadel and Rizal memorial destination.','Fort Santiago anchors many first-time Intramuros itineraries with bastions, river views, and galleries tracing the final days of Jose Rizal.','Historic Landmark','Spanish Colonial','Santa Clara Street, Intramuros','8:00 AM - 11:00 PM','PHP 75 regular / PHP 50 student','(02) 8527 2961','Bring water and sun protection; surfaces can be hot by noon.','Early morning or late afternoon',14.5953,120.9701,'/qrventure/images/fort-santiago.svg',TRUE,'open',1),
                ('san-agustin-church','San Agustin Church','UNESCO-listed baroque church and museum.','Completed in 1607, San Agustin Church preserves rich liturgical art and museum archives that frame Manila''s layered colonial history.','Religious Heritage','Spanish Colonial','General Luna Street, Intramuros','8:00 AM - 5:00 PM','Church free / Museum PHP 200','(02) 8527 4064','Observe quiet etiquette during mass hours.','Weekday mornings',14.5898,120.9747,'/qrventure/images/san-agustin.svg',TRUE,'open',2),
                ('casa-manila','Casa Manila','Recreation of a Spanish colonial bahay na bato.','Casa Manila curates period interiors, furniture, and social history to show elite domestic life in old Manila.','Museum','Spanish Colonial','Plaza San Luis Complex, Intramuros','9:00 AM - 6:00 PM','PHP 75','(02) 8527 4084','Pair your visit with nearby San Agustin Museum.','Mid-morning',14.5892,120.9749,'/qrventure/images/baluarte.svg',TRUE,'open',3),
                ('baluarte-de-san-diego','Baluarte de San Diego','Gardened bastion with panoramic wall views.','Baluarte de San Diego combines restored masonry and landscaped grounds ideal for relaxed heritage walks and photo stops.','Historic Fortification','Spanish Colonial','Sta. Lucia Street, Intramuros','8:00 AM - 5:00 PM','PHP 75','(02) 8527 4084','Wear comfortable shoes for uneven stone paths.','Golden hour',14.5888,120.9753,'/qrventure/images/baluarte.svg',TRUE,'open',4),
                ('manila-cathedral','Manila Cathedral','Neo-romanesque cathedral beside Plaza Roma.','The Manila Cathedral remains a spiritual and architectural focal point, rebuilt across centuries after earthquakes and war.','Religious Heritage','Spanish Colonial to Modern','Cabildo Street, Intramuros','7:30 AM - 5:30 PM','Free admission','(02) 8527 1796','Dress modestly when entering the church.','Late afternoon',14.5911,120.9736,'/qrventure/images/san-agustin.svg',TRUE,'open',5),
                ('plaza-roma','Plaza Roma','Historic civic square in the heart of Intramuros.','Plaza Roma links major civic and religious landmarks, making it a natural orientation point for walking tours.','Plaza','Spanish Colonial','Cabildo Street, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Use this as your meetup and starting point.','Sunset',14.5906,120.9734,'/qrventure/images/fort-santiago.svg',FALSE,'open',6),
                ('puerta-del-parian','Puerta del Parian','Restored gate that once controlled trade access.','Puerta del Parian reflects Intramuros'' defensive planning and historical circulation between districts.','City Gate','Spanish Colonial','Muralla Street, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Best seen with nearby wall walk segments.','Morning',14.5923,120.9718,'/qrventure/images/fort-santiago.svg',FALSE,'open',7),
                ('puerta-real-gardens','Puerta Real Gardens','Landscaped leisure pocket beside the walls.','Puerta Real Gardens offers shaded benches and open lawns along southern Intramuros fortifications.','Park','Modern Heritage Zone','Real Street, Intramuros','6:00 AM - 10:00 PM','Free admission','Intramuros Administration','Good resting stop between route segments.','Late afternoon',14.5868,120.9758,'/qrventure/images/baluarte.svg',FALSE,'open',8),
                ('memorare-manila-monument','Memorare Manila Monument','War memorial honoring civilian lives lost in 1945.','The monument offers an important reflective stop that contextualizes the Battle of Manila and post-war memory in the walled city.','Memorial','World War II','General Luna corner Anda, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Maintain respectful silence in the area.','Early morning',14.5920,120.9719,'/qrventure/images/fort-santiago.svg',FALSE,'open',9)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "dining_places")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO dining_places (slug,name,short_description,full_description,dining_type,cuisine,location_text,opening_hours,price_range,contact_details,visitor_notes,latitude,longitude,image_path,is_featured,status,sort_order) VALUES
                ('barbara-s-heritage','Barbara''s Heritage Restaurant','Heritage dining with cultural performances.','A popular heritage dining hall known for Filipino set menus and occasional cultural showcases near key plazas.','Restaurant','Filipino','General Luna Street, Intramuros','10:00 AM - 9:00 PM','PHP 500-1,200','(02) 8527 3893','Reserve ahead on weekends.',14.5894,120.9750,'/qrventure/images/dining-heritage.svg',TRUE,'open',1),
                ('cafe-intramuros','Cafe Intramuros','Relaxed heritage cafe for breaks.','A quiet cafe for coffee, rice meals, and snacks close to museums and churches.','Cafe','Filipino Comfort','Plaza San Luis, Intramuros','8:00 AM - 8:00 PM','PHP 200-500','(02) 8536 1120','Good brunch stop before museum hours.',14.5899,120.9740,'/qrventure/images/dining-cafe.svg',TRUE,'open',2),
                ('carta-filipina','Carta Filipina','Modern Filipino plates near major sites.','A contemporary Filipino dining room suitable for lunch and early dinner while exploring northern Intramuros.','Restaurant','Modern Filipino','Anda Street, Intramuros','10:30 AM - 9:30 PM','PHP 300-800','0917 100 2211','Try off-peak hours after 2 PM.',14.5949,120.9720,'/qrventure/images/dining-tapas.svg',FALSE,'open',3)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "local_services")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO local_services (slug,name,short_description,full_description,service_type,location_text,hours,contact_details,visitor_notes,latitude,longitude,image_path,status,sort_order) VALUES
                ('plaza-moriones-atm','Plaza Moriones ATM Cluster','ATM kiosks near major visitor flows.','Several bank ATM kiosks clustered near Plaza Moriones for quick cash access before route starts.','ATM','Plaza Moriones, Intramuros','24/7','Bank hotlines onsite','Withdraw small bills for transport and small vendors.',14.5957,120.9698,'/qrventure/images/service-atm.svg','open',1),
                ('intramuros-info-center','Intramuros Information Center','Visitor orientation and maps.','Primary information desk for maps, updates, and event advisories before beginning walking routes.','Info Center','Fort Santiago Gate Area','8:00 AM - 6:00 PM','(02) 8527 3120','Ask for closures during heritage events.',14.5950,120.9709,'/qrventure/images/service-info.svg','open',2),
                ('intra-police-assist','Intramuros Police Assistance Desk','Tourist safety assistance point.','Assistance desk for incident reporting, lost and found, and safety-related concerns in the district.','Police','Anda Circle, Intramuros','24/7','PNP Hotline 911','Keep emergency contacts ready.',14.5942,120.9723,'/qrventure/images/service-firstaid.svg','open',3)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "tour_routes")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO tour_routes (slug,name,short_description,full_description,route_type,starting_point,estimated_duration,travel_tips,distance_text,map_link,is_featured,status,sort_order) VALUES
                ('historic-core-loop','Historic Core Loop','A complete first-time Intramuros walking circuit.','Covers Plaza Roma, Manila Cathedral, San Agustin Church, Casa Manila, and Baluarte de San Diego in one coherent path.','Heritage Walk','Plaza Roma','2.5 hours','Start early and carry water.','2.6 km','https://maps.google.com/?q=14.5906,120.9734',TRUE,'open',1),
                ('fort-and-walls','Fort and Walls','Fort Santiago plus wallside viewpoints.','Begins at Fort Santiago and continues through wall promenades and river-facing segments for history-focused visitors.','Fortification Focus','Fort Santiago Gate','1.5 hours','Bring hat and sun protection.','1.8 km','https://maps.google.com/?q=14.5953,120.9701',TRUE,'open',2),
                ('churches-and-plazas','Churches and Plazas','Faith and civic heritage route.','Connects Manila Cathedral, San Agustin Church, Plaza Roma, and nearby plazas with interpretation stops.','Culture & Faith','Manila Cathedral','1 hour 45 minutes','Respect mass schedules and quiet zones.','1.9 km','https://maps.google.com/?q=14.5911,120.9736',FALSE,'open',3)
                """.trimIndent()
            )
        }

    private fun tableHasData(connection: Connection, table: String): Boolean {
        connection.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM $table").use { rs ->
                rs.next()
                return rs.getInt(1) > 0
            }
        }
    }

    private fun columnExists(connection: Connection, table: String, column: String): Boolean {
        connection.metaData.getColumns(null, null, table, column).use { rs ->
            return rs.next()
        }
    }
}


private fun columnExists(connection: Connection, table: String, column: String): Boolean =
    connection.metaData.getColumns(null, null, table, column).use { it.next() }
