package io.primer.sample

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.GsonBuilder
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.qrcode.QrCodeCheckoutAdditionalInfo
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import io.primer.sample.constants.PrimerDropInCallbacks
import io.primer.sample.databinding.FragmentUniversalCheckoutBinding
import io.primer.sample.datamodels.CheckoutDataWithError
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toMappedError
import io.primer.sample.viewmodels.MainViewModel

class MerchantCheckoutFragment : Fragment() {

    private val callbacks: ArrayList<String> = arrayListOf()
    private var checkoutDataWithError: CheckoutDataWithError? = null
    private var _binding: FragmentUniversalCheckoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUniversalCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vaultButton.isVisible = false
        binding.checkoutButton.isVisible = false
        binding.intentToggleGroup.check(binding.checkout.id)

        // VAULT MANAGER
        binding.vaultButton.setOnClickListener {
            callbacks.clear()
            viewModel.mode.postValue(MainViewModel.Mode.VAULT)
            viewModel.clientToken.value?.let { token ->
                checkoutDataWithError = null
                callbacks.add(PrimerDropInCallbacks.ON_VAULT_MANAGER_CLICKED)
                activity?.let { context -> Primer.instance.showVaultManager(context, token) }
            }
        }

        // UNIVERSAL CHECKOUT
        binding.checkoutButton.setOnClickListener {
            viewModel.mode.postValue(MainViewModel.Mode.CHECKOUT)
            callbacks.clear()
            viewModel.clientToken.value?.let { token ->
                checkoutDataWithError = null
                callbacks.add(PrimerDropInCallbacks.ON_UNIVERSAL_CHECKOUT_CLICKED)
                activity?.let { context -> Primer.instance.showUniversalCheckout(context, token) }
            }
        }

        binding.showPaymentMethodButton.setOnClickListener {
            viewModel.clientToken.value?.let { token ->
                checkoutDataWithError = null
                callbacks.add(PrimerDropInCallbacks.ON_SHOW_PAYMENT_METHOD_BUTTON_CLICKED)
                activity?.let { context ->
                    Primer.instance.showPaymentMethod(
                        context,
                        token,
                        viewModel.useStandalonePaymentMethod.value!!,
                        when (binding.intentToggleGroup.checkedButtonId) {
                            binding.checkout.id -> PrimerSessionIntent.CHECKOUT
                            else -> PrimerSessionIntent.VAULT
                        }
                    )
                }
            }
        }

        binding.showPaymentMethodInput.doAfterTextChanged {
            viewModel.useStandalonePaymentMethod.postValue(it.toString())
        }

        viewModel.useStandalonePaymentMethod.observe(viewLifecycleOwner) { paymentMethodType ->
            binding.showPaymentMethodButton.isVisible = paymentMethodType.isNullOrBlank().not()
        }
        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            binding.intentToggleGroup.isVisible = token != null
            binding.walletProgressBar.isVisible = token == null
            binding.vaultButton.isVisible = token != null
            binding.showPaymentMethodInputLayout.isVisible = token != null

