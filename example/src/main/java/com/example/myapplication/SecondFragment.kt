package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.myapplication.components.PaymentMethodItem
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.viewmodels.MainViewModel
import io.primer.android.Primer
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentSecondBinding
import com.xwray.groupie.GroupieAdapter
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

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vaultButton.isVisible = false
        binding.checkoutButton.isVisible = false

        // VAULT MANAGER
        binding.vaultButton.setOnClickListener {
            viewModel.mode.postValue(MainViewModel.Mode.VAULT)
            viewModel.clientToken.value?.let { token ->
                activity?.let { context -> Primer.instance.showVaultManager(context, token) }
            }
        }

        // UNIVERSAL CHECKOUT
        binding.checkoutButton.setOnClickListener {
            viewModel.mode.postValue(MainViewModel.Mode.CHECKOUT)
            viewModel.clientToken.value?.let { token ->
                activity?.let { context -> Primer.instance.showUniversalCheckout(context, token) }
            }
        }

        viewModel.postalCode.observe(viewLifecycleOwner) { value ->
            binding.postalCodeLabel.text = requireContext().getString(R.string.postal_code, value)
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
        
        viewModel.fetchClientSession()
    }

    private fun initializeCheckout() = viewModel.configure(listener)

    private val listener = object : PrimerCheckoutListener {

        override fun onBeforePaymentCreated(
            paymentMethodData: PrimerPaymentMethodData,
            decisionHandler: PrimerPaymentCreationDecisionHandler
        ) {
            decisionHandler.continuePaymentCreation()
            Log.d(TAG, "onBeforePaymentCreated with $paymentMethodData")
        }

        override fun onFailed(
            error: PrimerError,
            checkoutData: PrimerCheckoutData?,
            errorHandler: PrimerErrorDecisionHandler?
        ) {
            AlertDialog.Builder(context).setMessage("onFailed ${error.description} with data $checkoutData")
                .show()
            errorHandler?.showErrorMessage("Something went wrong!")
        }

        override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
            AlertDialog.Builder(context).setMessage("onCheckoutCompleted $checkoutData").show()
        }

        override fun onBeforeClientSessionUpdated() {
            Log.d(TAG, "onBeforeClientSessionUpdated")
        }

        override fun onClientSessionUpdated(clientSession: PrimerClientSession) {
            super.onClientSessionUpdated(clientSession)
            Log.d(TAG, "onClientSessionUpdated with result $clientSession")
        }

        override fun onFailed(error: PrimerError, errorHandler: PrimerErrorDecisionHandler?) {
            AlertDialog.Builder(context).setMessage("onFailed $error")
                .show()
            errorHandler?.showErrorMessage("Something went wrong!")
        }

        override fun onTokenizeSuccess(
            paymentMethodTokenData: PrimerPaymentMethodTokenData,
            decisionHandler: PrimerResumeDecisionHandler
        ) {
            Log.d(TAG, "onTokenizeSuccess")
            viewModel.createPayment(paymentMethodTokenData, decisionHandler)
        }

        override fun onResumeSuccess(resumeToken: String, decisionHandler: PrimerResumeDecisionHandler) {
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
            Log.d(TAG, "onDismissed")
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
        viewModel.clientToken.postValue(null)
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