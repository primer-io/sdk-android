package io.primer.sample.utils

import android.content.Context

fun Context.showMandateDialog(
    text: String,
    onOkClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
    builder.apply {
        setMessage(text)
        setPositiveButton(android.R.string.ok) { dialog, _ ->
            onOkClick()
            dialog.dismiss()
        }
        setNegativeButton(android.R.string.cancel) { dialog, _ ->
            onCancelClick()
            dialog.dismiss()
        }
    }.show()
}