package com.mercadopago.android.px.internal.tracking

import com.mercadopago.android.px.internal.di.ConfigurationModule

object TrackingRepositoryHelper {

    @JvmStatic fun setLegacyFlowIdAndDetail(flowId: String?, flowDetail: Map<String, Any>) {
        LegacyFlowProvider(ConfigurationModule.INSTANCE.applicationContext)
            .configure(flowId, flowDetail)
    }
}