package io.primer.sample

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.GsonBuilder
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
import io.primer.components.ui.checkout.PrimerCheckout
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

    private fun setupPaymentMethod() {
        binding.composeView.setContent {
            PrimerCheckout(clientToken = "fwfw")
        }
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.progressLayoutRoot.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.progressLayoutRoot.isVisible = false
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
