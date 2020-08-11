package com.mercadopago.android.px.internal.viewmodel

import android.content.Context
import com.mercadopago.android.px.R
import com.mercadopago.android.px.internal.extensions.orIfEmpty
import com.mercadopago.android.px.internal.repository.CustomTextsRepository

class TotalLocalized(private val customTextsRepository: CustomTextsRepository) : ILocalizedCharSequence {

    override fun get(context: Context): CharSequence {
        return customTextsRepository.customTexts.totalDescription.orIfEmpty(context.getString(R.string.px_total_to_pay))
    }
}