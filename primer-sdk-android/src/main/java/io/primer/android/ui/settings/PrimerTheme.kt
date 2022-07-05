package io.primer.android.ui.settings

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import io.primer.android.R
import kotlinx.serialization.Serializable

@Serializable
data class PrimerTheme internal constructor(
    internal val isDarkMode: Boolean?,
    internal val primaryColor: ColorData,
    internal val backgroundColor: ColorData,
    internal val splashColor: ColorData,
    internal val defaultCornerRadius: DimensionData,
    internal val bottomSheetCornerRadius: DimensionData,
    internal val titleText: TextTheme,
    internal val amountLabelText: TextTheme,
    internal val subtitleText: TextTheme,
    internal val paymentMethodButton: ButtonTheme,
    internal val mainButton: ButtonTheme,
    internal val systemText: TextTheme,
    internal val defaultText: TextTheme,
    internal val errorText: TextTheme,
    internal val input: InputTheme,
    internal val searchInput: SearchInputTheme,
    internal val windowMode: WindowMode,
    internal val inputMode: InputMode,
) {

    enum class WindowMode { BOTTOM_SHEET, FULL_HEIGHT }

    enum class InputMode { UNDERLINED, OUTLINED }

    companion object {

        /**
         * Style the Primer SDK using Android XML resources
         * */
        @JvmStatic
        fun build(
            isDarkMode: Boolean? = null,
            @ColorRes primaryColor: Int? = null,
            @ColorRes backgroundColor: Int? = null,
            @ColorRes disabledColor: Int? = null,
            @ColorRes errorColor: Int? = null,
            @DimenRes defaultCornerRadius: Int? = null,
            @DimenRes bottomSheetCornerRadius: Int? = null,
            defaultBorder: BorderThemeData? = null,
            defaultText: TextThemeData? = null,
            titleText: TextThemeData? = null,
            amountLabelText: TextThemeData? = null,
            subtitleText: TextThemeData? = null,
            paymentMethodButton: ButtonThemeData? = null,
            mainButton: ButtonThemeData? = null,
            systemButton: TextThemeData? = null,
            errorText: TextThemeData? = null,
            input: InputThemeData? = null,
            searchInput: SearchInputThemeData? = null,
            inputMode: InputMode = InputMode.OUTLINED,
        ): PrimerTheme {

            val styledPrimaryColor = ResourceColor.valueOf(
                default = primaryColor ?: R.color.primer_primary
            )
            val styledBackgroundColor = ResourceColor.valueOf(
                default = backgroundColor ?: R.color.primer_background
            )
            val styledDisabledColor = ResourceColor.valueOf(
                default = disabledColor ?: R.color.primer_disabled
            )

            val styledCornerRadius = ResourceDimension.valueOf(
                default = defaultCornerRadius ?: R.dimen.primer_default_corner_radius
            )

            val styledBottomSheetCornerRadius = ResourceDimension.valueOf(
                default = bottomSheetCornerRadius ?: R.dimen.primer_bottom_sheet_corner_radius,
            )

            val styledTitleText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = titleText?.defaultColor ?: defaultText?.defaultColor
                        ?: R.color.primer_title
                ),
                fontsize = ResourceDimension.valueOf(
                    default = titleText?.fontsize ?: R.dimen.primer_title_fontsize,
                ),
            )

            val styledAmountLabelText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = amountLabelText?.defaultColor ?: defaultText?.defaultColor
                        ?: R.color.primer_amount
                ),
                fontsize = ResourceDimension.valueOf(
                    default = amountLabelText?.fontsize ?: R.dimen.primer_amount_label_fontsize,
                ),
            )

            val styledSubtitleText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = subtitleText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontsize = ResourceDimension.valueOf(
                    default = subtitleText?.fontsize ?: R.dimen.primer_subtitle_fontsize,
                ),
            )

            val styledPaymentMethodButtonBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.border?.defaultColor ?: disabledColor
                        ?: R.color.primer_payment_method_button_border
                ),
                selectedColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.border?.selectedColor ?: primaryColor
                        ?: R.color.primer_payment_method_button_border_selected
                ),
                errorColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.border?.errorColor ?: errorColor
                        ?: R.color.primer_payment_method_button_border_error
                ),
                width = ResourceDimension.valueOf(
                    default = paymentMethodButton?.border?.width ?: defaultBorder?.width
                        ?: R.dimen.primer_payment_method_button_border_width,
                ),
            )

            val styledPaymentMethodButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.text?.defaultColor
                        ?: R.color.primer_payment_method_button_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = paymentMethodButton?.text?.fontsize
                        ?: R.dimen.primer_payment_method_button_fontsize,
                ),
            )

            val styledPaymentMethodButton = ButtonTheme(
                defaultColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.defaultColor
                        ?: R.color.primer_payment_method_button
                ),
                disabledColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.disabledColor ?: disabledColor
                        ?: R.color.primer_payment_method_button_disabled
                ),
                errorColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.errorColor ?: errorColor
                        ?: R.color.primer_payment_method_button_error
                ),
                border = styledPaymentMethodButtonBorder,
                text = styledPaymentMethodButtonText,
                cornerRadius = ResourceDimension.valueOf(
                    default = paymentMethodButton?.cornerRadius ?: defaultCornerRadius
                        ?: R.dimen.primer_payment_method_button_corner_radius,
                ),
            )

            val styledMainButtonBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.border?.defaultColor ?: disabledColor
                        ?: R.color.primer_disabled
                ),
                selectedColor = ResourceColor.valueOf(
                    default = mainButton?.border?.selectedColor ?: primaryColor
                        ?: R.color.primer_primary
                ),
                errorColor = ResourceColor.valueOf(
                    default = mainButton?.border?.errorColor ?: errorColor
                        ?: R.color.primer_error
                ),
                width = ResourceDimension.valueOf(
                    default = mainButton?.border?.width ?: R.dimen.primer_main_button_border_width,
                ),
            )

            val styledMainButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.text?.defaultColor ?: R.color.primer_main_button_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = mainButton?.text?.fontsize ?: R.dimen.primer_subtitle_fontsize,
                ),
            )

            val styledMainButton = ButtonTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.defaultColor ?: primaryColor
                        ?: R.color.primer_main_button
                ),
                disabledColor = ResourceColor.valueOf(
                    default = mainButton?.disabledColor ?: disabledColor ?: R.color.primer_disabled
                ),
                errorColor = ResourceColor.valueOf(
                    default = mainButton?.errorColor ?: errorColor ?: R.color.primer_error
                ),
                border = styledMainButtonBorder,
                text = styledMainButtonText,
                cornerRadius = ResourceDimension.valueOf(
                    default = mainButton?.cornerRadius ?: defaultCornerRadius
                        ?: R.dimen.primer_main_button_corner_radius,
                )
            )

            val styledSystemText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = systemButton?.defaultColor ?: primaryColor
                        ?: R.color.primer_system_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = systemButton?.fontsize ?: R.dimen.primer_subtitle_fontsize,
                ),
            )

            val styledDefaultText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = defaultText?.defaultColor ?: R.color.primer_default_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = defaultText?.fontsize ?: R.dimen.primer_default_fontsize,
                ),
            )

            val styledErrorText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = errorText?.defaultColor ?: R.color.primer_error
                ),
                fontsize = ResourceDimension.valueOf(
                    default = errorText?.fontsize ?: R.dimen.primer_text_size_sm,
                ),
            )

            val styledInputBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.border?.defaultColor ?: primaryColor
                        ?: R.color.primer_input_border
                ),
                selectedColor = ResourceColor.valueOf(
                    default = input?.border?.selectedColor ?: primaryColor
                        ?: R.color.primer_input_border_selected
                ),
                errorColor = ResourceColor.valueOf(
                    default = input?.border?.errorColor ?: errorColor
                        ?: R.color.primer_input_border_error
                ),
                width = ResourceDimension.valueOf(
                    default = input?.border?.width ?: defaultBorder?.width
                        ?: R.dimen.primer_input_border_width,
                ),
            )

            val styledInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.text?.defaultColor ?: R.color.primer_input_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = input?.text?.fontsize ?: R.dimen.primer_input_fontsize,
                ),
            )

            val styledHintInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.hintText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontsize = ResourceDimension.valueOf(
                    default = input?.hintText?.fontsize ?: R.dimen.primer_input_fontsize,
                ),
            )

            val styledInput = InputTheme(
                backgroundColor = ResourceColor.valueOf(
                    default = input?.backgroundColor ?: backgroundColor
                        ?: R.color.primer_input_background
                ),
                border = styledInputBorder,
                text = styledInputText,
                hintText = styledHintInputText,
                cornerRadius = ResourceDimension.valueOf(
                    default = input?.cornerRadius ?: defaultCornerRadius
                        ?: R.dimen.primer_default_corner_radius,
                ),
            )

            val styledSearchInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = searchInput?.text?.defaultColor ?: R.color.primer_search_input_text
                ),
                fontsize = ResourceDimension.valueOf(
                    default = searchInput?.text?.fontsize ?: R.dimen.primer_search_input_fontsize,
                ),
            )

            val styledSearchHintInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = searchInput?.hintText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontsize = ResourceDimension.valueOf(
                    default = searchInput?.hintText?.fontsize
                        ?: R.dimen.primer_search_input_fontsize,
                ),
            )

            val searchInput = SearchInputTheme(
                backgroundColor = ResourceColor.valueOf(
                    default = searchInput?.backgroundColor ?: backgroundColor
                        ?: R.color.primer_search_input_background
                ),
                text = styledSearchInputText,
                hintText = styledSearchHintInputText,
                cornerRadius = ResourceDimension.valueOf(
                    default = searchInput?.cornerRadius ?: defaultCornerRadius
                        ?: R.dimen.primer_default_corner_radius,
                ),
            )

            return PrimerTheme(
                isDarkMode = isDarkMode,
                primaryColor = styledPrimaryColor,
                backgroundColor = styledBackgroundColor,
                splashColor = styledDisabledColor,
                defaultCornerRadius = styledCornerRadius,
                bottomSheetCornerRadius = styledBottomSheetCornerRadius,
                titleText = styledTitleText,
                amountLabelText = styledAmountLabelText,
                subtitleText = styledSubtitleText,
                paymentMethodButton = styledPaymentMethodButton,
                mainButton = styledMainButton,
                systemText = styledSystemText,
                defaultText = styledDefaultText,
                errorText = styledErrorText,
                input = styledInput,
                searchInput = searchInput,
                windowMode = WindowMode.BOTTOM_SHEET,
                inputMode = inputMode,
            )
        }
    }
}

