package com.mercadopago.android.px.internal.features.pay_button

import com.mercadopago.android.px.addons.model.SecurityValidationData
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel as ButtonConfig
import com.mercadopago.android.px.model.exceptions.MercadoPagoError

internal sealed class PayButtonState

internal open class UIProgress : PayButtonState() {
    data class FingerprintRequired(val validationData: SecurityValidationData) : UIProgress()
    data class ButtonLoadingStarted(val timeOut: Int, val buttonConfig: ButtonConfig) : UIProgress()
    data class ButtonLoadingFinished(val explodeDecorator: ExplodeDecorator) : UIProgress()
    object ButtonLoadingCanceled : UIProgress()
}

internal open class UIResult : PayButtonState() {
    object VisualProcessorResult : UIResult()
    data class PaymentResult(val model: PaymentModel) : UIResult()
    data class BusinessPaymentResult(val model: BusinessPaymentModel) : UIResult()
    data class NoCongratsResult(val model : PaymentModel) : UIResult()
}

internal open class UIError(val message: String, val detail: String) : PayButtonState() {
    class ConnectionError(error: MercadoPagoError) : UIError(error.message.orEmpty(), error.errorDetail.orEmpty())
}