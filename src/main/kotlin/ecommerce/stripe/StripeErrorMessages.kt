package ecommerce.stripe

fun String?.toUserFriendlyMessage(): String =
    when (this?.lowercase()) {
        "generic_decline" -> "The payment was declined by the bank."
        "insufficient_funds" -> "The card has insufficient funds."
        "lost_card" -> "The card has been reported lost."
        "stolen_card" -> "The card has been reported stolen."
        "expired_card" -> "The card has expired."
        "incorrect_cvc" -> "The CVC code is incorrect."
        "processing_error" -> "A processing error occurred. Please try again later."
        "card_velocity_exceeded" -> "The card exceeded its transaction limit."

        "requires_payment_method" -> "Please choose a different payment method."
        "requires_action" -> "Additional authentication is required to complete the payment."
        "canceled" -> "The payment session was canceled or expired."

        null, "" -> "The payment was declined. Please try a different payment method."
        else -> "The payment was declined ($this)."
    }
