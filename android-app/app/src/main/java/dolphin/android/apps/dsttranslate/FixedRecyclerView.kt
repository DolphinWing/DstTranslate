@file:Suppress("PackageName")

package dolphin.android.apps.dsttranslate

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

/**
 * http://stackoverflow.com/a/25227797
 */

class FixedRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun canScrollVertically(direction: Int): Boolean {
        if (direction < 1) {// check if scrolling up
            val original = super.canScrollVertically(direction)
            return !original && getChildAt(0) != null && getChildAt(0).top < paddingTop || original
        }
        return super.canScrollVertically(direction)
    }
}
