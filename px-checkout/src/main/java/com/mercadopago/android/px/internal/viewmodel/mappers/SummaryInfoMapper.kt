package com.mercadopago.android.px.internal.viewmodel.mappers

import com.mercadopago.android.px.internal.extensions.orIfEmpty
import com.mercadopago.android.px.model.internal.AdditionalInfo
import com.mercadopago.android.px.model.internal.SummaryInfo
import com.mercadopago.android.px.preferences.CheckoutPreference

class SummaryInfoMapper : Mapper<CheckoutPreference, SummaryInfo>() {

    override fun map(preference: CheckoutPreference): SummaryInfo {
        return AdditionalInfo.newInstance(preference.additionalInfo)?.summaryInfo ?:
        preference.items[0].run {
            SummaryInfo(description.orIfEmpty(title), pictureUrl)
        }
    }
}