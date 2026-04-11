package app.QRventure.dto

private val slugRegex = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")
private val allowedStatus = setOf("open", "closed", "maintenance")

fun validateAttraction(dto: AttractionUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("shortDescription", dto.shortDescription, 20)?.let { return it }
    validateRequired("fullDescription", dto.fullDescription, 40)?.let { return it }
    validateRequired("category", dto.category)?.let { return it }
    validateRequired("historicalPeriod", dto.historicalPeriod)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("openingHours", dto.openingHours)?.let { return it }
    validateRequired("entranceFee", dto.entranceFee)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateRequired("visitorTips", dto.visitorTips, 10)?.let { return it }
    validateRequired("bestTimeToVisit", dto.bestTimeToVisit)?.let { return it }
    validateSortOrder(dto.sortOrder)?.let { return it }
    validateStatus(dto.status)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    return null
}

fun validateDining(dto: DiningUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("shortDescription", dto.shortDescription, 20)?.let { return it }
    validateRequired("fullDescription", dto.fullDescription, 40)?.let { return it }
    validateRequired("diningType", dto.diningType)?.let { return it }
    validateRequired("cuisine", dto.cuisine)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("openingHours", dto.openingHours)?.let { return it }
    validateRequired("priceRange", dto.priceRange)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateRequired("visitorNotes", dto.visitorNotes, 10)?.let { return it }
    validateSortOrder(dto.sortOrder)?.let { return it }
    validateStatus(dto.status)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    return null
}

fun validateService(dto: ServiceUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("shortDescription", dto.shortDescription, 20)?.let { return it }
    validateRequired("fullDescription", dto.fullDescription, 30)?.let { return it }
    validateRequired("serviceType", dto.serviceType)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("hours", dto.hours)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateRequired("visitorNotes", dto.visitorNotes, 8)?.let { return it }
    validateSortOrder(dto.sortOrder)?.let { return it }
    validateStatus(dto.status)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    return null
}

fun validateTourRoute(dto: TourRouteUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("shortDescription", dto.shortDescription, 20)?.let { return it }
    validateRequired("fullDescription", dto.fullDescription, 30)?.let { return it }
    validateRequired("routeType", dto.routeType)?.let { return it }
    validateRequired("startingPoint", dto.startingPoint)?.let { return it }
    validateRequired("estimatedDuration", dto.estimatedDuration)?.let { return it }
    validateRequired("travelTips", dto.travelTips, 10)?.let { return it }
    validateRequired("distanceText", dto.distanceText)?.let { return it }
    validateRequired("mapLink", dto.mapLink)?.let { return it }
    validateSortOrder(dto.sortOrder)?.let { return it }
    validateStatus(dto.status)?.let { return it }
    return null
}

private fun validateSlug(slug: String): String? =
    if (!slugRegex.matches(slug)) "slug must be lowercase alphanumeric words separated by hyphens." else null

private fun validateRequired(field: String, value: String, minLen: Int = 1): String? =
    if (value.trim().length < minLen) "$field is required${if (minLen > 1) " (min $minLen chars)" else ""}." else null

private fun validateCoordinates(latitude: Double, longitude: Double): String? {
    if (latitude !in -90.0..90.0) return "latitude must be between -90 and 90."
    if (longitude !in -180.0..180.0) return "longitude must be between -180 and 180."
    return null
}

private fun validateImagePath(path: String): String? =
    if (!path.startsWith("/qrventure/uploads/") && !path.startsWith("/qrventure/images/")) {
        "imagePath must point to /qrventure/uploads/ or /qrventure/images/."
    } else null

private fun validateSortOrder(sortOrder: Int): String? = if (sortOrder < 0) "sortOrder must be zero or greater." else null
private fun validateStatus(status: String): String? = if (status.lowercase() !in allowedStatus) "status must be one of: open, closed, maintenance." else null
