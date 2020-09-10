package com.mercadopago.android.px.internal.callbacks

import com.mercadopago.android.px.internal.livedata.MutableSingleLiveData
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.tracking.internal.model.Reason

internal class PaymentServiceEventHandler {
    val paymentFinishedLiveData = MutableSingleLiveData<PaymentModel>()
    val requireCvvLiveData = MutableSingleLiveData<Pair<Card, Reason>>()
    val recoverInvalidEscLiveData = MutableSingleLiveData<PaymentRecovery>()
    val paymentErrorLiveData = MutableSingleLiveData<MercadoPagoError>()
    val visualPaymentLiveData = MutableSingleLiveData<Unit>()
}