package app.QRventure.repositories

import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import app.QRventure.models.TourRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

class JdbcTourismRepository(
    private val dataSource: DataSource
) : TourismRepository, AutoCloseable {

    override suspend fun attractions(): List<Attraction> = queryList(
        """
        SELECT * FROM attractions
        WHERE LOWER(status) = 'open'
        ORDER BY sort_order ASC, is_featured DESC, name ASC
        """.trimIndent()
    ) { it.toAttraction() }

    override suspend fun dining(): List<DiningPlace> = queryList(
        """
        SELECT * FROM dining_places
        WHERE LOWER(status) = 'open'
        ORDER BY sort_order ASC, is_featured DESC, name ASC
        """.trimIndent()
    ) { it.toDining() }

    override suspend fun services(): List<LocalService> = queryList(
        """
        SELECT * FROM local_services
        WHERE LOWER(status) = 'open'
        ORDER BY sort_order ASC, name ASC
        """.trimIndent()
    ) { it.toService() }

    override suspend fun tourRoutes(): List<TourRoute> = queryList(
        """
        SELECT * FROM tour_routes
        WHERE LOWER(status) = 'open'
        ORDER BY sort_order ASC, is_featured DESC, name ASC
        """.trimIndent()
    ) { it.toTourRoute() }

    override fun close() {
        if (dataSource is AutoCloseable) {
            dataSource.close()
        }
    }

    private suspend fun <T> queryList(
        sql: String,
        binder: PreparedStatement.() -> Unit = {},
        mapper: (ResultSet) -> T
    ): List<T> = withContext(Dispatchers.IO) {
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.binder()
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(mapper(resultSet))
                        }
                    }
                }
            }
        }
    }
}

private fun ResultSet.toAttraction() = Attraction(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    alternateNames = parseJsonStringArray(getOptionalString("alternate_names")),
    shortDescription = getString("short_description"),
    fullDescription = getString("full_description"),
    category = getString("category"),
    historicalPeriod = getString("historical_period"),
    locationText = getString("location_text"),
    openingHours = getString("opening_hours"),
    entranceFee = getString("entrance_fee"),
    contactDetails = getString("contact_details"),
    visitorTips = getString("visitor_tips"),
    bestTimeToVisit = getString("best_time_to_visit"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    imageUrl = primaryImageUrl(getOptionalString("image_urls"), getOptionalString("image_path")),
    imagePath = getOptionalString("image_path").orEmpty(),
    imageUrls = parseImageUrls(getOptionalString("image_urls"), getOptionalString("image_path")),
    isFeatured = getBoolean("is_featured"),
    status = getString("status"),
    sortOrder = getInt("sort_order")
)

private fun ResultSet.toDining() = DiningPlace(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    shortDescription = getString("short_description"),
    fullDescription = getString("full_description"),
    diningType = getString("dining_type"),
    cuisine = getString("cuisine"),
    locationText = getString("location_text"),
    openingHours = getString("opening_hours"),
    priceRange = getString("price_range"),
    contactDetails = getString("contact_details"),
    visitorNotes = getString("visitor_notes"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    imageUrl = primaryImageUrl(getOptionalString("image_urls"), getOptionalString("image_path")),
    imagePath = getOptionalString("image_path").orEmpty(),
    imageUrls = parseImageUrls(getOptionalString("image_urls"), getOptionalString("image_path")),
    isFeatured = getBoolean("is_featured"),
    status = getString("status"),
    sortOrder = getInt("sort_order")
)

private fun ResultSet.toService() = LocalService(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    shortDescription = getString("short_description"),
    fullDescription = getString("full_description"),
    serviceType = getString("service_type"),
    locationText = getString("location_text"),
    hours = getString("hours"),
    contactDetails = getString("contact_details"),
    visitorNotes = getString("visitor_notes"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    imageUrl = primaryImageUrl(getOptionalString("image_urls"), getOptionalString("image_path")),
    imagePath = getOptionalString("image_path").orEmpty(),
    imageUrls = parseImageUrls(getOptionalString("image_urls"), getOptionalString("image_path")),
    status = getString("status"),
    sortOrder = getInt("sort_order")
)

private fun ResultSet.toTourRoute() = TourRoute(
    id = getInt("id"),
    slug = getString("slug"),
    name = getString("name"),
    shortDescription = getString("short_description"),
    fullDescription = getString("full_description"),
    routeType = getString("route_type"),
    startingPoint = getString("starting_point"),
    estimatedDuration = getString("estimated_duration"),
    travelTips = getString("travel_tips"),
    distanceText = getString("distance_text"),
    mapLink = getString("map_link"),
    imageUrl = primaryImageUrl(getOptionalString("image_urls"), getOptionalString("image_url")),
    imageUrls = parseImageUrls(getOptionalString("image_urls"), getOptionalString("image_url")),
    isFeatured = getBoolean("is_featured"),
    status = getString("status"),
    sortOrder = getInt("sort_order")
)

private fun ResultSet.getOptionalString(columnName: String): String? {
    return try {
        getString(columnName)
    } catch (_: SQLException) {
        null
    }
}

private fun parseImageUrls(raw: String?, fallback: String?): List<String> {
    val parsed = parseJsonStringArray(raw).filter { it.startsWith("http://") || it.startsWith("https://") }
    if (parsed.isNotEmpty()) {
        return parsed.distinct()
    }

    val fallbackValue = fallback?.trim().orEmpty()
    return if (fallbackValue.startsWith("http://") || fallbackValue.startsWith("https://")) {
        listOf(fallbackValue)
    } else {
        emptyList()
    }
}

private fun primaryImageUrl(raw: String?, fallback: String?): String {
    return parseImageUrls(raw, fallback).firstOrNull().orEmpty()
}

private fun parseJsonStringArray(raw: String?): List<String> {
    val source = raw?.trim().orEmpty()
    if (source.isBlank()) {
        return emptyList()
    }

    return try {
        Json.parseToJsonElement(source).jsonArray.mapNotNull { node ->
            node.jsonPrimitive.content.trim().takeIf { it.isNotBlank() }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
