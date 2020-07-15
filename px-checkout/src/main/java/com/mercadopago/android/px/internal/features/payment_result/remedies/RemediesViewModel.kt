package com.mercadopago.android.px.internal.features.payment_result.remedies

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.base.BaseViewModel
import com.mercadopago.android.px.internal.features.pay_button.PayButton
import com.mercadopago.android.px.internal.repository.*
import com.mercadopago.android.px.internal.services.Response
import com.mercadopago.android.px.internal.services.awaitCallback
import com.mercadopago.android.px.internal.util.CVVRecoveryWrapper
import com.mercadopago.android.px.internal.util.TokenCreationWrapper
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PayerCost
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.internal.InitResponse
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.model.internal.remedies.RemedyPaymentMethod
import com.mercadopago.android.px.tracking.internal.events.RemedyEvent
import com.mercadopago.android.px.tracking.internal.model.RemedyTrackData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RemediesViewModel(
    private val remediesModel: RemediesModel,
    private val previousPaymentModel: PaymentModel,
    paymentRepository: PaymentRepository,
    private val paymentSettingRepository: PaymentSettingRepository,
    private val cardTokenRepository: CardTokenRepository,
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val initRepository: InitRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository
) : BaseViewModel(), Remedies.ViewModel {

    private var paymentRecovery = paymentRepository.createPaymentRecovery()
    val remedyState: MutableLiveData<RemedyState> = MutableLiveData()
    private val isSilverBullet = remediesModel.retryPayment?.isAnotherMethod == true
    private var cvv = ""
    private var paymentConfiguration: PaymentConfiguration? = null
    private var card: Card? = null

    init {
        val methodIds = getMethodIds()
        val customOptionId = methodIds.customOptionId
        CoroutineScope(Dispatchers.IO).launch {
            loadInitResponse()?.let {initResponse ->
                val methodData = initResponse.express.find { it.customOptionId == customOptionId }
                val isCard = methodData?.isCard == true
                if (isCard) {
                    card = initResponse.getCardById(customOptionId)
                }
                paymentConfiguration = PaymentConfiguration(methodIds.methodId, methodIds.typeId, customOptionId, isCard, false,
                    getPayerCost(customOptionId))
                withContext(Dispatchers.Main) {
                    remediesModel.retryPayment?.let {
                        remedyState.value = RemedyState.ShowRetryPaymentRemedy(Pair(it, methodData))
                    }
                    remediesModel.highRisk?.let {
                        remedyState.value = RemedyState.ShowKyCRemedy(it)
                    }
                }
            }
        }
    }

    override fun onPayButtonPressed(callback: PayButton.OnEnqueueResolvedCallback) {
        if (isSilverBullet) {
            startPayment(callback)
        } else {
            startCvvRecovery(callback)
        }
    }

    override fun onPrePayment(callback: PayButton.OnReadyForPaymentCallback) {
        callback.call(paymentConfiguration!!)
    }

    private fun getMethodIds(): MethodIds {
        return previousPaymentModel.run {
            if (isSilverBullet) {
                remedies.suggestedPaymentMethod!!.alternativePaymentMethod.let {
                    MethodIds.with(it)
                }
            } else {
                MethodIds.with(paymentResult.paymentData)
            }
        }
    }

    private fun getPayerCost(customOptionId: String): PayerCost? {
        return previousPaymentModel.run {
            if (isSilverBullet) {
                remedies.suggestedPaymentMethod?.alternativePaymentMethod?.installmentsList?.run {
                    if (isNotEmpty()) {
                        get(0).let {
                            amountConfigurationRepository.getConfigurationFor(customOptionId)?.run {
                                for (i in 0 until payerCosts.size) {
                                    val payerCost = payerCosts[i]
                                    if (payerCost.installments == it.installments) {
                                        remediesModel.retryPayment?.payerCost = RemediesPayerCost(i, it.installments)
                                        return payerCost
                                    }
                                }
                            }
                        }
                    }
                    return null
                }
            } else {
                paymentResult.paymentData.payerCost
            }
        }
    }

    private suspend fun loadInitResponse() =
        when (val callbackResult = initRepository.init().awaitCallback<InitResponse>()) {
            is Response.Success<*> -> callbackResult.result as InitResponse
            is Response.Failure<*> -> null
        }

    private fun startPayment(callback: PayButton.OnEnqueueResolvedCallback) {
        RemedyEvent(getRemedyTrackData(RemedyType.PAYMENT_METHOD_SUGGESTION)).track()
        remediesModel.retryPayment?.cvvModel?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val tokenCreationWrapper = TokenCreationWrapper.Builder(cardTokenRepository, escManagerBehaviour)
                    .with(card!!).build()
                try {
                    tokenCreationWrapper.createToken(cvv).apply { paymentSettingRepository.configure(this) }
                    withContext(Dispatchers.Main) {
                        callback.success()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback.failure()
                    }
                }
            }
        } ?: callback.success()
    }

    private fun startCvvRecovery(callback: PayButton.OnEnqueueResolvedCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            CVVRecoveryWrapper(cardTokenRepository, escManagerBehaviour, paymentRecovery)
                .recoverWithCVV(cvv)?.let {
                    paymentSettingRepository.configure(it)
                    withContext(Dispatchers.Main) {
                        RemedyEvent(getRemedyTrackData(RemedyType.CVV_REQUEST)).track()
                        callback.success()
                    }
                } ?: withContext(Dispatchers.Main) {
                callback.failure()
            }
        }
    }

    override fun onButtonPressed(action: RemedyButton.Action) {
        when(action) {
            RemedyButton.Action.CHANGE_PM -> remedyState.value = RemedyState.ChangePaymentMethod
            RemedyButton.Action.KYC -> remediesModel.highRisk?.let {
                RemedyEvent(getRemedyTrackData(RemedyType.KYC_REQUEST)).track()
                remedyState.value = RemedyState.GoToKyc(it.deepLink)
            }
            else -> TODO()
        }
    }

    override fun onCvvFilled(cvv: String) {
        this.cvv = cvv
    }

    override fun recoverFromBundle(bundle: Bundle) {
        super.recoverFromBundle(bundle)
        cvv = bundle.getString(EXTRA_CVV, "")
        paymentRecovery = bundle.getSerializable(EXTRA_PAYMENT_RECOVERY) as PaymentRecovery
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.putString(EXTRA_CVV, cvv)
        bundle.putSerializable(EXTRA_PAYMENT_RECOVERY, paymentRecovery)
    }

    private fun getRemedyTrackData(type: RemedyType) = previousPaymentModel.payment!!.let {
        RemedyTrackData(type.getType(), remediesModel.trackingData, it.paymentStatus, it.paymentStatusDetail)
    }

    private data class MethodIds(val methodId: String, val typeId: String, val customOptionId: String) {
        companion object {
            fun with(paymentData: PaymentData): MethodIds {
                return paymentData.run {
                    val methodId = paymentMethod.id
                    MethodIds(methodId, paymentMethod.paymentTypeId, token?.cardId ?: methodId)
                }
            }
            fun with(remedyPaymentMethod: RemedyPaymentMethod) =
                MethodIds(remedyPaymentMethod.paymentMethodId, remedyPaymentMethod.paymentTypeId,
                remedyPaymentMethod.customOptionId)
        }
    }

    companion object {
        private const val EXTRA_CVV = "extra_cvv"
        private const val EXTRA_PAYMENT_RECOVERY = "payment_recovery"
    }
}