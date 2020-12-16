package io.primer.android.events

enum class CheckoutEventType {
  TOKENIZE_SUCCESS,
  TOKENIZE_ERROR,
  TOKEN_ADDED_TO_VAULT,
  TOKEN_REMOVED_FROM_VAULT,
  DISMISS,
}