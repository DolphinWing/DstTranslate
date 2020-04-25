package dolphin.android.apps.dsttranslate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import java.io.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "dst"

        // This is an array of all the permission specified in the manifest.
        private val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var replaceList: Array<String>
    private lateinit var replace3dot: String
    private lateinit var replace6dot: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replaceList = resources.getStringArray(R.array.replacement_list)
        replace3dot = getString(R.string.replacement_3dot)
        replace6dot = "$replace3dot$replace3dot"
        setContentView(R.layout.activity_main)
        if (allPermissionsGranted()) {
            Handler().post { runDstTranslation() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.button1 ->
                if (allPermissionsGranted()) {
                    runDstTranslation()
                } else {
                    startAppInfoActivity()
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun runDstTranslation() {
        executor.submit {
            runTranslation(Runnable {
                val l = wordList.filter { entry -> entry.newly }
                var text = "total changes: ${l.size}\n\n"
                l.forEach { entry ->
                    //Log.d(TAG, "added: ${entry.key}")
                    text += "${entry.key}: ${entry.str.drop(1).dropLast(1)}\n"
                }
                findViewById<TextView>(android.R.id.text1)?.text = text
                Log.d(TAG, "list changed: ${l.size}")
                Toast.makeText(
                    this@MainActivity,
                    "DONE ${targetFile.absolutePath}",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun startAppInfoActivity() {
        //https://stackoverflow.com/a/32983128/2673859
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val wordList = ArrayList<WordEntry>()

    private fun runTranslation(postAction: Runnable? = null) {
        Log.d(TAG, "run translation")
        val start = System.currentTimeMillis()

        val s = loadAssetFile("chinese_s.po")
        val stop1 = System.currentTimeMillis()
        Log.d(TAG, "original SC size: ${s.size} (${stop1 - start} ms)")

        val map1 = HashMap<String, WordEntry>()
        loadAssetFile("dst_cht.po").filter { entry ->
            entry.id != "\"\"" && entry.str != "\"\""
        }.forEach { entry ->
            map1[entry.key] = entry
        }
        val stop3 = System.currentTimeMillis()
        Log.d(TAG, "previous data size: ${map1.size} (${stop3 - stop1} ms)")

        wordList.clear()
        s.forEach { entry ->
            var newly = false
            var str = ""
            if (map1.containsKey(entry.key)) {
                val str1 = map1[entry.key]?.str ?: ""
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

        writeEntryToFile(targetFile, wordList)
        Log.d(TAG, "write data done. ${System.currentTimeMillis() - start} ms")
        if (postAction != null) runOnUiThread(postAction)
    }

    private fun sc2tc(str: String): String =
        ChineseConverter.convert(str, ConversionType.S2TW, this)

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
                getString(if (i++ % 2 == 0) R.string.replacement_left_bracket else R.string.replacement_right_bracket)
            }
            if (i % 2 == 0) {
                Log.d(TAG, "$str ==> $str1")
                str = str1 //only replace the paired string
            }
        }
        return str
    }

    private val sdcard =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    private val targetFile: File
        get() = File(sdcard, "dst_cht.po")

    @Suppress("RemoveExplicitTypeArguments")
    private fun loadSdCardFile(name: String): ArrayList<WordEntry> {
        Log.d(TAG, "load sd card: $name")
        val file = File(sdcard, name)
        val list: ArrayList<WordEntry> = if (file.exists()) try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            loadFromReader(reader, true)
        } catch (e: Exception) {
            ArrayList<WordEntry>()
        } else {
            ArrayList()
        }
        Log.d(TAG, "SD: done with $name (${list.size})")
        return list
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun loadAssetFile(name: String, line2Enabled: Boolean = true): ArrayList<WordEntry> {
        var list: ArrayList<WordEntry> = loadSdCardFile(name)
        if (list.isEmpty()) {
            Log.d(TAG, "load asset: $name")
            val reader = BufferedReader(InputStreamReader(assets.open(name)))
            list = try {
                loadFromReader(reader, line2Enabled)
            } catch (e: Exception) {
                ArrayList<WordEntry>()
            }
            Log.d(TAG, "A: done with $name (${list.size})")
        }
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
                //Log.d(TAG, "${line.length}: $line")
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
                    //Log.d(TAG, "entry: ${entry.id}")
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

    private fun writeEntryToFile(dst: File, list: ArrayList<WordEntry>): Boolean {
        val writer: BufferedWriter?
        try { //http://stackoverflow.com/a/1053474
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
            //writer = null
        } catch (e: Exception) {
            //e.printStackTrace()
            Log.e(TAG, "writeStringToFile: ${e.message}")
            return false
        }
        Log.d(TAG, "write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }
}
