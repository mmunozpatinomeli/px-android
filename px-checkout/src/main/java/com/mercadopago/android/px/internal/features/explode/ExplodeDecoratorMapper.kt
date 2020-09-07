package com.mercadopago.android.px.internal.features.explode

import com.mercadopago.android.px.internal.features.PaymentResultViewModelFactory
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentResultType
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper
import com.mercadopago.android.px.model.BusinessPayment

class ExplodeDecoratorMapper : Mapper<PaymentModel, ExplodeDecorator>() {
    override fun map(model: PaymentModel): ExplodeDecorator {
        val payment = model.payment
        return when {
            model.remedies.hasRemedies() -> ExplodeDecorator.from(RemediesModel.DECORATOR)
            payment is BusinessPayment -> ExplodeDecorator.from(PaymentResultType.from(payment.decorator))
            else -> {
                val decorator = PaymentResultViewModelFactory.createPaymentResultDecorator(model.paymentResult)
                ExplodeDecorator(decorator.primaryColor, decorator.statusIcon)
            }
        }
    }
}