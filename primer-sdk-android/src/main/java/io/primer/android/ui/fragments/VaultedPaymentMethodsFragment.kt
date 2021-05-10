package io.primer.android.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.ui.AlternativePaymentMethodData
import io.primer.android.ui.CardData
import io.primer.android.ui.PaymentMethodItemData
import io.primer.android.ui.VaultViewAction
import io.primer.android.ui.VaultedPaymentMethodRecyclerAdapter
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

const val DEFAULT_LAST_FOUR: Int = 1234
const val DEFAULT_MONTH: Int = 1
const val DEFAULT_YEAR: Int = 2021

@KoinApiExtension
class VaultedPaymentMethodsFragment : Fragment() {

    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    // FIXME replace with view binding
    private lateinit var readOnlyHeaderLinearLayout: ViewGroup
    private lateinit var editHeaderLinearLayout: ViewGroup

    private var paymentMethods: List<PaymentMethodTokenInternal> = listOf()

    private var isEditing = false
        private set(value) {
            field = value

            adapter.isEditing = isEditing

            view?.findViewById<TextView>(R.id.vault_title_label)?.apply {
                text = if (isEditing) getString(R.string.edit_saved_payment_methods)
                else getString(R.string.other_ways_to_pay)
            }

            view?.findViewById<TextView>(R.id.edit_vaulted_payment_methods)?.text =
                if (isEditing) getString(R.string.cancel) else getString(R.string.edit)
        }

    private var adapter: VaultedPaymentMethodRecyclerAdapter =
        VaultedPaymentMethodRecyclerAdapter(::onClickWith)

    private fun configureRecyclerView(view: View, paymentMethods: List<PaymentMethodItemData>) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.vault_recycler_view)
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let {
            itemDecorator.setDrawable(it)
        }
        recyclerView.addItemDecoration(itemDecorator)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter.itemData = paymentMethods
        adapter.selectedPaymentMethodId = viewModel.getSelectedPaymentMethodId()
        recyclerView.adapter = adapter
    }

    private fun generateItemDataFromPaymentMethods(
        paymentMethods: List<PaymentMethodTokenInternal>,
    ): List<PaymentMethodItemData> = paymentMethods.map {
        when (it.paymentInstrumentType) {
            "KLARNA_CUSTOMER_TOKEN" -> {
                val email = it.paymentInstrumentData?.sessionData?.billingAddress?.email
                AlternativePaymentMethodData(email ?: "Klarna Payment Method", it.token)
            }
            "GOCARDLESS_MANDATE" -> {
                AlternativePaymentMethodData("Direct Debit Mandate", it.token)
            }
            "PAYMENT_CARD" -> {
                val title = it.paymentInstrumentData?.cardholderName ?: "unknown"
                val lastFour = it.paymentInstrumentData?.last4Digits ?: DEFAULT_LAST_FOUR
                val expiryMonth = it.paymentInstrumentData?.expirationMonth ?: DEFAULT_MONTH
                val expiryYear = it.paymentInstrumentData?.expirationYear ?: DEFAULT_YEAR
                val network = it.paymentInstrumentData?.network ?: "unknown"
                CardData(title, lastFour, expiryMonth, expiryYear, network, it.token)
            }
            else -> {
                AlternativePaymentMethodData("title", it.token)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_vaulted_payment_methods, container, false)

    private fun onClickWith(id: String, action: VaultViewAction) {
        when (action) {
            VaultViewAction.SELECT -> viewModel.setSelectedPaymentMethodId(id)
            VaultViewAction.DELETE -> onDeleteSelectedWith(id)
        }
    }

    private fun onDeleteSelectedWith(id: String) {
        val dialog = AlertDialog.Builder(view?.context, R.style.Primer_AlertDialog)
            .setTitle(getString(R.string.payment_method_deletion_message))
            // positive button as delete since it defaults to right
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                // delete payment method from vault then dismiss
                val methodToBeDeleted = paymentMethods.find {
                    it.token == id
                }

                if (methodToBeDeleted == null) {
                    dialog.dismiss()
                } else {
                    tokenizationViewModel.deleteToken(methodToBeDeleted)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readOnlyHeaderLinearLayout = view.findViewById(
            R.id.primer_view_vaulted_payment_methods_header
        )

        editHeaderLinearLayout = view.findViewById(R.id.primer_edit_vaulted_payment_methods_header)

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { data ->

            // return to checkout view if no saved payment methods
            if (data.isEmpty()) {
                viewModel.goToSelectPaymentMethodsView()
            }

            paymentMethods = data

            configureRecyclerView(view, generateItemDataFromPaymentMethods(paymentMethods))
        }

        view.findViewById<View>(R.id.vaulted_payment_methods_go_back).setOnClickListener {
            viewModel.goToSelectPaymentMethodsView()
        }

        view.findViewById<Button>(R.id.edit_vaulted_payment_methods).setOnClickListener {
            isEditing = !isEditing
        }
    }

    companion object {

        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
