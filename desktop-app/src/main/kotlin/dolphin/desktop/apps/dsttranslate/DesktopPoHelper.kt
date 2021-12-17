package dolphin.desktop.apps.dsttranslate

import com.github.houbb.opencc4j.util.ZhTwConverterUtil
import dolphin.android.apps.dsttranslate.PoHelper
import dolphin.android.apps.dsttranslate.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory

class DesktopPoHelper(val ini: Ini = Ini()) : PoHelper() {
    override fun log(message: String) {
        println(message)
    }

    override fun prepare() {
        // load replacement from xml
    }

    private val replaceMap = HashMap<String, String>()

    private fun loadXml(xml: File) {
        try {
            // See https://stackoverflow.com/a/7373596
            val dbf = DocumentBuilderFactory.newInstance()
            val dom = dbf.newDocumentBuilder().parse(xml)

            dom?.getElementsByTagName("string-array")?.let { list ->
                (list.item(0) as? Element)?.let { replacementList ->
                    val items = replacementList.getElementsByTagName("item")
                    repeat(items.length) { index ->
                        (items.item(index) as? Element)?.let { item ->
                            val line = item.textContent.dropWhile { it == ' ' }.trim()
                            // println("found $line")
                            replaceMap["entry-$index"] = line
                        }
                    }
                    // println("  found ${items.length} entries")
                }
            }
            dom?.getElementsByTagName("string")?.let { list ->
                repeat(list.length) { index ->
                    (list.item(index) as? Element)?.let { item ->
                        val key = item.getAttribute("name")
                        // println("$key: ${item.textContent}")
                        if (key.startsWith("replacement")) {
                            replaceMap[key] = item.textContent.trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("replacement: ${e.message}")
        }
    }

    private fun findReplaceXml(): File {
        // locate debug build
        if (File(ini.workingDir, "resources").exists()) {
            val file = File("${ini.workingDir}/resources/common/strings.xml")
            if (file.exists()) return file
        }
        // locate release build
        if (File(ini.workingDir, "app").exists()) {
            val file = File("${ini.workingDir}/app/resources/strings.xml")
            if (file.exists()) return file
        }
        // locate in workshop dir
        return File(ini.workshopDir, "strings.xml")
    }

    suspend fun setupXml() = withContext(Dispatchers.IO) {
        loadXml(findReplaceXml()) // load from replacement xml
        replaceMap.filter { entry -> entry.key.startsWith("entry-") }
            .map { entry -> entry.value }
            .forEach { entry -> replaceList.add(entry) }
        println("replace list: ${replaceList.size}")
        replace3dot = replaceMap["replacement_3dot"] ?: ""
        replaceLeftBracket = replaceMap["replacement_left_bracket"] ?: ""
        replaceRightBracket = replaceMap["replacement_right_bracket"] ?: ""
    }

    override fun loadAssetFile(name: String, line2Enabled: Boolean): ArrayList<WordEntry> {
        if (name == "dst_cht.po") return loadFile(ini.workshopDir, name)
        return loadFile(ini.assetsDir, name)
    }

    private fun loadFile(dir: String, name: String): ArrayList<WordEntry> {
        log("load asset: $dir/$name")
        val file = File(dir, name)
        val list: ArrayList<WordEntry> = if (file.exists()) try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            loadFromReader(reader, true)
        } catch (e: Exception) {
            ArrayList()
        } else {
            ArrayList()
        }
        log("asset: done with $name (${list.size})")
        return list
    }

    override fun getOutputFile(): File = File(ini.workshopDir, "dst_cht.po")

    override fun getCachedFile(): File = File(System.getProperty("java.io.tmpdir"), "dst_cht.po")

    override fun sc2tc(str: String): String {
        return ZhTwConverterUtil.toTraditional(str)
    }
}