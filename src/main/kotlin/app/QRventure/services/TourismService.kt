package app.QRventure.services

import app.QRventure.dto.*
import app.QRventure.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.sql.Connection
import java.sql.ResultSet

class TourismService(private val connection: Connection) {

    fun featured(): FeaturedResponse = FeaturedResponse(
        attractions = attractions().filter { it.isFeatured }.take(6),
        dining = dining().filter { it.isFeatured }.take(6),
        services = services().take(6),
        routes = tourRoutes().filter { it.isFeatured }.take(6)
    )

    fun search(query: String): SearchResponse {
        val q = query.trim().lowercase()
        return SearchResponse(
            query = query.trim(),
            attractions = attractions(search = q),
            dining = dining(search = q),
            services = services(search = q),
            routes = tourRoutes(search = q)
        )
    }

    fun attractions(search: String? = null, category: String? = null): List<Attraction> {
        val sql = buildString {
            append("SELECT * FROM attractions WHERE LOWER(status) = 'open'")
            if (!search.isNullOrBlank()) append(" AND (LOWER(name) LIKE ? OR LOWER(short_description) LIKE ? OR LOWER(full_description) LIKE ?)")
            if (!category.isNullOrBlank()) append(" AND LOWER(category) = ?")
            append(" ORDER BY sort_order ASC, is_featured DESC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            var idx = 1
            if (!search.isNullOrBlank()) repeat(3) { ps.setString(idx++, "%${search.lowercase()}%") }
            if (!category.isNullOrBlank()) ps.setString(idx, category.lowercase())
            ps.executeQuery().toList { it.toAttraction() }
        }
    }

    fun attractionBySlugOrId(key: String): Attraction? = connection.lookupByKey("attractions", key) { it.toAttraction() }

    fun dining(type: String? = null, search: String? = null): List<DiningPlace> {
        val sql = buildString {
            append("SELECT * FROM dining_places WHERE LOWER(status) = 'open'")
            if (!type.isNullOrBlank()) append(" AND LOWER(dining_type) LIKE ?")
            if (!search.isNullOrBlank()) append(" AND (LOWER(name) LIKE ? OR LOWER(short_description) LIKE ? OR LOWER(full_description) LIKE ? OR LOWER(cuisine) LIKE ?)")
            append(" ORDER BY sort_order ASC, is_featured DESC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            var i = 1
            if (!type.isNullOrBlank()) ps.setString(i++, "%${type.lowercase()}%")
            if (!search.isNullOrBlank()) repeat(4) { ps.setString(i++, "%${search.lowercase()}%") }
            ps.executeQuery().toList { it.toDining() }
        }
    }

    fun diningBySlugOrId(key: String): DiningPlace? = connection.lookupByKey("dining_places", key) { it.toDining() }

    fun services(type: String? = null, search: String? = null): List<LocalService> {
        val sql = buildString {
            append("SELECT * FROM local_services WHERE LOWER(status) = 'open'")
            if (!type.isNullOrBlank()) append(" AND LOWER(service_type) LIKE ?")
            if (!search.isNullOrBlank()) append(" AND (LOWER(name) LIKE ? OR LOWER(short_description) LIKE ? OR LOWER(full_description) LIKE ? OR LOWER(visitor_notes) LIKE ?)")
            append(" ORDER BY sort_order ASC, name ASC")
        }
        return connection.prepareStatement(sql).use { ps ->
            var i = 1
            if (!type.isNullOrBlank()) ps.setString(i++, "%${type.lowercase()}%")
            if (!search.isNullOrBlank()) repeat(4) { ps.setString(i++, "%${search.lowercase()}%") }
            ps.executeQuery().toList { it.toService() }
        }
    }

    fun serviceBySlugOrId(key: String): LocalService? = connection.lookupByKey("local_services", key) { it.toService() }

    fun tourRoutes(search: String? = null): List<TourRoute> {
        val sql = if (search.isNullOrBlank()) {
            "SELECT * FROM tour_routes WHERE LOWER(status) = 'open' ORDER BY sort_order ASC, is_featured DESC, name ASC"
        } else {
            "SELECT * FROM tour_routes WHERE LOWER(status) = 'open' AND (LOWER(name) LIKE ? OR LOWER(short_description) LIKE ? OR LOWER(full_description) LIKE ?) ORDER BY sort_order ASC, is_featured DESC, name ASC"
        }
        return connection.prepareStatement(sql).use { ps ->
            if (!search.isNullOrBlank()) {
                ps.setString(1, "%${search.lowercase()}%")
                ps.setString(2, "%${search.lowercase()}%")
                ps.setString(3, "%${search.lowercase()}%")
            }
            ps.executeQuery().toList { it.toTourRoute() }
        }
    }

    fun tourRouteBySlugOrId(key: String): TourRoute? = connection.lookupByKey("tour_routes", key) { it.toTourRoute() }

}

private fun <T> ResultSet.toList(mapper: (ResultSet) -> T): List<T> {
    val out = mutableListOf<T>()
    while (next()) out += mapper(this)
    return out
}

private fun <T> Connection.lookupByKey(table: String, key: String, mapper: (ResultSet) -> T): T? {
    val byId = key.toIntOrNull()
    val sql = if (byId != null) "SELECT * FROM $table WHERE id = ?" else "SELECT * FROM $table WHERE slug = ?"
    return prepareStatement(sql).use { ps ->
        if (byId != null) ps.setInt(1, byId) else ps.setString(1, key)
        val rs = ps.executeQuery()
        if (rs.next()) mapper(rs) else null
    }
}

private fun ResultSet.toAttraction() = Attraction(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), shortDescription = getString("short_description"),
    fullDescription = getString("full_description"), category = getString("category"), historicalPeriod = getString("historical_period"),
    locationText = getString("location_text"), openingHours = getString("opening_hours"), entranceFee = getString("entrance_fee"),
    contactDetails = getString("contact_details"), visitorTips = getString("visitor_tips"), bestTimeToVisit = getString("best_time_to_visit"),
    latitude = getDouble("latitude"), longitude = getDouble("longitude"),
    imageUrl = primaryImageUrl(getString("image_urls"), getString("image_path")),
    imagePath = getString("image_path"),
    imageUrls = parseImageUrls(getString("image_urls"), getString("image_path")),
    isFeatured = getBoolean("is_featured"), status = getString("status"), sortOrder = getInt("sort_order")
)

private fun ResultSet.toDining() = DiningPlace(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), shortDescription = getString("short_description"),
    fullDescription = getString("full_description"), diningType = getString("dining_type"), cuisine = getString("cuisine"),
    locationText = getString("location_text"), openingHours = getString("opening_hours"), priceRange = getString("price_range"),
    contactDetails = getString("contact_details"), visitorNotes = getString("visitor_notes"), latitude = getDouble("latitude"),
    longitude = getDouble("longitude"), imageUrl = primaryImageUrl(getString("image_urls"), getString("image_path")), imagePath = getString("image_path"),
    imageUrls = parseImageUrls(getString("image_urls"), getString("image_path")), isFeatured = getBoolean("is_featured"),
    status = getString("status"), sortOrder = getInt("sort_order")
)

