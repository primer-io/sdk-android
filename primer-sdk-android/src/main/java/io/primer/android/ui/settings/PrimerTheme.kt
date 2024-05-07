package io.primer.android.ui.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import io.primer.android.R
import io.primer.android.extensions.readParcelable

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
    internal val inputMode: InputMode
) : Parcelable {

    constructor(parcel: Parcel) : this(
        isDarkMode = parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        primaryColor = parcel.readParcelable<ColorData>() ?: ResourceColor.valueOf(R.color.primer_primary),

        backgroundColor = parcel.readParcelable<ColorData>() ?: ResourceColor.valueOf(R.color.primer_background),

        splashColor = parcel.readParcelable<ColorData>() ?: ResourceColor.valueOf(R.color.primer_disabled),

        defaultCornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius),
        bottomSheetCornerRadius = ResourceDimension.valueOf(R.dimen.primer_bottom_sheet_corner_radius),

        titleText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_title),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_title_fontsize)
        ),

        amountLabelText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_amount),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_amount_label_fontsize)
        ),

        subtitleText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_subtitle),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
        ),

        paymentMethodButton = parcel.readParcelable<ButtonTheme>() ?: ButtonTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_payment_method_button),
            disabledColor = ResourceColor.valueOf(R.color.primer_payment_method_button_disabled),
            errorColor = ResourceColor.valueOf(R.color.primer_payment_method_button_error),
            border = BorderTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border),
                selectedColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border_selected),
                errorColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border_error),
                width = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_border_width)
            ),
            text = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_payment_method_button_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_fontsize)
            ),
            cornerRadius = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_corner_radius)
        ),
        mainButton = parcel.readParcelable<ButtonTheme>() ?: ButtonTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_main_button),
            disabledColor = ResourceColor.valueOf(R.color.primer_disabled),
            errorColor = ResourceColor.valueOf(R.color.primer_error),
            border = BorderTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_disabled),
                selectedColor = ResourceColor.valueOf(R.color.primer_primary),
                errorColor = ResourceColor.valueOf(R.color.primer_error),
                width = ResourceDimension.valueOf(R.dimen.primer_main_button_border_width)
            ),
            text = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_main_button_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
            ),
            cornerRadius = ResourceDimension.valueOf(R.dimen.primer_main_button_corner_radius)
        ),

        systemText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_system_text),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
        ),
        defaultText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_default_text),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_default_fontsize)
        ),

        errorText = parcel.readParcelable<TextTheme>() ?: TextTheme(
            defaultColor = ResourceColor.valueOf(R.color.primer_error),
            fontSize = ResourceDimension.valueOf(R.dimen.primer_text_size_sm)
        ),

        input = parcel.readParcelable<InputTheme>() ?: InputTheme(
            backgroundColor = ResourceColor.valueOf(R.color.primer_input_background),
            border = BorderTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_input_border),
                selectedColor = ResourceColor.valueOf(R.color.primer_input_border_selected),
                errorColor = ResourceColor.valueOf(R.color.primer_input_border_error),
                width = ResourceDimension.valueOf(R.dimen.primer_input_border_width)
            ),
            text = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_input_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_input_fontsize)
            ),
            hintText = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_subtitle),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_input_fontsize)
            ),
            cornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius)
        ),

        searchInput = parcel.readParcelable<SearchInputTheme>() ?: SearchInputTheme(
            backgroundColor = ResourceColor.valueOf(R.color.primer_search_input_background),
            text = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_search_input_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_search_input_fontsize)
            ),
            hintText = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_subtitle),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_search_input_fontsize)
            ),
            cornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius)
        ),
        windowMode = WindowMode.BOTTOM_SHEET,
        inputMode = InputMode.OUTLINED
    )

    enum class WindowMode { BOTTOM_SHEET, FULL_HEIGHT }

    enum class InputMode { UNDERLINED, OUTLINED }

    companion object CREATOR : Parcelable.Creator<PrimerTheme> {
        override fun createFromParcel(parcel: Parcel): PrimerTheme {
            return PrimerTheme(parcel)
        }

        override fun newArray(size: Int): Array<PrimerTheme?> {
            return arrayOfNulls(size)
        }

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
            inputMode: InputMode = InputMode.OUTLINED
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
                default = bottomSheetCornerRadius ?: R.dimen.primer_bottom_sheet_corner_radius
            )

            val styledTitleText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = titleText?.defaultColor ?: defaultText?.defaultColor ?: R.color.primer_title
                ),
                fontSize = ResourceDimension.valueOf(
                    default = titleText?.fontsize ?: R.dimen.primer_title_fontsize
                )
            )

            val styledAmountLabelText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = amountLabelText?.defaultColor ?: defaultText?.defaultColor ?: R.color.primer_amount
                ),
                fontSize = ResourceDimension.valueOf(
                    default = amountLabelText?.fontsize ?: R.dimen.primer_amount_label_fontsize
                )
            )

            val styledSubtitleText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = subtitleText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontSize = ResourceDimension.valueOf(
                    default = subtitleText?.fontsize ?: R.dimen.primer_subtitle_fontsize
                )
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
                        ?: R.dimen.primer_payment_method_button_border_width
                )
            )

            val styledPaymentMethodButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = paymentMethodButton?.text?.defaultColor
                        ?: R.color.primer_payment_method_button_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = paymentMethodButton?.text?.fontsize
                        ?: R.dimen.primer_payment_method_button_fontsize
                )
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
                        ?: R.dimen.primer_payment_method_button_corner_radius
                )
            )

            val styledMainButtonBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.border?.defaultColor ?: disabledColor ?: R.color.primer_disabled
                ),
                selectedColor = ResourceColor.valueOf(
                    default = mainButton?.border?.selectedColor ?: primaryColor ?: R.color.primer_primary
                ),
                errorColor = ResourceColor.valueOf(
                    default = mainButton?.border?.errorColor ?: errorColor ?: R.color.primer_error
                ),
                width = ResourceDimension.valueOf(
                    default = mainButton?.border?.width ?: R.dimen.primer_main_button_border_width
                )
            )

            val styledMainButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.text?.defaultColor ?: R.color.primer_main_button_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = mainButton?.text?.fontsize ?: R.dimen.primer_subtitle_fontsize
                )
            )

            val styledMainButton = ButtonTheme(
                defaultColor = ResourceColor.valueOf(
                    default = mainButton?.defaultColor ?: primaryColor ?: R.color.primer_main_button
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
                        ?: R.dimen.primer_main_button_corner_radius
                )
            )

            val styledSystemText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = systemButton?.defaultColor ?: primaryColor ?: R.color.primer_system_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = systemButton?.fontsize ?: R.dimen.primer_subtitle_fontsize
                )
            )

            val styledDefaultText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = defaultText?.defaultColor ?: R.color.primer_default_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = defaultText?.fontsize ?: R.dimen.primer_default_fontsize
                )
            )

            val styledErrorText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = errorText?.defaultColor ?: R.color.primer_error
                ),
                fontSize = ResourceDimension.valueOf(
                    default = errorText?.fontsize ?: R.dimen.primer_text_size_sm
                )
            )

            val styledInputBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.border?.defaultColor ?: primaryColor ?: R.color.primer_input_border
                ),
                selectedColor = ResourceColor.valueOf(
                    default = input?.border?.selectedColor ?: primaryColor ?: R.color.primer_input_border_selected
                ),
                errorColor = ResourceColor.valueOf(
                    default = input?.border?.errorColor ?: errorColor ?: R.color.primer_input_border_error
                ),
                width = ResourceDimension.valueOf(
                    default = input?.border?.width ?: defaultBorder?.width ?: R.dimen.primer_input_border_width
                )
            )

            val styledInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.text?.defaultColor ?: R.color.primer_input_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = input?.text?.fontsize ?: R.dimen.primer_input_fontsize
                )
            )

            val styledHintInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = input?.hintText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontSize = ResourceDimension.valueOf(
                    default = input?.hintText?.fontsize ?: R.dimen.primer_input_fontsize
                )
            )

            val styledInput = InputTheme(
                backgroundColor = ResourceColor.valueOf(
                    default = input?.backgroundColor ?: backgroundColor ?: R.color.primer_input_background
                ),
                border = styledInputBorder,
                text = styledInputText,
                hintText = styledHintInputText,
                cornerRadius = ResourceDimension.valueOf(
                    default = input?.cornerRadius ?: defaultCornerRadius ?: R.dimen.primer_default_corner_radius
                )
            )

            val styledSearchInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = searchInput?.text?.defaultColor ?: R.color.primer_search_input_text
                ),
                fontSize = ResourceDimension.valueOf(
                    default = searchInput?.text?.fontsize ?: R.dimen.primer_search_input_fontsize
                )
            )

            val styledSearchHintInputText = TextTheme(
                defaultColor = ResourceColor.valueOf(
                    default = searchInput?.hintText?.defaultColor ?: R.color.primer_subtitle
                ),
                fontSize = ResourceDimension.valueOf(
                    default = searchInput?.hintText?.fontsize
                        ?: R.dimen.primer_search_input_fontsize
                )
            )

            val searchInputTheme = SearchInputTheme(
                backgroundColor = ResourceColor.valueOf(
                    default = searchInput?.backgroundColor ?: backgroundColor ?: R.color.primer_search_input_background
                ),
                text = styledSearchInputText,
                hintText = styledSearchHintInputText,
                cornerRadius = ResourceDimension.valueOf(
                    default = searchInput?.cornerRadius ?: defaultCornerRadius ?: R.dimen.primer_default_corner_radius
                )
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
                searchInput = searchInputTheme,
                windowMode = WindowMode.BOTTOM_SHEET,
                inputMode = inputMode
            )
        }

        /**
         * Style the Primer SDK using React Native
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmStatic
        fun buildRN(
            isDarkMode: Boolean? = null,
            mainColor: String? = null,
            backgroundColor: String? = null,
            textColor: String? = null,
            disabledColor: String? = null,
            errorColor: String? = null,
            bordersColor: String? = null
        ): PrimerTheme {
            println(
                "mainColor: $mainColor, \n" +
                    "backgroundColor: $backgroundColor, \n" +
                    "textColor: $textColor, \n" +
                    "disabledColor: $disabledColor, \n" +
                    "errorColor: $errorColor, \n" +
                    "bordersColor: $bordersColor"
            )

            val styledPrimaryColor = mainColor?.let {
                DynamicColor.valueOf(default = it)
            } ?: ResourceColor.valueOf(R.color.primer_primary)

            val styledBackgroundColor = backgroundColor?.let {
                DynamicColor.valueOf(default = it)
            } ?: ResourceColor.valueOf(R.color.primer_background)

            val styledDisabledColor = disabledColor?.let {
                DynamicColor.valueOf(default = it)
            } ?: ResourceColor.valueOf(R.color.primer_disabled)

            val styledCornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius)

            val styledBottomSheetCornerRadius = ResourceDimension.valueOf(R.dimen.primer_bottom_sheet_corner_radius)

            val styledTitleText = TextTheme(
                defaultColor = mainColor?.let {
                    DynamicColor.valueOf(default = it)
                } ?: ResourceColor.valueOf(R.color.primer_title),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_title_fontsize)
            )

            val styledAmountLabelText = TextTheme(
                defaultColor = mainColor?.let {
                    DynamicColor.valueOf(default = it)
                } ?: ResourceColor.valueOf(R.color.primer_amount),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_amount_label_fontsize)
            )

            val styledSubtitleText = TextTheme(
                defaultColor = mainColor?.let {
                    DynamicColor.valueOf(default = it)
                } ?: ResourceColor.valueOf(R.color.primer_subtitle),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
            )

            val styledPaymentMethodButtonBorder = BorderTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border),
                selectedColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border_selected),
                errorColor = ResourceColor.valueOf(R.color.primer_payment_method_button_border_error),
                width = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_border_width)
            )

            val styledPaymentMethodButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_payment_method_button_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_fontsize)
            )

            val styledPaymentMethodButton = ButtonTheme(
                defaultColor =
                ResourceColor.valueOf(R.color.primer_payment_method_button),
                disabledColor = when {
                    disabledColor != null -> DynamicColor.valueOf(default = disabledColor)
                    else -> ResourceColor.valueOf(R.color.primer_payment_method_button_disabled)
                },
                errorColor = when {
                    errorColor != null -> DynamicColor.valueOf(default = errorColor)
                    else -> ResourceColor.valueOf(R.color.primer_payment_method_button_error)
                },
                border = styledPaymentMethodButtonBorder,
                text = styledPaymentMethodButtonText,
                cornerRadius = ResourceDimension.valueOf(R.dimen.primer_payment_method_button_corner_radius)
            )

            val styledMainButtonBorder = BorderTheme(
                defaultColor = when {
                    bordersColor != null -> DynamicColor.valueOf(default = bordersColor)
                    else -> ResourceColor.valueOf(R.color.primer_disabled)
                },
                selectedColor = when {
                    mainColor != null -> DynamicColor.valueOf(default = mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_primary)
                },
                errorColor = when {
                    errorColor != null -> DynamicColor.valueOf(default = errorColor)
                    else -> ResourceColor.valueOf(R.color.primer_error)
                },
                width = ResourceDimension.valueOf(R.dimen.primer_main_button_border_width)
            )

            val styledMainButtonText = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_main_button_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
            )

            val styledMainButton = ButtonTheme(
                defaultColor = when {
                    mainColor != null -> DynamicColor.valueOf(default = mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_main_button)
                },
                disabledColor = when {
                    disabledColor != null -> DynamicColor.valueOf(default = disabledColor)
                    else -> ResourceColor.valueOf(R.color.primer_disabled)
                },
                errorColor = when {
                    errorColor != null -> DynamicColor.valueOf(default = errorColor)
                    else -> ResourceColor.valueOf(R.color.primer_error)
                },
                border = styledMainButtonBorder,
                text = styledMainButtonText,
                cornerRadius = ResourceDimension.valueOf(R.dimen.primer_main_button_corner_radius)
            )

            val styledSystemText = TextTheme(
                defaultColor = when {
                    mainColor != null -> DynamicColor.valueOf(default = mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_system_text)
                },
                fontSize = ResourceDimension.valueOf(R.dimen.primer_subtitle_fontsize)
            )

            val styledDefaultText = TextTheme(
                defaultColor = ResourceColor.valueOf(R.color.primer_default_text),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_default_fontsize)
            )

            val styledErrorText = TextTheme(
                defaultColor = errorColor?.let { DynamicColor.valueOf(it) }
                    ?: ResourceColor.valueOf(R.color.primer_error),
                fontSize = ResourceDimension.valueOf(R.dimen.primer_text_size_sm)
            )

            val styledInputBorder = BorderTheme(
                defaultColor = when {
                    bordersColor != null -> DynamicColor.valueOf(default = bordersColor)
                    mainColor != null -> DynamicColor.valueOf(default = mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_input_border)
                },
                selectedColor = when {
                    bordersColor != null -> DynamicColor.valueOf(default = bordersColor)
                    mainColor != null -> DynamicColor.valueOf(default = mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_input_border_selected)
                },
                errorColor = when {
                    errorColor != null -> DynamicColor.valueOf(default = errorColor)
                    else -> ResourceColor.valueOf(R.color.primer_input_border_error)
                },
                width = ResourceDimension.valueOf(R.dimen.primer_input_border_width)
            )

            val styledInputText = TextTheme(
                defaultColor = when {
                    textColor != null -> DynamicColor.valueOf(textColor)
                    mainColor != null -> DynamicColor.valueOf(mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_input_text)
                },
                fontSize = ResourceDimension.valueOf(R.dimen.primer_input_fontsize)
            )

            val styledHintInputText = TextTheme(
                defaultColor = when {
                    textColor != null -> DynamicColor.valueOf(textColor)
                    mainColor != null -> DynamicColor.valueOf(mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_subtitle)
                },
                fontSize = ResourceDimension.valueOf(R.dimen.primer_input_fontsize)
            )

            val styledInput = InputTheme(
                backgroundColor = when {
                    backgroundColor != null -> DynamicColor.valueOf(default = backgroundColor)
                    else -> ResourceColor.valueOf(R.color.primer_input_background)
                },
                border = styledInputBorder,
                text = styledInputText,
                hintText = styledHintInputText,
                cornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius)
            )

            val styledSearchInputText = TextTheme(
                defaultColor = when {
                    textColor != null -> DynamicColor.valueOf(textColor)
                    mainColor != null -> DynamicColor.valueOf(mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_search_input_text)
                },
                fontSize = ResourceDimension.valueOf(R.dimen.primer_search_input_fontsize)
            )

            val styledSearchHintInputText = TextTheme(
                defaultColor = when {
                    textColor != null -> DynamicColor.valueOf(textColor)
                    mainColor != null -> DynamicColor.valueOf(mainColor)
                    else -> ResourceColor.valueOf(R.color.primer_subtitle)
                },
                fontSize = ResourceDimension.valueOf(R.dimen.primer_search_input_fontsize)
            )

            val searchInputTheme = SearchInputTheme(
                backgroundColor = when {
                    backgroundColor != null -> DynamicColor.valueOf(default = backgroundColor)
                    else -> ResourceColor.valueOf(R.color.primer_search_input_background)
                },
                text = styledSearchInputText,
                hintText = styledSearchHintInputText,
                cornerRadius = ResourceDimension.valueOf(R.dimen.primer_default_corner_radius)
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
                searchInput = searchInputTheme,
                windowMode = WindowMode.BOTTOM_SHEET,
                inputMode = InputMode.OUTLINED
            )
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(isDarkMode)
        parcel.writeParcelable(primaryColor, flags)
        parcel.writeParcelable(backgroundColor, flags)
        parcel.writeParcelable(splashColor, flags)
        parcel.writeParcelable(titleText, flags)
        parcel.writeParcelable(amountLabelText, flags)
        parcel.writeParcelable(subtitleText, flags)
        parcel.writeParcelable(paymentMethodButton, flags)
        parcel.writeParcelable(mainButton, flags)
        parcel.writeParcelable(systemText, flags)
        parcel.writeParcelable(defaultText, flags)
        parcel.writeParcelable(errorText, flags)
        parcel.writeParcelable(input, flags)
        parcel.writeParcelable(searchInput, flags)
    }

    override fun describeContents(): Int {
        return 0
    }
}

// Developer models

data class ButtonThemeData(
    @ColorRes val defaultColor: Int? = null,
    @ColorRes val disabledColor: Int? = null,
    @ColorRes val errorColor: Int? = null,
    val text: TextThemeData? = null,
    val border: BorderThemeData? = null,
    @DimenRes val cornerRadius: Int? = null
)

data class TextThemeData(
    @ColorRes val defaultColor: Int? = null,
    @DimenRes val fontsize: Int? = null
)

data class BorderThemeData(
    @ColorRes val defaultColor: Int? = null,
    @ColorRes val selectedColor: Int? = null,
    @ColorRes val errorColor: Int? = null,
    @DimenRes val width: Int? = null
)

data class InputThemeData(
    @ColorRes val backgroundColor: Int? = null,
    val text: TextThemeData? = null,
    val hintText: TextThemeData? = null,
    val border: BorderThemeData? = null,
    @DimenRes val cornerRadius: Int? = null
)

data class SearchInputThemeData(
    @ColorRes val backgroundColor: Int? = null,
    val text: TextThemeData? = null,
    val hintText: TextThemeData? = null,
    @DimenRes val cornerRadius: Int? = null
)

// Internal models

internal data class ButtonTheme(
    val defaultColor: ColorData,
    val disabledColor: ColorData,
    val errorColor: ColorData,
    val text: TextTheme,
    val border: BorderTheme,
    val cornerRadius: DimensionData
) : Parcelable {
    constructor(parcel: Parcel) : this(
        defaultColor = parcel.readParcelable<ColorData>()!!,
        disabledColor = parcel.readParcelable<ColorData>()!!,
        errorColor = parcel.readParcelable<ColorData>()!!,
        text = parcel.readParcelable<TextTheme>()!!,
        border = parcel.readParcelable<BorderTheme>()!!,
        cornerRadius = parcel.readParcelable<DimensionData>()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(defaultColor, flags)
        parcel.writeParcelable(disabledColor, flags)
        parcel.writeParcelable(errorColor, flags)
        parcel.writeParcelable(text, flags)
        parcel.writeParcelable(border, flags)
        parcel.writeParcelable(cornerRadius, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ButtonTheme> {
        override fun createFromParcel(parcel: Parcel): ButtonTheme {
            return ButtonTheme(parcel)
        }

        override fun newArray(size: Int): Array<ButtonTheme?> {
            return arrayOfNulls(size)
        }
    }
}

internal data class TextTheme(
    val defaultColor: ColorData,
    val fontSize: DimensionData
) : Parcelable {
    constructor(parcel: Parcel) : this(
        defaultColor = parcel.readParcelable<ColorData>()!!,
        fontSize = parcel.readParcelable<DimensionData>()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(defaultColor, flags)
        parcel.writeParcelable(fontSize, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TextTheme> {
        override fun createFromParcel(parcel: Parcel): TextTheme {
            return TextTheme(parcel)
        }

        override fun newArray(size: Int): Array<TextTheme?> {
            return arrayOfNulls(size)
        }
    }
}

internal data class BorderTheme(
    val defaultColor: ColorData,
    val selectedColor: ColorData,
    val errorColor: ColorData,
    val width: DimensionData
) : Parcelable {
    constructor(parcel: Parcel) : this(
        defaultColor = parcel.readParcelable<ColorData>()!!,
        selectedColor = parcel.readParcelable<ColorData>()!!,
        errorColor = parcel.readParcelable<ColorData>()!!,
        width = parcel.readParcelable<DimensionData>()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(defaultColor, flags)
        parcel.writeParcelable(selectedColor, flags)
        parcel.writeParcelable(errorColor, flags)
        parcel.writeParcelable(width, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BorderTheme> {
        override fun createFromParcel(parcel: Parcel): BorderTheme {
            return BorderTheme(parcel)
        }

        override fun newArray(size: Int): Array<BorderTheme?> {
            return arrayOfNulls(size)
        }
    }
}

internal data class InputTheme(
    val backgroundColor: ColorData,
    val cornerRadius: DimensionData,
    val text: TextTheme,
    val hintText: TextTheme,
    val border: BorderTheme
) : Parcelable {
    constructor(parcel: Parcel) : this(
        backgroundColor = parcel.readParcelable<ColorData>()!!,
        cornerRadius = parcel.readParcelable<DimensionData>()!!,
        text = parcel.readParcelable<TextTheme>()!!,
        hintText = parcel.readParcelable<TextTheme>()!!,
        border = parcel.readParcelable<BorderTheme>()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(backgroundColor, flags)
        parcel.writeParcelable(cornerRadius, flags)
        parcel.writeParcelable(text, flags)
        parcel.writeParcelable(hintText, flags)
        parcel.writeParcelable(border, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InputTheme> {
        override fun createFromParcel(parcel: Parcel): InputTheme {
            return InputTheme(parcel)
        }

        override fun newArray(size: Int): Array<InputTheme?> {
            return arrayOfNulls(size)
        }
    }
}

internal data class SearchInputTheme(
    val backgroundColor: ColorData,
    val cornerRadius: DimensionData,
    val text: TextTheme,
    val hintText: TextTheme
) : Parcelable {
    constructor(parcel: Parcel) : this(
        backgroundColor = parcel.readParcelable<ColorData>()!!,
        cornerRadius = parcel.readParcelable<DimensionData>()!!,
        text = parcel.readParcelable<TextTheme>()!!,
        hintText = parcel.readParcelable<TextTheme>()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(backgroundColor, flags)
        parcel.writeParcelable(cornerRadius, flags)
        parcel.writeParcelable(text, flags)
        parcel.writeParcelable(hintText, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchInputTheme> {
        override fun createFromParcel(parcel: Parcel): SearchInputTheme {
            return SearchInputTheme(parcel)
        }

        override fun newArray(size: Int): Array<SearchInputTheme?> {
            return arrayOfNulls(size)
        }
    }
}
