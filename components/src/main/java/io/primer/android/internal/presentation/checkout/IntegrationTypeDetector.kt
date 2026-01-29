package io.primer.android.internal.presentation.checkout

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import io.primer.android.components.analytics.data.model.IntegrationType

internal object IntegrationTypeDetector {

    /**
     * Detects integration type by analyzing view hierarchy from LocalView.current.
     *
     * @return COMPOSE_VIEW for hybrid (ComposeView in XML), COMPOSABLE for pure Compose (setContent)
     */
    fun detectIntegrationType(view: View): IntegrationType {
        // Walk up to find ComposeView (guaranteed to exist since called from @Composable)
        var currentView: View? = view
        var composeView: ComposeView? = null

        while (currentView != null) {
            if (currentView is ComposeView) {
                composeView = currentView
                break
            }
            currentView = currentView.parent as? View
        }

        // Count custom ViewGroups between ComposeView and android.R.id.content
        var parent = composeView?.parent as? View
        var customViewGroupCount = 0

        while (parent != null) {
            // Stop at activity's content view (boundary)
            if (parent.id == android.R.id.content) {
                break
            }

            // Count merchant's ViewGroups (excludes ComposeView itself)
            if (parent is ViewGroup && parent !is ComposeView) {
                customViewGroupCount++
            }

            parent = parent.parent as? View
        }

        // Decide based on custom ViewGroup presence
        return if (customViewGroupCount > 0) {
            IntegrationType.COMPOSE_VIEW // Hybrid: merchant XML layouts detected
        } else {
            IntegrationType.COMPOSABLE // Pure Compose: direct under content
        }
    }
}
