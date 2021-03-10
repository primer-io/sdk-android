package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormTitleState

class CustomerNameViewState(buttonLabelId: Int, showProgress: Boolean) :
    GoCardlessFormSceneState(
        Scene.CUSTOMER_NAME,
        title = FormTitleState(
            titleId = R.string.enter_name,
        ),
        fields = listOf(
            FormField(
                name = DD_FIELD_NAME_CUSTOMER_NAME,
                labelId = R.string.first_last_name,
                required = true,
                autoFocus = true,
                inputType = FormField.Type.PERSON_NAME,
                minWordCount = 2,
            ),
        ),
        button = ButtonState(
            labelId = buttonLabelId,
        ),
        progress = if (showProgress) FormProgressState(current = 2, max = 5) else null,
    )
