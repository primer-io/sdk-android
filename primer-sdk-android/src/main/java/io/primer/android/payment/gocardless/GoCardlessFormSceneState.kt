package io.primer.android.payment.gocardless

import io.primer.android.ui.*
import java.util.*
import kotlin.collections.ArrayList

abstract class GoCardlessFormSceneState(
  val scene: Scene,
  title: FormTitleState? = null,
  fields: List<FormField> = ArrayList(),
  button: ButtonState? = null,
  summary: FormSummaryState? = null,
  initialValues: Map<String, String>? = null,
) : FormViewState(
  title = title,
  fields = fields,
  button = button,
  summary = summary,
  initialValues = initialValues,
) {
  enum class Scene {
    IBAN,
    CUSTOMER,
    ADDRESS,
    SUMMARY,
  }
}