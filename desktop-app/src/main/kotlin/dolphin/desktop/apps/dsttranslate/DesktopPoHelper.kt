package dolphin.desktop.apps.dsttranslate

import com.github.houbb.opencc4j.util.ZhTwConverterUtil
import dolphin.android.apps.dsttranslate.PoHelper
import dolphin.android.apps.dsttranslate.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory

class DesktopPoHelper(val ini: Ini = Ini()) : PoHelper() {
    override fun log(message: String) {
        println(message)
    }

    override fun prepare() {
        // load replacement from xml
    }

    private val replaceMap = HashMap<String, String>()

    @Suppress("unused")
    private fun loadXmlByDom(xml: File) {
        try {
            // See https://stackoverflow.com/a/7373596
            val dbf = DocumentBuilderFactory.newInstance()
            val dom = dbf.newDocumentBuilder().parse(xml)

            dom?.getElementsByTagName("string-array")?.let { list ->
                (list.item(0) as? Element)?.let { replacementList ->
                    replacementList.getElementsByTagName("item").items()
                        .forEachIndexed { index, node ->
                            replaceMap["entry-$index"] = node.content()
                        }
                    // println("  found ${items.length} entries")
                }
            }
            dom?.getElementsByTagName("string")?.items()?.forEach { node ->
                val key = node.attribute("name")
                // println("{node.attribute("name")}: ${node.content()}")
                if (key.startsWith("replacement")) {
                    replaceMap[key] = node.content()
                }
            }
        } catch (e: Exception) {
            println("replacement: ${e.message}")
        }
    }

    /**
     * https://mkyong.com/java/how-to-read-xml-file-in-java-sax-parser/
     */
    private fun loadXmlBySax(xml: File) {
        val factory = SAXParserFactory.newInstance()
        try {
            factory.newSAXParser().parse(xml, SaxDocumentHandler(replaceMap))
        } catch (e: Exception) {
            println("SAXParser: ${e.message}")
        }
    }

    private class SaxDocumentHandler(private val map: HashMap<String, String>) : DefaultHandler() {
        var tag: String = ""
        var name: String? = null

        override fun startElement(
            uri: String?,
            localName: String?,
            qName: String?,
            attributes: Attributes?
        ) {
            // println("startElement: $uri: $localName: $qName")
            qName?.let { tag = it }
            attributes?.let { a ->
                repeat(a.length) { index ->
                    // println("${a.getQName(index)}: ${a.getValue(index)}")
                    if (a.getQName(index) == "name") name = a.getValue(index)
                }
            } ?: kotlin.run { name = null }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (tag.isNotEmpty() && ch != null) {
                val content = ch.joinToString("").substring(start, start + length)
                // println("<$tag name='${name ?: ""}'>$content</$tag>")
                putMap(name ?: "", content, start)
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            // println("endElement: $uri: $localName: $qName")
            if (tag == qName) tag = ""
        }

        private fun putMap(name: String, value: String, start: Int) {
            if (tag == "string" && name.startsWith("replacement")) {
                map[name] = value.trim()
            }
            if (tag == "item" && name == "replacement_list") {
                map["entry-$start"] = value
            }
        }
    }

    private fun findReplacementXml(): File {
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

    suspend fun loadXml() = withContext(Dispatchers.IO) {
        // loadXmlByDom(findReplaceXml()) // load from replacement xml
        loadXmlBySax(findReplacementXml())
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
            // Read UTF-8 https://stackoverflow.com/a/14918597
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
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

fun NodeList.items(): List<Node> {
    val list = java.util.ArrayList<Node>()
    repeat(this.length) { list.add(item(it)) }
    return list
}

fun Node.content(): String = (this as? Element)?.textContent?.dropWhile { it == ' ' }?.trim() ?: ""
fun Node.attribute(key: String): String = (this as? Element)?.getAttribute(key) ?: ""
