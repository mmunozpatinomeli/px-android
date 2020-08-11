package com.mercadopago.android.px.internal.features.express;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.core.internal.TriggerableQueue;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.experiments.BadgeVariant;
import com.mercadopago.android.px.internal.experiments.PulseVariant;
import com.mercadopago.android.px.internal.experiments.Variant;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentRowHolder;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.features.generic_modal.ActionType;
import com.mercadopago.android.px.internal.features.generic_modal.ActionTypeWrapper;
import com.mercadopago.android.px.internal.features.generic_modal.FromModalToGenericDialogItem;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
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
import com.mercadopago.android.px.internal.util.CardFormWithFragmentWrapper;
import com.mercadopago.android.px.internal.util.PayerComplianceWrapper;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.experiments.ExperimentHelper;
import com.mercadopago.android.px.internal.viewmodel.ConfirmButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.PaymentMethodDrawableItemMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.ConfirmButtonViewModelMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.ElementDescriptorMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.InstallmentViewModelMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodDescriptorMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SplitHeaderMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SummaryInfoMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SummaryViewModelMapper;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.OfflinePaymentTypesMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.FromExpressMetadataToPaymentConfiguration;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.model.internal.Modal;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import com.mercadopago.android.px.model.internal.SummaryInfo;
import com.mercadopago.android.px.model.one_tap.CheckoutBehaviour;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.ConfirmEvent;
import com.mercadopago.android.px.tracking.internal.events.InstallmentsEventTrack;
import com.mercadopago.android.px.tracking.internal.events.SuspendedFrictionTracker;
import com.mercadopago.android.px.tracking.internal.events.SwipeOneTapEventTracker;
import com.mercadopago.android.px.tracking.internal.events.TargetBehaviourEvent;
import com.mercadopago.android.px.tracking.internal.mapper.FromSelectedExpressMetadataToAvailableMethods;
import com.mercadopago.android.px.tracking.internal.model.ConfirmData;
import com.mercadopago.android.px.tracking.internal.model.TargetBehaviourTrackData;
import com.mercadopago.android.px.tracking.internal.views.OneTapViewTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* default */ class ExpressPaymentPresenter extends BasePresenter<ExpressPayment.View>
    implements ExpressPayment.Actions, AmountDescriptorView.OnClickListener {

    private static final String BUNDLE_STATE_SPLIT_PREF = "state_split_pref";
    private static final String BUNDLE_STATE_CURRENT_PM_INDEX = "state_current_pm_index";
    private static final String BUNDLE_STATE_OTHER_PM_CLICKABLE = "state_other_pm_clickable";

    @NonNull private final PaymentRepository paymentRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull private final DisabledPaymentMethodRepository disabledPaymentMethodRepository;
    @NonNull private final ChargeRepository chargeRepository;
    @NonNull private final ESCManagerBehaviour escManagerBehaviour;
    @NonNull private final CongratsRepository congratsRepository;
    @NonNull private final ExperimentsRepository experimentsRepository;
    @NonNull private final PayerComplianceRepository payerComplianceRepository;
    @NonNull private final TrackingRepository trackingRepository;
    @NonNull private final PaymentMethodDescriptorMapper paymentMethodDescriptorMapper;
    @NonNull private final CustomTextsRepository customTextsRepository;
    @Nullable private Runnable unattendedEvent;
    @NonNull /* default */ final InitRepository initRepository;
    private final PayerCostSelectionRepository payerCostSelectionRepository;
    private final PaymentMethodDrawableItemMapper paymentMethodDrawableItemMapper;
    private SplitSelectionState splitSelectionState;
    private Set<String> cardsWithSplit;
    private boolean otherPaymentMethodClickable = true;
    /* default */ List<ExpressMetadata> expressMetadataList; //FIXME remove.
    /* default */ Map<String, Modal> modals; //FIXME remove.
    /* default */ PayerComplianceWrapper payerCompliance; //FIXME remove.
    /* default */ int paymentMethodIndex;
    /* default */ ActionTypeWrapper actionTypeWrapper;
    /* default */ TriggerableQueue triggerableQueue;

    /* default */ ExpressPaymentPresenter(@NonNull final PaymentRepository paymentRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final DisabledPaymentMethodRepository disabledPaymentMethodRepository,
        @NonNull final PayerCostSelectionRepository payerCostSelectionRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final InitRepository initRepository,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository,
        @NonNull final ChargeRepository chargeRepository,
        @NonNull final ESCManagerBehaviour escManagerBehaviour,
        @NonNull final PaymentMethodDrawableItemMapper paymentMethodDrawableItemMapper,
        @NonNull final CongratsRepository congratsRepository,
        @NonNull final ExperimentsRepository experimentsRepository,
        @NonNull final PayerComplianceRepository payerComplianceRepository,
        @NonNull final TrackingRepository trackingRepository,
        @NonNull final PaymentMethodDescriptorMapper paymentMethodDescriptorMapper,
        @NonNull final CustomTextsRepository customTextsRepository) {

        this.paymentRepository = paymentRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.disabledPaymentMethodRepository = disabledPaymentMethodRepository;
        this.payerCostSelectionRepository = payerCostSelectionRepository;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.initRepository = initRepository;
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.chargeRepository = chargeRepository;
        this.escManagerBehaviour = escManagerBehaviour;
        this.paymentMethodDrawableItemMapper = paymentMethodDrawableItemMapper;
        this.congratsRepository = congratsRepository;
        this.experimentsRepository = experimentsRepository;
        this.payerComplianceRepository = payerComplianceRepository;
        this.trackingRepository = trackingRepository;
        this.paymentMethodDescriptorMapper = paymentMethodDescriptorMapper;
        this.customTextsRepository = customTextsRepository;

        splitSelectionState = new SplitSelectionState();
        triggerableQueue = new TriggerableQueue();
    }

    /* default */ void onFailToRetrieveInitResponse() {
        throw new IllegalStateException("groups missing rendering one tap");
    }

    @Override
    public void loadViewModel() {
        final SummaryInfo summaryInfo = new SummaryInfoMapper().map(paymentSettingRepository.getCheckoutPreference());

        final ElementDescriptorView.Model elementDescriptorModel =
            new ElementDescriptorMapper().map(summaryInfo);

        final List<SummaryView.Model> summaryModels =
            new SummaryViewModelMapper(paymentSettingRepository.getCurrency(), discountRepository, amountRepository,
                elementDescriptorModel, this, summaryInfo, chargeRepository, amountConfigurationRepository,
                customTextsRepository).map(new ArrayList<>(expressMetadataList));

        final List<PaymentMethodDescriptorView.Model> paymentModels =
            paymentMethodDescriptorMapper.map(expressMetadataList);

        final List<SplitPaymentHeaderAdapter.Model> splitHeaderModels =
            new SplitHeaderMapper(paymentSettingRepository.getCurrency(), amountConfigurationRepository)
                .map(expressMetadataList);

        final List<ConfirmButtonViewModel> confirmButtonViewModels =
            new ConfirmButtonViewModelMapper(disabledPaymentMethodRepository).map(expressMetadataList);

        final HubAdapter.Model model =
            new HubAdapter.Model(paymentModels, summaryModels, splitHeaderModels, confirmButtonViewModels);

        getView().configurePaymentMethodHeader(getExperiment());
        getView().showToolbarElementDescriptor(elementDescriptorModel);
        getView().configureAdapters(paymentSettingRepository.getSite(), paymentSettingRepository.getCurrency());
        getView().updateAdapters(model);
        updateElements();
        getView().updatePaymentMethods(paymentMethodDrawableItemMapper.map(expressMetadataList));
        getView().updateBottomSheetStatus(!otherPaymentMethodClickable);
    }

    @Override
    public void attachView(final ExpressPayment.View view) {
        super.attachView(view);
        initPresenter();
    }

    private void initPresenter() {
        initRepository.init().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse initResponse) {
                if (isViewAttached()) {
                    expressMetadataList = initResponse.getExpress();
                    actionTypeWrapper = new ActionTypeWrapper(expressMetadataList);
                    modals = initResponse.getModals();
                    payerCompliance = new PayerComplianceWrapper(initResponse.getPayerCompliance());
                    paymentMethodDrawableItemMapper.setCustomSearchItems(initResponse.getCustomSearchItems());
                    cardsWithSplit = initResponse.getIdsWithSplitAllowed();
                    triggerableQueue.execute();
                    loadViewModel();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                onFailToRetrieveInitResponse();
            }
        });
    }

    @Override
    public void recoverFromBundle(@NonNull final Bundle bundle) {
        splitSelectionState = bundle.getParcelable(BUNDLE_STATE_SPLIT_PREF);
        paymentMethodIndex = bundle.getInt(BUNDLE_STATE_CURRENT_PM_INDEX);
        otherPaymentMethodClickable = bundle.getBoolean(BUNDLE_STATE_OTHER_PM_CLICKABLE);
    }

    @NonNull
    @Override
    public Bundle storeInBundle(@NonNull final Bundle bundle) {
        bundle.putParcelable(BUNDLE_STATE_SPLIT_PREF, splitSelectionState);
        bundle.putInt(BUNDLE_STATE_CURRENT_PM_INDEX, paymentMethodIndex);
        bundle.putBoolean(BUNDLE_STATE_OTHER_PM_CLICKABLE, otherPaymentMethodClickable);
        return bundle;
    }

    @Override
    public void onFreshStart() {
        triggerableQueue.enqueue(() -> {
            trackOneTapView();
            return null;
        });
    }

    private void trackOneTapView() {
        final OneTapViewTracker oneTapViewTracker =
            new OneTapViewTracker(expressMetadataList, paymentSettingRepository.getCheckoutPreference(),
                discountRepository.getCurrentConfiguration(), escManagerBehaviour.getESCCardIds(), cardsWithSplit,
                disabledPaymentMethodRepository.getDisabledPaymentMethods().size());
        setCurrentViewTracker(oneTapViewTracker);
    }

    private ExpressMetadata getCurrentExpressMetadata() {
        return expressMetadataList.get(paymentMethodIndex);
    }

    @Override
    public void cancel() {
        tracker.trackBack();
        getView().cancel();
    }

    @Override
    public void onBack() {
        tracker.trackAbort();
    }

    private void updateElementPosition(final int selectedPayerCost) {
        payerCostSelectionRepository.save(getCurrentExpressMetadata().getCustomOptionId(), selectedPayerCost);
        updateElements();
    }

    @Override
    public void onInstallmentsRowPressed() {
        final ExpressMetadata expressMetadata = getCurrentExpressMetadata();
        final String customOptionId = expressMetadata.getCustomOptionId();
        final AmountConfiguration amountConfiguration =
            amountConfigurationRepository.getConfigurationFor(customOptionId);
        final List<PayerCost> payerCostList =
            amountConfiguration.getAppliedPayerCost(splitSelectionState.userWantsToSplit());
        final int selectedIndex = amountConfiguration.getCurrentPayerCostIndex(splitSelectionState.userWantsToSplit(),
            payerCostSelectionRepository.get(customOptionId));
        final List<InstallmentRowHolder.Model> models =
            new InstallmentViewModelMapper(paymentSettingRepository.getCurrency(), expressMetadata.getBenefits())
                .map(payerCostList);
        getView().showInstallmentsList(selectedIndex, models);
        new InstallmentsEventTrack(expressMetadata, amountConfiguration).track();
    }

    /**
     * When user cancel the payer cost selection this method will be called with the current payment method position
     */
    @Override
    public void onInstallmentSelectionCanceled() {
        updateElements();
        getView().collapseInstallmentsSelection();
    }

    /**
     * When user selects a new payment method this method will be called with the new current paymentMethodIndex.
     *
     * @param paymentMethodIndex current payment method paymentMethodIndex.
     */
    @Override
    public void onSliderOptionSelected(final int paymentMethodIndex) {
        this.paymentMethodIndex = paymentMethodIndex;
        new SwipeOneTapEventTracker().track();
        updateElementPosition(payerCostSelectionRepository.get(getCurrentExpressMetadata().getCustomOptionId()));
    }

    private void updateElements() {
        getView().updateViewForPosition(paymentMethodIndex,
            payerCostSelectionRepository.get(getCurrentExpressMetadata().getCustomOptionId()), splitSelectionState);
    }

    /**
     * When user selects a new payer cost for certain payment method this method will be called.
     *
     * @param payerCostSelected user selected payerCost.
     */
    @Override
    public void onPayerCostSelected(final PayerCost payerCostSelected) {
        final String customOptionId = getCurrentExpressMetadata().getCustomOptionId();
        final int selected = amountConfigurationRepository.getConfigurationFor(customOptionId)
            .getAppliedPayerCost(splitSelectionState.userWantsToSplit())
            .indexOf(payerCostSelected);
        updateElementPosition(selected);
        getView().collapseInstallmentsSelection();
    }

    public void onDisabledDescriptorViewClick() {
        getView().showDisabledPaymentMethodDetailDialog(
            disabledPaymentMethodRepository.getDisabledPaymentMethod(getCurrentExpressMetadata().getCustomOptionId()),
            getCurrentExpressMetadata().getStatus());
    }

    @Override
    public void onDiscountAmountDescriptorClicked(@NonNull final DiscountConfigurationModel discountModel) {
        getView().showDiscountDetailDialog(paymentSettingRepository.getCurrency(), discountModel);
    }

    @Override
    public void onChargesAmountDescriptorClicked(@NonNull final DynamicDialogCreator dynamicDialogCreator) {
        final DynamicDialogCreator.CheckoutData checkoutData = new DynamicDialogCreator.CheckoutData(
            paymentSettingRepository.getCheckoutPreference(), Collections.singletonList(new PaymentData()));
        getView().showDynamicDialog(dynamicDialogCreator, checkoutData);
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        if (splitSelectionState.userWantsToSplit() != isChecked) {
            resetPayerCostSelection();
        }
        splitSelectionState.setUserWantsToSplit(isChecked);
        // cancel also update the position.
        // it is used because the installment selection can be expanded by the user.
        onInstallmentSelectionCanceled();
    }

    @Override
    public void onHeaderClicked() {
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final DynamicDialogConfiguration dynamicDialogConfiguration =
            paymentSettingRepository.getAdvancedConfiguration().getDynamicDialogConfiguration();

        final DynamicDialogCreator.CheckoutData checkoutData =
            new DynamicDialogCreator.CheckoutData(checkoutPreference, Collections.singletonList(new PaymentData()));

        if (dynamicDialogConfiguration.hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER)) {
            getView().showDynamicDialog(
                dynamicDialogConfiguration.getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER),
                checkoutData);
        }
    }

    /* default */ void resetPayerCostSelection() {
        payerCostSelectionRepository.reset();
    }

    @Override
    public void onPostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        postPaymentAction.execute(new PostPaymentAction.ActionController() {
            @Override
            public void recoverPayment(@NonNull final PostPaymentAction postPaymentAction) {
                //nothing to do here
            }

            @Override
            public void onChangePaymentMethod() {
                postDisableModelUpdate();
            }
        });
    }

    /* default */ void postDisableModelUpdate() {
        initRepository.refresh().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse initResponse) {
                if (isViewAttached()) {
                    resetState(initResponse);
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                onFailToRetrieveInitResponse();
            }
        });
    }

    @Override
    public void onOtherPaymentMethodClicked(@NonNull final OfflinePaymentTypesMetadata offlineMethods) {
        final Runnable event = () -> getView().showOfflineMethods(offlineMethods);
        if (otherPaymentMethodClickable) {
            event.run();
        } else {
            unattendedEvent = event;
        }
    }

    @Override
    public void onOtherPaymentMethodClickableStateChanged(final boolean state) {
        otherPaymentMethodClickable = state;
        if (otherPaymentMethodClickable) {
            executeUnattendedEvent();
        }
    }

    private void executeUnattendedEvent() {
        if (unattendedEvent != null) {
            unattendedEvent.run();
            unattendedEvent = null;
        }
    }

    @Override
    @SuppressLint("WrongConstant")
    public void handlePrePaymentAction(@NonNull final PayButton.OnReadyForPaymentCallback callback) {
        if (!handleBehaviour(CheckoutBehaviour.Type.TAP_PAY)) {
            requireCurrentConfiguration(callback);
        }
    }

    private boolean handleBehaviour(@CheckoutBehaviour.Type @NonNull final String behaviourType) {
        final ExpressMetadata expressMetadata = getCurrentExpressMetadata();

        final CheckoutBehaviour behaviour = expressMetadata.getBehaviour(behaviourType);
        final Modal modal = behaviour != null && behaviour.getModal() != null ? modals.get(behaviour.getModal()) : null;
        final String target = behaviour != null ? behaviour.getTarget() : null;
        final boolean isMethodSuspended = expressMetadata.getStatus().isSuspended();

        if (isMethodSuspended && modal != null) {
            getView().showGenericDialog(
                new FromModalToGenericDialogItem(actionTypeWrapper.getActionType(), behaviour.getModal()).map(modal));
            return true;
        } else if (isMethodSuspended && TextUtil.isNotEmpty(target)) {
            new TargetBehaviourEvent(viewTracker, new TargetBehaviourTrackData(behaviourType, target)).track();
            getView().startDeepLink(target);
            return true;
        } else if (isMethodSuspended) {
            // is a friction if the method is suspended and does not have any behaviour to handle
            SuspendedFrictionTracker.INSTANCE.track();
            return true;
        } else {
            return false;
        }
    }

    private void requireCurrentConfiguration(@NonNull PayButton.OnReadyForPaymentCallback callback) {
        final ExpressMetadata expressMetadata = getCurrentExpressMetadata();

        final PaymentConfiguration configuration = new FromExpressMetadataToPaymentConfiguration(
            amountConfigurationRepository, splitSelectionState, payerCostSelectionRepository).map(expressMetadata);

        final ConfirmData confirmTrackerData = new ConfirmData(ConfirmEvent.ReviewType.ONE_TAP, paymentMethodIndex,
            new FromSelectedExpressMetadataToAvailableMethods(escManagerBehaviour.getESCCardIds(),
                configuration.getPayerCost(), configuration.getSplitPayment())
                .map(expressMetadata));

        callback.call(configuration, confirmTrackerData);
    }

    @Override
    public void handleGenericDialogAction(@NonNull @ActionType final String type) {
        switch (type) {
        case ActionType.PAY_WITH_OTHER_METHOD:
        case ActionType.PAY_WITH_OFFLINE_METHOD:
            getView().setPagerIndex(actionTypeWrapper.getIndexToReturn());
            break;
        case ActionType.ADD_NEW_CARD:
            getView().setPagerIndex(actionTypeWrapper.getIndexToReturn());
            getView().startAddNewCardFlow(
                new CardFormWithFragmentWrapper(paymentSettingRepository, trackingRepository));
            break;
        default: // do nothing
        }
    }

    /* default */ void resetState(@NonNull final InitResponse initResponse) {
        expressMetadataList = initResponse.getExpress();
        actionTypeWrapper = new ActionTypeWrapper(expressMetadataList);
        modals = initResponse.getModals();
        payerCompliance = new PayerComplianceWrapper(initResponse.getPayerCompliance());
        resetPayerCostSelection();
        paymentMethodIndex = 0;
        getView().clearAdapters();
        loadViewModel();
    }

    @Override
    public void handleDeepLink() {
        disabledPaymentMethodRepository.reset();
        if (isViewAttached()) {
            getView().showLoading();
        }
        initRepository.cleanRefresh().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse initResponse) {
                if (isViewAttached()) {
                    if (payerCompliance.turnedIFPECompliant(initResponse.getPayerCompliance())) {
                        payerComplianceRepository.turnIFPECompliant();
                    }
                    resetState(initResponse);
                    getView().hideLoading();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                if (isViewAttached()) {
                    getView().hideLoading();
                }
            }
        });
    }

    @Override
    public void onCardFormResult() {
        postDisableModelUpdate();
    }

    private List<Variant> getExperiment() {
        return ExperimentHelper.INSTANCE
            .getVariantsFrom(experimentsRepository.getExperiments(), new PulseVariant(), new BadgeVariant());
    }
}