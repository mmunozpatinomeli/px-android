package com.mercadopago.android.px.internal.tracking

import android.content.Context
import android.content.SharedPreferences

class TrackingRepositoryImpl(context: Context, private val sharedPreferences: SharedPreferences) : TrackingRepository {

    private var internalSessionId: String? = null
    override val sessionId: String
        get() {
            if (internalSessionId == null) {
                internalSessionId = sharedPreferences.getString(PREF_SESSION_ID, null)
            }
            return internalSessionId ?: DEFAULT_SESSION_ID
        }

    private val flowProvider = FlowProvider(sharedPreferences)
    private val legacyFlowProvider = LegacyFlowProvider(context)

    override val flowId: String
        get() = flowProvider.flowId ?: legacyFlowProvider.flowId ?: DEFAULT_FLOW_ID
    override val flowDetail: Map<String, Any>
        get() = flowProvider.flowDetail ?: legacyFlowProvider.flowDetail ?: emptyMap()

    override fun configure(model: TrackingRepository.Model) {
        internalSessionId = model.sessionId
        with(sharedPreferences.edit()) {
            putString(PREF_SESSION_ID, internalSessionId)
            apply()
        }
        flowProvider.configure(model.flowId, model.flowDetail)
    }

    override fun reset() {
        internalSessionId = null
        with(sharedPreferences.edit()) {
            remove(PREF_SESSION_ID)
            apply()
        }
        flowProvider.reset()
    }

    companion object {
        private const val DEFAULT_SESSION_ID = "no-value"
        private const val DEFAULT_FLOW_ID = "unknown"
        private const val PREF_SESSION_ID = "PREF_SESSION_ID"
    }
}