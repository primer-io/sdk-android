package io.primer.android

private const val DEFAULT_BACKGROUND_COLOR = "#FFFFFFFF"
private const val DEFAULT_PRIMARY_TEXT_COLOR = "#000000FF"
private const val DEFAULT_SECONDARY_TEXT_COLOR = "#D0D0D0FF"
private const val DEFAULT_CONTRAST_COLOR = "#FFFFFFFF"
private const val DEFAULT_PRIMARY_COLOR = "#FF0000FF"
private const val DEFAULT_DANDER_TEXT_COLOR = "#FF0000FF"
private const val DEFAULT_DANDER_BORDER_COLOR = "#FF00002D"
private const val DEFAULT_DANDER_BACKGROUND_COLOR = "#FF00000F"
private const val DEFAULT_BORDER_COLOR = "#0000002D"
private const val DEFAULT_INPUT_TEXT_COLOR = "#000000FF"
private const val DEFAULT_INPUT_BACKGROUND_COLOR = "#FFFFFFFF"
private const val DEFAULT_INPUT_PLACEHOLDER_COLOR = "#000000FF"

data class ThemeConfig(
  val backgroundColor: String = DEFAULT_BACKGROUND_COLOR,
  val primaryTextColor: String = DEFAULT_PRIMARY_TEXT_COLOR,
  val secondaryTextColor: String = DEFAULT_SECONDARY_TEXT_COLOR,
  val contrastTextColor: String = DEFAULT_CONTRAST_COLOR,
  val primaryColor: String = DEFAULT_PRIMARY_COLOR,
  val danderTextColor: String = DEFAULT_DANDER_TEXT_COLOR,
  val danderBorderColor: String = DEFAULT_DANDER_BORDER_COLOR,
  val danderBackgroundColor: String = DEFAULT_DANDER_BACKGROUND_COLOR,
  val borderColor: String = DEFAULT_BORDER_COLOR,
  val inputTextColor: String = DEFAULT_INPUT_TEXT_COLOR,
  val inputBackgroundColor: String = DEFAULT_INPUT_BACKGROUND_COLOR,
  val inputPlaceholderColor: String = DEFAULT_INPUT_PLACEHOLDER_COLOR
)