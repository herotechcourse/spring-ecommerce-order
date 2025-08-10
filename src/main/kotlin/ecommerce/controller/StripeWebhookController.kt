package ecommerce.controller

import com.stripe.net.Webhook
import ecommerce.config.StripeProperties
import ecommerce.service.OrderService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader

@RestController
@RequestMapping("/webhook/stripe")
class StripeWebhookController(
    private val stripeProperties: StripeProperties,
    private val orderService: OrderService,
) {
    @PostMapping
    fun handleStripeEvent(request: HttpServletRequest): String {
        val payload = request.reader.use(BufferedReader::readText)
        val sigHeader = request.getHeader("Stripe-Signature")

        val event = Webhook.constructEvent(payload, sigHeader, stripeProperties.webhookSecret)

        if (event.type == "payment_intent.succeeded") {
            val paymentIntent = event.dataObjectDeserializer.`object`.get() as com.stripe.model.PaymentIntent
            orderService.markOrderAsPaid(paymentIntent.id)
        }

        return "ok"
    }
}
