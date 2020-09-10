package com.mercadopago.android.px.internal.datasource;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.mercadopago.android.px.KArgumentCaptor;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.core.internal.PaymentWrapper;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.core.FileManager;
import com.mercadopago.android.px.internal.model.SecurityType;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.CongratsRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.EscPaymentManager;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PayerCostSelectionRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.mocks.InitResponseStub;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.FromExpressMetadataToPaymentConfiguration;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import kotlin.Pair;
import kotlin.Unit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mercadopago.android.px.ArgumentCaptorKt.argumentCaptor;
import static com.mercadopago.android.px.utils.ReflectionArgumentMatchers.reflectionEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    private static final String CARD_ID_ESC_APPROVED = "122232111";
    private static final String CARD_ID_ESC_REJECTED = "113210123";
    private static final String CARD_ID_ESC_NOT_AVAILABLE = "113210124";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private PluginRepository pluginRepository;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private DisabledPaymentMethodService disabledPaymentMethodService;
    @Mock private DiscountRepository discountRepository;
    @Mock private AmountRepository amountRepository;
    @Mock private SplitPaymentProcessor paymentProcessor;
    @Mock private Context context;
    @Mock private EscPaymentManager escPaymentManager;
    @Mock private ESCManagerBehaviour escManagerBehaviour;
    @Mock private TokenRepository tokenRepository;
    @Mock private InstructionsRepository instructionsRepository;
    @Mock private InitRepository initRepository;
    @Mock private AmountConfigurationRepository amountConfigurationRepository;
    @Mock private CongratsRepository congratsRepository;
    @Mock private SplitSelectionState splitSelectionState;
    @Mock private PayerCostSelectionRepository payerCostSelectionRepository;

    @Mock private ExpressMetadata node;
    @Mock private PayerCost payerCost;
    @Mock private PaymentMethod paymentMethod;
    @Mock private FileManager fileManager;

    private PaymentService paymentService;

    private static final DiscountConfigurationModel WITHOUT_DISCOUNT =
        new DiscountConfigurationModel(null, null, false);

    @Before
    public void setUp() {
        paymentService = new PaymentService(userSelectionRepository,
            paymentSettingRepository,
            disabledPaymentMethodService,
            pluginRepository,
            discountRepository,
            amountRepository,
            context,
            escPaymentManager,
            escManagerBehaviour,
            tokenRepository,
            instructionsRepository,
            initRepository,
            amountConfigurationRepository,
            congratsRepository,
            fileManager);

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(mock(CheckoutPreference.class));
        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(mock(com.mercadopago.android.px.configuration.PaymentConfiguration.class));
        when(paymentSettingRepository.getPaymentConfiguration().getPaymentProcessor()).thenReturn(paymentProcessor);
        when(discountRepository.getCurrentConfiguration()).thenReturn(WITHOUT_DISCOUNT);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
    }

    private PaymentConfiguration mockPaymentConfiguration(@NonNull final ExpressMetadata expressMetadata,
        @Nullable final PayerCost payerCost) {
        final AmountConfiguration amountConfiguration = mock(AmountConfiguration.class);
        when(amountConfigurationRepository.getConfigurationFor(anyString())).thenReturn(amountConfiguration);
        when(amountConfiguration.getCurrentPayerCost(anyBoolean(), anyInt())).thenReturn(payerCost);
        return new FromExpressMetadataToPaymentConfiguration(amountConfigurationRepository, splitSelectionState,
            payerCostSelectionRepository).map(expressMetadata);
    }

    @Test
    public void whenStorePayment() {
        paymentService.storePayment(mock(IPaymentDescriptor.class));

        verify(fileManager).writeToFile(any(), any(PaymentWrapper.class));
    }

    @Test
    public void whenRemoveStorePayment() {
        paymentService.reset();

        verify(fileManager).removeFile(any());
    }

    @Test
    public void whenOneTapPaymentIsCardSelectCard() {
        final Card card = creditCardPresetMock(CARD_ID_ESC_APPROVED);

        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(reflectionEquals(card), any());
    }

    @Test
    public void whenOneTapPaymentIsCardSelectPayerCost() {
        creditCardPresetMock(CARD_ID_ESC_APPROVED);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenOneTapPaymentIsCardPayerCostAndCardSet() {
        final Card card = creditCardPresetMock(CARD_ID_ESC_APPROVED);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(reflectionEquals(card), any());
        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenSavedCardAndESCSavedThenAskTokenButFailApiCallThenCVVIsRequiered() {
        final Observer<Pair<Card, Reason>> cvvRequiredObserver = mock(Observer.class);
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(tokenRepository.createToken(card)).thenReturn(new StubFailMpCall(mock(ApiException.class)));
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        paymentService.getObservableEvents().getRequireCvvLiveData().observeForever(cvvRequiredObserver);
        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);

        verify(tokenRepository).createToken(card);
        verifyNoMoreInteractions(tokenRepository);

        // if api call to tokenize fails, then ask for CVV.
        verify(cvvRequiredObserver).onChanged(any(Pair.class));
        verifyNoMoreInteractions(cvvRequiredObserver);
    }

    @Test
    public void whenOneTapPaymentWhenSavedCardAndESCSavedThenAskTokenSuccess() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        final MPCall<Token> tokenMPCall = mock(MPCall.class);
        when(tokenRepository.createToken(card)).thenReturn(tokenMPCall);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        verify(tokenRepository).createToken(card);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    public void whenOneTapPaymentWhenNotSavedCardAndESCSavedThenAskCVV() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(false);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    public void whenOneTapPaymentWhenCapExceededThenAskCVV() {
        final Observer<Pair<Card, Reason>> cvvRequiredObserver = mock(Observer.class);
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_REJECTED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        paymentService.getObservableEvents().getRequireCvvLiveData().observeForever(cvvRequiredObserver);

        verify(escPaymentManager).hasEsc(card);
        verify(cvvRequiredObserver).onChanged(any(Pair.class));
        verifyNoMoreInteractions(cvvRequiredObserver);
        verifyNoMoreInteractions(escPaymentManager);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    public void whenOneTapPaymentWhenCapNotExceededAndNotApproved() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_NOT_AVAILABLE);
        final MPCall<Token> tokenMPCall = mock(MPCall.class);
        when(tokenRepository.createToken(card)).thenReturn(tokenMPCall);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        verify(tokenRepository).createToken(card);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    public void whenOneTapStartPaymentAndPaymentError() {
        final Observer<MercadoPagoError> errorObserver = mock(Observer.class);
        when(userSelectionRepository.getPaymentMethod().getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(userSelectionRepository.getPayerCost()).thenReturn(null);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(mock(AmountConfiguration.class));

        paymentService.handlerWrapper.createTransactionLiveData();
        paymentService.getObservableEvents().getPaymentErrorLiveData().observeForever(errorObserver);
        paymentService.startPayment();

        verify(errorObserver).onChanged(any());
    }

    @Test
    public void whenOneTapStartPaymentAndShouldShowVisualPayment() {
        final Observer<Unit> visualPaymentObserver = mock(Observer.class);
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(userSelectionRepository.getPayerCost()).thenReturn(mock(PayerCost.class));
        when(paymentSettingRepository.hasToken()).thenReturn(true);
        when(paymentSettingRepository.getSecurityType()).thenReturn(SecurityType.SECOND_FACTOR);
        when(paymentProcessor.shouldShowFragmentOnPayment(any(CheckoutPreference.class))).thenReturn(true);

        paymentService.handlerWrapper.createTransactionLiveData();
        paymentService.getObservableEvents().getVisualPaymentLiveData().observeForever(visualPaymentObserver);
        paymentService.startPayment();

        verify(visualPaymentObserver).onChanged(any());
    }

    @Test
    public void whenOneTapPaymentWhenHasTokenAndPaymentSuccess() {
        final KArgumentCaptor<SplitPaymentProcessor.CheckoutData> captor =
            argumentCaptor(SplitPaymentProcessor.CheckoutData.class);

        savedCreditCardOneTapPresent(CARD_ID_ESC_NOT_AVAILABLE);
        when(paymentSettingRepository.hasToken()).thenReturn(true);
        when(paymentSettingRepository.getSecurityType()).thenReturn(SecurityType.SECOND_FACTOR);
        when(paymentProcessor.shouldShowFragmentOnPayment(any(CheckoutPreference.class))).thenReturn(false);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(mock(AmountConfiguration.class));

        final PaymentConfiguration configuration = mockPaymentConfiguration(node, payerCost);
        paymentService.startExpressPayment(configuration);
        verify(paymentProcessor).startPayment(any(), captor.capture(), any());

        final PaymentMethod actualPm = captor.getValue().paymentDataList.get(0).getPaymentMethod();
        final PayerCost actualPc = captor.getValue().paymentDataList.get(0).getPayerCost();

        assertEquals(actualPm.getId(), configuration.component1());
        assertEquals(actualPm.getPaymentTypeId(), configuration.component2());
        assertTrue(new ReflectionEquals(actualPc).matches(payerCost));
    }


    @NonNull
    private Card savedCreditCardOneTapPresent(String cardId) {
        final Card card = creditCardPresetMock(cardId);
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(userSelectionRepository.getPayerCost()).thenReturn(mock(PayerCost.class));
        when(userSelectionRepository.getCard()).thenReturn(card);
        when(paymentMethod.getId()).thenReturn(PaymentMethods.ARGENTINA.AMEX);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        return card;
    }

    private Card creditCardPresetMock(String cardId) {
        final InitResponse initResponse = InitResponseStub.FULL.get();
        when(initRepository.init()).thenReturn(new StubSuccessMpCall<>(initResponse));
        when(node.getPaymentMethodId()).thenReturn(PaymentMethods.ARGENTINA.AMEX);
        when(node.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(node.isCard()).thenReturn(true);
        when(node.getCustomOptionId()).thenReturn(cardId);
        return initResponse.getCardById(cardId);
    }
}