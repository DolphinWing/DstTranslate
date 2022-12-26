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
 * @param os os name
 */
class Ini(
    val workingDir: String = System.getProperty("user.dir"),
    private val os: String = System.getProperty("os.name") ?: "Linux",
) {
    private val homeConfigs: File
        get() {
            val s = File.separator
            val folder = if (isLinux) {
                File("${System.getProperty("user.home")}${s}.config${s}dst-translator")
            } else {
                File("${System.getProperty("user.home")}${s}AppData${s}Local${s}dst-translator")
            }
            try {
                if (!folder.exists()) folder.mkdirs()
                return folder // make sure it exists
            } catch (e: Exception) {
                println("no such folder")
            }
            return File(System.getProperty("user.home")) // it must exist
        }

    private val configFile: File
        get() = File(homeConfigs, "configs.ini")

    val isLinux: Boolean = os.startsWith("Linux") || os.startsWith("Ubuntu")

    /**
     * User workshop code folder
     */
    var dstWorkshopDir: String = ""
    var oniWorkshopDir: String = ""

    /**
     * Klei PO file source folder
     */
    var dstAssetsDir: String = ""
    var oniAssetsDir: String = ""

    /**
     * Replacement strings
     */
    var dstStringMap: String = ""

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
                "workshopDir" -> dstWorkshopDir = value
                "assetsDir" -> dstAssetsDir = value
                "stringMap" -> dstStringMap = value
                "workshopDir_oni" -> oniWorkshopDir = value
                "assetsDir_oni" -> oniAssetsDir = value
            }
        } else {
            println("invalid line: $line")
        }
    }

    /**
     * Save ini file
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun save() = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        builder.append("workshopDir=$dstWorkshopDir\n")
        builder.append("assetsDir=$dstAssetsDir\n")
        builder.append("stringMap=$dstStringMap\n")
        builder.append("workshopDir_oni=$oniWorkshopDir\n")
        builder.append("assetsDir_oni=$oniAssetsDir\n")
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

    private suspend fun updateMaps(srcFile: File) = withContext(Dispatchers.IO) {
        if (!srcFile.exists()) return@withContext
        val map = File(homeConfigs, "strings.xml")
        srcFile.copyTo(target = map, overwrite = true)
        dstStringMap = map.absolutePath
        save() // write configs
    }

    /**
     * Apply configs changed
     *
     * @param workingDir app working dir
     * @param assetsDir source Klei PO assets
     * @param stringMap refactor list
     */
    suspend fun apply(
        workingDir: String? = null,
        assetsDir: String? = null,
        stringMap: String? = null,
        workingDirOni: String? = null,
        assetsDirOni: String? = null,
    ) = withContext(Dispatchers.IO) {
        if (stringMap != null && stringMap != this@Ini.dstStringMap) {
            this@Ini.updateMaps(File(stringMap))
        }
        workingDir?.let { dir -> this@Ini.dstWorkshopDir = dir }
        assetsDir?.let { dir -> this@Ini.dstAssetsDir = dir }
        workingDirOni?.let { dir -> this@Ini.oniWorkshopDir = dir }
        assetsDirOni?.let { dir -> this@Ini.oniAssetsDir = dir }
        save()
    }
}
