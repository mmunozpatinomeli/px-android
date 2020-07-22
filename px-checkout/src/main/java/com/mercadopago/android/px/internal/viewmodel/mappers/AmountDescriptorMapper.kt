package com.mercadopago.android.px.internal.viewmodel.mappers

import com.mercadopago.android.px.internal.viewmodel.AmountDescriptor
import com.mercadopago.android.px.model.DiscountOverview

class AmountDescriptorMapper: Mapper<DiscountOverview, AmountDescriptor>() {

    override fun map(model: DiscountOverview) = AmountDescriptor(
            model.description,
            model.amount,
            model.brief,
            model.url
    )
}