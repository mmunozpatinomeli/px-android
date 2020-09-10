package com.mercadopago.android.px.internal.features.pay_button

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.mercadopago.android.px.BasicRobolectricTest
import com.mercadopago.android.px.argumentCaptor
import com.mercadopago.android.px.any
import com.mercadopago.android.px.internal.callbacks.PaymentServiceEventHandler
import com.mercadopago.android.px.internal.core.ConnectionHelper
import com.mercadopago.android.px.internal.core.ProductIdProvider
import com.mercadopago.android.px.internal.datasource.CustomTextsRepositoryImpl
import com.mercadopago.android.px.internal.datasource.PaymentService
import com.mercadopago.android.px.internal.datasource.PaymentSettingService
import com.mercadopago.android.px.internal.di.Session
import com.mercadopago.android.px.internal.features.PaymentResultViewModelFactory
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesModel
import com.mercadopago.android.px.internal.livedata.MutableSingleLiveData
import com.mercadopago.android.px.internal.model.SecurityType
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentResultType
import com.mercadopago.android.px.internal.viewmodel.mappers.PayButtonViewModelMapper
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.Payment.StatusCodes.STATUS_APPROVED
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.CustomTexts
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.model.internal.remedies.RemediesResponse
import com.mercadopago.android.px.preferences.CheckoutPreference
import com.mercadopago.android.px.tracking.internal.model.ConfirmData
import com.mercadopago.android.px.tracking.internal.model.Reason
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.powermock.reflect.Whitebox
import org.robolectric.RobolectricTestRunner
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel as PayButtonTexts

@RunWith(RobolectricTestRunner::class)
internal class PayButtonViewModelTest: BasicRobolectricTest() {

    private lateinit var payButtonViewModel: PayButtonViewModel

    @Mock
    private lateinit var paymentService: PaymentService
    @Mock
    private lateinit var productIdProvider: ProductIdProvider
    @Mock
    private lateinit var connectionHelper: ConnectionHelper
    @Mock
    private lateinit var paymentSettingService: PaymentSettingService
    @Mock
    private lateinit var customTextsRepositoryImpl: CustomTextsRepositoryImpl
    @Mock
    private lateinit var payButtonViewModelMapper: PayButtonViewModelMapper
    @Mock
    private lateinit var customTexts: CustomTexts
    @Mock
    private lateinit var payButtonTexts: PayButtonTexts
    @Mock
    private lateinit var handler: PayButton.Handler
    @Mock
    private lateinit var buttonTextObserver: Observer<PayButtonTexts>
    @Mock
    private lateinit var uiStateObserver: Observer<PayButtonState>
    @Mock
    private lateinit var cvvRequiredObserver: Observer<Pair<Card, Reason>?>
    @Mock
    private lateinit var recoverRequiredObserver: Observer<PaymentRecovery?>
    private val paymentErrorLiveData = MutableSingleLiveData<MercadoPagoError>()
    private val paymentFinishedLiveData = MutableSingleLiveData<PaymentModel>()
    private val requireCvvLiveData = MutableSingleLiveData<Pair<Card, Reason>>()
    private val recoverInvalidEscLiveData = MutableSingleLiveData<PaymentRecovery>()
    private val visualPaymentLiveData = MutableSingleLiveData<Unit>()

    /*
    * https://stackoverflow.com/questions/29945087/kotlin-and-new-activitytestrule-the-rule-must-be-public
    * */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        initMocks(this)

        Session.initialize(getContext())

        `when`(customTextsRepositoryImpl.customTexts).thenReturn(customTexts)
        `when`(payButtonViewModelMapper.map(customTexts)).thenReturn(payButtonTexts)
        `when`(connectionHelper.checkConnection()).thenReturn(true)
        `when`(paymentSettingService.checkoutPreference).thenReturn(mock(CheckoutPreference::class.java))
        configurePaymentSettingServiceObservableEvents()

        payButtonViewModel = PayButtonViewModel(
                paymentService,
                productIdProvider,
                connectionHelper,
                paymentSettingService,
                customTextsRepositoryImpl,
                payButtonViewModelMapper)

