package dolphin.android.apps.dsttranslate

import android.app.Activity
import android.os.Environment
import android.util.Log
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader

class PoHelper(private val context: Activity) {
    companion object {
        private const val TAG = "dst-po"
    }

    private val replaceList: Array<String> =
        context.resources.getStringArray(R.array.replacement_list)
    private val replace3dot: String = context.getString(R.string.replacement_3dot)
    private val replace6dot: String = "$replace3dot$replace3dot"

    val sourceMap = HashMap<String, WordEntry>()
    val revisedMap = HashMap<String, WordEntry>()
    val originMap = HashMap<String, WordEntry>()
    val wordList = ArrayList<WordEntry>()

    @Suppress("DEPRECATION")
    private val sdcard =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val targetFile: File
        get() = File(sdcard, "dst_cht.po")

    val cacheFile: File
        get() = File(context.cacheDir, "dst_cht.po")

    private fun loadSdCardFile(name: String): ArrayList<WordEntry> {
        Log.d(TAG, "load sd card: $name")
        val file = File(sdcard, name)
        val list: ArrayList<WordEntry> = if (file.exists()) try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            loadFromReader(reader, true)
        } catch (e: Exception) {
            ArrayList()
        } else {
            ArrayList()
        }
        Log.d(TAG, "SD: done with $name (${list.size})")
        return list
    }

    private fun loadFromReader(
        reader: BufferedReader,
        line2Enabled: Boolean = true
    ): ArrayList<WordEntry> {
        val list = ArrayList<WordEntry>()
        try {
            // do reading, usually loop until end of file reading
            var line: String? = ""//reader.readLine()
            while (line != null) {
                // Log.d(TAG, "${line.length}: $line")
                val line1 = reader.readLine()
                if (!line1.startsWith("#")) {
                    Log.d(TAG, "bypass $line1")
                    continue //bypass some invalid header
                }
                val line2 = if (line2Enabled) reader.readLine() else ""
                val line3 = reader.readLine()
                val line4 = reader.readLine()
                val entry = WordEntry.from(line1, line2, line3, line4)
                if (entry != null) {
                    // Log.d(TAG, "entry: ${entry.id}")
                    list.add(entry)
                } else {
                    Log.e(TAG, "invalid input: $line1")
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                Log.e(TAG, "close: ${e.message}")
            }
        }
        return list
    }

    private fun loadAssetFile(name: String, line2Enabled: Boolean = true): ArrayList<WordEntry> {
        var list: ArrayList<WordEntry> = loadSdCardFile(name)
        if (list.isEmpty()) {
            Log.d(TAG, "load asset: $name")
            val reader = BufferedReader(InputStreamReader(context.assets.open(name)))
            list = try {
                loadFromReader(reader, line2Enabled)
            } catch (e: Exception) {
                ArrayList()
            }
            Log.d(TAG, "A: done with $name (${list.size})")
        }
        return list
    }

    fun writeEntryToFile(dst: File = targetFile, list: ArrayList<WordEntry> = wordList): Boolean {
        val writer: BufferedWriter?
        try { // http://stackoverflow.com/a/1053474
            writer = BufferedWriter(FileWriter(dst))
            var content = "\"Language: zh-tw\"\n\"POT Version: 2.0\"\n"
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
            Log.e(TAG, "writeStringToFile: ${e.message}")
            return false
        }
        Log.d(TAG, "write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }

    fun runTranslation(postAction: ((timeCost: Long) -> Unit)? = null) {
        Log.d(TAG, "run translation")
        val start = System.currentTimeMillis()

        val s = loadAssetFile("chinese_s.po")
        sourceMap.clear()
        s.forEach { entry ->
            // entry.str = sc2tc(entry.str).trim() // translate to traditional
            sourceMap[entry.key] = entry
        }
        val stop1 = System.currentTimeMillis()
        Log.d(TAG, "original SC size: ${s.size} (${stop1 - start} ms)")

        val t = loadAssetFile("chinese_t.po")
        revisedMap.clear()
        t.forEach { entry ->
            revisedMap[entry.key] = entry
        }
        val stop2 = System.currentTimeMillis()
        Log.d(TAG, "original TC size: ${t.size} (${stop2 - start} ms)")

        originMap.clear()
        loadAssetFile("dst_cht.po").filter { entry ->
            entry.id != "\"\"" && entry.str != "\"\""
        }.forEach { entry ->
            originMap[entry.key] = entry
        }
        val stop3 = System.currentTimeMillis()
        Log.d(TAG, "previous data size: ${originMap.size} (${stop3 - stop1} ms)")

        wordList.clear()
        s.forEach { entry ->
            var newly = false
            var str = ""
            if (originMap.containsKey(entry.key)) {
                val str1 = originMap[entry.key]?.str ?: ""
                if (str1.isNotEmpty()) str = str1.trim()
            } else {
                Log.d(TAG, "add ${entry.key}")
            }
            if (str.isEmpty()) {//not in the translated po
                newly = true
                str = sc2tc(entry.str).trim()
                //Log.d(TAG, ">> use $str")
            }
            str = getReplacement(str)
            wordList.add(WordEntry(entry.key, entry.text, entry.id, str, newly))
        }
        val stop4 = System.currentTimeMillis()
        Log.d(TAG, "new list size: ${wordList.size} (${stop4 - stop3} ms)")

        writeEntryToFile(cacheFile, wordList)
        val cost = System.currentTimeMillis() - start
        Log.d(TAG, "write data done. $cost ms")
        postAction?.let { action -> context.runOnUiThread { action(cost) } }
    }

    fun sc2tc(str: String): String = ChineseConverter.convert(str, ConversionType.S2TWP, context)

    private fun getReplacement(src: String): String {
        var str = src.replace("...", replace3dot)
        str = if (str != "\"$replace6dot\"") str.replace(replace6dot, replace3dot) else str
        replaceList.forEach {
            val pair = it.split("|")
            str = str.replace(pair[0], pair[1])
        }
        if (str.contains("\\\"")) {
            var i = 0
            val str1 = str.replace("\\\"", "%@%").replace("%@%".toRegex()) {
                //Log.d(TAG, "$i ${it.value}")
                context.getString(if (i++ % 2 == 0) R.string.replacement_left_bracket else R.string.replacement_right_bracket)
            }
            if (i % 2 == 0) {
                Log.d(TAG, "$str ==> $str1")
                str = str1 //only replace the paired string
            }
        }
        return str
    }
}