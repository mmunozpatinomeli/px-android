package com.mercadopago.android.px.internal.features.business_result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.addons.FlowBehaviour;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsResponse;
import com.mercadopago.android.px.internal.features.payment_result.CongratsAutoReturn;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.PaymentResultBody;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.internal.PrimaryExitAction;
import com.mercadopago.android.px.model.internal.SecondaryExitAction;
import com.mercadopago.android.px.tracking.internal.events.AbortEvent;
import com.mercadopago.android.px.tracking.internal.events.CongratsSuccessDeepLink;
import com.mercadopago.android.px.tracking.internal.events.CrossSellingEvent;
import com.mercadopago.android.px.tracking.internal.events.DeepLinkType;
import com.mercadopago.android.px.tracking.internal.events.DiscountItemEvent;
import com.mercadopago.android.px.tracking.internal.events.DownloadAppEvent;
import com.mercadopago.android.px.tracking.internal.events.PrimaryActionEvent;
import com.mercadopago.android.px.tracking.internal.events.ScoreEvent;
import com.mercadopago.android.px.tracking.internal.events.SecondaryActionEvent;
import com.mercadopago.android.px.tracking.internal.events.SeeAllDiscountsEvent;
import com.mercadopago.android.px.tracking.internal.events.ViewReceiptEvent;
import com.mercadopago.android.px.tracking.internal.views.ResultViewTrack;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

/* default */ class BusinessPaymentResultPresenter extends BasePresenter<BusinessPaymentResultContract.View>
    implements ActionDispatcher, BusinessPaymentResultContract.Presenter, PaymentResultBody.Listener {

    private final PaymentCongratsModel model;
    //@NonNull private final PaymentSettingRepository paymentSettings;
    private final ResultViewTrack viewTracker;
    private final FlowBehaviour flowBehaviour;
    @Nullable private CongratsAutoReturn.Timer autoReturnTimer;

    /* default */ BusinessPaymentResultPresenter(/*@NonNull final PaymentSettingRepository paymentSettings,*/
        @NonNull final PaymentCongratsModel model,
        @NonNull final FlowBehaviour flowBehaviour, final boolean isMP) {
        this.model = model;
        this.flowBehaviour = flowBehaviour;
        viewTracker = new ResultViewTrack(model, isMP);
    }

    @Override
    public void attachView(final BusinessPaymentResultContract.View view) {
        super.attachView(view);
        mapPaymentModel();
    }

    @Override
    public void onFreshStart() {
        viewTracker.track();
        final FlowBehaviour.Result result = getResult();
        flowBehaviour.trackConversion(result);
    }

    @Override
    public void onStart() {
        if (autoReturnTimer != null) {
            autoReturnTimer.start();
        }
    }

    @Override
    public void onStop() {
        if (autoReturnTimer != null) {
            autoReturnTimer.cancel();
        }
        viewTracker.track();
        final FlowBehaviour.Result result = getResult();
        flowBehaviour.trackConversion(result);
    }

    @NotNull
    private FlowBehaviour.Result getResult() {
        final FlowBehaviour.Result result;
        switch (model.getCongratsType()) {
        case APPROVED:
            result = FlowBehaviour.Result.SUCCESS;
            break;
        case REJECTED:
            result = FlowBehaviour.Result.FAILURE;
            break;
        default:
            result = FlowBehaviour.Result.PENDING;
            break;
        }
        return result;
    }

    @Override
    public void onAbort() {
        new AbortEvent(viewTracker).track();
        getView().processCustomExit();
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof ExitAction) {
            if (action instanceof PrimaryExitAction) {
                new PrimaryActionEvent(viewTracker).track();
            } else if (action instanceof SecondaryExitAction) {
                new SecondaryActionEvent(viewTracker).track();
            }
            getView().processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException("this Action class can't be executed in this screen");
        }
    }

    private void mapPaymentModel() {
        final BusinessPaymentResultViewModel viewModel = new BusinessPaymentResultMapper(
            model.getAutoReturn()).map(model);
        getView().configureViews(viewModel, this);
        getView().setStatusBarColor(viewModel.headerModel.getBackgroundColor());
        initAutoReturn(viewModel.shouldAutoReturn);
    }

    private void initAutoReturn(final boolean shouldAutoReturn) {
        if (shouldAutoReturn) {
            autoReturnTimer = new CongratsAutoReturn.Timer(() -> {
                autoReturnTimer = null;
                onAbort();
                return Unit.INSTANCE;
            });
        }
    }

    @Override
    public void OnClickDownloadAppButton(@NonNull final String deepLink) {
        new DownloadAppEvent(viewTracker).track();
        getView().launchDeepLink(deepLink);
    }

    @Override
    public void OnClickCrossSellingButton(@NonNull final String deepLink) {
        new CrossSellingEvent(viewTracker).track();
        getView().processCrossSellingBusinessAction(deepLink);
    }

    @Override
    public void onClickLoyaltyButton(@NonNull final String deepLink) {
        new ScoreEvent(viewTracker).track();
        getView().launchDeepLink(deepLink);
    }

    @Override
    public void onClickShowAllDiscounts(@NonNull final String deepLink) {
        new SeeAllDiscountsEvent(viewTracker).track();
        getView().launchDeepLink(deepLink);
    }

    @Override
    public void onClickViewReceipt(@NonNull final String deepLink) {
        new ViewReceiptEvent(viewTracker).track();
        getView().launchDeepLink(deepLink);
    }

    @Override
    public void onClickTouchPoint(@Nullable final String deepLink) {
        new DiscountItemEvent(viewTracker, 0, TextUtil.EMPTY).track();
        if (deepLink != null) {
            getView().launchDeepLink(deepLink);
        }
    }

    @Override
    public void onClickDiscountItem(final int index, @Nullable final String deepLink, @Nullable final String trackId) {
        new DiscountItemEvent(viewTracker, index, trackId).track();
        if (deepLink != null) {
            getView().launchDeepLink(deepLink);
        }
    }

    @Override
    public void onClickMoneySplit() {
        final PaymentCongratsResponse.ExpenseSplit moneySplit = model.getPaymentCongratsResponse().getExpenseSplit();
        final String deepLink;
        if (moneySplit != null && (deepLink = moneySplit.getAction().getTarget()) != null) {
            new CongratsSuccessDeepLink(DeepLinkType.MONEY_SPLIT_TYPE, deepLink).track();
            getView().launchDeepLink(deepLink);
        }
    }
}