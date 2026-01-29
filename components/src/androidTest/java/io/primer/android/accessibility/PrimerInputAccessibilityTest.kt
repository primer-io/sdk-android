package io.primer.android.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.preview.PreviewContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for PrimerInput component.
 */
@RunWith(AndroidJUnit4::class)
class PrimerInputAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hasTestTag_whenModifierWithTestTagProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    modifier = Modifier.testTag("card_number_input"),
                    value = "",
                    onValueChange = {},
                    accessibilityLabel = "Card number",
                )
            }
        }

        composeTestRule
            .onNodeWithTag("card_number_input")
            .assertIsDisplayed()
    }

    @Test
    fun hasContentDescription_withLabel() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    modifier = Modifier.testTag("card_number_input"),
                    value = "",
                    onValueChange = {},
                    accessibilityLabel = "Card number",
                )
            }
        }

        composeTestRule
            .onNodeWithTag("card_number_input")
            .assertContentDescriptionEquals("Card number")
    }

    @Test
    fun hasContentDescription_withRequired() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    modifier = Modifier.testTag("card_number_input"),
                    value = "",
                    onValueChange = {},
                    accessibilityLabel = "Card number",
                    isRequired = true,
                )
            }
        }

        // Should combine label + "required"
        composeTestRule
            .onNodeWithTag("card_number_input")
            .assertContentDescriptionEquals("Card number, required")
    }

    @Test
    fun hasContentDescription_withLabel_notRequired() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    modifier = Modifier.testTag("card_number_input"),
                    value = "",
                    onValueChange = {},
                    accessibilityLabel = "Cardholder name",
                    isRequired = false,
                )
            }
        }

        // Should only have label (no "required")
        composeTestRule
            .onNodeWithTag("card_number_input")
            .assertContentDescriptionEquals("Cardholder name")
    }

    @Test
    fun errorMessage_isDisplayed() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    value = "123",
                    onValueChange = {},
                    error = "Invalid card number",
                    accessibilityLabel = "Card number",
                )
            }
        }

        composeTestRule
            .onNodeWithText("Invalid card number")
            .assertIsDisplayed()
    }

    @Test
    fun errorMessage_hasLiveRegion() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerInput(
                    value = "123",
                    onValueChange = {},
                    error = "Invalid card number",
                    accessibilityLabel = "Card number",
                )
            }
        }

        // Error text should have liveRegion for TalkBack announcement
        composeTestRule
            .onNode(
                hasText("Invalid card number") and hasLiveRegion(),
            )
            .assertIsDisplayed()
    }
}

/**
 * Custom matcher to check if a node has a live region set.
 */
private fun hasLiveRegion(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
}
