package com.mercadopago.android.px.internal.di

import android.content.Context
import com.mercadopago.android.px.internal.core.ApplicationModule
import com.mercadopago.android.px.internal.core.ProductIdProvider
import com.mercadopago.android.px.internal.tracking.TrackingRepository
import com.mercadopago.android.px.internal.tracking.TrackingRepositoryImpl

abstract class ConfigurationModule(context: Context) : ApplicationModule(context) {

    val productIdProvider by lazy { ProductIdProvider(sharedPreferences) }
    val trackingRepository: TrackingRepository by lazy { TrackingRepositoryImpl(applicationContext, sharedPreferences) }

    open fun reset() {
        productIdProvider.reset()
        trackingRepository.reset()
    }

    companion object {
        lateinit var INSTANCE: ConfigurationModule
        @JvmStatic fun initialize(configurationModule: ConfigurationModule) {
            INSTANCE = configurationModule
        }
    }
}