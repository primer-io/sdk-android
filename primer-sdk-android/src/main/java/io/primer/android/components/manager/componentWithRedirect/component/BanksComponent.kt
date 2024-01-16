package io.primer.android.components.manager.componentWithRedirect.component

import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep

/**
 * A component for handling payments via a bank.
 */
interface BanksComponent : PrimerHeadlessMainComponent<BanksCollectableData, BanksStep>