        payButtonViewModel.stateUILiveData.observeForever(uiStateObserver)
        payButtonViewModel.buttonTextLiveData.observeForever(buttonTextObserver)
        payButtonViewModel.cvvRequiredLiveData.observeForever(cvvRequiredObserver)
        payButtonViewModel.recoverRequiredLiveData.observeForever(recoverRequiredObserver)
        payButtonViewModel.attach(handler)

        Whitebox.setInternalState(payButtonViewModel, "paymentConfiguration", mock(PaymentConfiguration::class.java))

        verify(buttonTextObserver).onChanged(any(PayButtonTexts::class.java))
        assertNotNull(handler)
    }

    @Test
    fun preparePaymentWhenNonConnection() {
        `when`(connectionHelper.checkConnection()).thenReturn(false)
        payButtonViewModel.preparePayment()
        verify(uiStateObserver).onChanged(any(UIError.ConnectionError::class.java))
    }

    @Test
    fun preparePaymentWhenHasConnection() {
        val callback = argumentCaptor<PayButton.OnReadyForPaymentCallback>()
        payButtonViewModel.preparePayment()
        verify(handler).prePayment(callback.capture())
        callback.value.call(mock(PaymentConfiguration::class.java), mock(ConfirmData::class.java))
        verify(uiStateObserver).onChanged(any(UIProgress.FingerprintRequired::class.java))
    }

    @Test
    fun handleBiometricsResultWithoutFrictionAndSuccess() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        Whitebox.setInternalState(payButtonViewModel, "confirmTrackerData", mock(ConfirmData::class.java))

        payButtonViewModel.handleBiometricsResult(isSuccess = true, securityRequested = true)

        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingStarted::class.java))
        verify(paymentSettingService).configure(SecurityType.SECOND_FACTOR)
        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(paymentService).startExpressPayment(any(PaymentConfiguration::class.java))
    }

    @Test
    fun handleBiometricsResultWithoutFrictionAndFailure() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()

        payButtonViewModel.handleBiometricsResult(isSuccess = true, securityRequested = true)

        verify(paymentSettingService).configure(SecurityType.SECOND_FACTOR)
        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.failure()
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingCanceled::class.java))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsErrorEventAndIsPaymentProcessingError() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val error = mock(MercadoPagoError::class.java)
        val payButtonViewModelSpy = spy(payButtonViewModel)
        `when`(error.isPaymentProcessing).thenReturn(true)
        `when`(paymentSettingService.currency).thenReturn(mock(Currency::class.java))
        `when`(paymentService.paymentDataList).thenReturn(mock(MutableList::class.java) as MutableList<PaymentData>)

        payButtonViewModelSpy.startPayment()
        paymentErrorLiveData.value = error

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(payButtonViewModelSpy).onPostPayment(any(PaymentModel::class.java))
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingFinished::class.java))
        verify(handler).onPaymentError(error)
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingCanceled::class.java))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsErrorEventAndIsNoRecoverableError() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val error = mock(MercadoPagoError::class.java)

        `when`(error.isPaymentProcessing).thenReturn(false)

        payButtonViewModel.startPayment()
        paymentErrorLiveData.value = error

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(uiStateObserver).onChanged(any(UIError.ConnectionError::class.java))
        verify(handler).onPaymentError(error)
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingCanceled::class.java))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsVisualPaymentEvent() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()

        payButtonViewModel.startPayment()
        visualPaymentLiveData.value = Unit

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(uiStateObserver).onChanged(any(UIResult.VisualProcessorResult::class.java))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsPaymentFinishedEventAndIsRemedies() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val paymentModel = mock(PaymentModel::class.java)
        `when`(paymentModel.remedies).thenReturn(mock(RemediesResponse::class.java))
        `when`(paymentModel.remedies.hasRemedies()).thenReturn(true)

        payButtonViewModel.startPayment()
        paymentFinishedLiveData.value = paymentModel

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingFinished::class.java))

        val actual = (payButtonViewModel.stateUILiveData.value as UIProgress.ButtonLoadingFinished)
        assertTrue(ReflectionEquals(actual.explodeDecorator).matches(ExplodeDecorator.from(RemediesModel.DECORATOR)))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsPaymentFinishedEventAndIsBusiness() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val paymentModel = mock(BusinessPaymentModel::class.java)
        val payment = mock(BusinessPayment::class.java)
        `when`(paymentModel.remedies).thenReturn(mock(RemediesResponse::class.java))
        `when`(paymentModel.remedies.hasRemedies()).thenReturn(false)
        `when`(paymentModel.payment).thenReturn(payment)
        `when`(paymentModel.payment.decorator).thenReturn(BusinessPayment.Decorator.APPROVED)

        payButtonViewModel.startPayment()
        paymentFinishedLiveData.value = paymentModel

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingFinished::class.java))

        val actual = (payButtonViewModel.stateUILiveData.value as UIProgress.ButtonLoadingFinished)
        assertTrue(ReflectionEquals(actual.explodeDecorator).matches(ExplodeDecorator.from(PaymentResultType.from(payment.decorator))))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsPaymentFinishedEventAndIsPaymentResult() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val paymentModel = mock(PaymentModel::class.java)
        val paymentResult = mock(PaymentResult::class.java)
        `when`(paymentModel.remedies).thenReturn(mock(RemediesResponse::class.java))
        `when`(paymentModel.remedies.hasRemedies()).thenReturn(false)
        `when`(paymentModel.paymentResult).thenReturn(paymentResult)
        `when`(paymentModel.paymentResult.paymentStatus).thenReturn(STATUS_APPROVED)
        `when`(paymentModel.paymentResult.paymentStatusDetail).thenReturn(null)

        payButtonViewModel.startPayment()
        paymentFinishedLiveData.value = paymentModel

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(uiStateObserver).onChanged(any(UIProgress.ButtonLoadingFinished::class.java))

        val actual = (payButtonViewModel.stateUILiveData.value as UIProgress.ButtonLoadingFinished)
        val expectedPaymentResult = PaymentResultViewModelFactory.createPaymentResultDecorator(paymentResult)
        val expected = ExplodeDecorator.from(expectedPaymentResult)
        assertTrue(ReflectionEquals(actual.explodeDecorator).matches(expected))
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsPaymentCvvRequiredEvent() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val pair = Pair(mock(Card::class.java), mock(Reason::class.java))

        payButtonViewModel.startPayment()
        requireCvvLiveData.value = pair

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(cvvRequiredObserver).onChanged(pair)
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsInvalidEscEventAndShouldAskForCvv() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val paymentRecoveryMock = mock(PaymentRecovery::class.java)
        `when`(paymentRecoveryMock.shouldAskForCvv()).thenReturn(true)

        payButtonViewModel.startPayment()
        recoverInvalidEscLiveData.value = paymentRecoveryMock

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(recoverRequiredObserver).onChanged(paymentRecoveryMock)
    }

    @Test
    fun startPaymentAndObserveServiceWhenIsInvalidEscEventAndNotShouldAskForCvv() {
        val callback = argumentCaptor<PayButton.OnEnqueueResolvedCallback>()
        val paymentRecoveryMock = mock(PaymentRecovery::class.java)
        `when`(paymentRecoveryMock.shouldAskForCvv()).thenReturn(false)

        payButtonViewModel.startPayment()
        recoverInvalidEscLiveData.value = paymentRecoveryMock

        verify(handler).enqueueOnExploding(callback.capture())
        callback.value.success()
        verify(recoverRequiredObserver).onChanged(null)
    }

    private fun configurePaymentSettingServiceObservableEvents() {
        `when`(paymentService.observableEvents).thenReturn(mock(PaymentServiceEventHandler::class.java))
        `when`(paymentService.isExplodingAnimationCompatible).thenReturn(true)
        `when`(paymentService.observableEvents.paymentErrorLiveData).thenReturn(paymentErrorLiveData)
        `when`(paymentService.observableEvents.paymentFinishedLiveData).thenReturn(paymentFinishedLiveData)
        `when`(paymentService.observableEvents.requireCvvLiveData).thenReturn(requireCvvLiveData)
        `when`(paymentService.observableEvents.recoverInvalidEscLiveData).thenReturn(recoverInvalidEscLiveData)
        `when`(paymentService.observableEvents.visualPaymentLiveData).thenReturn(visualPaymentLiveData)
    }
}