package io.primer.sample.demos

// Level 1 - Basic UI Overrides
object FullscreenDemo : CheckoutDemo(
    title = "Fullscreen Container",
    description = "Override container to display checkout in fullscreen",
    customizationLevel = 1,
    render = {
        container = { content ->
            content()
        }
    }
)
