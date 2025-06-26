# Surcharge Grouping Implementation Plan

## Overview
Implement grouping of payment methods by surcharge in the `DefaultPaymentMethodSelectionScreen` using the existing `surcharge` property from `PrimerComposablePaymentMethod`.

## Current State Analysis

### Existing Components
- **DefaultPaymentMethodSelectionScreen**: Located in `components/src/main/java/io/primer/android/internal/presentation/screens/paymentMethodSelection/DefaultPaymentMethodSelectionScreen.kt`
- **PrimerComposablePaymentMethod**: Contains `surcharge: Surcharge?` property
- **SurchargeFormatter**: Already exists in `ui-core` module with interface + default implementation
- **Surcharge Model**: Sealed interface with `PaymentMethodSurcharge` and `CardNetworksSurcharge` types

### Current Ready() Implementation
```kotlin
LazyColumn {
    item { /* Description text */ }
    items(state.paymentMethods) { primerMethod ->
        PaymentMethodSelector(primerMethod, onPaymentMethodSelected)
    }
}
```

## Implementation Plan

### Phase 1: Shared Grouping Logic [MODERATE]
**Files affected:** 1 file, ~40 lines
- Create shared grouping utility in `ui-core/src/main/java/io/primer/android/ui/core/payment/domain/utils/PaymentMethodGroupingUtils.kt`
- Extract and adapt core grouping algorithm from `PaymentMethodButtonGroupFactory.getPaymentMethodGroupKey()`
- Create extension functions:
  ```kotlin
  fun List<PrimerComposablePaymentMethod>.groupBySurcharge(surcharges: Map<String, Surcharge>): Map<Int, List<PrimerComposablePaymentMethod>>
  fun List<PrimerComposablePaymentMethod>.groupBySurchargeForUI(): Pair<List<PrimerComposablePaymentMethod>, List<PrimerComposablePaymentMethod>>
  ```

### Phase 2: String Resources Integration [SAFE]
**Files affected:** 1 file, ~5 lines  
- Add required strings to `components/src/main/res/values/strings.xml`:
  - `no_additional_fee`
  - `additional_fees_may_apply`
- Reuse existing `SurchargeFormatter` from `ui-core` with these strings

### Phase 3: Screen Integration [MODERATE]
**Files affected:** 1 file, ~35 lines
- Modify `DefaultPaymentMethodSelectionScreen.Ready()` to use shared grouping logic
- Use existing `SurchargeFormatter` from `ui-core` for label generation
- Structure using drop-in grouping strategy:
  ```kotlin
  LazyColumn {
      item { /* Description text */ }
      
      val (noFeePaymentMethods, withFeesPaymentMethods) = state.paymentMethods.groupBySurchargeForUI()
      
      // No additional fee section
      if (noFeePaymentMethods.isNotEmpty()) {
          item { SectionHeader(stringResource(R.string.no_additional_fee)) }
          items(noFeePaymentMethods) { /* PaymentMethodSelector */ }
      }
      
      // Additional fees section  
      if (withFeesPaymentMethods.isNotEmpty()) {
          item { SectionHeader(stringResource(R.string.additional_fees_may_apply)) }
          items(withFeesPaymentMethods) { /* PaymentMethodSelector */ }
      }
  }
  ```

### Phase 4: Section Header Component [SAFE]
**Files affected:** 1 file, ~15 lines
- Create `SectionHeader` composable using existing design tokens
- Follow existing typography and color patterns from the screen

## Technical Considerations

### Reusing Drop-in Logic
**Core Algorithm from `PaymentMethodButtonGroupFactory`:**
```kotlin
// Adapted from drop-in/PaymentMethodButtonGroupFactory.getPaymentMethodGroupKey()
private fun getPaymentMethodGroupKey(surcharge: Surcharge?): Int {
    return when (surcharge) {
        is Surcharge.CardNetworksSurcharge -> if (surcharge.surcharges.any { it.value != 0 }) 100000 else 0
        is Surcharge.PaymentMethodSurcharge -> surcharge.amount
        null -> 0
    }
}
```

