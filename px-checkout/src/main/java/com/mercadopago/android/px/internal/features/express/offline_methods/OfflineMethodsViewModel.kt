package com.mercadopago.android.px.internal.features.express.offline_methods

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mercadopago.android.px.internal.base.BaseViewModel
import com.mercadopago.android.px.internal.extensions.orIfEmpty
import com.mercadopago.android.px.internal.features.pay_button.PayButton.OnReadyForPaymentCallback
import com.mercadopago.android.px.internal.livedata.MutableSingleLiveData
import com.mercadopago.android.px.internal.repository.AmountRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.InitRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized
import com.mercadopago.android.px.model.OfflineMethodsCompliance
import com.mercadopago.android.px.model.SensitiveInformation
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.events.BackEvent
import com.mercadopago.android.px.tracking.internal.events.KnowYourCustomerFlowEvent
import com.mercadopago.android.px.tracking.internal.model.ConfirmData
import com.mercadopago.android.px.tracking.internal.views.OfflineMethodsViewTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class OfflineMethodsViewModel(private val initRepository: InitRepository,
    private val paymentSettingRepository: PaymentSettingRepository,
    private val amountRepository: AmountRepository,
    private val discountRepository: DiscountRepository) : BaseViewModel(), OfflineMethods.ViewModel {

    private lateinit var viewTracker: OfflineMethodsViewTracker
    private var payerCompliance: OfflineMethodsCompliance? = null
    private var selectedItem: OfflineMethodItem? = null

    private val observableDeepLink = MutableSingleLiveData<String>()

    override fun onViewLoaded(): LiveData<OfflineMethods.Model> {
        val liveData = MutableLiveData<OfflineMethods.Model>()
        CoroutineScope(Dispatchers.IO).launch {
            initRepository.loadInitResponse()?.let {
                val offlineMethods = it.express.firstOrNull { express -> express.isOfflineMethods }?.offlineMethods
                val bottomDescription = offlineMethods?.displayInfo?.bottomDescription
                val defaultPaymentTypeId = offlineMethods?.paymentTypes?.firstOrNull()?.id ?: TextUtil.EMPTY
                val amountLocalized = AmountLocalized(
                    amountRepository.getAmountToPay(defaultPaymentTypeId, discountRepository.currentConfiguration),
                    paymentSettingRepository.currency)
                payerCompliance = it.payerCompliance?.offlineMethods
                val offlinePaymentTypes = offlineMethods?.paymentTypes.orEmpty()
                viewTracker = OfflineMethodsViewTracker(offlinePaymentTypes)
                liveData.postValue(OfflineMethods.Model(bottomDescription, amountLocalized, offlinePaymentTypes))
            }
        }
        return liveData
    }

    override fun onSheetShowed() {
        viewTracker.track()
    }

    override fun onMethodSelected(selectedItem: OfflineMethodItem) {
        this.selectedItem = selectedItem
    }

    override fun onPrePayment(callback: OnReadyForPaymentCallback) {
        selectedItem?.let { item ->
            payerCompliance?.let {
                if (item.isAdditionalInfoNeeded && it.isCompliant) {
                    completePayerInformation(it.sensitiveInformation)
                } else if (item.isAdditionalInfoNeeded) {
                    KnowYourCustomerFlowEvent(viewTracker).track()
                    observableDeepLink.value = it.turnComplianceDeepLink
                    return
                }
            }
            requireCurrentConfiguration(item, callback)
        }
    }

    private fun requireCurrentConfiguration(item: OfflineMethodItem, callback: OnReadyForPaymentCallback) {
        val paymentMethodId = item.paymentMethodId.orIfEmpty(TextUtil.EMPTY)
        val paymentTypeId = item.paymentTypeId.orIfEmpty(TextUtil.EMPTY)
        val confirmData = ConfirmData.from(paymentTypeId, paymentMethodId,
            payerCompliance?.isCompliant == true, item.isAdditionalInfoNeeded)
        val paymentConfiguration = PaymentConfiguration(paymentMethodId, paymentTypeId,
            paymentMethodId, isCard = false, splitPayment = false, payerCost = null)
        callback.call(paymentConfiguration, confirmData)
    }

    private fun completePayerInformation(sensitiveInformation: SensitiveInformation) {
        val checkoutPreference = paymentSettingRepository.checkoutPreference
        val payer = checkoutPreference!!.payer
        payer.firstName = sensitiveInformation.firstName
        payer.lastName = sensitiveInformation.lastName
        payer.identification = sensitiveInformation.identification
        paymentSettingRepository.configure(checkoutPreference)
    }

    override fun onBack() {
        BackEvent(viewTracker).track()
    }

    override fun getObservableDeepLink(): LiveData<String> = observableDeepLink
}