            binding.checkoutButton.isVisible = token != null
            if (token != null) {
                initializeCheckout()
            }
        }

        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> viewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        if (viewModel.clientToken.value == null ||
            viewModel.selectedFlow.value != MainViewModel.SelectedFlow.CLIENT_TOKEN
        ) {
            viewModel.fetchClientSession()
        }
    }

    private fun initializeCheckout() = viewModel.configure(listener)

    private val listener = object : PrimerCheckoutListener {

        override fun onBeforePaymentCreated(
            paymentMethodData: PrimerPaymentMethodData,
            decisionHandler: PrimerPaymentCreationDecisionHandler
        ) {
            decisionHandler.continuePaymentCreation()
            callbacks.add(PrimerDropInCallbacks.ON_BEFORE_PAYMENT_CREATED)
            Log.d(TAG, "onBeforePaymentCreated - $paymentMethodData")
        }

        override fun onFailed(
            error: PrimerError,
            checkoutData: PrimerCheckoutData?,
            errorHandler: PrimerErrorDecisionHandler?
        ) {
            Log.d(TAG, "onFailed - $error - $checkoutData")
            callbacks.add(PrimerDropInCallbacks.ON_FAILED_WITH_CHECKOUT_DATA)
            checkoutDataWithError =
                CheckoutDataWithError(checkoutData?.payment, error.toMappedError())
            errorHandler?.showErrorMessage(
                "SDK error id: ${error.errorId}, description: ${error.description}"
            )
        }

        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
            Log.d(TAG, "onCheckoutCompleted - $checkoutData")
            checkoutDataWithError = CheckoutDataWithError(checkoutData.payment)
            callbacks.add(PrimerDropInCallbacks.ON_CHECKOUT_COMPLETED)
        }

        override fun onBeforeClientSessionUpdated() {
            Log.d(TAG, "onBeforeClientSessionUpdated")
            callbacks.add(PrimerDropInCallbacks.ON_BEFORE_CLIENT_SESSION_UPDATED)
        }

        override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
            super.onClientSessionUpdated(clientSession)
            Log.d(TAG, "onClientSessionUpdated - $clientSession")
            callbacks.add(PrimerDropInCallbacks.ON_CLIENT_SESSION_UPDATED)
        }

        override fun onFailed(error: PrimerError, errorHandler: PrimerErrorDecisionHandler?) {
            Log.d(TAG, "onFailed - $error")
            callbacks.add(PrimerDropInCallbacks.ON_FAILED_WITHOUT_CHECKOUT_DATA)
            checkoutDataWithError =
                CheckoutDataWithError(null, error.toMappedError())
            errorHandler?.showErrorMessage(
                "SDK error id: ${error.errorId}, description: ${error.description}"
            )
        }

        override fun onTokenizeSuccess(
            paymentMethodTokenData: PrimerPaymentMethodTokenData,
            decisionHandler: PrimerResumeDecisionHandler
        ) {
            Log.d(TAG, "onTokenizeSuccess - $paymentMethodTokenData")
            callbacks.add(PrimerDropInCallbacks.ON_TOKENIZE_SUCCESS)
            when {
                paymentMethodTokenData.isVaulted &&
                    paymentMethodTokenData.paymentMethodType == "KLARNA_CUSTOMER_TOKEN" ->
                    decisionHandler.handleSuccess()

                else -> viewModel.createPayment(paymentMethodTokenData, decisionHandler)
            }
        }

        override fun onResumeSuccess(
            resumeToken: String,
            decisionHandler: PrimerResumeDecisionHandler
        ) {
            Log.d(TAG, "onResumeSuccess")
            callbacks.add(PrimerDropInCallbacks.ON_RESUME_SUCCESS)
            viewModel.resumePayment(resumeToken, decisionHandler)
        }

        override fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) {
            Log.d(TAG, "onResumePending - $additionalInfo")
            callbacks.add(PrimerDropInCallbacks.ON_RESUME_PENDING)
            when (additionalInfo) {
                is MultibancoCheckoutAdditionalInfo -> {
                    Log.d(TAG, "onResumePending MULTIBANCO: $additionalInfo")
                }
            }
        }

        override fun onAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) {
            super.onAdditionalInfoReceived(additionalInfo)
            Log.d(TAG, "onAdditionalInfoReceived - $additionalInfo")
            when (additionalInfo) {
                is QrCodeCheckoutAdditionalInfo -> {
                    Log.d(TAG, "onAdditionalInfoReceived: $additionalInfo")
                }
            }
        }

        override fun onDismissed() {
            Log.d(TAG, "onDismissed")
            callbacks.add(PrimerDropInCallbacks.ON_DISMISSED)
            lifecycleScope.launchWhenResumed {
                findNavController().navigate(
                    R.id.action_MerchantCheckoutFragment_to_MerchantResultFragment,
                    Bundle().apply {
                        putInt(
                            MerchantResultFragment.PAYMENT_STATUS_KEY,
                            if (checkoutDataWithError?.error != null) MerchantResultFragment.Companion.PaymentStatus.FAILURE.ordinal
                            else MerchantResultFragment.Companion.PaymentStatus.SUCCESS.ordinal
                        )
                        putStringArrayList(
                            MerchantResultFragment.INVOKED_CALLBACKS_KEY,
                            callbacks
                        )
                        putString(
                            MerchantResultFragment.PAYMENT_RESPONSE_KEY,
                            GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                                .toJson(checkoutDataWithError)
                        )
                    })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setClientToken(null)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Primer.instance.cleanup()
        Log.i("second fragment 🔥", "🔥")
    }

    private companion object {
        const val TAG = "CheckoutEventListener"
    }
}
