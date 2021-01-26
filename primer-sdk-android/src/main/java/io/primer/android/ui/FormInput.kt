package io.primer.android.ui

import android.content.Context
import io.primer.android.R
import io.primer.android.viewmodel.FormField

class FormInput(context: Context, val field: FormField) :
  androidx.appcompat.widget.AppCompatEditText(context, null, R.style.Primer_FormInput_Input) {
}