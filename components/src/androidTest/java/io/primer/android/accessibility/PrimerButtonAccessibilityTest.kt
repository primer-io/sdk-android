package io.primer.android.accessibility

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.preview.PreviewContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for PrimerButton component.
 */
@RunWith(AndroidJUnit4::class)
class PrimerButtonAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hasTestTag_whenModifierWithTestTagProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerButton(
                    modifier = Modifier.testTag("test_button"),
                    onClick = {},
                ) {
                    Text("Pay Now")
                }
            }
        }

        composeTestRule
            .onNodeWithTag("test_button")
            .assertIsDisplayed()
    }

    @Test
    fun hasContentDescription_whenAccessibilityLabelProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerButton(
                    modifier = Modifier.testTag("test_button"),
                    onClick = {},
                    accessibilityLabel = "Submit payment",
                ) {
                    Text("Pay Now")
                }
            }
        }

        composeTestRule
            .onNodeWithTag("test_button")
            .assertContentDescriptionEquals("Submit payment")
    }

    @Test
    fun hasStateDescription_whenDisabled() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerButton(
                    modifier = Modifier.testTag("test_button"),
                    onClick = {},
                    enabled = false,
                    accessibilityStateDescription = "Button disabled",
                ) {
                    Text("Pay Now")
                }
            }
        }

        composeTestRule
            .onNode(hasTestTag("test_button") and hasStateDescription("Button disabled"))
            .assertIsDisplayed()
    }

    @Test
    fun hasStateDescription_whenCustomStateProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerButton(
                    modifier = Modifier.testTag("test_button"),
                    onClick = {},
                    accessibilityStateDescription = "Processing payment",
                ) {
                    Text("Pay Now")
                }
            }
        }

        composeTestRule
            .onNode(hasTestTag("test_button") and hasStateDescription("Processing payment"))
            .assertIsDisplayed()
    }

    @Test
    fun hasMinimumTouchTargetSize() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerButton(
                    modifier = Modifier.testTag("test_button"),
                    onClick = {},
                ) {
                    Text("Pay")
                }
            }
        }

        // WCAG 2.5.5 requires minimum 44x44dp (Android recommends 48dp but 44dp is compliant)
        composeTestRule
            .onNodeWithTag("test_button")
            .assertHeightIsAtLeast(44.dp)
    }
}
