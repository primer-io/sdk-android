package io.primer.composable

// TODO Overridable Composable functions with default values are not currently supported
//interface Primer {
//
//    fun configure(
//        clientToken: String,
//        settings: PrimerSettings = PrimerSettings(),
//    )
//
//    @Composable
//    fun ComposableCheckout(
//        loadingScreen: (@Composable () -> Unit)? = null,
//        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null,
//        cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)? = null,
//        successScreen: (@Composable () -> Unit)? = null,
//        errorScreen: (@Composable (cause: PrimerError) -> Unit)? = null
//    )
//
//    companion object {
//        val instance: Primer = PrimerImpl()
//    }
//
//}
