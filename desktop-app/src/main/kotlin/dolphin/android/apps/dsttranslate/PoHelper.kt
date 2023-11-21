package dolphin.android.apps.dsttranslate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets

abstract class PoHelper {
    companion object {
        private const val DST_CHS_PO = "chinese_s.po"
        private const val DST_CHT_PO = "chinese_t.po"
        const val DST_PO = "dst_cht.po"

        private const val ONI_PO_TEMPLATE = "strings_template.pot"
        private const val ONI_CHS_PO = "strings_preinstalled_zh_klei.po"
        const val ONI_PO = "strings.po"
    }

    enum class Mode { DST, ONI }

    protected val replaceList = ArrayList<Pair<String, String>>()
    protected var replace3dot: String = ""
    private val replace6dot: String
        get() = "$replace3dot$replace3dot"
    protected var replaceLeftBracket: String = ""
    protected var replaceRightBracket: String = ""

    private val sourceMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from official simplified chinese po file
     */
    fun chs(key: String): WordEntry? = sourceMap[key]

    private val revisedMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from official traditional chinese po file
     */
    fun cht(key: String): WordEntry? = revisedMap[key]

    private val originMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from original map
     */
    fun dst(key: String): WordEntry? = originMap[key]

//    /**
//     * @return all word entry in original map
//     */
//    fun dstValues(): List<WordEntry> = originMap.map { entry -> entry.value }

    private val wordList = ArrayList<WordEntry>()

    /**
     * @return full word list entry
     */
    fun allValues(): List<WordEntry> = wordList

    /**
     * A debug log output implementation.
     *
     * @param message log message to standard output
     */
    protected abstract fun log(message: String)

    /**
     * Prepare the helper instance. Usually call this when init.
     */
    abstract fun prepare()

