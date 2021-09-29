package io.primer.android.ui.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.ui.AlternativePaymentMethodData
import io.primer.android.ui.AlternativePaymentMethodType
import io.primer.android.ui.CardData
import io.primer.android.ui.PaymentMethodItemData
import io.primer.android.ui.VaultViewAction
import io.primer.android.ui.VaultedPaymentMethodRecyclerAdapter
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

const val DEFAULT_LAST_FOUR: Int = 1234
const val DEFAULT_MONTH: Int = 1
const val DEFAULT_YEAR: Int = 2021

@KoinApiExtension
class VaultedPaymentMethodsFragment : Fragment(), DIAppComponent {

    private val theme: PrimerTheme by inject()

    private val viewModel: PrimerViewModel by activityViewModels()
    private val tokenizationViewModel: TokenizationViewModel by viewModel()

    // FIXME replace with view binding
    private lateinit var readOnlyHeaderLinearLayout: ViewGroup
    private lateinit var editHeaderLinearLayout: ViewGroup

    private lateinit var vaultTitleLabel: TextView
    private lateinit var editLabel: TextView

    private var paymentMethods: List<PaymentMethodTokenInternal> = listOf()

    private var isEditing = false
        private set(value) {
            field = value

            adapter.isEditing = isEditing
            adapter.notifyDataSetChanged()

            vaultTitleLabel.text = if (isEditing) getString(R.string.edit_saved_payment_methods)
            else getString(R.string.other_ways_to_pay)

            editLabel.text = if (isEditing) getString(R.string.cancel) else getString(R.string.edit)
        }

    private var adapter: VaultedPaymentMethodRecyclerAdapter =
        VaultedPaymentMethodRecyclerAdapter(::onClickWith, theme)

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
    ): List<PaymentMethodItemData> =
        paymentMethods.map {
            when (it.paymentInstrumentType) {
                "KLARNA_CUSTOMER_TOKEN" -> {
                    val email = it.paymentInstrumentData?.sessionData?.billingAddress?.email
                    AlternativePaymentMethodData(
                        email ?: "Klarna Payment Method",
                        it.token,
                        AlternativePaymentMethodType.Klarna,
                    )
                }
                "PAYPAL_BILLING_AGREEMENT" -> {
                    val title = it.paymentInstrumentData?.externalPayerInfo?.email ?: "PayPal"
                    AlternativePaymentMethodData(
                        title,
                        it.token,
                        AlternativePaymentMethodType.PayPal,
                    )
                }
                "GOCARDLESS_MANDATE" -> {
                    AlternativePaymentMethodData(
                        "Direct Debit Mandate",
                        it.token,
                        AlternativePaymentMethodType.DirectDebit,
                    )
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
                    AlternativePaymentMethodData(
                        "saved payment method",
                        it.token,
                        AlternativePaymentMethodType.Generic,
                    )
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_vaulted_payment_methods, container, false)

    private fun onClickWith(id: String, action: VaultViewAction) {
        when (action) {
            VaultViewAction.SELECT -> viewModel.setSelectedPaymentMethodId(id)
            VaultViewAction.DELETE -> onDeleteSelectedWith(id)
        }
    }

    private fun onDeleteSelectedWith(id: String) {
        val dialog = AlertDialog.Builder(view?.context, R.style.Primer_AlertDialog)
            .setTitle(getString(R.string.payment_method_deletion_message))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                val methodToBeDeleted = paymentMethods.find {
                    it.token == id
                }

                // FIXME: add loading view for this.
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

        vaultTitleLabel = view.findViewById(R.id.vault_title_label)

        vaultTitleLabel.setTextColor(theme.titleText.defaultColor.getColor(requireContext()))

        readOnlyHeaderLinearLayout = view.findViewById(
            R.id.primer_view_vaulted_payment_methods_header
        )

        editHeaderLinearLayout = view.findViewById(R.id.primer_edit_vaulted_payment_methods_header)
        renderEditLabel(view)

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { data ->

            // return to checkout view if no saved payment methods
            if (data.isEmpty()) {
                viewModel.goToSelectPaymentMethodsView()
            }

            paymentMethods = data

            configureRecyclerView(view, generateItemDataFromPaymentMethods(paymentMethods))
        }

        val iconButton = view.findViewById<ImageButton>(R.id.vaulted_payment_methods_go_back)
        iconButton.setOnClickListener {
            viewModel.goToSelectPaymentMethodsView()
        }
        iconButton.imageTintList = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(requireContext())
        )

        view.findViewById<TextView>(R.id.edit_vaulted_payment_methods).setOnClickListener {
            isEditing = !isEditing
        }
    }

    private fun renderEditLabel(view: View) {
        editLabel = view.findViewById(R.id.edit_vaulted_payment_methods)
        editLabel.setTextColor(theme.systemText.defaultColor.getColor(requireContext()))
        editLabel.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            theme.systemText.fontsize.getDimension(requireContext()),
        )
    }

    companion object {

        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