// Developer models

data class ButtonThemeData(
    @ColorRes val defaultColor: Int? = null,
    @ColorRes val disabledColor: Int? = null,
    @ColorRes val errorColor: Int? = null,
    val text: TextThemeData? = null,
    val border: BorderThemeData? = null,
    @DimenRes val cornerRadius: Int? = null,
)

data class TextThemeData(
    @ColorRes val defaultColor: Int? = null,
    @DimenRes val fontsize: Int? = null,
)

data class BorderThemeData(
    @ColorRes val defaultColor: Int? = null,
    @ColorRes val selectedColor: Int? = null,
    @ColorRes val errorColor: Int? = null,
    @DimenRes val width: Int? = null,
)

data class InputThemeData(
    @ColorRes val backgroundColor: Int? = null,
    val text: TextThemeData? = null,
    val hintText: TextThemeData? = null,
    val border: BorderThemeData? = null,
    @DimenRes val cornerRadius: Int? = null,
)

data class SearchInputThemeData(
    @ColorRes val backgroundColor: Int? = null,
    val text: TextThemeData? = null,
    val hintText: TextThemeData? = null,
    @DimenRes val cornerRadius: Int? = null,
)

// Internal models

@Serializable
internal data class ButtonTheme constructor(
    val defaultColor: ColorData,
    val disabledColor: ColorData,
    val errorColor: ColorData,
    val text: TextTheme,
    val border: BorderTheme,
    val cornerRadius: DimensionData,
)

@Serializable
internal data class TextTheme(
    val defaultColor: ColorData,
    val fontsize: DimensionData,
)

@Serializable
internal data class BorderTheme(
    val defaultColor: ColorData,
    val selectedColor: ColorData,
    val errorColor: ColorData,
    val width: DimensionData,
)

@Serializable
internal data class InputTheme(
    val backgroundColor: ColorData,
    val cornerRadius: DimensionData,
    val text: TextTheme,
    val hintText: TextTheme,
    val border: BorderTheme,
)

@Serializable
internal data class SearchInputTheme(
    val backgroundColor: ColorData,
    val cornerRadius: DimensionData,
    val text: TextTheme,
    val hintText: TextTheme,
)
