package app.QRventure

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRootServesHomepage() = testApplication {
        environment {
            config = MapApplicationConfig(
                "postgres.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                "postgres.user" to "sa",
                "postgres.password" to ""
            )
        }
        application { module() }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
