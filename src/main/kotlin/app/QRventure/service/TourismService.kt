package app.QRventure.service

import app.QRventure.dto.FeaturedResponse
import app.QRventure.dto.SearchResponse
import app.QRventure.model.Attraction
import app.QRventure.model.DiningPlace
import app.QRventure.model.LocalService
import java.sql.Connection

class TourismService(private val connection: Connection) {

    fun featured(): FeaturedResponse = FeaturedResponse(
        attractions = attractions().filter { it.isFeatured },
        dining = dining().filter { it.isFeatured },
        services = services().filter { it.isFeatured }
    )

    fun attractions(search: String? = null, category: String? = null): List<Attraction> {
        val sql = buildString {
            append("SELECT * FROM attractions WHERE status = 'open'")
            if (!search.isNullOrBlank()) append(" AND LOWER(name) LIKE ?")
            if (!category.isNullOrBlank()) append(" AND LOWER(category) = ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }

        connection.prepareStatement(sql).use { ps ->
            var i = 1
            if (!search.isNullOrBlank()) ps.setString(i++, "%${search.lowercase()}%")
            if (!category.isNullOrBlank()) ps.setString(i, category.lowercase())
            val rs = ps.executeQuery()
            val items = mutableListOf<Attraction>()
            while (rs.next()) items += rs.toAttraction()
            return items
        }
    }

    fun attractionBySlugOrId(key: String): Attraction? {
        val byId = key.toIntOrNull()
        val sql = if (byId != null) "SELECT * FROM attractions WHERE id = ?" else "SELECT * FROM attractions WHERE slug = ?"
        connection.prepareStatement(sql).use { ps ->
            if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
            val rs = ps.executeQuery()
            return if (rs.next()) rs.toAttraction() else null
        }
    }

    fun dining(type: String? = null): List<DiningPlace> {
        val sql = buildString {
            append("SELECT * FROM dining_places")
            if (!type.isNullOrBlank()) append(" WHERE LOWER(cuisine_or_type) LIKE ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }
        connection.prepareStatement(sql).use { ps ->
            if (!type.isNullOrBlank()) ps.setString(1, "%${type.lowercase()}%")
            val rs = ps.executeQuery()
            val items = mutableListOf<DiningPlace>()
            while (rs.next()) items += rs.toDining()
            return items
        }
    }

    fun diningBySlugOrId(key: String): DiningPlace? {
        val byId = key.toIntOrNull()
        val sql = if (byId != null) "SELECT * FROM dining_places WHERE id = ?" else "SELECT * FROM dining_places WHERE slug = ?"
        connection.prepareStatement(sql).use { ps ->
            if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
            val rs = ps.executeQuery()
            return if (rs.next()) rs.toDining() else null
        }
    }

    fun services(type: String? = null): List<LocalService> {
        val sql = buildString {
            append("SELECT * FROM local_services")
            if (!type.isNullOrBlank()) append(" WHERE LOWER(service_type) LIKE ?")
            append(" ORDER BY is_featured DESC, name ASC")
        }
        connection.prepareStatement(sql).use { ps ->
            if (!type.isNullOrBlank()) ps.setString(1, "%${type.lowercase()}%")
            val rs = ps.executeQuery()
            val items = mutableListOf<LocalService>()
            while (rs.next()) items += rs.toService()
            return items
        }
    }

    fun serviceBySlugOrId(key: String): LocalService? {
        val byId = key.toIntOrNull()
        val sql = if (byId != null) "SELECT * FROM local_services WHERE id = ?" else "SELECT * FROM local_services WHERE slug = ?"
        connection.prepareStatement(sql).use { ps ->
            if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
            val rs = ps.executeQuery()
            return if (rs.next()) rs.toService() else null
        }
    }

    fun search(query: String): SearchResponse {
        val q = query.trim()
        return SearchResponse(
            query = q,
            attractions = attractions(search = q),
            dining = dining(type = q),
            services = services(type = q)
        )
    }
}

private fun java.sql.ResultSet.toAttraction() = Attraction(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    shortDescription = getString("short_description"),
    fullDescription = getString("full_description"),
    category = getString("category"),
    locationText = getString("location_text"),
    openingHours = getString("opening_hours"),
    entranceFee = getString("entrance_fee"),
    contactDetails = getString("contact_details"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    imagePath = getString("image_path"),
    status = getString("status"),
    isFeatured = getBoolean("is_featured")
)

private fun java.sql.ResultSet.toDining() = DiningPlace(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    description = getString("description"),
    cuisineOrType = getString("cuisine_or_type"),
    locationText = getString("location_text"),
    openingHours = getString("opening_hours"),
    priceRange = getString("price_range"),
    contactDetails = getString("contact_details"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    imagePath = getString("image_path"),
    isFeatured = getBoolean("is_featured")
)

private fun java.sql.ResultSet.toService() = LocalService(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    description = getString("description"),
    serviceType = getString("service_type"),
    locationText = getString("location_text"),
    operatingHours = getString("operating_hours"),
    contactDetails = getString("contact_details"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    nearbyLandmarkNotes = getString("nearby_landmark_notes"),
    travelTips = getString("travel_tips"),
    imagePath = getString("image_path"),
    isFeatured = getBoolean("is_featured")
)
