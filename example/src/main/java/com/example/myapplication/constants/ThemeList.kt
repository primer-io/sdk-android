package com.example.myapplication.constants

import android.content.res.Configuration
import com.example.myapplication.R
import io.primer.android.ui.settings.BorderThemeData
import io.primer.android.ui.settings.ButtonThemeData
import io.primer.android.ui.settings.InputThemeData
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.ui.settings.TextThemeData

class ThemeList {
    companion object {
        fun themeBySystem(configuration: Configuration?): PrimerTheme {
            if (configuration == null) return PrimerTheme.build()
            return when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> darkTheme
                else -> PrimerTheme.build(isDarkMode = false)
            }
        }

        val darkTheme = PrimerTheme.build(
            isDarkMode = true,
            primaryColor = R.color.apple_dark_6,
            backgroundColor = R.color.apple_dark_4,
            disabledColor = R.color.apple_dark_3,
            defaultCornerRadius = R.dimen.sheet_corner_radius,
            bottomSheetCornerRadius = R.dimen.sheet_corner_radius,
            input = InputThemeData(
                border = BorderThemeData(
                    defaultColor = R.color.apple_dark_1,
                ),
                text = TextThemeData(
                    defaultColor = R.color.apple_dark_5,
                    fontsize = R.dimen.primer_title_fontsize
                )
            ),
            mainButton = ButtonThemeData(
                defaultColor = R.color.apple_dark_6,
                text = TextThemeData(
                    defaultColor = R.color.apple_dark_5,
                    fontsize = R.dimen.primer_title_fontsize
                ),
            ),
            inputMode = PrimerTheme.InputMode.OUTLINED,
            titleText = TextThemeData(defaultColor = R.color.apple_dark_5),
            amountLabelText = TextThemeData(defaultColor = R.color.apple_dark_5),
            subtitleText = TextThemeData(defaultColor = R.color.apple_dark_1),
            paymentMethodButton = ButtonThemeData(
                defaultColor = R.color.apple_dark_4,
                text = TextThemeData(
                    defaultColor = R.color.apple_dark_5
                ),
                border = BorderThemeData(
                    defaultColor = R.color.apple_dark_3,
                    selectedColor = R.color.apple_dark_6,
                    width = R.dimen.border_width
                ),
            ),
            systemButton = TextThemeData(
                defaultColor = R.color.apple_dark_6
            )
        )

        val tropicalTheme = PrimerTheme.build(
            primaryColor = R.color.tropical_10,
            backgroundColor = R.color.tropical_1,
            disabledColor = R.color.tropical_8,
            bottomSheetCornerRadius = R.dimen.sheet_corner_radius,
            input = InputThemeData(
                border = BorderThemeData(
                    defaultColor = R.color.tropical_3,
                ),
                text = TextThemeData(
                    defaultColor = R.color.tropical_3,
                    fontsize = R.dimen.primer_title_fontsize
                )
            ),
            mainButton = ButtonThemeData(
                defaultColor = R.color.tropical_2,
                text = TextThemeData(
                    defaultColor = R.color.tropical_4,
                    fontsize = R.dimen.primer_title_fontsize
                ),
            ),
            inputMode = PrimerTheme.InputMode.UNDERLINED,
            titleText = TextThemeData(defaultColor = R.color.tropical_10),
            amountLabelText = TextThemeData(defaultColor = R.color.tropical_10),
            subtitleText = TextThemeData(defaultColor = R.color.tropical_3),
            paymentMethodButton = ButtonThemeData(
                defaultColor = R.color.tropical_9,
                text = TextThemeData(
                    defaultColor = R.color.tropical_3
                ),
                border = BorderThemeData(
                    defaultColor = R.color.tropical_9,
                    selectedColor = R.color.tropical_3,
                ),
            ),
            systemButton = TextThemeData(
                defaultColor = R.color.tropical_4
            )
        )
    }
}