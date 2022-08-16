package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.presentation.payment.forms.FormsViewModel
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.inject

@ExperimentalCoroutinesApi
internal abstract class BaseFormFragment : Fragment(), DIAppComponent {

    protected val theme: PrimerTheme by inject()
    protected abstract val baseFormBinding: BaseFormBinding

    protected val primerViewModel: PrimerViewModel by activityViewModels()
    protected val tokenizationViewModel: TokenizationViewModel by activityViewModels()

    protected val viewModel: FormsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logAnalyticsViewed()
        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)

        viewModel.formLiveData.observe(viewLifecycleOwner) { form ->
            setupForm(form)
        }
        viewModel.getForms(
            primerViewModel.selectedPaymentMethod.value?.config?.type.orEmpty()
        )
    }

    protected open fun setupBackIcon() {
        val backIcon = baseFormBinding.formBackIcon
        backIcon.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )
        backIcon.setOnClickListener {
            logAnalyticsBackPressed()
            parentFragmentManager.popBackStack()
        }
    }

    protected open fun setupForm(form: Form) {
        setupBackIcon()
        setupTitle(form.title)
        setupDescription(form.description)
        setupLogo(form.logo)
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

    private fun setupLogo(logo: Int) {
        val formIcon = baseFormBinding.formIcon
        formIcon.setImageResource(logo)
    }

    private fun logAnalyticsViewed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.VIEW,
            Place.DYNAMIC_FORM,
            ObjectId.VIEW,
            primerViewModel.selectedPaymentMethod.value?.config?.type?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsBackPressed() = viewModel.addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.BUTTON,
            Place.DYNAMIC_FORM,
            ObjectId.BACK,
            primerViewModel.selectedPaymentMethod.value?.config?.type?.let {
                PaymentMethodContextParams(it)
            }
        )
    )
}
