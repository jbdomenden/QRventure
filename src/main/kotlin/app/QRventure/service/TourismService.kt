package app.QRventure.service

import app.QRventure.dto.*
import app.QRventure.model.*
import java.sql.Connection
import java.sql.Statement

class TourismService(private val connection: Connection) {

    fun featured(): FeaturedResponse = FeaturedResponse(
        attractions = attractions().filter { it.isFeatured },
        dining = dining().filter { it.isFeatured },
        services = services().filter { it.isFeatured }
    )

    fun search(query: String): SearchResponse {
        val q = query.trim()
        return SearchResponse(
            query = q,
            attractions = attractions(search = q),
            dining = dining(type = q),
            services = services(type = q)
        )
    }

    fun attractions(search: String? = null, category: String? = null): List<Attraction> {
        val sql = buildString {
            append("SELECT * FROM attractions WHERE status = 'open'")
            if (!search.isNullOrBlank()) append(" AND LOWER(name) LIKE ?")
            if (!category.isNullOrBlank()) append(" AND LOWER(category) = ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            var i = 1
            if (!search.isNullOrBlank()) ps.setString(i++, "%${search.lowercase()}%")
            if (!category.isNullOrBlank()) ps.setString(i, category.lowercase())
            ps.executeQuery().toList { it.toAttraction() }
        }
    }

    fun attractionBySlugOrId(key: String): Attraction? = connection.lookupByKey("attractions", key) { it.toAttraction() }

    fun createAttraction(payload: AttractionUpsertDto): Attraction {
        val sql = """
            INSERT INTO attractions (slug, name, short_description, full_description, category, location_text, opening_hours, entrance_fee, contact_details, latitude, longitude, image_path, status, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        val id = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
            ps.setString(1, payload.slug)
            ps.setString(2, payload.name)
            ps.setString(3, payload.shortDescription)
            ps.setString(4, payload.fullDescription)
            ps.setString(5, payload.category)
            ps.setString(6, payload.locationText)
            ps.setString(7, payload.openingHours)
            ps.setString(8, payload.entranceFee)
            ps.setString(9, payload.contactDetails)
            ps.setDouble(10, payload.latitude)
            ps.setDouble(11, payload.longitude)
            ps.setString(12, payload.imagePath)
            ps.setString(13, payload.status)
            ps.setBoolean(14, payload.isFeatured)
            ps.executeUpdate()
            ps.generatedKeys.use { keys -> if (keys.next()) keys.getInt(1) else error("No key returned") }
        }
        return attractionBySlugOrId(id.toString())!!
    }

    fun updateAttraction(key: String, payload: AttractionUpsertDto): Attraction? {
        val target = attractionBySlugOrId(key) ?: return null
        connection.prepareStatement(
            """
            UPDATE attractions
            SET slug=?, name=?, short_description=?, full_description=?, category=?, location_text=?, opening_hours=?, entrance_fee=?, contact_details=?, latitude=?, longitude=?, image_path=?, status=?, is_featured=?
            WHERE id=?
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, payload.slug)
            ps.setString(2, payload.name)
            ps.setString(3, payload.shortDescription)
            ps.setString(4, payload.fullDescription)
            ps.setString(5, payload.category)
            ps.setString(6, payload.locationText)
            ps.setString(7, payload.openingHours)
            ps.setString(8, payload.entranceFee)
            ps.setString(9, payload.contactDetails)
            ps.setDouble(10, payload.latitude)
            ps.setDouble(11, payload.longitude)
            ps.setString(12, payload.imagePath)
            ps.setString(13, payload.status)
            ps.setBoolean(14, payload.isFeatured)
            ps.setInt(15, target.id)
            ps.executeUpdate()
        }
        return attractionBySlugOrId(target.id.toString())
    }

    fun deleteAttraction(key: String): Boolean = connection.deleteByKey("attractions", key)

    fun dining(type: String? = null): List<DiningPlace> {
        val sql = buildString {
            append("SELECT * FROM dining_places")
            if (!type.isNullOrBlank()) append(" WHERE LOWER(cuisine_or_type) LIKE ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            if (!type.isNullOrBlank()) ps.setString(1, "%${type.lowercase()}%")
            ps.executeQuery().toList { it.toDining() }
        }
    }

    fun diningBySlugOrId(key: String): DiningPlace? = connection.lookupByKey("dining_places", key) { it.toDining() }
    fun createDining(payload: DiningUpsertDto): DiningPlace {
        connection.prepareStatement(
            """
            INSERT INTO dining_places (slug, name, description, cuisine_or_type, location_text, opening_hours, price_range, contact_details, latitude, longitude, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, payload.slug)
            ps.setString(2, payload.name)
            ps.setString(3, payload.description)
            ps.setString(4, payload.cuisineOrType)
            ps.setString(5, payload.locationText)
            ps.setString(6, payload.openingHours)
            ps.setString(7, payload.priceRange)
            ps.setString(8, payload.contactDetails)
            ps.setDouble(9, payload.latitude)
            ps.setDouble(10, payload.longitude)
            ps.setString(11, payload.imagePath)
            ps.setBoolean(12, payload.isFeatured)
            ps.executeUpdate()
        }
        return diningBySlugOrId(payload.slug)!!
    }

    fun updateDining(key: String, payload: DiningUpsertDto): DiningPlace? {
        val target = diningBySlugOrId(key) ?: return null
        connection.prepareStatement(
            """
            UPDATE dining_places
            SET slug=?, name=?, description=?, cuisine_or_type=?, location_text=?, opening_hours=?, price_range=?, contact_details=?, latitude=?, longitude=?, image_path=?, is_featured=?
            WHERE id=?
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, payload.slug); ps.setString(2, payload.name); ps.setString(3, payload.description)
            ps.setString(4, payload.cuisineOrType); ps.setString(5, payload.locationText); ps.setString(6, payload.openingHours)
            ps.setString(7, payload.priceRange); ps.setString(8, payload.contactDetails); ps.setDouble(9, payload.latitude)
            ps.setDouble(10, payload.longitude); ps.setString(11, payload.imagePath); ps.setBoolean(12, payload.isFeatured); ps.setInt(13, target.id)
            ps.executeUpdate()
        }
        return diningBySlugOrId(target.id.toString())
    }

    fun deleteDining(key: String): Boolean = connection.deleteByKey("dining_places", key)

    fun services(type: String? = null): List<LocalService> {
        val sql = buildString {
            append("SELECT * FROM local_services")
            if (!type.isNullOrBlank()) append(" WHERE LOWER(service_type) LIKE ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            if (!type.isNullOrBlank()) ps.setString(1, "%${type.lowercase()}%")
            ps.executeQuery().toList { it.toService() }
        }
    }

    fun serviceBySlugOrId(key: String): LocalService? = connection.lookupByKey("local_services", key) { it.toService() }
    fun createService(payload: ServiceUpsertDto): LocalService {
        connection.prepareStatement(
            """
            INSERT INTO local_services (slug, name, description, service_type, location_text, operating_hours, contact_details, latitude, longitude, nearby_landmark_notes, travel_tips, image_path, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, payload.slug); ps.setString(2, payload.name); ps.setString(3, payload.description)
            ps.setString(4, payload.serviceType); ps.setString(5, payload.locationText); ps.setString(6, payload.operatingHours)
            ps.setString(7, payload.contactDetails); ps.setDouble(8, payload.latitude); ps.setDouble(9, payload.longitude)
            ps.setString(10, payload.nearbyLandmarkNotes); ps.setString(11, payload.travelTips); ps.setString(12, payload.imagePath)
            ps.setBoolean(13, payload.isFeatured)
            ps.executeUpdate()
        }
        return serviceBySlugOrId(payload.slug)!!
    }

    fun updateService(key: String, payload: ServiceUpsertDto): LocalService? {
        val target = serviceBySlugOrId(key) ?: return null
        connection.prepareStatement(
            """
            UPDATE local_services
            SET slug=?, name=?, description=?, service_type=?, location_text=?, operating_hours=?, contact_details=?, latitude=?, longitude=?, nearby_landmark_notes=?, travel_tips=?, image_path=?, is_featured=?
            WHERE id=?
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, payload.slug); ps.setString(2, payload.name); ps.setString(3, payload.description)
            ps.setString(4, payload.serviceType); ps.setString(5, payload.locationText); ps.setString(6, payload.operatingHours)
            ps.setString(7, payload.contactDetails); ps.setDouble(8, payload.latitude); ps.setDouble(9, payload.longitude)
            ps.setString(10, payload.nearbyLandmarkNotes); ps.setString(11, payload.travelTips); ps.setString(12, payload.imagePath)
            ps.setBoolean(13, payload.isFeatured); ps.setInt(14, target.id)
            ps.executeUpdate()
        }
        return serviceBySlugOrId(target.id.toString())
    }

    fun deleteService(key: String): Boolean = connection.deleteByKey("local_services", key)

    fun tourRoutes(search: String? = null): List<TourRoute> {
        val sql = if (search.isNullOrBlank()) "SELECT * FROM tour_routes ORDER BY is_featured DESC, name ASC"
        else "SELECT * FROM tour_routes WHERE LOWER(name) LIKE ? OR LOWER(highlights) LIKE ? ORDER BY is_featured DESC, name ASC"
        return connection.prepareStatement(sql).use { ps ->
            if (!search.isNullOrBlank()) {
                ps.setString(1, "%${search.lowercase()}%")
                ps.setString(2, "%${search.lowercase()}%")
            }
            ps.executeQuery().toList { it.toTourRoute() }
        }
    }

    fun tourRouteBySlugOrId(key: String): TourRoute? = connection.lookupByKey("tour_routes", key) { it.toTourRoute() }
    fun createTourRoute(payload: TourRouteUpsertDto): TourRoute {
        connection.prepareStatement(
            "INSERT INTO tour_routes (slug, name, duration_text, start_point, route_description, distance_km, highlights, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        ).use { ps ->
            ps.setString(1, payload.slug); ps.setString(2, payload.name); ps.setString(3, payload.durationText); ps.setString(4, payload.startPoint)
            ps.setString(5, payload.routeDescription); ps.setDouble(6, payload.distanceKm); ps.setString(7, payload.highlights); ps.setBoolean(8, payload.isFeatured)
            ps.executeUpdate()
        }
        return tourRouteBySlugOrId(payload.slug)!!
    }

    fun updateTourRoute(key: String, payload: TourRouteUpsertDto): TourRoute? {
        val target = tourRouteBySlugOrId(key) ?: return null
        connection.prepareStatement(
            "UPDATE tour_routes SET slug=?, name=?, duration_text=?, start_point=?, route_description=?, distance_km=?, highlights=?, is_featured=? WHERE id=?"
        ).use { ps ->
            ps.setString(1, payload.slug); ps.setString(2, payload.name); ps.setString(3, payload.durationText); ps.setString(4, payload.startPoint)
            ps.setString(5, payload.routeDescription); ps.setDouble(6, payload.distanceKm); ps.setString(7, payload.highlights); ps.setBoolean(8, payload.isFeatured); ps.setInt(9, target.id)
            ps.executeUpdate()
        }
        return tourRouteBySlugOrId(target.id.toString())
    }

    fun deleteTourRoute(key: String): Boolean = connection.deleteByKey("tour_routes", key)

    fun adminByEmail(email: String): Admin? = connection.prepareStatement("SELECT * FROM admins WHERE LOWER(email)=?").use { ps ->
        ps.setString(1, email.lowercase())
        val rs = ps.executeQuery()
        if (rs.next()) rs.toAdmin() else null
    }
}

