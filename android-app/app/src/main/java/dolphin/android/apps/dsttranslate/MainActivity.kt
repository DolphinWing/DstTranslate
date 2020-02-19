package dolphin.android.apps.dsttranslate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(android.R.id.button1)?.setOnClickListener {
            if (allPermissionsGranted()) {
                executor.submit { runTranslation() }
            } else {
                startAppInfoActivity()
            }
        }
        if (allPermissionsGranted()) {
            Handler().post {
                executor.submit {
                    runTranslation(Runnable {
                        Toast.makeText(this@MainActivity, "DONE", Toast.LENGTH_SHORT).show()
                    })
                }
            }
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

    private fun runTranslation(postAction: Runnable? = null) {
        Log.d(TAG, "run translation")
        val start = System.currentTimeMillis()

        val s = loadAssetFile("chinese_s.po")
        val stop1 = System.currentTimeMillis()
        Log.d(TAG, "original SC size: ${s.size} (${stop1 - start} ms)")

        //add character wordings
        val mapT = HashMap<String, WordEntry>()
        loadAssetFile("chinese_t.po").filter {
            it.id != "\"\"" && it.str != "\"\"" &&
                    (it.key.startsWith("STRINGS.CHARACTER") ||
                            it.key.startsWith("STRINGS.LUCY.") ||
                            it.key.startsWith("STRINGS.LAVALUCY.") ||
                            it.key.startsWith("STRINGS.EPITAPHS.") ||
                            it.key.startsWith("STRINGS.RECIPE_DESC"))
        }.forEach {
            //Log.d(TAG, "ADD: ${it.key}")
            mapT[it.key] = it
        }
        val stop2 = System.currentTimeMillis()
        Log.d(TAG, "official TC size: ${mapT.size} (${stop2 - stop1} ms)")

        val map1 = HashMap<String, WordEntry>()
        loadAssetFile("dst_cht.po").filter {
            it.id != "\"\"" && it.str != "\"\""
        }.forEach {
            map1[it.key] = it
        }
        val stop3 = System.currentTimeMillis()
        Log.d(TAG, "previous data size: ${map1.size} (${stop3 - stop2} ms)")

        val newList = ArrayList<WordEntry>()
        s.forEach {
            var str = ""
            if (mapT.containsKey(it.key)) {//character wording
                val str2 = mapT[it.key]?.str ?: ""
                if (str2.isNotEmpty()) str = str2
            } else if (map1.containsKey(it.key)) {
                val str1 = map1[it.key]?.str ?: ""
                if (str1.isNotEmpty()) str = str1
            } else {
                Log.d(TAG, "add ${it.key}")
            }
            if (str.isEmpty()) {
                str = ChineseConverter.convert(it.str, ConversionType.S2TW, this@MainActivity)
                //Log.d(TAG, ">> use $str")
            }
            newList.add(WordEntry(it.key, it.text, it.id, str))
        }
        val stop4 = System.currentTimeMillis()
        Log.d(TAG, "new list size: ${newList.size} (${stop4 - stop3} ms)")

        writeEntryToFile(File("/sdcard/dst_cht.po"), newList)
        Log.d(TAG, "write data done. ${System.currentTimeMillis() - start} ms")
        if (postAction != null) runOnUiThread(postAction)
    }

    private fun loadAssetFile(name: String, line2Enabled: Boolean = true): ArrayList<WordEntry> {
        Log.d(TAG, "load asset: $name")
        val list = ArrayList<WordEntry>()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(assets.open(name)))
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
                reader?.close()
            } catch (e: Exception) {
                Log.e(TAG, "close: ${e.message}")
            }
        }
        Log.d(TAG, "done with $name")
        return list
    }

    private fun writeEntryToFile(dst: File, list: ArrayList<WordEntry>): Boolean {
        var writer: BufferedWriter? = null
        try { //http://stackoverflow.com/a/1053474
            writer = BufferedWriter(FileWriter(dst))
            var content = "\"Language: zh-tw\"\n\"POT Version: 2.0\"\n"
            writer.write(content, 0, content.length)
            list.forEach {
                content = "\n"
                content += "#. ${it.key}\n"
                content += "msgctxt ${it.text}\n"
                content += "msgid ${it.id}\n"
                content += "msgstr ${it.str}\n"
                writer.write(content, 0, content.length)
            }
            writer.close()
            //writer = null
        } catch (e: FileNotFoundException) {
            //e.printStackTrace()
            Log.e(TAG, "FileNotFoundException: ${e.message}")
            return false
        } catch (e: Exception) {
            //e.printStackTrace()
            Log.e(TAG, "writeStringToFile: ${e.message}")
            return false
        }
        Log.d(TAG, "write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }
}
