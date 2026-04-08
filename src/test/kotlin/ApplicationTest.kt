package app.QRventure

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun testAdminLoginAndMeFlow() = testApplication {
        environment {
            config = MapApplicationConfig(
                "postgres.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                "postgres.user" to "sa",
                "postgres.password" to "",
                "adminAuth.username" to "admin",
                "adminAuth.passwordHash" to "uuwgBkpbBLDkauDW88ClZxZDarRvu9hsHzJUJCGI2VY=",
                "adminAuth.passwordSalt" to "7FEZnOhXPhFJr4JKFEtqQQ==",
                "adminAuth.iterations" to "120000"
            )
        }
        application { module() }

        val unauthorized = client.get("/api/admin/me")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val login = client.post("/api/admin/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin","password":"IntramurosAdmin!2026"}""")
        }
        assertEquals(HttpStatusCode.OK, login.status)

        val me = client.get("/api/admin/me")
        assertEquals(HttpStatusCode.OK, me.status)
        assertTrue(me.bodyAsText().contains("admin"))

        val logout = client.post("/api/admin/logout")
        assertEquals(HttpStatusCode.OK, logout.status)

        val unauthorizedAgain = client.get("/api/admin/me")
        assertEquals(HttpStatusCode.Unauthorized, unauthorizedAgain.status)
    }
}
