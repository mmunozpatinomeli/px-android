package com.mercadopago.android.px.tracking.internal

import com.mercadopago.android.px.addons.model.Track

abstract class TrackWrapper {

    open fun track() {
        getTrack()?.let { MPTracker.getInstance().track(it) }
    }

    abstract fun getTrack(): Track?

    companion object {
        const val BASE_PATH = "/px_checkout"
        const val ADD_PAYMENT_METHOD_PATH = "/add_payment_method"
        const val PAYMENTS_PATH = "/payments"
    }
}