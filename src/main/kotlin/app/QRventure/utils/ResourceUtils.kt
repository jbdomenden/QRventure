package app.QRventure.utils

object ResourceUtils {
    fun readResourceOrNull(path: String): String? =
        javaClass.classLoader.getResource(path)?.readText()
}
