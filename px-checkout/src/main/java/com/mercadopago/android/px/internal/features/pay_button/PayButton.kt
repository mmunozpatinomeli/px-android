package com.mercadopago.android.px.internal.features.pay_button

import com.mercadopago.android.px.internal.features.explode.ExplodingFragment
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.model.ConfirmData

interface PayButton {

    interface View : ExplodingFragment.Handler {
        fun isExploding(): Boolean
        fun stimulate()
        fun enable()
        fun disable()
    }

    interface ViewModel {
        fun attach(handler: Handler)
        fun detach()
        fun preparePayment()
        fun handleBiometricsResult(isSuccess: Boolean, securityRequested: Boolean)
        fun startPayment()
        fun hasFinishPaymentAnimation()
        fun recoverPayment()
        fun onRecoverPaymentEscInvalid(recovery: PaymentRecovery)
        fun onPostPayment(paymentModel: PaymentModel)
        fun onPostPaymentAction(postPaymentAction: PostPaymentAction)
    }

    interface Handler {
        fun prePayment(callback: OnReadyForPaymentCallback)
        @JvmDefault fun enqueueOnExploding(callback: OnEnqueueResolvedCallback) = callback.success()
        @JvmDefault fun onPostPaymentAction(postPaymentAction: PostPaymentAction) = Unit
        @JvmDefault fun onPaymentFinished(paymentModel: PaymentModel, callback: OnPaymentFinishedCallback) = callback.call()
        @JvmDefault fun onPaymentError(error: MercadoPagoError) = Unit
    }

    interface OnReadyForPaymentCallback {
        fun call(paymentConfiguration: PaymentConfiguration, confirmTrackerData: ConfirmData? = null)
    }

    interface OnEnqueueResolvedCallback {
        fun success()
        fun failure()
    }

    interface OnPaymentFinishedCallback {
        fun call()
    }
}