private fun ResultSet.toService() = LocalService(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), shortDescription = getString("short_description"),
    fullDescription = getString("full_description"), serviceType = getString("service_type"), locationText = getString("location_text"),
    hours = getString("hours"), contactDetails = getString("contact_details"), visitorNotes = getString("visitor_notes"),
    latitude = getDouble("latitude"), longitude = getDouble("longitude"),
    imageUrl = primaryImageUrl(getString("image_urls"), getString("image_path")), imagePath = getString("image_path"),
    imageUrls = parseImageUrls(getString("image_urls"), getString("image_path")),
    status = getString("status"), sortOrder = getInt("sort_order")
)

private fun ResultSet.toTourRoute() = TourRoute(
    id = getInt("id"), slug = getString("slug"), name = getString("name"), shortDescription = getString("short_description"),
    fullDescription = getString("full_description"), routeType = getString("route_type"), startingPoint = getString("starting_point"),
    estimatedDuration = getString("estimated_duration"), travelTips = getString("travel_tips"), distanceText = getString("distance_text"),
    mapLink = getString("map_link"),
    imageUrl = primaryImageUrl(getString("image_urls"), getString("image_url")),
    imageUrls = parseImageUrls(getString("image_urls"), getString("image_url")),
    isFeatured = getBoolean("is_featured"), status = getString("status"), sortOrder = getInt("sort_order")
)

private fun parseImageUrls(raw: String?, fallbackImagePath: String?): List<String> {
    val fromJson = try {
        val source = raw?.trim().orEmpty()
        if (source.isBlank()) emptyList()
        else Json.parseToJsonElement(source).jsonArray.mapNotNull { node ->
            node.jsonPrimitive.content.trim().takeIf { it.startsWith("http://") || it.startsWith("https://") }
        }
    } catch (_: Exception) {
        emptyList()
    }
    if (fromJson.isNotEmpty()) return fromJson.distinct()
    val fallback = fallbackImagePath?.trim().orEmpty()
    return if (fallback.startsWith("http://") || fallback.startsWith("https://")) listOf(fallback) else emptyList()
}

private fun primaryImageUrl(raw: String?, fallbackImagePath: String?): String {
    return parseImageUrls(raw, fallbackImagePath).firstOrNull() ?: ""
}
