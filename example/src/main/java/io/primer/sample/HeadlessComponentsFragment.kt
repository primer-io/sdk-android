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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.navigation.fragment.findNavController
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
import io.primer.android.klarna.api.component.KlarnaComponent
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
import io.primer.ui_components.PaymentStateX
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

                val flowController: PrimerPaymentFlowController = PrimerPaymentFlowController.provideInstance(
                    owner = LocalViewModelStoreOwner.current ?: error("...")
                )

                Column {
                    Box(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                    ) {
                        PaymentFlowContainer("sss") {
                            Column {
                                Text(text = "Flaviu")
                                TextField(value = "test", onValueChange = {})
                                PaymentMethods(
                                    modifier = Modifier.fillMaxWidth(),
                                    parentLayout = { methods, content ->
                                        LazyRow(modifier = Modifier.fillMaxWidth()) {
                                            items(methods) { method ->
                                                content(method)
                                            }
                                        }
                                    },
                                    paymentMethodContent = { method ->
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                RadioButton(
                                                    selected = false,
                                                    onClick = { selectPaymentMethod(method) })
                                                Text(text = method.paymentMethodName.orEmpty())
                                            }
                                        }
                                    })


                                paymentState.selectedMethod?.let {
                                    ModalBottomSheet(onDismissRequest = { /*TODO*/ }) {
                                        Text(text = "Please select to pay with Klarna")
                                        DynamicPaymentMethodUI(
                                            modifier = Modifier.fillMaxWidth(),
                                            method = it,
                                            onStateChanged = { state ->
                                                when(state) {
                                                    PaymentFlowScopeY.PaymentMethodScope.Initializing -> CircularProgressIndicator()
                                                    PaymentFlowScopeY.PaymentMethodScope.Rendered -> {}
                                                    is PaymentFlowScopeY.PaymentMethodScope.ValidationState -> TODO()
                                                }
                                            })
                                    }
                                }
                            }

                            if (globalState.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .matchParentSize()
                                        .animateContentSize()
                                ) {
                                    CircularProgressIndicator()
                                }
                            }


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
                        }
                    }
                }
            }
        }
    }

                @Composable
                fun PrimerPaymentFlowComponent(
                    paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
                    onPaymentMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
                    customContent: @Composable (selectedMethod: PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
                    modifier: Modifier = Modifier
                ) {
                    // Keep track of the selected payment method
                    var selectedPaymentMethod by remember { mutableStateOf<PrimerHeadlessUniversalCheckoutPaymentMethod?>(null) }

                    // Render the payment method buttons
                    Column(modifier = modifier) {
                        paymentMethods.forEach { paymentMethod ->
                            if (selectedPaymentMethod == paymentMethod) {
                                customContent(paymentMethod)
                                PaymentMethodUi(paymentMethod = paymentMethod)
                            } else {
                                PrimerPaymentMethodButtonComponent(
                                    paymentMethod = paymentMethod,
                                    onMethodSelected = { selectedMethod ->
                                        selectedPaymentMethod = selectedMethod
                                        onPaymentMethodSelected(selectedMethod) // Call the callback
                                    }
                                )
                            }
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
