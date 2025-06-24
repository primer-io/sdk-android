# Co-badge Implementation Plan - CardFormScope

## Overview
Port co-badge (multi-network card selection) functionality from drop-in module to CardFormScope in components module.

## Current State Analysis

### Drop-in Implementation
- **CardFormFragment**: `updateCardNetworkViews()` handles dropdown UI based on network count
- **CardViewModel**: Manages `CardNetworksState` with `networks`, `preferredNetwork`, `selectedNetwork`
- **RawDataManager Integration**: Listens to `onMetadataStateChanged` for network updates
- **UI Flow**: Single network → show icon, Multiple networks → show dropdown with caret

### Components Current State
- **CardFormScope.State**: Only exposes `detectedCardNetwork: CardNetwork.Type` (single)
- **SetCardDataInteractor**: Local network detection only, no metadata exposure
- **RawDataManagerRepository**: No metadata state exposure

## Implementation Plan

### Phase 1: Repository Layer [safe]
**Files to modify (2 files)**:
- `RawDataManagerRepository.kt` - Add metadata state flow
- `RawDataManagerRepositoryImpl.kt` - Implement metadata listener

**Changes**:
- Add `metadataState: Flow<PrimerPaymentMethodMetadataState>` to repository interface
- Implement metadata listener in repository implementation
- Share metadata state via Flow

### Phase 2: Domain Layer [moderate]
**Files to modify (1 file)**:
- `SetCardDataInteractor.kt` - Add co-badge state management

**Changes**:
- Add `availableNetworks: Flow<List<PrimerCardNetwork>>` 
- Add `selectedNetwork: Flow<CardNetwork.Type?>`
- Add `fun selectNetwork(network: CardNetwork.Type)`
- Process metadata changes to extract selectable networks

### Phase 3: Presentation Layer [moderate]
**Files to modify (2 files)**:
- `PrimerCardFormScope.kt` - Extend state and interface
- `CardFormViewModel.kt` - Implement co-badge logic

**Changes to State**:
```kotlin
data class State(
    // existing fields...
    val availableNetworks: List<PrimerCardNetwork> = emptyList(),
    val selectedNetwork: CardNetwork.Type? = null,
    val preferredNetwork: CardNetwork.Type? = null,
)
```

**Changes to Interface**:
```kotlin
interface PrimerCardFormScope {
    // existing methods...
    fun selectCardNetwork(network: CardNetwork.Type)
    
    // existing composables...
    var cardNetworkSelector: @Composable (
        modifier: Modifier,
        networks: List<PrimerCardNetwork>,
        selectedNetwork: CardNetwork.Type?,
        onNetworkSelected: (CardNetwork.Type) -> Unit
    ) -> Unit
}
```

### Phase 4: UI Components [safe]
**Files to create (2 files)**:
- `CardNetworkSelector.kt` - Co-badge dropdown composable
- `CardNetworkIcon.kt` - Network icon display composable

**Implementation**:
- Single network: Show icon only
- Multiple networks: Show dropdown with icon + caret
- Handle network selection callbacks
- Match drop-in UI behavior

### Phase 5: Integration [safe]
**Files to modify (2 files)**:
- `DefaultCardFormScope.kt` - Add default network selector composable
- `CardDetailsForm.kt` - Integrate network selector into card form

**Changes**:
- Wire up network selector composable
- Position selector near card number input
- Handle network selection state updates

## Key Implementation Details

### Metadata Processing Logic
```kotlin
private fun handleMetadataChange(metadata: PrimerCardMetadataState.Fetched) {
    val selectableNetworks = metadata.cardNumberEntryMetadata.selectableCardNetworks?.items
    val detectedNetwork = metadata.cardNumberEntryMetadata.detectedCardNetworks.preferred
        ?: metadata.cardNumberEntryMetadata.detectedCardNetworks.items.firstOrNull()
    
    val networks = selectableNetworks ?: listOfNotNull(detectedNetwork)
    val preferred = metadata.cardNumberEntryMetadata.selectableCardNetworks?.preferred?.network
    val selected = selectedNetwork ?: preferred ?: networks.firstOrNull()?.network
    
    updateNetworkState(networks, preferred, selected)
}
```

### Network Selection Flow
1. User selects network from dropdown
2. `selectCardNetwork()` called on scope
3. SetCardDataInteractor updates selected network
4. RawDataManager receives updated card data with network
5. UI updates to reflect selection

## Risk Assessment

### Safe Changes
- UI components (new files)
- Repository interface extensions
- State data class extensions

### Moderate Risk Changes  
- SetCardDataInteractor modifications (core business logic)
- CardFormViewModel state management

### Testing Strategy
- Unit tests for metadata processing logic
- Integration tests for network selection flow
- UI tests for dropdown interaction
- Regression tests against drop-in behavior

## Success Criteria
- [ ] Co-badge dropdown appears when multiple networks detected
- [ ] Network selection updates card data correctly  
- [ ] Single network shows icon only (no dropdown)
- [ ] Preferred network selection follows drop-in logic
- [ ] UI matches drop-in visual design
- [ ] All existing card form functionality unchanged

## Estimated Implementation Time
- Phase 1-2 (Repository/Domain): 2-3 hours
- Phase 3 (Presentation): 2-3 hours  
- Phase 4-5 (UI/Integration): 3-4 hours
- Testing & Polish: 2-3 hours
- **Total: 9-13 hours**