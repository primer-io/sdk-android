package io.primer.android.components.ui.widgets

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.components.ui.widgets.elements.PrimerInputElementListener
import io.primer.android.ui.TextInputMask

abstract class PrimerEditText(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatEditText(context, attrs), PrimerInputElement {

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s)
        }
    }
    private var textChangedListener: PrimerTextChangedListener? = null
    protected var listener: PrimerInputElementListener? = null

    init {
        super.addTextChangedListener(textWatcher)
    }

    override fun addTextChangedListener(watcher: TextWatcher?) = Unit

    override fun getText(): Editable? {
        val text = super.getText()
        return if (isCalledFromSuperMethod()) text
        else SpannableStringBuilder(MASKING_CHARACTER.repeat(text?.length ?: 0))
    }

    override fun isSuggestionsEnabled() = false

    override fun setPrimerInputElementListener(listener: PrimerInputElementListener) {
        this.listener = listener
    }

    internal fun getSanitizedText() = super.getText()?.trim()

    internal fun attachTextFormatter(textInputMask: TextInputMask) = super.addTextChangedListener(
        textInputMask
    )

    internal fun setTextChangedListener(textChangedListener: PrimerTextChangedListener) {
        this.textChangedListener = textChangedListener
    }

    internal open fun onTextChanged(s: Editable?) {
        textChangedListener?.onTextChanged(s.toString())
        listener?.inputElementValueChanged(this@PrimerEditText)
        listener?.inputElementValueIsValid(this@PrimerEditText, isValid())
    }

    protected fun isCalledFromSuperMethod() = getCallerCallerClassName()?.startsWith(
        ANDROID_WIDGET_PACKAGE
    ) == true

    private fun getCallerCallerClassName(): String? {
        val stacktrace = Thread.currentThread().stackTrace
        var callerClassName: String? = null
        for (i in 1 until stacktrace.size) {
            val stackTraceElement = stacktrace[i]
            if (stackTraceElement.className != this::class.java.name &&
                stackTraceElement.className.indexOf(THREAD_CLASS_NAME) != 0
            ) {
                if (callerClassName == null) {
                    callerClassName = stackTraceElement.className
                } else if (callerClassName != stackTraceElement.className) {
                    return stackTraceElement.className
                }
            }
        }
        return null
    }

    private companion object {
        const val MASKING_CHARACTER = "*"
        const val ANDROID_WIDGET_PACKAGE = "android.widget"
        const val THREAD_CLASS_NAME = "java.lang.Thread"
    }
}
