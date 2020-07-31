package com.mercadopago.android.px.internal.features.business_result

import com.mercadolibre.android.mlbusinesscomponents.components.actioncard.MLBusinessActionCardViewData
import com.mercadolibre.android.mlbusinesscomponents.components.common.downloadapp.MLBusinessDownloadAppData
import com.mercadolibre.android.mlbusinesscomponents.components.crossselling.MLBusinessCrossSellingBoxData
import com.mercadolibre.android.mlbusinesscomponents.components.loyalty.MLBusinessLoyaltyRingData
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsResponse
import com.mercadopago.android.px.model.internal.Text

internal data class CongratsViewModel(
        val loyaltyRingData: MLBusinessLoyaltyRingData?,
        val discountBoxData: PXDiscountBoxData?,
        val showAllDiscounts: PaymentCongratsResponse.Action?,
        val downloadAppData: MLBusinessDownloadAppData?,
        val actionCardViewData: MLBusinessActionCardViewData?,
        val crossSellingBoxData: List<MLBusinessCrossSellingBoxData>?,
        val topTextBox: Text,
        val viewReceipt: PaymentCongratsResponse.Action?,
        val customOrder: Boolean)