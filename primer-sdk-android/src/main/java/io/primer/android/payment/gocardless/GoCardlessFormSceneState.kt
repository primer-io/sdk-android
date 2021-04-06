package io.primer.android.payment.gocardless

import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormSummaryState
import io.primer.android.ui.FormTitleState
import io.primer.android.ui.FormViewState

abstract class GoCardlessFormSceneState(
    val scene: Scene,
    title: FormTitleState? = null,
    fields: List<FormField> = ArrayList(),
    button: ButtonState? = null,
    summary: FormSummaryState? = null,
    progress: FormProgressState? = null,
    initialValues: Map<String, String>? = null,
) : FormViewState(
    title = title,
    fields = fields,
    button = button,
    summary = summary,
    progress = progress,
    initialValues = initialValues,
) {

    enum class Scene {
        IBAN,
        CUSTOMER_NAME,
        CUSTOMER_EMAIL,
        ADDRESS,
        SUMMARY,
    }
}
