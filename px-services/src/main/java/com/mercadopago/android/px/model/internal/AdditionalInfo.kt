package com.mercadopago.android.px.model.internal

import com.google.gson.annotations.SerializedName
import com.mercadopago.android.px.internal.core.extensions.isNotNullNorEmpty
import com.mercadopago.android.px.internal.util.JsonUtil

data class AdditionalInfo(
    @SerializedName("px_summary") val summaryInfo: SummaryInfo?,
    @SerializedName("px_custom_texts") val customTexts: CustomTexts?) {

    companion object {
        fun newInstance(additionalInfo: String?): AdditionalInfo? {
            return if (additionalInfo.isNotNullNorEmpty()) {
                try {
                    JsonUtil.fromJson(additionalInfo, AdditionalInfo::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
}