private fun <T> java.sql.ResultSet.toList(mapper: (java.sql.ResultSet) -> T): List<T> {
    val out = mutableListOf<T>()
    while (next()) out += mapper(this)
    return out
}

private fun <T> Connection.lookupByKey(table: String, key: String, mapper: (java.sql.ResultSet) -> T): T? {
    val byId = key.toIntOrNull()
    val sql = if (byId != null) "SELECT * FROM $table WHERE id = ?" else "SELECT * FROM $table WHERE slug = ?"
    return prepareStatement(sql).use { ps ->
        if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
        val rs = ps.executeQuery()
        if (rs.next()) mapper(rs) else null
    }
}

private fun Connection.deleteByKey(table: String, key: String): Boolean {
    val byId = key.toIntOrNull()
    val sql = if (byId != null) "DELETE FROM $table WHERE id = ?" else "DELETE FROM $table WHERE slug = ?"
    return prepareStatement(sql).use { ps ->
        if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
        ps.executeUpdate() > 0
    }
}

private fun java.sql.ResultSet.toAdmin() = Admin(
    id = getInt("id"),
    email = getString("email"),
    passwordHash = getString("password_hash"),
    role = getString("role"),
    createdAt = getTimestamp("created_at").toInstant().toString()
)

private fun java.sql.ResultSet.toAttraction() = Attraction(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), shortDescription = getString("short_description"),
    fullDescription = getString("full_description"), category = getString("category"), locationText = getString("location_text"),
    openingHours = getString("opening_hours"), entranceFee = getString("entrance_fee"), contactDetails = getString("contact_details"),
    latitude = getDouble("latitude"), longitude = getDouble("longitude"), imagePath = getString("image_path"),
    status = getString("status"), isFeatured = getBoolean("is_featured")
)

private fun java.sql.ResultSet.toDining() = DiningPlace(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), description = getString("description"),
    cuisineOrType = getString("cuisine_or_type"), locationText = getString("location_text"), openingHours = getString("opening_hours"),
    priceRange = getString("price_range"), contactDetails = getString("contact_details"), latitude = getDouble("latitude"),
    longitude = getDouble("longitude"), imagePath = getString("image_path"), isFeatured = getBoolean("is_featured")
)

private fun java.sql.ResultSet.toService() = LocalService(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), description = getString("description"),
    serviceType = getString("service_type"), locationText = getString("location_text"), operatingHours = getString("operating_hours"),
    contactDetails = getString("contact_details"), latitude = getDouble("latitude"), longitude = getDouble("longitude"),
    nearbyLandmarkNotes = getString("nearby_landmark_notes"), travelTips = getString("travel_tips"),
    imagePath = getString("image_path"), isFeatured = getBoolean("is_featured")
)

private fun java.sql.ResultSet.toTourRoute() = TourRoute(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), durationText = getString("duration_text"),
    startPoint = getString("start_point"), routeDescription = getString("route_description"), distanceKm = getDouble("distance_km"),
    highlights = getString("highlights"), isFeatured = getBoolean("is_featured")
)
