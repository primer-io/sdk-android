package io.primer.android.ui.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentInstrumentIdContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.databinding.FragmentVaultedPaymentMethodsBinding
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.activityViewModel
import io.primer.android.di.extension.inject
import io.primer.android.ui.AlternativePaymentMethodData
import io.primer.android.ui.AlternativePaymentMethodType
import io.primer.android.ui.CardData
import io.primer.android.ui.PaymentMethodItemData
import io.primer.android.ui.VaultViewAction
import io.primer.android.ui.VaultedPaymentMethodRecyclerAdapter
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.PrimerViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val DEFAULT_LAST_FOUR: Int = 1234
const val DEFAULT_MONTH: Int = 1
const val DEFAULT_YEAR: Int = 2021

@ExperimentalCoroutinesApi
class VaultedPaymentMethodsFragment : Fragment(), DISdkComponent {

    private val theme: PrimerTheme by inject()
    private var binding: FragmentVaultedPaymentMethodsBinding by autoCleaned()

    private val viewModel: PrimerViewModel by
    activityViewModel<PrimerViewModel, PrimerViewModelFactory>()

    private var paymentMethods: List<BasePaymentToken> = listOf()

    private var isEditing = false
        private set(value) {
            field = value

            adapter.isEditing = isEditing
            adapter.notifyDataSetChanged()

            binding.vaultTitleLabel.text =
                if (isEditing) {
                    getString(R.string.edit_saved_payment_methods)
                } else { getString(R.string.other_ways_to_pay) }

            binding.editVaultedPaymentMethods.text =
                if (isEditing) getString(R.string.cancel) else getString(R.string.edit)
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
        adapter.selectedPaymentMethodId = viewModel.getSelectedPaymentMethodId()
        binding.vaultRecyclerView.adapter = adapter
    }

    private fun generateItemDataFromPaymentMethods(
        paymentMethods: List<BasePaymentToken>
    ): List<PaymentMethodItemData> =
        paymentMethods.map {
            when (it.paymentMethodType) {
                PaymentMethodType.KLARNA.name -> {
                    val email = it.paymentInstrumentData?.sessionData?.billingAddress?.email
                    AlternativePaymentMethodData(
                        email ?: "Klarna Payment Method",
                        it.token,
                        AlternativePaymentMethodType.Klarna
                    )
                }

                PaymentMethodType.PAYPAL.name -> {
                    val title = it.paymentInstrumentData?.externalPayerInfo?.email ?: "PayPal"
                    AlternativePaymentMethodData(
                        title,
                        it.token,
                        AlternativePaymentMethodType.PayPal
                    )
                }

                PaymentMethodType.PAYMENT_CARD.name -> {
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
                        AlternativePaymentMethodType.Generic
                    )
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVaultedPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onClickWith(id: String, action: VaultViewAction) {
        when (action) {
            VaultViewAction.SELECT -> {
                logSelectedPaymentMethodId(id)
                viewModel.setSelectedPaymentMethodId(id)
            }

            VaultViewAction.DELETE -> {
                onDeleteSelectedWith(id)
            }
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
                    logDeletePaymentMethodDialogAction(id, ObjectId.DELETE)
                    viewModel.deleteToken(methodToBeDeleted)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                logDeletePaymentMethodDialogAction(id, ObjectId.CANCEL)
                dialog.cancel()
            }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vaultTitleLabel.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        renderEditLabel()

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { data ->

            // return to checkout view if no saved payment methods
            if (data.isEmpty()) {
                viewModel.goToSelectPaymentMethodsView()
            }

            paymentMethods = data

            configureRecyclerView(generateItemDataFromPaymentMethods(paymentMethods))
        }

        binding.vaultedPaymentMethodsGoBack.setOnClickListener {
            viewModel.goToSelectPaymentMethodsView()
        }
        binding.vaultedPaymentMethodsGoBack.imageTintList = ColorStateList.valueOf(
            theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        )
        binding.editVaultedPaymentMethods.setOnClickListener {
            isEditing = !isEditing
        }
    }

    private fun renderEditLabel() = binding.editVaultedPaymentMethods.apply {
        setTextColor(
            theme.systemText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            theme.systemText.fontSize.getDimension(requireContext())
        )
    }

    private fun logSelectedPaymentMethodId(id: String) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.LIST_ITEM,
                Place.PAYMENT_METHODS_LIST,
                ObjectId.SELECT,
                PaymentInstrumentIdContextParams(id)
            )
        )

    private fun logDeletePaymentMethodDialogAction(id: String, objectId: ObjectId) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.ALERT,
                Place.PAYMENT_METHODS_LIST,
                objectId,
                PaymentInstrumentIdContextParams(id)
            )
        )

    companion object {

        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