    protected fun loadFromReader(reader: BufferedReader): ArrayList<WordEntry> {
        val list = ArrayList<WordEntry>()
        try {
            // do reading, usually loop until end of file reading
            var line: String? = ""//reader.readLine()
            while (line != null) {
                val line1 = reader.readLine()
                if (!line1.startsWith("#")) {
                    // log("bypass $line1")
                    continue //bypass some invalid header
                }
                var line2 = reader.readLine()
                var line3 = reader.readLine()
                while (!line3.startsWith("msgid")) {
                    line2 = line2.dropLast(1) + line3.drop(1)
                    line3 = reader.readLine()
                }
                var line4 = reader.readLine()
                while (!line4.startsWith("msgstr")) {
                    line3 = line3.dropLast(1) + line4.drop(1)
                    line4 = reader.readLine()
                }
                line = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    line4 = line4.dropLast(1) + line.drop(1)
                    line = reader.readLine()
                }
                val entry = WordEntry.from(line1, line2, line3, line4)
                if (entry != null) {
                    list.add(entry)
                } else {
                    log("invalid input: $line1")
                }
            }
        } catch (e: Exception) {
            log("Exception: ${e.message}")
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                log("close: ${e.message}")
            }
        }
        return list
    }

    private fun writeEntryToFile(
        mode: Mode = Mode.DST,
        dst: File = getOutputFile(mode),
        list: ArrayList<WordEntry> = wordList
    ): Boolean {
        if (list.isEmpty()) return false // no list, don't write
        val writer: BufferedWriter?
        try { // http://stackoverflow.com/a/1053474
            writer = BufferedWriter(FileWriter(dst, StandardCharsets.UTF_8))
            var content = "\"Language: zh-tw\"\n\"POT Version: 2.0\"\n"
            if (mode == Mode.ONI) {
                content += "Application: Oxygen Not Included"
                content += "Last-Translator: DolphinWing"
                content += "MIME-Version: 1.0"
                content += "Content-Type: text/plain; charset=UTF-8"
            }
            writer.write(content, 0, content.length)
            list.forEach { entry ->
                content = "\n"
                content += "#. ${entry.key}\n"
                content += "msgctxt ${entry.text}\n"
                content += "msgid ${entry.id}\n"
                content += "msgstr ${entry.str}\n"
                writer.write(content, 0, content.length)
            }
            writer.close()
            // writer = null
        } catch (e: Exception) {
            // e.printStackTrace()
            log("writeStringToFile: ${e.message}")
            return false
        }
        log("write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }

    /**
     * Implementation of loading asset file to memory
     *
     * @param name asset name
     * @param mode app mode
     * @return word entry list
     */
    abstract fun loadAssetFile(name: String, mode: Mode = Mode.DST): ArrayList<WordEntry>

//    fun runTranslation(postAction: ((timeCost: Long) -> Unit)? = null) {
//        val cost = runBlocking { runTranslationProcess() }
//        postAction?.let { action -> context.runOnUiThread { action(cost) } }
//    }

    private val processStatus = MutableStateFlow("")

    /**
     * Process status
     */
    val status: StateFlow<String> = processStatus

    /**
     * Loading status. True means the app is processing data.
     */
    val loading = MutableStateFlow(true)

    /**
     * Load chs and cht translation file to app.
     *
     * @return total process time
     */
    suspend fun runTranslationProcess(mode: Mode = Mode.DST): Long = withContext(Dispatchers.IO) {
        // log("run translation")
        loading.emit(true)
        val start = System.currentTimeMillis()

        var chsPoFile = DST_CHS_PO
        if (mode == Mode.ONI) {
            chsPoFile = ONI_CHS_PO
        }
        processStatus.emit("load $chsPoFile")
        val s = loadAssetFile(chsPoFile, mode)
        sourceMap.clear()
        s.forEach { entry ->
            // entry.str = sc2tc(entry.str).trim() // translate to traditional
            sourceMap[entry.key] = entry
        }
        val stop1 = System.currentTimeMillis()
        log("original SC size: ${s.size} (${stop1 - start} ms)")

        var chtPoFile = DST_CHT_PO
        if (mode == Mode.ONI) {
            chtPoFile = ONI_PO_TEMPLATE
        }
        processStatus.emit("load $chtPoFile")
        val t = loadAssetFile(chtPoFile, mode)
        revisedMap.clear()
        t.forEach { entry ->
            revisedMap[entry.key] = entry
        }
        val stop2 = System.currentTimeMillis()
        log("original TC size: ${t.size} (${stop2 - start} ms)")

        var outputPoFile = DST_PO
        if (mode == Mode.ONI) {
            outputPoFile = ONI_PO
        }
        processStatus.emit("load $outputPoFile")
        originMap.clear()
        loadAssetFile(outputPoFile, mode).filter { entry ->
            entry.id != "\"\"" && entry.str != "\"\""
        }.forEach { entry ->
            originMap[entry.key] = entry
        }
        val stop3 = System.currentTimeMillis()
        log("previous data size: ${originMap.size} (${stop3 - stop1} ms)")

        processStatus.emit("prepare word list")
        wordList.clear()
        if (mode == Mode.DST) {
            s.forEachIndexed { index, entry ->
                var newly = false
                var str = ""
                if (originMap.containsKey(entry.key)) {
                    val str1 = originMap[entry.key]?.str ?: ""
                    if (str1.isNotEmpty()) str = str1.trim()
                } else {
                    processStatus.emit("${entry.key} (${index + 1}/${s.size})")
                }
                if (str.isEmpty()) { // not in the translated po
                    newly = true
                    str = sc2tc(entry.str).trim()
                }
                str = refactor(str, mode)
                addToTodoList(WordEntry(entry.key, entry.text, entry.id, str, newly))
            }
        } else {
            t.forEachIndexed { index, entry ->
                var newly = false
                var str = ""
                if (originMap.containsKey(entry.key)) {
                    val str1 = originMap[entry.key]?.str ?: ""
                    if (str1.isNotEmpty()) str = str1.trim()
                } else {
                    processStatus.emit("${entry.key} (${index + 1}/${s.size})")
                }
                if (str.isEmpty()) { // not in the translated po
                    newly = true
                    if (sourceMap.containsKey(entry.key)) {
                        str = sourceMap[entry.key]?.str ?: ""
                    }
                    if (str.isEmpty())
                        str = entry.origin()
                    str = sc2tc(str)
                }
                str = refactor(str, mode)
                val id = revisedMap[entry.key]?.id ?: entry.id
                addToTodoList(WordEntry(entry.key, entry.text, id, str, newly))
            }
        }

        val stop4 = System.currentTimeMillis()
        log("new list size: ${wordList.size} (${stop4 - stop3} ms)")

        writeEntryToFile(mode, getCachedFile(mode), wordList) // runTranslationProcess
        val cost = System.currentTimeMillis() - start
        log("write data done. $cost ms")
        processStatus.emit("")
        loading.emit(false) // complete
        return@withContext cost
    }

    /**
     * Add an entry to the database
     *
     * @param entry new word
     */
    fun addToTodoList(entry: WordEntry) {
        wordList.add(entry)
    }

    /**
     * Write all word entries to a file
     *
     * @param dst destination file
     * @param list word entry list
     * @return true if file written is success
     */
    suspend fun writeTranslationFile(
        mode: Mode = Mode.DST,
        dst: File = getOutputFile(mode),
        list: ArrayList<WordEntry> = wordList
    ): Boolean = withContext(Dispatchers.IO) {
        loading.emit(true)
        val start = System.currentTimeMillis()
        val result = writeEntryToFile(mode, dst, list) // writeTranslationFile
        val cost = System.currentTimeMillis() - start
        log("write data done. $cost ms")
        loading.emit(false) // complete
        return@withContext result
    }

    /**
     * @return actual output file
     */
    abstract fun getOutputFile(mode: Mode = Mode.DST): File

    /**
     * @return cache file
     */
    abstract fun getCachedFile(mode: Mode = Mode.DST): File

    /**
     * Convert simplified chinese to traditional chinese
     *
     * @param str simplified chinese text
     * @return traditional chinese text
     */
    abstract fun sc2tc(str: String): String

    private fun refactor(src: String, mode: Mode = Mode.DST): String {
        var str = src;
        if (mode == Mode.DST) {
            str = str.replace("...", replace3dot)
            str = if (str != "\"$replace6dot\"") str.replace(replace6dot, replace3dot) else str
        }
        replaceList.forEach { (o, n) ->
            str = str.replace(o, n)
        }
        if (mode == Mode.DST) {
            if (str.contains("\\\"")) {
                var i = 0
                val str1 = str.replace("\\\"", "%@%").replace("%@%".toRegex()) {
                    if (i++ % 2 == 0) replaceLeftBracket else replaceRightBracket
                }
                if (i % 2 == 0) {
                    str = str1 // only replace the paired string
                }
            }
        }
        return str
    }

    /**
     * Build a list with changed entries
     *
     * @return word list with change items
     */
    fun buildChangeList(): List<WordEntry> = wordList.filter { entry ->
        val dst = dst(entry.key)
        val chs = chs(entry.key)
        (entry.newly || // new entry
                dst?.id != entry.id || // english text changed
                dst.str != entry.str || // translation changed
                entry.changed > 0 || // entry itself changed by editor
                revisedMap[entry.key]?.id != dst.id || // english template changed
                chs?.id != dst.id) // source english text changed
                && entry.str.length > 2 // no translation
                && !entry.string().startsWith("only_used_by")
    }

    /**
     * Update text of specific word entry
     *
     * @param key entry key
     * @param value entry text
     */
    fun update(key: String, value: String) {
        wordList.find { entry -> entry.key == key }?.apply {
            str = value
            // println("set new $key to $str")
            changed = System.currentTimeMillis() // set new change time
        }
    }
}
