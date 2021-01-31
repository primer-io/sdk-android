package io.primer.android

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
class UniversalCheckoutTheme private constructor(
  // Corner radii
  val buttonCornerRadius: Float,
  val inputCornerRadius: Float,

  // Surface colors
  @ColorInt val backgroundColor: Int,

  // Button Colors
  @ColorInt val buttonPrimaryColor: Int,
  @ColorInt val buttonDefaultColor: Int,

  // Text Colors
  @ColorInt val textDefaultColor: Int,
  @ColorInt val textDangerColor: Int,
  @ColorInt val textMutedColor: Int,

  // General theme
  @ColorInt val primaryColor: Int,
  @ColorInt val inputBackgroundColor: Int,
) {
  companion object {
    fun getDefault(): UniversalCheckoutTheme {
      return create()
    }

    fun create(
      buttonCornerRadius: Float? = null,
      inputCornerRadius: Float? = null,
      @ColorInt backgroundColor: Int? = null,
      @ColorInt buttonPrimaryColor: Int? = null,
      @ColorInt buttonDefaultColor: Int? = null,
      @ColorInt textDefaultColor: Int? = null,
      @ColorInt textDangerColor: Int? = null,
      @ColorInt textMutedColor: Int? = null,
      @ColorInt primaryColor: Int? = null,
      @ColorInt inputBackgroundColor: Int? = null,
    ): UniversalCheckoutTheme {
      return UniversalCheckoutTheme(
        buttonCornerRadius = buttonCornerRadius ?: 12.0f,
        inputCornerRadius = inputCornerRadius ?: 12.0f,
        backgroundColor = backgroundColor ?: Color.WHITE,
        buttonPrimaryColor = buttonPrimaryColor ?: Color.parseColor("#FF2C98F0"),
        buttonDefaultColor = buttonDefaultColor ?: Color.WHITE,
        textDefaultColor = textDefaultColor ?: Color.parseColor("#FF000000"),
        textDangerColor = textDangerColor ?: Color.parseColor("#FFEB001B"),
        textMutedColor = textMutedColor ?: Color.parseColor("#FF808080"),
        primaryColor = primaryColor ?: Color.parseColor("#FF2C98F0"),
        inputBackgroundColor = (inputBackgroundColor ?: Color.parseColor("#FFFFFFFF"))
      )
    }
  }
}