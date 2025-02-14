package io.primer.components.examples.navigation

/**
 * To use this example, it needs to be moved to the example app module. we don't import the navigation library in the ui components module
 */
/*
@Composable
fun MerchantApp() {
    val navController = rememberNavController()

    // Main merchant navigation
    NavHost(navController, startDestination = "checkout") {
        composable("home") {
            // Home screen
        }

        composable("cart") {
            // Cart screen with checkout button
            Button(onClick = {
                navController.navigate("checkout")
            }) {
                Text("Proceed to Checkout")
            }
        }

        composable("checkout") {
            // Our checkout flow embedded in merchant navigation
            MerchantCheckoutFlow()
        }
    }
}

@Composable
fun MerchantCheckoutFlow(
    navController: NavHostController = rememberNavController()
) {
    PrimerCheckout(clientToken = "token") {
        NavHost(navController, startDestination = "order_summary") {
            composable("order_summary") {
                OrderSummaryScreen(
                    onSelectPayment = {
                        navController.navigate("payment_methods")
                    }
                )
            }

            composable("payment_methods") {
                PaymentMethodSelectionScreen(
                    scope = this@PrimerCheckout,
                    onMethodSelected = { method ->
                        selectPaymentMethod(method)
                        if (method.paymentMethodManagerCategories.none { it == PrimerPaymentMethodManagerCategory.NATIVE_UI }) {
                            navController.navigate("payment_form/${method.paymentMethodType}")
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "payment_form/{methodId}",
                arguments = listOf(navArgument("methodId") { type = NavType.StringType })
            ) {
                val selectedMethod by selectedMethod.collectAsState()
                selectedMethod?.let { method ->
                    PaymentFormScreen(
                        scope = this@PrimerCheckout,
                        method = method,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
 */
