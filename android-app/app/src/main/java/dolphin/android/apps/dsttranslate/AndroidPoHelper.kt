package dolphin.android.apps.dsttranslate

import android.app.Activity
import android.os.Environment
import android.util.Log
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class AndroidPoHelper(private val context: Activity) : PoHelper() {
    companion object {
        private const val TAG = "dst-po"
    }

    override fun log(message: String) {
        Log.d(TAG, message)
    }

    override fun prepare() {
        replaceList.addAll(context.resources.getStringArray(R.array.replacement_list))
        replace3dot = context.getString(R.string.replacement_3dot)
        replaceLeftBracket = context.getString(R.string.replacement_left_bracket)
        replaceRightBracket = context.getString(R.string.replacement_right_bracket)
    }

    @Suppress("DEPRECATION")
    private val sdcard =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    private fun loadSdCardFile(name: String): ArrayList<WordEntry> {
        log("load sd card: $name")
        val file = File(sdcard, name)
        val list: ArrayList<WordEntry> = if (file.exists()) try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            loadFromReader(reader, true)
        } catch (e: Exception) {
            ArrayList()
        } else {
            ArrayList()
        }
        log("SD: done with $name (${list.size})")
        return list
    }

    override fun loadAssetFile(name: String, line2Enabled: Boolean): ArrayList<WordEntry> {
        var list: ArrayList<WordEntry> = loadSdCardFile(name)
        if (list.isEmpty()) {
            log("load asset: $name")
            val reader = BufferedReader(InputStreamReader(context.assets.open(name)))
            list = try {
                loadFromReader(reader, line2Enabled)
            } catch (e: Exception) {
                ArrayList()
            }
            log("A: done with $name (${list.size})")
        }
        return list
    }

    override fun sc2tc(str: String): String {
        return ChineseConverter.convert(str, ConversionType.S2TWP, context)
    }

    override fun getCachedFile(): File = File(context.cacheDir, "dst_cht.po")

    override fun getOutputFile(): File = File(sdcard, "dst_cht.po")
}