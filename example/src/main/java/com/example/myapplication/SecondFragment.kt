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
import com.example.myapplication.utils.CheckoutListener
import com.xwray.groupie.GroupieAdapter
import io.primer.android.PaymentMethodIntent
import io.primer.android.model.dto.*
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.threeds.data.models.ResponseCode

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
        binding.showPaymentMethodButton.isVisible = false

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

        // SHOW PAYMENT METHOD
        binding.showPaymentMethodButton.setOnClickListener {
            viewModel.clientToken.value?.let { token ->
                viewModel.paymentMethodList.firstOrNull()?.let { paymentMethod ->
                    activity?.let { context ->
                        Primer.instance.showPaymentMethod(
                            context,
                            token,
                            viewModel.getPrimerPaymentMethod(paymentMethod),
                            PaymentMethodIntent.VAULT
                        )
                    }
                }
            }
        }

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (viewModel.vaultDisabled) {
                binding.vaultButton.isVisible = false
            } else {
                binding.vaultButton.isVisible = token != null
            }

            binding.checkoutButton.isVisible = token != null
            binding.showPaymentMethodButton.isVisible =
                token != null && viewModel.isStandalonePaymentMethod

            if (token != null) {
                initializeCheckout()
                fetchSavedPaymentMethods(token)
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

        viewModel.vaultedPaymentTokens.observe(viewLifecycleOwner) { token ->
            val adapter = GroupieAdapter()
            token.iterator().forEach { t -> adapter.add(PaymentMethodItem(t, ::onSelect)) }
            binding.paymentMethodList.adapter = adapter
        }

        viewModel.threeDsResult.observe(viewLifecycleOwner) {
            val message = it?.let {
                when (it.responseCode) {
                    ResponseCode.AUTH_SUCCESS -> getString(R.string.three_ds_success_message)
                    else -> getString(
                        R.string.three_ds_error_message,
                        it.responseCode,
                        it.reasonCode,
                        it.reasonText
                    )
                }
            }
            message?.let {
                AlertDialog.Builder(context).setMessage(it).show()
                viewModel.clearThreeDsResult()
            }
        }

        viewModel.fetchClientSession()
    }

    private fun initializeCheckout() = viewModel.configure(listener)

    private val listener = CheckoutListener(
        onTokenizeSuccess = { token, completionHandler ->
            viewModel.createPayment(token, completionHandler)
        },
        onTokenSelected = { token, completionHandler ->
            viewModel.createPayment(token, completionHandler)
        },
        onResumeSuccess = { resumeToken, completionHandler ->
            viewModel.resumePayment(resumeToken, completionHandler)
        },
        onResumeError = {
//            Primer.instance.dismiss(true)
            AlertDialog.Builder(context).setMessage(it.toString()).show()
        },
        onSavedPaymentInstrumentsFetched = {
            setBusy(false)
            viewModel.vaultedPaymentTokens.postValue(it)
            val adapter = GroupieAdapter()
            it.forEach { t ->
                adapter.add(PaymentMethodItem(t, ::onSelect))
            }
            binding.paymentMethodList.adapter = adapter
        },
        onApiError = {
//            Primer.instance.dismiss(true)
            AlertDialog.Builder(context).setMessage(it.toString()).show()
        },
        onExit = {
            viewModel.clientToken.value?.let { token -> fetchSavedPaymentMethods(token) }
        },
        onActions = { request, completion -> viewModel.postAction(request, completion) }
    )

    private fun onConfirmDialogAction(token: PaymentMethodToken) {
        viewModel.createPayment(token)
    }

    private fun onSelect(token: PaymentMethodToken) {
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

    private fun fetchSavedPaymentMethods(token: String) {
        activity?.runOnUiThread { setBusy(true) }
        Primer.instance.fetchSavedPaymentInstruments(token)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Primer.instance.cleanup()
        Log.i("second fragment 🔥", "🔥")
    }
}