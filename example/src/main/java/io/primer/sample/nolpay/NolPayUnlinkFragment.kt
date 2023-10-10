package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCardStep
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayUnlinkBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NolPayUnlinkFragment : Fragment() {

    private lateinit var binding: FragmentNolPayUnlinkBinding
    private lateinit var unlinkCardComponent: NolPayUnlinkCardComponent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayUnlinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.parentNavFragment) as NavHostFragment

        unlinkCardComponent =
            PrimerHeadlessUniversalCheckoutNolPayManager().provideNolPayUnlinkCardComponent(
                this
            )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unlinkCardComponent.componentStep.collectLatest {
                    when (it) {
                        is NolPayUnlinkCardStep.CollectCardAndPhoneData -> Unit
                        is NolPayUnlinkCardStep.CollectOtpData -> navHostFragment.findNavController()
                            .navigate(R.id.action_NolPayPhoneInputFragment_to_NolPayUnlinkOtpFragment)

                        is NolPayUnlinkCardStep.CardUnlinked -> {
                            Snackbar.make(
                                requireView(),
                                "Successfully unlinked!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                            findNavController().navigate(R.id.action_NolUnlinkFragment_to_NolFragment)
                        }
                    }
                }
            }
        }

        unlinkCardComponent.start()
    }
}
