package com.example.myapplication.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class SettingsItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val textView = TextView(context)

    init {
        addView(textView, 0)

        // background
        val background = GradientDrawable()
        val colors = intArrayOf(Color.LTGRAY, Color.GRAY)
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_selected),
            intArrayOf(android.R.attr.state_selected),
        )

        background.color = ColorStateList(states, colors)
        background.cornerRadius = 12f
        this.background = background
    }

    fun setText(value: String) {
        textView.text = value
    }
}
