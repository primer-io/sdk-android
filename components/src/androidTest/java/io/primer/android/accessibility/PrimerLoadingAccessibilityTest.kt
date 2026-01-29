package io.primer.android.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.primer.android.internal.presentation.components.PrimerLoading
import io.primer.android.internal.presentation.preview.PreviewContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for PrimerLoading component.
 */
@RunWith(AndroidJUnit4::class)
class PrimerLoadingAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hasCustomTestTag_whenTestIdProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerLoading(
                    modifier = Modifier.testTag("test_loading")
                )
            }
        }

        composeTestRule
            .onNodeWithTag("test_loading")
            .assertIsDisplayed()
    }

    @Test
    fun hasContentDescription_whenAccessibilityLabelProvided() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerLoading(
                    modifier = Modifier.testTag("test_loading"),
                    accessibilityLabel = "Loading payment methods",
                )
            }
        }

        composeTestRule
            .onNodeWithTag("test_loading")
            .assertContentDescriptionEquals("Loading payment methods")
    }

    @Test
    fun hasLiveRegion_forTalkBackAnnouncement() {
        composeTestRule.setContent {
            PreviewContainer {
                PrimerLoading(
                    modifier = Modifier.testTag("test_loading")
                )
            }
        }

        // Loading indicator should have liveRegion for TalkBack to announce it
        composeTestRule
            .onNode(hasTestTag("test_loading") and hasLiveRegionPolite())
            .assertIsDisplayed()
    }
}

/**
 * Custom matcher to check if a node has LiveRegionMode.Polite set.
 */
private fun hasLiveRegionPolite(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
}
