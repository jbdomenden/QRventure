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
                    image_urls TEXT NOT NULL DEFAULT '',
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
                    image_urls TEXT NOT NULL DEFAULT '',
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
                    image_urls TEXT NOT NULL DEFAULT '',
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
                    image_url VARCHAR(500) NOT NULL DEFAULT '',
                    image_urls TEXT NOT NULL DEFAULT '',
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
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE attractions ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0")

            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS short_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS full_description TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS dining_type VARCHAR(100) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS cuisine VARCHAR(100) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS visitor_notes TEXT NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE dining_places ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT ''")
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
            st.execute("ALTER TABLE local_services ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT ''")
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
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) NOT NULL DEFAULT ''")
            st.execute("ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS image_urls TEXT NOT NULL DEFAULT ''")
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

            st.execute("UPDATE attractions SET image_urls = CONCAT('[\"', image_path, '\"]') WHERE image_urls = ''")
            st.execute("UPDATE dining_places SET image_urls = CONCAT('[\"', image_path, '\"]') WHERE image_urls = ''")
            st.execute("UPDATE local_services SET image_urls = CONCAT('[\"', image_path, '\"]') WHERE image_urls = ''")
            st.execute("UPDATE tour_routes SET image_urls = CONCAT('[\"', image_url, '\"]') WHERE image_urls = '' AND image_url <> ''")
        }
    }

    fun seedData(connection: Connection) {
        if (!tableHasData(connection, "attractions")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO attractions (slug,name,short_description,full_description,category,historical_period,location_text,opening_hours,entrance_fee,contact_details,visitor_tips,best_time_to_visit,latitude,longitude,image_path,is_featured,status,sort_order) VALUES
                ('fort-santiago','Fort Santiago','Historic citadel and Rizal memorial destination.','Fort Santiago anchors many first-time Intramuros itineraries with bastions, river views, and galleries tracing the final days of Jose Rizal.','Historic Landmark','Spanish Colonial','Santa Clara Street, Intramuros','8:00 AM - 11:00 PM','PHP 75 regular / PHP 50 student','(02) 8527 2961','Bring water and sun protection; surfaces can be hot by noon.','Early morning or late afternoon',14.5953,120.9701,'https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate.jpg',TRUE,'open',1),
                ('san-agustin-church','San Agustin Church','UNESCO-listed baroque church and museum.','Completed in 1607, San Agustin Church preserves rich liturgical art and museum archives that frame Manila''s layered colonial history.','Religious Heritage','Spanish Colonial','General Luna Street, Intramuros','8:00 AM - 5:00 PM','Church free / Museum PHP 200','(02) 8527 4064','Observe quiet etiquette during mass hours.','Weekday mornings',14.5898,120.9747,'https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Church%20Manila.jpg',TRUE,'open',2),
                ('casa-manila','Casa Manila','Recreation of a Spanish colonial bahay na bato.','Casa Manila curates period interiors, furniture, and social history to show elite domestic life in old Manila.','Museum','Spanish Colonial','Plaza San Luis Complex, Intramuros','9:00 AM - 6:00 PM','PHP 75','(02) 8527 4084','Pair your visit with nearby San Agustin Museum.','Mid-morning',14.5892,120.9749,'https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1591%2012.JPG',TRUE,'open',3),
                ('baluarte-de-san-diego','Baluarte de San Diego','Gardened bastion with panoramic wall views.','Baluarte de San Diego combines restored masonry and landscaped grounds ideal for relaxed heritage walks and photo stops.','Historic Fortification','Spanish Colonial','Sta. Lucia Street, Intramuros','8:00 AM - 5:00 PM','PHP 75','(02) 8527 4084','Wear comfortable shoes for uneven stone paths.','Golden hour',14.5888,120.9753,'',TRUE,'open',4),
                ('manila-cathedral','Manila Cathedral','Neo-romanesque cathedral beside Plaza Roma.','The Manila Cathedral remains a spiritual and architectural focal point, rebuilt across centuries after earthquakes and war.','Religious Heritage','Spanish Colonial to Modern','Cabildo Street, Intramuros','7:30 AM - 5:30 PM','Free admission','(02) 8527 1796','Dress modestly when entering the church.','Late afternoon',14.5911,120.9736,'https://commons.wikimedia.org/wiki/Special:FilePath/The%20Manila%20Cathedral%20Facade.jpg',TRUE,'open',5),
                ('plaza-roma','Plaza Roma','Historic civic square in the heart of Intramuros.','Plaza Roma links major civic and religious landmarks, making it a natural orientation point for walking tours.','Plaza','Spanish Colonial','Cabildo Street, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Use this as your meetup and starting point.','Sunset',14.5906,120.9734,'',FALSE,'open',6),
                ('puerta-del-parian','Puerta del Parian','Restored gate that once controlled trade access.','Puerta del Parian reflects Intramuros'' defensive planning and historical circulation between districts.','City Gate','Spanish Colonial','Muralla Street, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Best seen with nearby wall walk segments.','Morning',14.5923,120.9718,'',FALSE,'open',7),
                ('puerta-real-gardens','Puerta Real Gardens','Landscaped leisure pocket beside the walls.','Puerta Real Gardens offers shaded benches and open lawns along southern Intramuros fortifications.','Park','Modern Heritage Zone','Real Street, Intramuros','6:00 AM - 10:00 PM','Free admission','Intramuros Administration','Good resting stop between route segments.','Late afternoon',14.5868,120.9758,'',FALSE,'open',8),
                ('memorare-manila-monument','Memorare Manila Monument','War memorial honoring civilian lives lost in 1945.','The monument offers an important reflective stop that contextualizes the Battle of Manila and post-war memory in the walled city.','Memorial','World War II','General Luna corner Anda, Intramuros','Open 24 hours','Free admission','Intramuros Administration','Maintain respectful silence in the area.','Early morning',14.5920,120.9719,'',FALSE,'open',9)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "dining_places")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO dining_places (slug,name,short_description,full_description,dining_type,cuisine,location_text,opening_hours,price_range,contact_details,visitor_notes,latitude,longitude,image_path,is_featured,status,sort_order) VALUES
                ('barbara-s-heritage','Barbara''s Heritage Restaurant','Heritage dining with cultural performances.','A popular heritage dining hall known for Filipino set menus and occasional cultural showcases near key plazas.','Restaurant','Filipino','General Luna Street, Intramuros','10:00 AM - 9:00 PM','PHP 500-1,200','(02) 8527 3893','Reserve ahead on weekends.',14.5894,120.9750,'https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20CNE%2021.jpg',TRUE,'open',1),
                ('cafe-intramuros','Cafe Intramuros','Relaxed heritage cafe for breaks.','A quiet cafe for coffee, rice meals, and snacks close to museums and churches.','Cafe','Filipino Comfort','Plaza San Luis, Intramuros','8:00 AM - 8:00 PM','PHP 200-500','(02) 8536 1120','Good brunch stop before museum hours.',14.5899,120.9740,'',FALSE,'open',2),
                ('carta-filipina','Carta Filipina','Modern Filipino plates near major sites.','A contemporary Filipino dining room suitable for lunch and early dinner while exploring northern Intramuros.','Restaurant','Modern Filipino','Anda Street, Intramuros','10:30 AM - 9:30 PM','PHP 300-800','0917 100 2211','Try off-peak hours after 2 PM.',14.5949,120.9720,'',FALSE,'open',3)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "local_services")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO local_services (slug,name,short_description,full_description,service_type,location_text,hours,contact_details,visitor_notes,latitude,longitude,image_path,status,sort_order) VALUES
                ('plaza-moriones-atm','Plaza Moriones ATM Cluster','ATM kiosks near major visitor flows.','Several bank ATM kiosks clustered near Plaza Moriones for quick cash access before route starts.','ATM','Plaza Moriones, Intramuros','24/7','Bank hotlines onsite','Withdraw small bills for transport and small vendors.',14.5957,120.9698,'','open',1),
                ('intramuros-info-center','Intramuros Information Center','Visitor orientation and maps.','Primary information desk for maps, updates, and event advisories before beginning walking routes.','Info Center','Fort Santiago Gate Area','8:00 AM - 6:00 PM','(02) 8527 3120','Ask for closures during heritage events.',14.5950,120.9709,'','open',2),
                ('intra-police-assist','Intramuros Police Assistance Desk','Tourist safety assistance point.','Assistance desk for incident reporting, lost and found, and safety-related concerns in the district.','Police','Anda Circle, Intramuros','24/7','PNP Hotline 911','Keep emergency contacts ready.',14.5942,120.9723,'','open',3)
                """.trimIndent()
            )
        }

        if (!tableHasData(connection, "tour_routes")) {
            connection.createStatement().executeUpdate(
                """
                INSERT INTO tour_routes (slug,name,short_description,full_description,route_type,starting_point,estimated_duration,travel_tips,distance_text,map_link,image_url,image_urls,is_featured,status,sort_order) VALUES
                ('historic-core-loop','Historic Core Loop','A complete first-time Intramuros walking circuit.','Covers Plaza Roma, Manila Cathedral, San Agustin Church, Casa Manila, and Baluarte de San Diego in one coherent path.','Heritage Walk','Plaza Roma','2.5 hours','Start early and carry water.','2.6 km','https://maps.google.com/?q=14.5906,120.9734','https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg','[\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20roma.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Aerial%20view%20of%20Plaza%20Roma%20and%20the%20Manila%20Cathedral%20inside%20Intramuros.jpg\"]',TRUE,'open',1),
                ('fort-and-walls','Fort and Walls','Fort Santiago plus wallside viewpoints.','Begins at Fort Santiago and continues through wall promenades and river-facing segments for history-focused visitors.','Fortification Focus','Fort Santiago Gate','1.5 hours','Bring hat and sun protection.','1.8 km','https://maps.google.com/?q=14.5953,120.9701','https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg','[\"https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Walking%20through%20the%20walls%20of%20Intramuros%2C%20Manila.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%2C%20Intramuros.JPG\"]',TRUE,'open',2),
                ('churches-and-plazas','Churches and Plazas','Faith and civic heritage route.','Connects Manila Cathedral, San Agustin Church, Plaza Roma, and nearby plazas with interpretation stops.','Culture & Faith','Manila Cathedral','1 hour 45 minutes','Respect mass schedules and quiet zones.','1.9 km','https://maps.google.com/?q=14.5911,120.9736','','[]',FALSE,'open',3)
                """.trimIndent()
            )
        }

        connection.createStatement().use { st ->
            st.executeUpdate(
                """
                INSERT INTO attractions (slug,name,short_description,full_description,category,historical_period,location_text,opening_hours,entrance_fee,contact_details,visitor_tips,best_time_to_visit,latitude,longitude,image_path,is_featured,status,sort_order)
                SELECT 'san-agustin-museum','San Agustin Museum','Museum of religious artifacts inside San Agustin complex.','Located within the San Agustin Church complex, the museum houses centuries-old artifacts, manuscripts, and ecclesiastical art from the Spanish period.','Museum','Spanish Colonial Period','General Luna St, Intramuros','8:00 AM - 5:00 PM','PHP 200','(02) 8527 4064','Pair your visit with San Agustin Church.','Weekday mornings',14.5899,120.9746,'https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Museum.jpg',TRUE,'open',10
                WHERE NOT EXISTS (SELECT 1 FROM attractions WHERE slug='san-agustin-museum')
                """.trimIndent()
            )
            st.executeUpdate(
                """
                INSERT INTO attractions (slug,name,short_description,full_description,category,historical_period,location_text,opening_hours,entrance_fee,contact_details,visitor_tips,best_time_to_visit,latitude,longitude,image_path,is_featured,status,sort_order)
                SELECT 'intramuros-walls-walk','Intramuros Walls Walk','Walk along the historic stone walls of Manila.','Visitors can walk along restored sections of the Intramuros walls, offering scenic views of the city and insight into Spanish-era defense systems.','Experience','Spanish Colonial Period','Various wall sections','Open 24 hours','Free admission','Intramuros Administration','Best enjoyed in the morning or late afternoon.','Late afternoon',14.5909,120.9722,'https://commons.wikimedia.org/wiki/Special:FilePath/Intramuros%20Walls.jpg',TRUE,'open',11
                WHERE NOT EXISTS (SELECT 1 FROM attractions WHERE slug='intramuros-walls-walk')
                """.trimIndent()
            )
            st.executeUpdate(
                """
                INSERT INTO attractions (slug,name,short_description,full_description,category,historical_period,location_text,opening_hours,entrance_fee,contact_details,visitor_tips,best_time_to_visit,latitude,longitude,image_path,is_featured,status,sort_order)
                SELECT 'plaza-san-luis-complex','Plaza San Luis Complex','Reconstructed colonial-style complex.','Plaza San Luis features reconstructed Spanish colonial buildings showcasing Filipino lifestyle during the Spanish era.','Cultural Site','Spanish Colonial Reconstruction','General Luna St','8:00 AM - 6:00 PM','Free admission','Intramuros Administration','Good stop before San Agustin Church and Casa Manila.','Mid-morning',14.5894,120.9748,'https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20San%20Luis.jpg',TRUE,'open',12
                WHERE NOT EXISTS (SELECT 1 FROM attractions WHERE slug='plaza-san-luis-complex')
                """.trimIndent()
            )

            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate%20in%20Manila%2C%202018%20%2802%29.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/The%20Gate%20of%20Fort%20Santiago.jpg\"]' WHERE slug='fort-santiago'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Church%20Manila.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Church%20Manila.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/San%20agustin%20facade.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Facade%20of%20San%20Agustin%20Church.jpg\"]' WHERE slug='san-agustin-church'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1591%2012.JPG', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1591%2012.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1568%2004.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Casa%20Manila%2C%20Manila%2C%20Filipinas%2C%202023-08-27%2C%20DD%2072.jpg\"]' WHERE slug='casa-manila'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/The%20Manila%20Cathedral%20Facade.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/The%20Manila%20Cathedral%20Facade.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Manila%20Cathedral%20facade%20hq%202023-10-29.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Facade%20details%20of%20the%20Manila%20Cathedral.jpg\"]' WHERE slug='manila-cathedral'")
            st.executeUpdate("UPDATE attractions SET name='Baluarte de San Diego', category='Fortification', short_description='A restored Spanish-era bastion with gardens and ruins.', full_description='Baluarte de San Diego is one of the oldest stone fortifications in Intramuros, featuring circular bastion ruins surrounded by landscaped gardens. It offers a glimpse into early Spanish military architecture.', location_text='Sta. Lucia St, Intramuros', historical_period='Spanish Colonial Period', image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Baluarte%20de%20San%20Diego%20Intramuros.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Baluarte%20de%20San%20Diego%20Intramuros.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Baluarte%20de%20San%20Diego%20ruins.jpg\"]' WHERE slug='baluarte-de-san-diego'")
            st.executeUpdate("UPDATE attractions SET name='Puerta del Parian', category='Gate', short_description='One of the original gates of Intramuros.', full_description='Puerta del Parian served as a main entrance for Chinese merchants during the Spanish era. Today it stands as a restored historic gate connecting the old city to Binondo.', location_text='Intramuros Wall, near Binondo', historical_period='Spanish Colonial Period', image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Puerta%20del%20Parian%20Intramuros.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Puerta%20del%20Parian%20Intramuros.jpg\"]' WHERE slug='puerta-del-parian'")
            st.executeUpdate("UPDATE attractions SET name='Puerta Real Gardens', category='Garden', short_description='A peaceful garden beside the historic walls.', full_description='Puerta Real Gardens is a landscaped open space near one of the southern gates of Intramuros. It is commonly used for events, relaxation, and cultural gatherings.', location_text='Muralla St, Intramuros', historical_period='Modern Restoration', image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Puerta%20Real%20Gardens%20Intramuros.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Puerta%20Real%20Gardens%20Intramuros.jpg\"]' WHERE slug='puerta-real-gardens'")
            st.executeUpdate("UPDATE attractions SET name='Memorare Manila Monument', category='Memorial', short_description='Memorial to civilian victims of WWII.', full_description='The Memorare Manila Monument honors the thousands of civilians who perished during the Battle of Manila in 1945. It stands near San Agustin Church as a solemn historical reminder.', location_text='Plaza San Luis Complex', historical_period='World War II', image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Memorare%20Manila%20Monument.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Memorare%20Manila%20Monument.jpg\"]' WHERE slug='memorare-manila-monument'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Museum.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Museum.jpg\"]' WHERE slug='san-agustin-museum'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Intramuros%20Walls.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Intramuros%20Walls.jpg\"]' WHERE slug='intramuros-walls-walk'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20San%20Luis.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20San%20Luis.jpg\"]' WHERE slug='plaza-san-luis-complex'")
            st.executeUpdate("UPDATE attractions SET image_path='', image_urls='[]' WHERE slug NOT IN ('fort-santiago','san-agustin-church','casa-manila','manila-cathedral','baluarte-de-san-diego','puerta-del-parian','puerta-real-gardens','memorare-manila-monument','san-agustin-museum','intramuros-walls-walk','plaza-san-luis-complex')")

            st.executeUpdate("UPDATE dining_places SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20CNE%2021.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20CNE%2021.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20Interior%20Panorama.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Restaurant.jpg\"]', is_featured=TRUE WHERE slug='barbara-s-heritage'")
            st.executeUpdate("UPDATE dining_places SET image_path='', image_urls='[]', is_featured=FALSE WHERE slug <> 'barbara-s-heritage'")
            st.executeUpdate("UPDATE local_services SET image_path='', image_urls='[]'")
            st.executeUpdate("UPDATE tour_routes SET image_url='https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20roma.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Aerial%20view%20of%20Plaza%20Roma%20and%20the%20Manila%20Cathedral%20inside%20Intramuros.jpg\"]' WHERE slug='historic-core-loop'")
            st.executeUpdate("UPDATE tour_routes SET image_url='https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Walking%20through%20the%20walls%20of%20Intramuros%2C%20Manila.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%2C%20Intramuros.JPG\"]' WHERE slug='fort-and-walls'")
            st.executeUpdate("UPDATE tour_routes SET image_url='', image_urls='[]' WHERE slug NOT IN ('historic-core-loop','fort-and-walls')")
        }

        connection.createStatement().use { st ->
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%20Gate%20in%20Manila%2C%202018%20%2802%29.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/The%20Gate%20of%20Fort%20Santiago.jpg\"]' WHERE slug='fort-santiago'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Church%20Manila.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/San%20Agustin%20Church%20Manila.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/San%20agustin%20facade.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Facade%20of%20San%20Agustin%20Church.jpg\"]' WHERE slug='san-agustin-church'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1591%2012.JPG', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1591%2012.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/CasaManilajf1568%2004.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Casa%20Manila%2C%20Manila%2C%20Filipinas%2C%202023-08-27%2C%20DD%2072.jpg\"]' WHERE slug='casa-manila'")
            st.executeUpdate("UPDATE attractions SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/The%20Manila%20Cathedral%20Facade.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/The%20Manila%20Cathedral%20Facade.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Manila%20Cathedral%20facade%20hq%202023-10-29.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Facade%20details%20of%20the%20Manila%20Cathedral.jpg\"]' WHERE slug='manila-cathedral'")
            st.executeUpdate("UPDATE attractions SET image_path='', image_urls='[]' WHERE slug NOT IN ('fort-santiago','san-agustin-church','casa-manila','manila-cathedral')")

            st.executeUpdate("UPDATE dining_places SET image_path='https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20CNE%2021.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20CNE%2021.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Heritage%20Restaurant%20Intramuros%20Interior%20Panorama.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Barbara%27s%20Restaurant.jpg\"]', is_featured=TRUE WHERE slug='barbara-s-heritage'")
            st.executeUpdate("UPDATE dining_places SET image_path='', image_urls='[]', is_featured=FALSE WHERE slug <> 'barbara-s-heritage'")
            st.executeUpdate("UPDATE local_services SET image_path='', image_urls='[]'")
            st.executeUpdate("UPDATE tour_routes SET image_url='https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20Roma%20Intramuros%202023-10-01.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Plaza%20de%20roma.JPG\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Aerial%20view%20of%20Plaza%20Roma%20and%20the%20Manila%20Cathedral%20inside%20Intramuros.jpg\"]' WHERE slug='historic-core-loop'")
            st.executeUpdate("UPDATE tour_routes SET image_url='https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg', image_urls='[\"https://commons.wikimedia.org/wiki/Special:FilePath/Manila%2C%20Intramuros%20walls%2C%20Philippines.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Walking%20through%20the%20walls%20of%20Intramuros%2C%20Manila.jpg\",\"https://commons.wikimedia.org/wiki/Special:FilePath/Fort%20Santiago%2C%20Intramuros.JPG\"]' WHERE slug='fort-and-walls'")
            st.executeUpdate("UPDATE tour_routes SET image_url='', image_urls='[]' WHERE slug NOT IN ('historic-core-loop','fort-and-walls')")
        }
    }

    private fun tableHasData(connection: Connection, table: String): Boolean {
        connection.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM $table").use { rs ->
                return if (rs.next()) rs.getInt(1) > 0 else false
            }
        }
    }

    private fun columnExists(connection: Connection, table: String, column: String): Boolean {
        connection.metaData.getColumns(null, null, table, column).use { rs ->
            return rs.next()
        }
    }
}
