package com.mercadopago.android.px.internal.features.pay_button

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import com.mercadopago.android.px.addons.model.SecurityValidationData
import com.mercadopago.android.px.internal.base.BaseViewModel
import com.mercadopago.android.px.internal.callbacks.PaymentServiceEventHandler
import com.mercadopago.android.px.internal.core.ConnectionHelper
import com.mercadopago.android.px.internal.core.ProductIdProvider
import com.mercadopago.android.px.internal.extensions.isNotNullNorEmpty
import com.mercadopago.android.px.internal.features.checkout.PostPaymentDriver
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper
import com.mercadopago.android.px.internal.features.pay_button.PayButton.OnReadyForPaymentCallback
import com.mercadopago.android.px.internal.features.pay_button.UIProgress.*
import com.mercadopago.android.px.internal.features.pay_button.UIResult.VisualProcessorResult
import com.mercadopago.android.px.internal.livedata.MediatorSingleLiveData
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModelMapper
import com.mercadopago.android.px.internal.model.SecurityType
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.internal.repository.PaymentRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.internal.util.SecurityValidationDataFactory
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction
import com.mercadopago.android.px.internal.viewmodel.mappers.PayButtonViewModelMapper
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.exceptions.NoConnectivityException
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.events.BiometricsFrictionTracker
import com.mercadopago.android.px.tracking.internal.events.ConfirmEvent
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker
import com.mercadopago.android.px.tracking.internal.model.ConfirmData
import com.mercadopago.android.px.tracking.internal.model.Reason
import com.mercadopago.android.px.tracking.internal.views.OneTapViewTracker
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel as ButtonConfig

