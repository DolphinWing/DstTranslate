package res

import java.util.Locale
import java.util.ResourceBundle

/**
 * https://lokalise.com/blog/java-internationalization-learn-the-basics/
 */
object Resources {
    var locale: Locale = Locale.getDefault()
}

/**
 * Load a string resource.
 *
 * @param id the resource identifier
 * @param locale language
 * @return string resource
 */
fun stringResource(id: String, locale: Locale = Resources.locale): String {
    // val locale = Locale("zh", "TW") // Locale.getDefault()
    val resourceBundle: ResourceBundle = ResourceBundle.getBundle("res.Bundle", locale)
    return try {
        resourceBundle.getString(id)
    } catch (e: Exception) {
        id
    }
}
