package com.mercadopago.android.px.internal.features.payment_congrats.model

import android.os.Parcel
import android.os.Parcelable
import com.mercadopago.android.px.internal.extensions.readBigDecimal
import com.mercadopago.android.px.internal.extensions.writeBigDecimal
import com.mercadopago.android.px.internal.util.JsonUtil
import org.json.JSONObject
import java.math.BigDecimal

data class PXPaymentCongratsTracking(
        val campaignId: String?,
        val currencyId: String?,
        val paymentStatusDetail: String?,
        val paymentId: Long?,
        val totalAmount: BigDecimal?,
        val flowExtraInfo: Map<String, Any>? = HashMap(),
        val flow: String?,
        val sessionId: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readBigDecimal(),
            JsonUtil.getMapFromJson(parcel.readString()),
            parcel.readString(),
            parcel.readString()
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(campaignId)
        parcel.writeString(currencyId)
        parcel.writeString(paymentStatusDetail)
        parcel.writeValue(paymentId)
        parcel.writeBigDecimal(totalAmount)
        val flowExtraInfoString: String? = JSONObject(flowExtraInfo).toString()
        parcel.writeString(flowExtraInfoString)
        parcel.writeString(flow)
        parcel.writeString(sessionId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PXPaymentCongratsTracking> {
        override fun createFromParcel(parcel: Parcel): PXPaymentCongratsTracking {
            return PXPaymentCongratsTracking(parcel)
        }

        override fun newArray(size: Int): Array<PXPaymentCongratsTracking?> {
            return arrayOfNulls(size)
        }
    }
}
