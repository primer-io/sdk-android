package io.primer.sample

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Shapes
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgument
import com.google.gson.GsonBuilder
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.assets.PrimerPaymentMethodAsset
import io.primer.android.components.ui.assets.PrimerPaymentMethodNativeView
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.qrcode.QrCodeCheckoutAdditionalInfo
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import io.primer.sample.databinding.FragmentHeadlessBinding
import io.primer.sample.datamodels.CheckoutDataWithError
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toMappedError
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.utils.showMandateDialog
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.UiState
import io.primer.ui_components.PaymentBottomSheetFlow
import io.primer.ui_components.PaymentEvent
import io.primer.ui_components.PaymentFlowContainer
import io.primer.ui_components.PaymentFlowScopeY
import io.primer.ui_components.PaymentFlowScopeZ
import io.primer.ui_components.PaymentStateX
import io.primer.ui_components.PrimerCheckout
import io.primer.ui_components.PrimerPaymentFlowController
import io.primer.ui_components.PrimerPaymentMethodButtonComponent
import io.primer.ui_components.PrimerPaymentMethodComponent
import io.primer.ui_components.PrimerPaymentMethodDynamicComponent
import io.primer.ui_components.hasCustomUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HeadlessComponentsFragment : Fragment() {

    private val callbacks get() = headlessManagerViewModel.callbacks
    private var checkoutDataWithError: CheckoutDataWithError? = null

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel

    private lateinit var binding: FragmentHeadlessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHeadlessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeClientToken()
        observePaymentMethodsLoaded()
        observeUiState()
        configureTypeToggleViews()

        if (savedInstanceState == null) {
            viewModel.fetchClientSession()
            showLoading("Loading client token.")
        }

        observeTransactionState()
    }

    override fun onDestroy() {
        super.onDestroy()
        headlessManagerViewModel.isLaunched = false
        viewModel.setClientToken(null)
    }

    private fun initViewModel() {
        headlessManagerViewModel = ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]
    }

    private fun observeClientToken() {
        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (headlessManagerViewModel.isLaunched != true) {
                token?.let {
                    headlessManagerViewModel.isLaunched = true
                    headlessManagerViewModel.startHeadless(
                        requireContext(), token, viewModel.settings
                    )
                }
            } else {
                headlessManagerViewModel.setHeadlessListeners()
            }
        }
    }

    private fun observePaymentMethodsLoaded() {
        headlessManagerViewModel.paymentMethodsLoaded.observe(viewLifecycleOwner) {
            binding.typeButtonGroup.isVisible = true
            setupPaymentMethod()
            hideLoading()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.InitializingHeadless -> showLoading("Initializing Headless.")
                    is UiState.InitializedHeadless -> hideLoading()
                    is UiState.TokenizationStarted -> {
                        showLoading("Tokenization started ${state.paymentMethodType}")
                    }

                    is UiState.PreparationStarted -> {
                        showLoading("Preparation started ${state.paymentMethodType}")
                    }

                    is UiState.PaymentMethodShowed -> {
                        showLoading("Presented ${state.paymentMethodType}")
                    }

                    is UiState.TokenizationSuccessReceived -> {
                        if (state.paymentMethodTokenData.isVaulted) {
                            hideLoading()
                            navigateToResultScreen()
                        } else {
                            showLoading("Tokenization success ${state.paymentMethodTokenData}. Creating payment.")
                            headlessManagerViewModel.createPayment(
                                paymentMethod = state.paymentMethodTokenData,
                                environment = requireNotNull(viewModel.environment.value),
                                descriptor = viewModel.descriptor.value.orEmpty(),
                                vaultOnSuccess = viewModel.vaultOnSuccess,
                                vaultOnAgreement = viewModel.vaultOnAgreement,
                                completion = state.decisionHandler
                            )
                        }
                    }

                    is UiState.ResumePaymentReceived -> {
                        showLoading("Resume success. Resuming payment.")
                        headlessManagerViewModel.resumePayment(
                            state.resumeToken,
                            requireNotNull(viewModel.environment.value),
                            state.decisionHandler
                        )
                    }

                    is UiState.ResumePendingReceived -> {
                        hideLoading()
                        when (state.additionalInfo) {
                            is MultibancoCheckoutAdditionalInfo -> {
                                AlertDialog.Builder(context)
                                    .setMessage("MultibancoData: $state.additionalInfo")
                                    .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                                Log.d(
                                    TAG,
                                    "onResumePending MULTIBANCO: $state.additionalInfo"
                                )
                            }
                        }
                    }

                    is UiState.AdditionalInfoReceived -> {
                        hideLoading()
                        when (state.additionalInfo) {
                            is QrCodeCheckoutAdditionalInfo -> {
                                AlertDialog.Builder(context)
                                    .setMessage("QrCode: ${state.additionalInfo}")
                                    .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                                Log.d(
                                    TAG,
                                    "onAdditionalInfoReceived: $state.additionalInfo"
                                )
                            }

                            is AchAdditionalInfo.ProvideActivityResultRegistry -> {
                                state.additionalInfo.provide(
                                    requireActivity().activityResultRegistry
                                )
                            }

                            is AchAdditionalInfo.DisplayMandate -> {
                                requireContext().showMandateDialog(
                                    text = "Would you like to accept this mandate?",
                                    onOkClick = {
                                        lifecycleScope.launch {
                                            state.additionalInfo.onAcceptMandate()
                                        }
                                    },
                                    onCancelClick = {
                                        lifecycleScope.launch {
                                            state.additionalInfo.onDeclineMandate()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    is UiState.BeforePaymentCreateReceived -> {
                        /* no-op */
                    }

                    is UiState.BeforeClientSessionUpdateReceived -> {
                        /* no-op */
                    }

                    is UiState.ClientSessionUpdatedReceived -> {
                        /* no-op */
                    }

                    is UiState.ShowError -> {
                        checkoutDataWithError =
                            CheckoutDataWithError(state.payment, state.error.toMappedError())
                        hideLoading()
                        navigateToResultScreen()
                    }

                    is UiState.CheckoutCompleted -> {
                        checkoutDataWithError =
                            CheckoutDataWithError(state.checkoutData.payment)
                        hideLoading()
                        navigateToResultScreen()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun observeTransactionState() {
        headlessManagerViewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> headlessManagerViewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }
    }

    private fun configureTypeToggleViews() {
        binding.typeButtonGroup.check(binding.checkout.id)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setupPaymentMethod() {
        binding.composeView.setContent {

            Box(modifier = Modifier.fillMaxSize()) {
//                paymentMethods.forEach { paymentMethod ->
//                    if (selectedPaymentMethod == paymentMethod) {
//                        PaymentMethodUi(paymentMethod = paymentMethod)
//                    } else {
//                        PaymentMethodButton(
//                            paymentMethod = paymentMethod,
//                            onMethodSelected = { selectedPaymentMethod = it })
//                    }
//                }
//                PaymentMethodList(onPaymentMethodSelected = {}) {
//                    // Custom UI for payment methods
//                    paymentMethods.forEach { method ->
//                        Text(method.paymentMethodName.orEmpty()) // Example rendering
//                    }
//                    Text(text = "uu")
//                }


//                PaymentBottomSheetFlow(onEvent = {}) {
//                    val myOnPaymentMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit = remember {
//                        {
//                            println("semirz" + it)
//                        }
//                    }
//                    Column {
//                        Text(text = "ado")
//                        PaymentMethodList(myOnPaymentMethodSelected) {
//                            // Custom UI for payment methods
//                            LazyRow {
//                                items(paymentMethods) { method ->
//                                    Button(onClick = { myOnPaymentMethodSelected(method) }) {
//                                        Text(method.paymentMethodName.orEmpty())
//                                        Text(text = "link")
//                                    }
//                                }
//                            }
//                            Text(text = "uu")
//                        }
//
//
//                    }
//                }

//                Column(modifier = Modifier.fillMaxSize()) {
//                    Box(
//                        modifier = Modifier
//                            .animateContentSize()
//                            .fillMaxWidth()
//                    ) {
//                        PaymentFlowContainer("sss") {
//                            Column() {
//                                Text(text = "Flaviu")
//                                TextField(value = "test", onValueChange = {})
//                                PaymentMethods(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    parentLayout = { methods, content ->
//                                        LazyRow(modifier = Modifier.fillMaxWidth()) {
//                                            items(methods) { method ->
//                                                content(method)
//                                            }
//                                        }
//                                    },
//                                    paymentMethodContent = { method ->
//                                        Column {
//                                            Row(modifier = Modifier.fillMaxWidth()) {
//                                                RadioButton(
//                                                    selected = false,
//                                                    onClick = { selectPaymentMethod(method) })
//                                                Text(text = method.paymentMethodName.orEmpty())
//                                            }
//                                        }
//                                    })
//
//
//                                paymentState.selectedMethod?.let {
//                                    ModalBottomSheet(onDismissRequest = { /*TODO*/ }) {
//                                        var dynamicPaymentState by
//                                            remember { mutableStateOf<PaymentFlowScopeY.PaymentMethodScope?>(null) }
//
//                                        if (dynamicPaymentState is PaymentFlowScopeY.PaymentMethodScope.Rendered) {
//                                            Text(text = "Please select to pay with Klarna")
//                                        }
//
//                                        DynamicPaymentMethodUI(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            method = it,
//                                            onStateChanged = { state, validationState ->
//                                                dynamicPaymentState = state
//                                            })
//
//                                        if (dynamicPaymentState is PaymentFlowScopeY.PaymentMethodScope.Initializing) {
//                                            CircularProgressIndicator()
//                                        }
//
//                                        if (dynamicPaymentState is PaymentFlowScopeY.PaymentMethodScope.Rendered) {
//                                            Button(onClick = {
//
//                                            }) {
//                                                Text(text = "Let me tell you how to pay")
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            if (globalState.isLoading) {
//                                Box(
//                                    modifier = Modifier
//                                        .background(Color.Black.copy(alpha = 0.6f))
//                                        .matchParentSize()
//                                        .animateContentSize()
//                                ) {
//                                    CircularProgressIndicator()
//                                }
//                            }


//                    PaymentMethods(modifier = Modifier
//                        .fillMaxWidth(), onPaymentMethodSelected = { selectedPaymentMethod ->
//                        selectedMethod = selectedPaymentMethod
//                    }, parentLayout = { methods, content ->
//                        LazyRow(modifier = Modifier.fillMaxWidth()) {
//                            items(methods) { method ->
//                                content(method)
//                            }
//                        }
//                    }, paymentMethodContent = { method ->
//                        Column {
//                            Row {
//                                RadioButton(
//                                    selected = selectedPaymentMethod == method,
//                                    onClick = { selectPaymentMethod(method) })
//                                PrimerPaymentMethodButtonComponent(paymentMethod = method) {
//                                    selectPaymentMethod(method)
//                                }
//                            }
//
//                            selectedMethod?.let {
//                                if (selectedMethod == method) {
//                                    CustomUiPaymentMethod(
//                                        flowController = flowController,
//                                        paymentMethod = it
//                                    )
//                                }
//                            }
//                        }
//                    })
                    //    }
                   // }
               // }


                PrimerCheckout(clientToken = "fwfw")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabLayoutCheckout() {
        PrimerCheckout(
            clientToken = "token",
            onPaymentCompleted = { /* Handle completion */ }
        ) {  // This is a PaymentFlowScope receiver
            val methods by paymentMethods.collectAsState()
            var selectedTabIndex by remember { mutableStateOf(0) }
            val selectedMethod by selectedMethod.collectAsState()

            Column(modifier = Modifier.fillMaxWidth()) {
                // Custom tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    methods.forEachIndexed { index, method ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                selectPaymentMethod(method)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                PrimerPaymentMethodButtonComponent(paymentMethod = method) {

                                }
                                Spacer(Modifier.height(8.dp))
                                Text(method.paymentMethodName.orEmpty())
                            }
                        }
                    }
                }

                // Content below tabs
                selectedMethod?.let { method ->
                   // ModalBottomSheet(onDismissRequest = { /*TODO*/ }) {
                        PaymentMethodContent(method) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    val coroutine = rememberCoroutineScope()
                                    // Payment form

                                    var input = remember {
                                        mutableStateOf("")
                                    }
                                    Text(text = "mate")
                                    Spacer(Modifier.height(16.dp))


                                    DefaultContent()

                                    Spacer(Modifier.height(16.dp))

                                    // Payment button
                                    val contentState by state.collectAsState()
                                    Button(
                                        onClick = {
                                            coroutine.launch { submit() }
                                        },
                                        enabled = contentState.validationState.isValid,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {

                                        Text("Pay with ${method.paymentMethodName.orEmpty()}")
                                    }
                                }
                            }
                        }
                  //  }
                }
            }
        }
    }

    // Merchant's navigation structure
    sealed class CheckoutRoute {
        data object OrderSummary : CheckoutRoute()
        data object PaymentMethods : CheckoutRoute()
        data class PaymentForm(val methodId: String) : CheckoutRoute()
        data object Confirmation : CheckoutRoute()
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

    @Composable
    private fun OrderSummaryScreen(
        onSelectPayment: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Order details...

            Button(
                onClick = onSelectPayment,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Payment Method")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PaymentMethodSelectionScreen(
        scope: PaymentFlowScopeZ,
        onMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
        onBackClick: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top bar
            TopAppBar(
                title = { Text("Select Payment Method") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )

            // Payment methods list
            val methods by scope.paymentMethods.collectAsState()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(methods) { method ->
                    PaymentMethodItem(
                        method = method,
                        onClick = { onMethodSelected(method) }
                    )
                }
            }
        }
    }

    @Composable
    private fun PaymentMethodItem(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Icon and name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = method.paymentMethodType,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PaymentFormScreen(
        scope: PaymentFlowScopeZ,
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        onBackClick: () -> Unit
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = { Text("Enter Card Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )

            scope.PaymentMethodContent(method) {
                val coroutine = rememberCoroutineScope()
                Column(modifier = Modifier.padding(16.dp)) {
                    // Payment form
                    DefaultContent()

                    Spacer(Modifier.height(16.dp))

                    // Pay button
                    val contentState by state.collectAsState()
                    if (contentState.isLoading.not()) {
                        Button(
                            onClick = {
                                coroutine.launch { submit() }
                            },
                            enabled = contentState.validationState.isValid,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pay with ${method.paymentMethodName.orEmpty()}")
                        }
                    }
                }
            }
        }
    }

    // Usage in merchant's app
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

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.progressLayoutRoot.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.progressLayoutRoot.isVisible = false
    }

    private fun onPaymentMethodSelectedx(paymentMethodType: String) {
        callbacks.clear()
        checkoutDataWithError = null
        try {
            val nativeUiManager =
                PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType)
                    .also {
                        headlessManagerViewModel.addCloseable {
                            it.cleanup()
                        }
                    }
            nativeUiManager.showPaymentMethod(requireContext(), getPrimerSessionIntent())
        } catch (e: SdkUninitializedException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        } catch (e: UnsupportedPaymentIntentException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        }
    }

    private fun getPrimerSessionIntent() = when (binding.typeButtonGroup.checkedButtonId) {
        binding.checkout.id -> PrimerSessionIntent.CHECKOUT
        else -> PrimerSessionIntent.VAULT
    }

    private fun navigateToResultScreen() {
        headlessManagerViewModel.resetUiState()
        findNavController().navigate(
            R.id.action_HeadlessComponentsFragment_to_MerchantResultFragment,
            Bundle().apply {
                putInt(
                    MerchantResultFragment.PAYMENT_STATUS_KEY,
                    if (checkoutDataWithError?.error != null) MerchantResultFragment.Companion.PaymentStatus.FAILURE.ordinal
                    else MerchantResultFragment.Companion.PaymentStatus.SUCCESS.ordinal
                )
                putStringArrayList(
                    MerchantResultFragment.INVOKED_CALLBACKS_KEY,
                    ArrayList(callbacks)
                )
                putString(
                    MerchantResultFragment.PAYMENT_RESPONSE_KEY,
                    GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                        .toJson(checkoutDataWithError)
                )
            })
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
