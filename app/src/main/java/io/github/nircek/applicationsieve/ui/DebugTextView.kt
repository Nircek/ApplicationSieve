package io.github.nircek.applicationsieve.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.AttributeSet
import android.view.View

class DebugTextView(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    var isDebuggable = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

    init { visibility = if(isDebuggable) View.VISIBLE else View.GONE }
}
