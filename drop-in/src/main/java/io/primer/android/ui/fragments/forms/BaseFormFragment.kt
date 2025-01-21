package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.presentation.payment.forms.FormsViewModel
import io.primer.android.presentation.payment.forms.FormsViewModelFactory
import io.primer.android.ui.fragments.SelectPaymentMethodFragment
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal abstract class BaseFormFragment : BaseFragment(), DISdkComponent {
    protected abstract val baseFormBinding: BaseFormBinding

    protected val viewModel: FormsViewModel by viewModel<FormsViewModel, FormsViewModelFactory>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        logAnalyticsViewed()

        viewModel.formLiveData.observe(viewLifecycleOwner) { form ->
            setupForm(form)
        }
        viewModel.getForms(
            primerViewModel.selectedPaymentMethod.value?.paymentMethodType.orEmpty(),
        )
    }

    protected open fun setupBackIcon() {
        getToolbar()?.getBackButton()?.apply {
            setOnClickListener {
                logAnalyticsBackPressed()
                popBackStack()
            }
            isVisible =
                primerViewModel.selectedPaymentMethod.value?.uiOptions
                    ?.isStandalonePaymentMethod?.not()
                    ?: false
        }
    }

    protected open fun setupForm(form: Form) {
        setupBackIcon()
        setupTitle(form.title)
        setupDescription(form.description)
        getToolbar()?.showOnlyLogo(form.logo)
    }

    private fun setupTitle(title: Int?) {
        val formTitle = baseFormBinding.formTitle
        title?.let {
            formTitle.text = getString(title)
            formTitle.isVisible = true
        } ?: run {
            formTitle.isVisible = false
        }
    }

    private fun setupDescription(description: Int?) {
        val formDescription = baseFormBinding.formDescription
        description?.let {
            formDescription.text = getString(description)
            formDescription.isVisible = true
        } ?: run {
            formDescription.isVisible = false
        }
    }

    private fun logAnalyticsViewed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.VIEW,
                ObjectType.VIEW,
                Place.DYNAMIC_FORM,
                ObjectId.VIEW,
                primerViewModel.selectedPaymentMethod.value?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    protected fun logAnalyticsBackPressed() =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                Place.DYNAMIC_FORM,
                ObjectId.BACK,
                primerViewModel.selectedPaymentMethod.value?.paymentMethodType?.let {
                    PaymentMethodContextParams(it)
                },
            ),
        )

    private fun popBackStack() {
        parentFragmentManager.popBackStackImmediate(
            SelectPaymentMethodFragment.TAG,
            FragmentManager.POP_BACK_STACK_INCLUSIVE,
        )
        runCatching {
            parentFragmentManager.commit {
                /*
                Manually remove this fragment as it is not added to the backstack,
                therefore it won't get removed automatically on pop.
                 */
                remove(this@BaseFormFragment)
            }
        }
        primerViewModel.goToSelectPaymentMethodsView()
    }
}
