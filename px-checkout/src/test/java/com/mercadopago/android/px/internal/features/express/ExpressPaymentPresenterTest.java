package com.mercadopago.android.px.internal.features.express;

import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.CongratsRepository;
import com.mercadopago.android.px.internal.repository.CustomTextsRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.ExperimentsRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.PayerComplianceRepository;
import com.mercadopago.android.px.internal.repository.PayerCostSelectionRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.tracking.TrackingRepository;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.drawables.PaymentMethodDrawableItemMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodDescriptorMapper;
import com.mercadopago.android.px.mocks.CurrencyStub;
import com.mercadopago.android.px.mocks.SiteStub;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.StatusMetadata;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PrepareForTest(MPTracker.class)
@RunWith(PowerMockRunner.class)
public class ExpressPaymentPresenterTest {

    @Mock
    private ExpressPayment.View view;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentSettingRepository paymentSettingRepository;

    @Mock
    private DisabledPaymentMethodRepository disabledPaymentMethodRepository;

    @Mock
    private PayerCostSelectionRepository payerCostSelectionRepository;

    @Mock
    private InitRepository initRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private AmountConfigurationRepository amountConfigurationRepository;

    @Mock
    private AmountRepository amountRepository;

    @Mock
    private InitResponse initResponse;

    @Mock
    private ExpressMetadata expressMetadata;

    @Mock
    private AmountConfiguration amountConfiguration;

    @Mock
    private DiscountConfigurationModel discountConfigurationModel;

    @Mock
    private AdvancedConfiguration advancedConfiguration;

    @Mock
    private DynamicDialogConfiguration dynamicDialogConfiguration;

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ESCManagerBehaviour escManagerBehaviour;

    @Mock
    private PaymentMethodDrawableItemMapper paymentMethodDrawableItemMapper;

    @Mock
    private CongratsRepository congratsRepository;

    @Mock
    private ExperimentsRepository experimentsRepository;

    @Mock
    private PayerComplianceRepository payerComplianceRepository;

    @Mock
    private TrackingRepository trackingRepository;

    private ExpressPaymentPresenter expressPaymentPresenter;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(MPTracker.class);
        when(MPTracker.getInstance()).thenReturn(mock(MPTracker.class));
        //This is needed for the presenter constructor
        final CheckoutPreference preference = mock(CheckoutPreference.class);
        final Item item = mock(Item.class);
        when(item.getTitle()).thenReturn("Item title");
        when(preference.getItems()).thenReturn(Collections.singletonList(item));
        when(paymentSettingRepository.getSite()).thenReturn(SiteStub.MLA.get());
        when(paymentSettingRepository.getCurrency()).thenReturn(CurrencyStub.MLA.get());
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(paymentSettingRepository.getAdvancedConfiguration()).thenReturn(advancedConfiguration);
        when(advancedConfiguration.getDynamicDialogConfiguration()).thenReturn(dynamicDialogConfiguration);
        when(initRepository.init()).thenReturn(new StubSuccessMpCall<>(initResponse));
        when(initResponse.getExpress()).thenReturn(Collections.singletonList(expressMetadata));
        when(expressMetadata.isCard()).thenReturn(true);
        when(expressMetadata.getCustomOptionId()).thenReturn("123");
        when(expressMetadata.getStatus()).thenReturn(mock(StatusMetadata.class));
        when(discountRepository.getConfigurationFor("123")).thenReturn(discountConfigurationModel);
        when(amountConfigurationRepository.getConfigurationFor("123")).thenReturn(amountConfiguration);

        expressPaymentPresenter =
            new ExpressPaymentPresenter(paymentRepository, paymentSettingRepository, disabledPaymentMethodRepository,
                payerCostSelectionRepository, discountRepository, amountRepository, initRepository,
                amountConfigurationRepository, chargeRepository, escManagerBehaviour, paymentMethodDrawableItemMapper,
                congratsRepository, experimentsRepository, payerComplianceRepository, trackingRepository,
                mock(PaymentMethodDescriptorMapper.class), mock(CustomTextsRepository.class));

        verifyAttachView();
    }

    @Test
    public void whenCanceledThenCancelAndTrack() {
        expressPaymentPresenter.cancel();

        verify(view).cancel();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenViewIsResumedThenPaymentRepositoryIsAttached() {
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenElementDescriptorViewClickedAndHasCreatorThenShowDynamicDialog() {
        final DynamicDialogCreator dynamicDialogCreatorMock = mock(DynamicDialogCreator.class);
        when(dynamicDialogConfiguration.hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER))
            .thenReturn(true);
        when(dynamicDialogConfiguration.getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER))
            .thenReturn(dynamicDialogCreatorMock);

        expressPaymentPresenter.onHeaderClicked();
        verify(dynamicDialogConfiguration)
            .hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);
        verify(dynamicDialogConfiguration)
            .getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);
        verify(view).showDynamicDialog(eq(dynamicDialogCreatorMock),
            any(DynamicDialogCreator.CheckoutData.class));

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenElementDescriptorViewClickedAndHasNotCreatorThenDoNotShowDynamicDialog() {
        expressPaymentPresenter.onHeaderClicked();
        verify(dynamicDialogConfiguration)
            .hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenSliderOptionSelectedThenShowInstallmentsRow() {
        when(payerCostSelectionRepository.get(anyString())).thenReturn(PayerCost.NO_SELECTED);
        final int currentElementPosition = 0;
        expressPaymentPresenter.onSliderOptionSelected(currentElementPosition);

        verify(view).updateViewForPosition(eq(currentElementPosition), eq(PayerCost.NO_SELECTED), any());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPayerCostSelectedThenItsReflectedOnView() {
        final int paymentMethodIndex = 0;
        final int selectedPayerCostIndex = mockPayerCosts();

        verify(view).updateViewForPosition(eq(paymentMethodIndex), eq(selectedPayerCostIndex), any());
        verify(view).collapseInstallmentsSelection();
        verifyNoMoreInteractions(view);
    }

    private int mockPayerCosts() {
        final int selectedPayerCostIndex = 1;
        when(payerCostSelectionRepository.get(anyString())).thenReturn(selectedPayerCostIndex);
        final PayerCost firstPayerCost = mock(PayerCost.class);
        final List<PayerCost> payerCostList =
            Arrays.asList(mock(PayerCost.class), firstPayerCost, mock(PayerCost.class));
        when(amountConfiguration.getAppliedPayerCost(false)).thenReturn(payerCostList);
        expressPaymentPresenter.onPayerCostSelected(payerCostList.get(selectedPayerCostIndex));
        return selectedPayerCostIndex;
    }

    private void verifyAttachView() {
        expressPaymentPresenter.attachView(view);

        verify(view).configurePaymentMethodHeader(anyList());
        verify(view).showToolbarElementDescriptor(any(ElementDescriptorView.Model.class));
        verify(view).updateAdapters(any(HubAdapter.Model.class));
        verify(view).updateViewForPosition(anyInt(), anyInt(), any(SplitSelectionState.class));
        verify(view).configureAdapters(any(Site.class), any(Currency.class));
        verify(view).updatePaymentMethods(anyListOf(DrawableFragmentItem.class));
        verify(view).updateBottomSheetStatus(false);
    }
}