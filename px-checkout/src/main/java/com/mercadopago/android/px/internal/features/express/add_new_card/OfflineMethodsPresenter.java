package com.mercadopago.android.px.internal.features.express.add_new_card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.core.ProductIdProvider;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.CongratsRepository;
import com.mercadopago.android.px.internal.repository.CustomTextsRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.PayButtonViewModelMapper;
import com.mercadopago.android.px.model.OfflineMethodsCompliance;
import com.mercadopago.android.px.model.OfflinePaymentTypesMetadata;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.KnowYourCustomerFlowEvent;
import com.mercadopago.android.px.tracking.internal.model.ConfirmData;
import com.mercadopago.android.px.tracking.internal.views.OfflineMethodsViewTracker;

class OfflineMethodsPresenter extends BasePresenter<OfflineMethods.OffMethodsView> implements
    OfflineMethods.Actions {

    @NonNull private final PaymentRepository paymentRepository;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final ExplodeDecoratorMapper explodeDecoratorMapper;
    @NonNull private final PayButtonViewModel payButtonViewModel;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull /* default */ final InitRepository initRepository;
    private ProductIdProvider productIdProvider;
    @NonNull private final String defaultPaymentTypeId;
    @Nullable OfflineMethodsCompliance payerCompliance;
    @NonNull private final CongratsRepository congratsRepository;

    private OfflineMethodItem selectedItem;

    /* default */ OfflineMethodsPresenter(@NonNull final PaymentRepository paymentRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final ProductIdProvider productIdProvider,
        @NonNull final String defaultPaymentTypeId,
        @NonNull final InitRepository initRepository,
        @NonNull final CongratsRepository congratsRepository,
        @NonNull final CustomTextsRepository customTextsRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.amountRepository = amountRepository;
        this.discountRepository = discountRepository;
        this.productIdProvider = productIdProvider;
        this.defaultPaymentTypeId = defaultPaymentTypeId;
        this.initRepository = initRepository;
        this.congratsRepository = congratsRepository;

        payButtonViewModel = new PayButtonViewModelMapper().map(customTextsRepository.getCustomTexts());

        explodeDecoratorMapper = new ExplodeDecoratorMapper();
    }

    @Override
    public void attachView(final OfflineMethods.OffMethodsView view) {
        super.attachView(view);
        initPresenter();
    }

    @Override
    public void updateModel() {
        final String paymentTypeId =
            selectedItem != null ? selectedItem.getPaymentTypeId() : defaultPaymentTypeId;

        final AmountLocalized amountLocalized = new AmountLocalized(
            amountRepository.getAmountToPay(paymentTypeId, discountRepository.getCurrentConfiguration()),
            paymentSettingRepository.getCurrency());

        getView().updateTotalView(amountLocalized);
    }

    @Override
    public void selectMethod(@NonNull final OfflineMethodItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    private void initPresenter() {
        initRepository.init().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse initResponse) {
                payerCompliance =
                    initResponse.getPayerCompliance() != null ? initResponse.getPayerCompliance().getOfflineMethods()
                        : null;
            }

            @Override
            public void failure(final ApiException apiException) {
                onFailToRetrieveInitResponse();
            }
        });
    }

    private void onFailToRetrieveInitResponse() {
        throw new IllegalStateException("off methods missing compliance");
    }

    public void handlePrePaymentAction(@NonNull final PayButton.OnReadyForPaymentCallback callback) {
        if (payerCompliance != null) {
            if (selectedItem.isAdditionalInfoNeeded() && payerCompliance.isCompliant()) {
                completePayerInformation();
            } else if (selectedItem.isAdditionalInfoNeeded()) {
                new KnowYourCustomerFlowEvent(viewTracker).track();
                getView().startKnowYourCustomerFlow(payerCompliance.getTurnComplianceDeepLink());
                return;
            }
        }
        requireCurrentConfiguration(callback);
    }

    private void requireCurrentConfiguration(PayButton.OnReadyForPaymentCallback callback) {
        final String paymentMethodId = selectedItem.getPaymentMethodId() != null ? selectedItem.getPaymentMethodId() : TextUtil.EMPTY;
        final String paymentTypeId = selectedItem.getPaymentTypeId() != null ? selectedItem.getPaymentTypeId() : TextUtil.EMPTY;
        ConfirmData confirmData = ConfirmData.from(paymentTypeId, paymentMethodId,
            payerCompliance == null || payerCompliance.isCompliant(), selectedItem.isAdditionalInfoNeeded());
        PaymentConfiguration paymentConfiguration = new PaymentConfiguration(paymentMethodId, paymentTypeId,
            paymentMethodId, false, false, null);
        callback.call(paymentConfiguration, confirmData);
    }

    private void completePayerInformation() {
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final Payer payer = checkoutPreference.getPayer();

        payer.setFirstName(payerCompliance.getSensitiveInformation().getFirstName());
        payer.setLastName(payerCompliance.getSensitiveInformation().getLastName());
        payer.setIdentification(payerCompliance.getSensitiveInformation().getIdentification());

        paymentSettingRepository.configure(checkoutPreference);
    }

    @Override
    public void onBack() {
        tracker.trackAbort();
    }

    public void trackOfflineMethodsView(final OfflinePaymentTypesMetadata model) {
        final OfflineMethodsViewTracker offlineMethodsViewTracker =
            new OfflineMethodsViewTracker(model);
        setCurrentViewTracker(offlineMethodsViewTracker);
    }
}