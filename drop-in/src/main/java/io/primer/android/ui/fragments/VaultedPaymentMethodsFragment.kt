package io.primer.android.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentInstrumentIdContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.databinding.PrimerFragmentVaultedPaymentMethodsBinding
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.AlternativePaymentMethodData
import io.primer.android.ui.AlternativePaymentMethodType
import io.primer.android.ui.BankData
import io.primer.android.ui.CardData
import io.primer.android.ui.PaymentMethodItemData
import io.primer.android.ui.VaultViewAction
import io.primer.android.ui.VaultedPaymentMethodRecyclerAdapter
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val DEFAULT_LAST_FOUR: Int = 1234
const val DEFAULT_MONTH: Int = 1
const val DEFAULT_YEAR: Int = 2021

@ExperimentalCoroutinesApi
internal class VaultedPaymentMethodsFragment : BaseFragment(), DISdkComponent {
    private var binding: PrimerFragmentVaultedPaymentMethodsBinding by autoCleaned()

    private var paymentMethods: List<PrimerVaultedPaymentMethod> = listOf()

    private var originalListener: View.OnClickListener? = null

    private var isEditing = false
        private set(value) {
            field = value

            adapter.isEditing = isEditing
            adapter.notifyDataSetChanged()

            getToolbar()?.showOnlyTitle(
                if (isEditing) {
                    R.string.edit_saved_payment_methods
                } else {
                    R.string.other_ways_to_pay
                },
            )

            getToolbar()?.getCloseButton()?.setImageResource(if (isEditing) R.drawable.ic_check else R.drawable.ic_edit)
        }

    private val adapter: VaultedPaymentMethodRecyclerAdapter by autoCleaned {
        VaultedPaymentMethodRecyclerAdapter(::onClickWith, theme)
    }

    private fun configureRecyclerView(paymentMethods: List<PaymentMethodItemData>) {
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider)?.let {
            itemDecorator.setDrawable(it)
        }
        binding.vaultRecyclerView.addItemDecoration(itemDecorator)
        adapter.itemData = paymentMethods
        adapter.selectedPaymentMethodId = primerViewModel.getSelectedPaymentMethodId()
        binding.vaultRecyclerView.adapter = adapter
    }

    @Suppress("CyclomaticComplexMethod")
    private fun generateItemDataFromPaymentMethods(
        paymentMethods: List<PrimerVaultedPaymentMethod>,
    ): List<PaymentMethodItemData> =
        paymentMethods.map {
            when (it.paymentMethodType) {
                PaymentMethodType.KLARNA.name -> {
                    val email = it.paymentInstrumentData.sessionData?.billingAddress?.email
                    AlternativePaymentMethodData(
                        email ?: "Klarna Payment Method",
                        it.id,
                        AlternativePaymentMethodType.Klarna,
                    )
                }

                PaymentMethodType.PAYPAL.name -> {
                    val title = it.paymentInstrumentData.externalPayerInfo?.email ?: "PayPal"
                    AlternativePaymentMethodData(
                        title,
                        it.id,
                        AlternativePaymentMethodType.PayPal,
                    )
                }

                PaymentMethodType.PAYMENT_CARD.name,
                PaymentMethodType.GOOGLE_PAY.name,
                -> {
                    val title = it.paymentInstrumentData.cardholderName ?: "unknown"
                    val lastFour = it.paymentInstrumentData.last4Digits ?: DEFAULT_LAST_FOUR
                    val expiryMonth = it.paymentInstrumentData.expirationMonth ?: DEFAULT_MONTH
                    val expiryYear = it.paymentInstrumentData.expirationYear ?: DEFAULT_YEAR
                    val network = it.paymentInstrumentData.binData?.network
                    CardData(
                        title,
                        lastFour,
                        expiryMonth,
                        expiryYear,
                        CardNetwork.Type.valueOrNull(network) ?: CardNetwork.Type.OTHER,
                        it.id,
                    )
                }

                PaymentMethodType.STRIPE_ACH.name -> {
                    BankData(
                        bankName = it.paymentInstrumentData.bankName ?: "-",
                        lastFour = it.paymentInstrumentData.accountNumberLast4Digits ?: 0,
                        tokenId = it.id,
                    )
                }

                else -> {
                    AlternativePaymentMethodData(
                        "saved payment method",
                        it.id,
                        AlternativePaymentMethodType.Generic,
                    )
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentVaultedPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onClickWith(
        id: String,
        action: VaultViewAction,
    ) {
        when (action) {
            VaultViewAction.SELECT -> {
                logSelectedPaymentMethodId(id)
                primerViewModel.setSelectedPaymentMethodId(id)
            }

            VaultViewAction.DELETE -> {
                onDeleteSelectedWith(id)
            }
        }
    }

    @Suppress("ForbiddenCommend")
    private fun onDeleteSelectedWith(id: String) {
        val dialog =
            AlertDialog.Builder(view?.context, R.style.Primer_AlertDialog)
                .setTitle(getString(R.string.payment_method_deletion_message))
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    val methodToBeDeleted =
                        paymentMethods.find {
                            it.id == id
                        }

                    // FIXME: add loading view for this.
                    if (methodToBeDeleted == null) {
                        dialog.dismiss()
                    } else {
                        logDeletePaymentMethodDialogAction(id, ObjectId.DELETE)
                        primerViewModel.deletePaymentMethodToken(paymentMethod = methodToBeDeleted)
                    }
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    logDeletePaymentMethodDialogAction(id, ObjectId.CANCEL)
                    dialog.cancel()
                }
        dialog.show()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        primerViewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { data ->

            // return to checkout view if no saved payment methods
            if (data.isEmpty()) {
                primerViewModel.goToSelectPaymentMethodsView()
            }

            paymentMethods = data

            configureRecyclerView(generateItemDataFromPaymentMethods(paymentMethods))
        }

        setupToolbar()
    }

    private fun setupToolbar() {
        getToolbar()?.apply {
            showOnlyTitle(R.string.other_ways_to_pay)
            originalListener = getCloseButton().tag as? View.OnClickListener
            getCloseButton().apply {
                setImageResource(R.drawable.ic_edit)
                setOnClickListener { isEditing = !isEditing }
            }
            getBackButton().setOnClickListener {
                primerViewModel.goToSelectPaymentMethodsView()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getToolbar()?.getCloseButton()?.apply {
            originalListener?.let {
                setImageResource(R.drawable.ic_clear)
                setOnClickListener(it)
            } ?: run {
                visibility = View.INVISIBLE
            }
        }
    }

    private fun logSelectedPaymentMethodId(id: String) =
        primerViewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.LIST_ITEM,
                Place.PAYMENT_METHODS_LIST,
                ObjectId.SELECT,
                PaymentInstrumentIdContextParams(id),
            ),
        )

    private fun logDeletePaymentMethodDialogAction(
        id: String,
        objectId: ObjectId,
    ) = primerViewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.ALERT,
            Place.PAYMENT_METHODS_LIST,
            objectId,
            PaymentInstrumentIdContextParams(id),
        ),
    )

    companion object {
        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
