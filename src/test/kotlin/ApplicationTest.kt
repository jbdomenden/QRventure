package app.QRventure

import app.QRventure.repositories.TourismRepository
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    private val repository = object : TourismRepository {
        override suspend fun attractions() = TestTourismData.attractions
        override suspend fun dining() = TestTourismData.dining
        override suspend fun services() = TestTourismData.services
        override suspend fun tourRoutes() = TestTourismData.routes
    }

    @Test
    fun testRootServesHomepage() = testApplication {
        application { module(repositoryOverride = repository, enableSelfPing = false) }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testFeaturedEndpointReturnsPublicContent() = testApplication {
        application { module(repositoryOverride = repository, enableSelfPing = false) }

        val response = client.get("/api/featured")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Fort Santiago"))
    }

    @Test
    fun testSearchRejectsShortQueries() = testApplication {
        application { module(repositoryOverride = repository, enableSelfPing = false) }

        val response = client.get("/api/search?q=f")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
