package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dolphin.android.apps.dsttranslate.WordEntry

object AppTheme {
    @Composable
    fun largerFontSize(): TextUnit = 18.sp

    object AppColor {
        val purple = Color(156, 39, 176)
        val blue = Color(33, 150, 243)
        val orange = Color(255, 87, 34)
        val green = Color(76, 175, 80)
        val primary = Color(96, 125, 139)
        val secondary = Color(233, 30, 99)
    }
}

@Composable
fun DstTranslatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = AppTheme.AppColor.primary,
            secondary = AppTheme.AppColor.secondary,
        ),
        content = content,
    )
}

object PreviewDefaults {
    val dst = WordEntry("key-dst", "text-dst", "id-dst", "str-dst")
    val chs = WordEntry("key-chs", "text-chs", "id-chs", "str-chs")
    val cht = WordEntry("key-cht", "text-cht", "id-cht", "str-cht")

    val samples = listOf(WordEntry.default(), dst, chs, cht)
}
