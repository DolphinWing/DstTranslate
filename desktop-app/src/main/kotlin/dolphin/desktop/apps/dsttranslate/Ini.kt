package dolphin.desktop.apps.dsttranslate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader

/**
 * App config ini handler
 *
 * @property workingDir app working dir
 */
class Ini(val workingDir: String = System.getProperty("user.dir")) {
    private val configFile: File
        get() = File(workingDir, "configs.ini")

    /**
     * User workshop code folder
     */
    var workshopDir: String = ""

    /**
     * Klei PO file source folder
     */
    var assetsDir: String = ""

    /**
     * Load ini file
     */
    suspend fun load() = withContext(Dispatchers.IO) {
        if (!configFile.exists()) {
            println("load ${configFile.absolutePath} failed")
            // try to copy one from resource
            if (huntForReleaseConfig().exists()) {
                huntForReleaseConfig().copyTo(configFile)
            } else if (huntForDebugConfig().exists()) {
                huntForDebugConfig().copyTo(configFile)
            }
            return@withContext
        }
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8"))
            try {
                // do reading, usually loop until end of file reading
                var line: String? = reader.readLine()
                while (line != null) {
                    parseIni(line)
                    line = reader.readLine()
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
            reader.close()
        } catch (e: Exception) {
            println("close: ${e.message}")
        }
    }

    private fun huntForReleaseConfig(): File {
        val s = File.separator
        return File("${workingDir}${s}app${s}resources${s}${configFile.name}")
    }

    private fun huntForDebugConfig(): File {
        val s = File.separator
        return File("${workingDir}${s}resources${s}common${s}${configFile.name}")
    }

    private fun parseIni(line: String) {
        if (line.contains("=")) {
            val data = line.split("=")
            val value = if (data.size > 1) data[1] else ""
            when (data[0]) {
                "workshopDir" -> workshopDir = value
                "assetsDir" -> assetsDir = value
            }
        } else {
            println("invalid line: $line")
        }
    }

    /**
     * Save ini file
     */
    suspend fun save() = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        builder.append("workshopDir=$workshopDir\n")
        builder.append("assetsDir=$assetsDir\n")
        val content = builder.toString()
        try { // http://stackoverflow.com/a/1053474
            val writer = BufferedWriter(FileWriter(configFile))
            writer.write(content, 0, content.length)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            println("writeStringToFile: " + e.message)
        }
    }
}
