package io.primer.android.accessibility

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.primer.android.internal.presentation.components.DefaultSubmitButton
import io.primer.android.internal.presentation.preview.PreviewContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for DefaultSubmitButton component.
 */
@RunWith(AndroidJUnit4::class)
class DefaultSubmitButtonAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hasCorrectTestTag() {
        composeTestRule.setContent {
            PreviewContainer {
                DefaultSubmitButton(
                    isLoading = false,
                    enabled = true,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag("primer_submit_button")
            .assertIsDisplayed()
    }

    @Test
    fun hasContentDescription() {
        composeTestRule.setContent {
            PreviewContainer {
                DefaultSubmitButton(
                    isLoading = false,
                    enabled = true,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag("primer_submit_button")
            .assertContentDescriptionEquals("Submit payment")
    }

    @Test
    fun hasStateDescription_whenLoading() {
        composeTestRule.setContent {
            PreviewContainer {
                DefaultSubmitButton(
                    isLoading = true,
                    enabled = true,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNode(
                hasTestTag("primer_submit_button") and
                    hasStateDescription("Processing payment, please wait"),
            )
            .assertIsDisplayed()
    }

    @Test
    fun hasStateDescription_whenDisabled() {
        composeTestRule.setContent {
            PreviewContainer {
                DefaultSubmitButton(
                    isLoading = false,
                    enabled = false,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNode(
                hasTestTag("primer_submit_button") and
                    hasStateDescription("Button disabled. Complete all required fields to enable payment"),
            )
            .assertIsDisplayed()
    }

    @Test
    fun hasNoStateDescription_whenEnabled() {
        composeTestRule.setContent {
            PreviewContainer {
                DefaultSubmitButton(
                    isLoading = false,
                    enabled = true,
                    onClick = {},
                )
            }
        }

        // When enabled and not loading, there should be no state description
        composeTestRule
            .onNodeWithTag("primer_submit_button")
            .assertIsDisplayed()

        // Verify it does NOT have the disabled or loading state descriptions
        composeTestRule
            .onNode(
                hasTestTag("primer_submit_button") and
                    hasStateDescription("Processing payment, please wait"),
            )
            .assertDoesNotExist()

        composeTestRule
            .onNode(
                hasTestTag("primer_submit_button") and
                    hasStateDescription("Button disabled. Complete all required fields to enable payment"),
            )
            .assertDoesNotExist()
    }
}
