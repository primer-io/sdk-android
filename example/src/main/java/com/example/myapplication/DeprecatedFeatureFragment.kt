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
import com.example.myapplication.components.PaymentMethodItem
import com.example.myapplication.databinding.FragmentDeprecatedFeatureBinding
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.utils.CheckoutListener
import com.example.myapplication.viewmodels.AppMainViewModel
import com.xwray.groupie.GroupieAdapter
import io.primer.android.UniversalCheckout
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.card.Card
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal

class DeprecatedFeatureFragment : Fragment() {

    private var _binding: FragmentDeprecatedFeatureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppMainViewModel by activityViewModels()

    private val card = Card()
    private val paypal = PayPal()
    private val klarna = Klarna(webViewTitle = "Add Klarna 💰")
    private val apaya = Apaya(webViewTitle = "Add Apaya 💰", "1234")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeprecatedFeatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vaultButton.isVisible = false
        binding.checkoutButton.isVisible = false
        binding.klarnaButton.isVisible = false

        // KLARNA
        binding.klarnaButton.setOnClickListener {
            activity?.let { context ->
                UniversalCheckout.loadPaymentMethods(listOf(klarna))
                UniversalCheckout.showVault(
                    context,
                    listener,
                    preferWebView = true,
                    isStandalonePaymentMethod = true,
                    doNotShowUi = true,
                    clearAllListeners = true,
                    amount = viewModel.amount.value,
                    currency = viewModel.countryCode.value?.currencyCode?.name,
                )
            }
        }

        // VAULT
        binding.vaultButton.setOnClickListener {
            activity?.let { context ->
                UniversalCheckout.loadPaymentMethods(listOf(klarna, apaya, paypal, card))
                UniversalCheckout.showVault(
                    context,
                    listener,
                    preferWebView = true,
                    clearAllListeners = true,
                    orderId = viewModel.orderId.value,
                    is3DSOnVaultingEnabled = viewModel.threeDsEnabled.value ?: false,
                    amount = viewModel.getAmountConverted(),
                    currency = viewModel.countryCode.value?.currencyCode?.name,
                    customer = viewModel.config.settings.customer,
                )
            }
        }

        // CHECKOUT
        binding.checkoutButton.setOnClickListener {
            activity?.let { context ->
                UniversalCheckout.loadPaymentMethods(listOf(klarna, paypal, card))
                UniversalCheckout.showCheckout(
                    context,
                    listener,
                    clearAllListeners = true,
                    orderId = viewModel.orderId.value,
                    amount = viewModel.getAmountConverted(),
                    currency = viewModel.countryCode.value?.currencyCode?.name,
                    customer = viewModel.config.settings.customer
                )
            }
        }

        // CLIENT TOKEN
        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            binding.vaultButton.isVisible = token != null
            binding.checkoutButton.isVisible = token != null
            binding.klarnaButton.isVisible = token != null
            setBusyAs(false)
            token?.let { clientToken ->
                activity?.let { context ->
                    UniversalCheckout.initialize(
                        context,
                        clientToken,
                        countryCode = CountryCode.SE,
                    )
                    fetchSavedPaymentMethods()
                }

            }
        }

        // PAYMENT RESULT
        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                TransactionState.SUCCESS -> onPaymentSuccess()
                TransactionState.ERROR -> onPaymentError()
                else -> Unit
            }
        }

        viewModel.fetchClientToken()
    }

    private fun onPaymentSuccess() {
        UniversalCheckout.dismiss()
        AlertDialog.Builder(context)
            .setMessage("Payment successful!")
            .show()
        setBusyAs(false)
        viewModel.resetTransactionState()
    }

    private fun onPaymentError() {
        AlertDialog.Builder(context)
            .setMessage("Something went wrong, please try a different payment method.")
            .show()
        setBusyAs(false)
        viewModel.resetTransactionState()
    }

    private fun setBusyAs(isBusy: Boolean) {
        binding.walletProgressBar.isVisible = isBusy
    }

    private val listener = CheckoutListener(
        onTokenizeSuccess = { data, handler ->
            viewModel.createTransaction(data, handler)
        },
        onTokenSelected = { data, handler ->
            viewModel.createTransaction(data, handler)
        },
        onResumeSuccess = { token, handler ->
            viewModel.resumePayment(token, handler)
        },
        onResumeError = {
            UniversalCheckout.dismiss(true)
            AlertDialog.Builder(context).setMessage(it.toString()).show()
        },
        onApiError = {
            UniversalCheckout.dismiss(true)
            AlertDialog.Builder(context).setMessage(it.toString()).show()
        },
        onExit = { fetchSavedPaymentMethods() }
    )

    private fun onSelect(data: PaymentMethodToken) {
        AlertDialog.Builder(context)
            .setTitle("Payment confirmation")
            .setMessage("Would you like to pay?")
            .setPositiveButton("Yes") { _, _ ->
                setBusyAs(true)
                viewModel.createTransaction(data)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun fetchSavedPaymentMethods() {
        activity?.runOnUiThread { setBusyAs(true) }
        UniversalCheckout.getSavedPaymentMethods {
            activity?.runOnUiThread {
                setBusyAs(false)
                val adapter = GroupieAdapter()
                it.forEach { t ->
                    adapter.add(PaymentMethodItem(t, ::onSelect))
                }
                binding.paymentMethodList.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(
            "onDestroy",
            "🔥 deprecated feature fragment was destroyed 🔥",
        )
    }
}