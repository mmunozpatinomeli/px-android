package com.mercadopago.android.px.internal.features.express.offline_methods

import androidx.lifecycle.LiveData
import com.mercadopago.android.px.internal.features.pay_button.PayButton
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized
import com.mercadopago.android.px.model.ExpressMetadata
import com.mercadopago.android.px.model.OfflinePaymentType
import com.mercadopago.android.px.model.internal.Text

internal interface OfflineMethods {

    interface View : PayButton.Handler {
        fun showExpanded()
        fun showCollapsed()
    }

    interface ViewModel {
        fun onSheetShowed()
        fun onViewLoaded(): LiveData<Model>
        fun onMethodSelected(selectedItem: OfflineMethodItem)
        fun onPrePayment(callback: PayButton.OnReadyForPaymentCallback)
        fun onBack()
        fun getObservableDeepLink(): LiveData<String>
    }

    interface OnMethodSelectedListener {
        fun onItemSelected(selectedItem: OfflineMethodItem)
    }

    data class Model(
        val bottomDescription: Text?,
        val amountLocalized: AmountLocalized,
        val offlinePaymentTypes: List<OfflinePaymentType>)

    companion object {
        @JvmStatic
        fun shouldLaunch(expressMetadataList: List<ExpressMetadata>): Boolean {
            return expressMetadataList.filter { express -> express.status.run { isActive && isEnabled } }
                .run { size == 1 && get(0).isOfflineMethods }
        }

    }

}