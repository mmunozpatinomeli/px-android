package com.mercadopago.android.px.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mercadopago.android.px.internal.core.ConnectionHelper
import com.mercadopago.android.px.internal.features.express.offline_methods.OfflineMethodsViewModel
import com.mercadopago.android.px.internal.features.pay_button.PayButtonViewModel
import com.mercadopago.android.px.internal.viewmodel.mappers.PayButtonViewModelMapper

internal class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PayButtonViewModel::class.java)) {
            return PayButtonViewModel(Session.getInstance().paymentRepository,
                Session.getInstance().configurationModule.productIdProvider,
                ConnectionHelper.instance,
                Session.getInstance().configurationModule.paymentSettings,
                Session.getInstance().configurationModule.customTextsRepository, PayButtonViewModelMapper()) as T
        } else if(modelClass.isAssignableFrom(OfflineMethodsViewModel::class.java)) {
            return OfflineMethodsViewModel(Session.getInstance().initRepository,
                Session.getInstance().configurationModule.paymentSettings,
                Session.getInstance().amountRepository,
                Session.getInstance().discountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}