**Shared Components:**
- **`SurchargeFormatter`**: Already in `ui-core` module - perfect for reuse
- **`SurchargeInteractor`**: Data layer for surcharge retrieval
- **`SurchargeCalculationInteractor`**: Business logic for calculations
- **Grouping Strategy**: Same algorithm as drop-in but adapted for Compose

### Dependencies
- **SurchargeFormatter**: Already available in `ui-core` module (no move needed)
- **Design Tokens**: Use existing `LocalPrimerTypographyTokens` and `LocalPrimerColorTokens`
- **String Resources**: Add to existing `components` module strings
- **Grouping Algorithm**: Extract from drop-in `PaymentMethodButtonGroupFactory`

### Grouping Logic Details
```kotlin
// Shared utility function (based on drop-in logic)
fun List<PrimerComposablePaymentMethod>.groupBySurchargeForUI(): Pair<List<PrimerComposablePaymentMethod>, List<PrimerComposablePaymentMethod>> {
    val (noFee, withFees) = partition { paymentMethod ->
        val groupKey = getPaymentMethodGroupKey(paymentMethod.surcharge)
        groupKey == 0 // 0 = no surcharge group
    }
    return noFee to withFees
}

private fun getPaymentMethodGroupKey(surcharge: Surcharge?): Int {
    return when (surcharge) {
        is Surcharge.CardNetworksSurcharge -> if (surcharge.surcharges.any { it.value != 0 }) 100000 else 0
        is Surcharge.PaymentMethodSurcharge -> surcharge.amount
        null -> 0
    }
}
```

### Backwards Compatibility
- No breaking changes to existing APIs
- Maintains existing `PaymentMethodSelector` component usage
- Uses existing state management patterns

## Risk Assessment

### Low Risk
- String resource additions
- Extension function for grouping
- Section header component creation

### Medium Risk
- UI layout changes in `Ready()` composable
- Testing grouping logic with different surcharge types

### Validation Steps
1. Build project successfully
2. Verify string resources are accessible
3. Test with payment methods that have no surcharge
4. Test with payment methods that have surcharges
5. Test with mixed payment method lists
6. Verify design consistency with existing screens

## Files Summary

| File | Type | Lines | Risk |
|------|------|-------|------|
| `components/src/main/res/values/strings.xml` | Resource | +2 | Safe |
| `DefaultPaymentMethodSelectionScreen.kt` | Kotlin | ~90 | Moderate |

## Implementation Notes

### Design Consistency
- Use existing spacing tokens (`LocalPrimerSpacingTokens.current.large/small`)
- Use existing typography (`LocalPrimerTypographyTokens.current.titleLarge`)
- Use existing colors (`LocalPrimerColorTokens.current.primerColorTextPrimary`)

### Performance Considerations
- Grouping happens once per state change (not per recomposition)
- Maintains lazy loading benefits of `LazyColumn`
- No additional network calls or heavy computations

### Future Extensibility
- Can easily add more surcharge-based grouping logic
- Section headers are reusable components
- Grouping logic can be extracted to domain layer if needed

## Post-Implementation Verification

### Manual Testing Scenarios
1. **No surcharges**: All payment methods in "No additional fee" section
2. **All with surcharges**: All payment methods in "Additional fees may apply" section  
3. **Mixed scenario**: Payment methods distributed across both sections
4. **Empty list**: Graceful handling with no sections displayed
5. **Single payment method**: Appropriate section display

### Design Review Points
- Section header typography matches design system
- Spacing between sections follows design guidelines
- Overall layout maintains existing visual hierarchy
- Dark mode compatibility (using design tokens)

---

**Total Implementation Effort**: ~2-3 hours
**Files Modified**: 2 files
**Lines of Code**: ~90 lines
**Risk Level**: Moderate (UI changes, new grouping logic)