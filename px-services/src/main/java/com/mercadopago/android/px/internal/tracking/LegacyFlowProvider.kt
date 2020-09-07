package com.mercadopago.android.px.internal.tracking

import android.content.Context

class LegacyFlowProvider(context: Context) : FlowProvider(
    context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)!!
) {
    override fun reset() {
        //We don't reset the legacy flow because it's setted in a static method
    }

    companion object {
        private const val SHARED_PREFERENCE_NAME = "com.mercadopago.checkout.store.tracking_legacy"
    }
}