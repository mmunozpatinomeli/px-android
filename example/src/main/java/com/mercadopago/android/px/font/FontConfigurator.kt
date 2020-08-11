package com.mercadopago.android.px.font

import com.mercadolibre.android.ui.font.TypefaceHelper

class FontConfigurator {
    companion object {
        @JvmStatic
        fun configure() {
            val sampleFontTypefaceMapper = MontserratFontTypefaceMapper()
            val sampleTypefaceSetter = SampleTypefaceSetter(sampleFontTypefaceMapper)
            TypefaceHelper.attachTypefaceSetter(sampleTypefaceSetter)
        }
    }
}