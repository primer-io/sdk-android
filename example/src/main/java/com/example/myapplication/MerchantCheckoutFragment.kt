package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.components.PaymentMethodItem
import com.example.myapplication.constants.PrimerDropInCallbacks
import com.example.myapplication.databinding.FragmentUniversalCheckoutBinding
import com.example.myapplication.datamodels.CheckoutDataWithError
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.datamodels.toMappedError
import com.example.myapplication.viewmodels.MainViewModel
import com.example.myapplication.viewmodels.MainViewModel.Mode
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupieAdapter
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PromptPayCheckoutAdditionalInfo
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

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

        // VAULT MANAGER
        binding.vaultButton.setOnClickListener {
            callbacks.clear()
            viewModel.mode.postValue(Mode.VAULT)
            viewModel.clientToken.value?.let { token ->
                checkoutDataWithError = null
                callbacks.add(PrimerDropInCallbacks.ON_VAULT_MANAGER_CLICKED)
                activity?.let { context -> Primer.instance.showVaultManager(context, token) }
            }
        }

        // UNIVERSAL CHECKOUT
        binding.checkoutButton.setOnClickListener {
            viewModel.mode.postValue(Mode.CHECKOUT)
            callbacks.clear()
            viewModel.clientToken.value?.let { token ->
                checkoutDataWithError = null
                callbacks.add(PrimerDropInCallbacks.ON_UNIVERSAL_CHECKOUT_CLICKED)
                activity?.let { context -> Primer.instance.showUniversalCheckout(context, token) }
            }
        }

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            binding.vaultButton.isVisible = token != null

            binding.checkoutButton.isVisible = token != null
            if (token != null) {
                initializeCheckout()
                fetchSavedPaymentMethods()
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

        viewModel.paymentInstruments.observe(viewLifecycleOwner) { data ->
            setBusy(false)
            val adapter = GroupieAdapter()
            data.iterator().forEach { t -> adapter.add(PaymentMethodItem(t, ::onSelect)) }
            binding.paymentMethodList.adapter = adapter
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
            Log.d(TAG, "onBeforePaymentCreated with $paymentMethodData")
        }

        override fun onFailed(
            error: PrimerError,
            checkoutData: PrimerCheckoutData?,
            errorHandler: PrimerErrorDecisionHandler?
        ) {
            callbacks.add(PrimerDropInCallbacks.ON_FAILED_WITH_CHECKOUT_DATA)
            checkoutDataWithError =
                CheckoutDataWithError(checkoutData?.payment, error.toMappedError())
            errorHandler?.showErrorMessage(
                "SDK error id: ${error.errorId}, description: ${error.description}"
            )
        }

        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
            Log.d(TAG, "onCheckoutCompleted")
            checkoutDataWithError = CheckoutDataWithError(checkoutData.payment)
            callbacks.add(PrimerDropInCallbacks.ON_CHECKOUT_COMPLETED)
        }

        override fun onBeforeClientSessionUpdated() {
            Log.d(TAG, "onBeforeClientSessionUpdated")
            callbacks.add(PrimerDropInCallbacks.ON_BEFORE_CLIENT_SESSION_UPDATED)
        }

        override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
            super.onClientSessionUpdated(clientSession)
            callbacks.add(PrimerDropInCallbacks.ON_CLIENT_SESSION_UPDATED)
            Log.d(TAG, "onClientSessionUpdated with result $clientSession")
        }

        override fun onFailed(error: PrimerError, errorHandler: PrimerErrorDecisionHandler?) {
            errorHandler?.showErrorMessage(
                "SDK error id: ${error.errorId}, description: ${error.description}"
            )
        }

        override fun onTokenizeSuccess(
            paymentMethodTokenData: PrimerPaymentMethodTokenData,
            decisionHandler: PrimerResumeDecisionHandler
        ) {
            Log.d(TAG, "onTokenizeSuccess")
            callbacks.add(PrimerDropInCallbacks.ON_TOKENIZE_SUCCESS)
            when {
                paymentMethodTokenData.isVaulted &&
                    viewModel.payAfterVaulting.value != true -> decisionHandler.handleSuccess()
                else -> viewModel.createPayment(paymentMethodTokenData, decisionHandler)
            }
        }

        override fun onResumeSuccess(
            resumeToken: String,
            decisionHandler: PrimerResumeDecisionHandler
        ) {
            Log.d(TAG, "onResumeSuccess")
            viewModel.resumePayment(resumeToken, decisionHandler)
        }

        override fun onResumePending(additionalInfo: PrimerCheckoutAdditionalInfo) {
            Log.d(TAG, "onResumePending $additionalInfo")
            when (additionalInfo) {
                is MultibancoCheckoutAdditionalInfo -> {
                    Log.d(TAG, "onResumePending MULTIBANCO: $additionalInfo")
                }
            }
        }

        override fun onAdditionalInfoReceived(additionalInfo: PrimerCheckoutAdditionalInfo) {
            super.onAdditionalInfoReceived(additionalInfo)
            Log.d(TAG, "onAdditionalInfoReceived $additionalInfo")
            when (additionalInfo) {
                is PromptPayCheckoutAdditionalInfo -> {
                    Log.d(TAG, "onAdditionalInfoReceived: $additionalInfo")
                }
            }
        }

        override fun onDismissed() {
            callbacks.add(PrimerDropInCallbacks.ON_DISMISSED)
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
            viewModel.clientToken.value?.let { _ -> fetchSavedPaymentMethods() }
        }
    }

    private fun onConfirmDialogAction(token: PrimerPaymentMethodTokenData) {
        viewModel.createPayment(token)
    }

    private fun onSelect(token: PrimerPaymentMethodTokenData) {
        AlertDialog.Builder(context)
            .setTitle("Payment confirmation")
            .setMessage("Would you like to pay?")
            .setPositiveButton("Yes") { _, _ -> onConfirmDialogAction(token) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setBusy(isBusy: Boolean) {
        binding.walletProgressBar.isVisible = isBusy
    }

    private fun fetchSavedPaymentMethods() {
        activity?.runOnUiThread { setBusy(true) }
        viewModel.fetchPaymentInstruments()
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
