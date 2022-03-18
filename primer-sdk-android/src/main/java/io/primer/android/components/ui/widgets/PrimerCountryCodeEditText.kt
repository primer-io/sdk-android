package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.method.QwertyKeyListener
import android.text.method.TextKeyListener
import android.util.AttributeSet
import android.util.Log
import io.primer.android.model.dto.PrimerInputFieldType

class PrimerCountryCodeEditText(context: Context, attrs: AttributeSet? = null) :
    PrimerEditText(context, attrs) {

    init {
        keyListener = QwertyKeyListener.getInstance(true, TextKeyListener.Capitalize.WORDS)
        setupListener()
    }

    private fun setupListener() {
        setOnClickListener {
            Log.d(javaClass.simpleName, "Click for open countries")
        }
    }

    override fun getType() = PrimerInputFieldType.COUNTRY_CODE

    override fun isValid(): Boolean = true
}
