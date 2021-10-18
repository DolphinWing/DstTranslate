package dolphin.android.apps.dsttranslate.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dolphin.android.apps.dsttranslate.R

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = colorResource(id = R.color.colorPrimary),
            primaryVariant = colorResource(id = R.color.colorPrimaryDark),
            secondary = colorResource(id = R.color.colorAccent),
        ),
        content = content,
    )
}

object AppTheme {
    @Composable
    private fun isDesktop(): Boolean {
        val context = LocalContext.current
        return context.packageManager.hasSystemFeature("org.chromium.arc.device_management")
    }

//    @Composable
//    fun fontSize(): TextUnit = if (isDesktop()) 18.sp else 14.sp

    @Composable
    fun largerFontSize(): TextUnit = if (isDesktop()) 24.sp else 18.sp
}