internal class PayButtonViewModel(
    private val paymentService: PaymentRepository,
    private val productIdProvider: ProductIdProvider,
    private val connectionHelper: ConnectionHelper,
    private val paymentSettingRepository: PaymentSettingRepository,
    customTextsRepository: CustomTextsRepository,
    payButtonViewModelMapper: PayButtonViewModelMapper) : BaseViewModel(), PayButton.ViewModel {

    val buttonTextLiveData = MutableLiveData<ButtonConfig>()
    private var buttonConfig: ButtonConfig = payButtonViewModelMapper.map(customTextsRepository.customTexts)

    init {
        buttonTextLiveData.value = buttonConfig
    }

    private var handler: PayButton.Handler? = null
    private var confirmTrackerData: ConfirmData? = null
    private var paymentConfiguration: PaymentConfiguration? = null
    private var paymentModel: PaymentModel? = null

    val cvvRequiredLiveData = MediatorSingleLiveData<Pair<Card, Reason>?>()
    val recoverRequiredLiveData = MediatorSingleLiveData<PaymentRecovery?>()
    val stateUILiveData = MediatorSingleLiveData<PayButtonState>()
    private var observingService = false

    private fun <X : Any, I> transform(liveData: LiveData<X>, block: (content: X) -> I): LiveData<I?> {
        return map(liveData) {
            observingService = false
            block(it)
        }
    }

    override fun attach(handler: PayButton.Handler) {
        this.handler = handler
    }

    override fun detach() {
        handler = null
    }

    override fun preparePayment() {
        paymentConfiguration = null
        confirmTrackerData = null
        if (connectionHelper.checkConnection()) {
            handler?.prePayment(object : OnReadyForPaymentCallback {
                override fun call(paymentConfiguration: PaymentConfiguration, confirmTrackerData: ConfirmData?) {
                    if (paymentConfiguration.customOptionId.isNotNullNorEmpty()) {
                        paymentSettingRepository.clearToken()
                    }
                    startSecuredPayment(paymentConfiguration, confirmTrackerData)
                }
            })
        } else {
            manageNoConnection()
        }
    }

    private fun startSecuredPayment(paymentConfiguration: PaymentConfiguration, confirmTrackerData: ConfirmData?) {
        this.paymentConfiguration = paymentConfiguration
        this.confirmTrackerData = confirmTrackerData
        val data: SecurityValidationData = SecurityValidationDataFactory
            .create(productIdProvider, paymentSettingRepository.checkoutPreference!!.totalAmount, paymentConfiguration)
        stateUILiveData.value = FingerprintRequired(data)
    }

    override fun handleBiometricsResult(isSuccess: Boolean, securityRequested: Boolean) {
        if (isSuccess) {
            paymentSettingRepository.configure(if (securityRequested) SecurityType.SECOND_FACTOR else SecurityType.NONE)
            startPayment()
        } else {
            BiometricsFrictionTracker.track()
        }
    }

    override fun startPayment() {
        if (paymentService.isExplodingAnimationCompatible) {
            stateUILiveData.postValue(ButtonLoadingStarted(paymentService.paymentTimeout, buttonConfig))
        }
        handler?.enqueueOnExploding(object : PayButton.OnEnqueueResolvedCallback {
            override fun success() {
                paymentService.startExpressPayment(paymentConfiguration!!)
                observeService(paymentService.observableEvents)
                confirmTrackerData?.let { ConfirmEvent(it).track() }
            }

            override fun failure() {
                stateUILiveData.value = (ButtonLoadingCanceled)
            }
        })
    }

    private fun observeService(serviceLiveData: PaymentServiceEventHandler) {
        observingService = true
        // Error event
        val paymentErrorLiveData: LiveData<ButtonLoadingCanceled?> =
            transform(serviceLiveData.paymentErrorLiveData) { error ->
                val shouldHandleError = error.isPaymentProcessing
                if (shouldHandleError) onPaymentProcessingError() else noRecoverableError(error)
                handler?.onPaymentError(error)
                ButtonLoadingCanceled
            }
        stateUILiveData.addSource(paymentErrorLiveData) { stateUILiveData.value = it }

        // Visual payment event
        val visualPaymentLiveData: LiveData<VisualProcessorResult?> =
            transform(serviceLiveData.visualPaymentLiveData) { VisualProcessorResult }
        stateUILiveData.addSource(visualPaymentLiveData) { stateUILiveData.value = it }

        // Payment finished event
        val paymentFinishedLiveData: LiveData<ButtonLoadingFinished?> =
            transform(serviceLiveData.paymentFinishedLiveData) { paymentModel ->
                this.paymentModel = paymentModel
                ButtonLoadingFinished(ExplodeDecoratorMapper().map(paymentModel))
            }
        stateUILiveData.addSource(paymentFinishedLiveData) { stateUILiveData.value = it }

        // Cvv required event
        val cvvRequiredLiveData: LiveData<Pair<Card, Reason>?> = transform(serviceLiveData.requireCvvLiveData) { it }
        this.cvvRequiredLiveData.addSource(cvvRequiredLiveData) { this.cvvRequiredLiveData.value = it }

        // Invalid esc event
        val recoverRequiredLiveData: LiveData<PaymentRecovery?> =
            transform(serviceLiveData.recoverInvalidEscLiveData) { it.takeIf { it.shouldAskForCvv() } }
        this.recoverRequiredLiveData.addSource(recoverRequiredLiveData) { this.recoverRequiredLiveData.value = it }
    }

    private fun onPaymentProcessingError() {
        val currency: Currency = paymentSettingRepository.currency
        val paymentResult: PaymentResult = PaymentResult.Builder()
            .setPaymentData(paymentService.paymentDataList)
            .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
            .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
            .build()
        onPostPayment(PaymentModel(paymentResult, currency))
    }

    override fun onPostPayment(paymentModel: PaymentModel) {
        this.paymentModel = paymentModel
        stateUILiveData.value = ButtonLoadingFinished(ExplodeDecoratorMapper().map(paymentModel))
    }

    override fun onPostPaymentAction(postPaymentAction: PostPaymentAction) {
        postPaymentAction.execute(object : PostPaymentAction.ActionController {
            override fun recoverPayment(postPaymentAction: PostPaymentAction) {
                stateUILiveData.value = ButtonLoadingCanceled
                recoverPayment()
            }

            override fun onChangePaymentMethod() {
                stateUILiveData.value = ButtonLoadingCanceled
            }
        })
        handler?.onPostPaymentAction(postPaymentAction)
    }

    override fun onRecoverPaymentEscInvalid(recovery: PaymentRecovery) {
        recoverPayment(recovery)
    }

    override fun recoverPayment() {
        recoverPayment(paymentService.createPaymentRecovery())
    }

    private fun recoverPayment(recovery: PaymentRecovery) {
        recoverRequiredLiveData.value = recovery
    }

    private fun manageNoConnection() {
        val exception = NoConnectivityException()
        val apiException = ApiUtil.getApiException(exception)
        val error = MercadoPagoError(apiException, null)
        noRecoverableError(error)
    }

    private fun noRecoverableError(error: MercadoPagoError) {
        FrictionEventTracker.with(OneTapViewTracker.PATH_REVIEW_ONE_TAP_VIEW,
            FrictionEventTracker.Id.GENERIC, FrictionEventTracker.Style.CUSTOM_COMPONENT, error).track()
        stateUILiveData.value = UIError.ConnectionError(error)
    }

    override fun hasFinishPaymentAnimation() {
        paymentModel?.let {
            handler?.onPaymentFinished(it, object : PayButton.OnPaymentFinishedCallback {
                override fun call() {
                    PostPaymentDriver.Builder(paymentSettingRepository, it).action(object : PostPaymentDriver.Action {
                        override fun showCongrats(model: PaymentModel) {
                            stateUILiveData.value = UIResult.PaymentResult(model)
                        }

                        override fun showCongrats(model: BusinessPaymentModel) {
                            stateUILiveData.value = UIResult.CongratsPaymentModel(PaymentCongratsModelMapper().map(model))
                        }

                        override fun skipCongrats(model: PaymentModel) {
                            stateUILiveData.value = UIResult.NoCongratsResult(model)
                        }
                    }).build().execute()
                }
            })
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.putParcelable(BUNDLE_PAYMENT_CONFIGURATION, paymentConfiguration)
        bundle.putParcelable(BUNDLE_CONFIRM_DATA, confirmTrackerData)
        bundle.putParcelable(BUNDLE_PAYMENT_MODEL, paymentModel)
        bundle.putBoolean(BUNDLE_OBSERVING_SERVICE, observingService)
    }

    override fun recoverFromBundle(bundle: Bundle) {
        paymentConfiguration = bundle.getParcelable(BUNDLE_PAYMENT_CONFIGURATION)
        confirmTrackerData = bundle.getParcelable(BUNDLE_CONFIRM_DATA)
        paymentModel = bundle.getParcelable(BUNDLE_PAYMENT_MODEL)
        observingService = bundle.getBoolean(BUNDLE_OBSERVING_SERVICE)
        if (observingService) {
            observeService(paymentService.observableEvents)
        }
    }

    companion object {
        const val BUNDLE_PAYMENT_CONFIGURATION = "BUNDLE_PAYMENT_CONFIGURATION"
        const val BUNDLE_CONFIRM_DATA = "BUNDLE_CONFIRM_DATA"
        const val BUNDLE_PAYMENT_MODEL = "BUNDLE_PAYMENT_MODEL"
        const val BUNDLE_OBSERVING_SERVICE = "BUNDLE_OBSERVING_SERVICE"
    }
}