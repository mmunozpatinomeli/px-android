package com.mercadopago.android.px.internal.viewmodel

import com.mercadopago.android.px.model.internal.Text

data class AmountDescriptor(val description: List<Text>,
                       val amount: Text,
                       val brief: List<Text>?,
                       val url: String?)