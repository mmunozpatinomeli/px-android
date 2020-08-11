package com.mercadopago.android.px.internal.features.business_result;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.tracking.print.MLBusinessTouchpointListener;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.addons.BehaviourProvider;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.PaymentResultBody;
import com.mercadopago.android.px.internal.view.PaymentResultHeader;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.ExitAction;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;
import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.getSafeIntent;
import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.isMP;

public class BusinessPaymentResultActivity extends PXActivity<BusinessPaymentResultPresenter>
    implements BusinessPaymentResultContract.View {

    private static final String TAG = BusinessPaymentResultActivity.class.getSimpleName();
    private static final String EXTRA_BUSINESS_PAYMENT_MODEL = "extra_business_payment_model";
    private static final String PAYMENT_CONGRATS = "payment_congrats";

    public static void startWithForwardResult(@NonNull final Activity activity, @NonNull final BusinessPaymentModel model) {
        final Intent intent = new Intent(activity, BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        intent.setFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        activity.startActivity(intent);
    }

    public static void start(@NonNull final Fragment fragment, final int requestCode,
        @NonNull final BusinessPaymentModel model) {
        final Activity activity = fragment.getActivity();
        if (activity instanceof PXActivity) {
            ((PXActivity) activity).overrideTransitionIn();
        }
        final Intent intent = new Intent(fragment.getContext(), BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static Intent getIntent(@NonNull final Context context,
        @NonNull final BusinessPaymentModel model) {
        final Intent intent = new Intent(context, BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        return intent;
    }

    @Override
    protected void onCreated(@Nullable final Bundle savedInstanceState) {
        setContentView(R.layout.px_activity_payment_result);

        presenter = createPresenter();
        presenter.attachView(this);
        if (savedInstanceState == null) {
            presenter.onFreshStart();
        }
        new MLBusinessTouchpointListener().setOnTouchListener(findViewById(R.id.scroll_view));
    }

    @NonNull
    private BusinessPaymentResultPresenter createPresenter() {
        final PaymentCongratsModel model = getIntent().getParcelableExtra(PAYMENT_CONGRATS);
        return new BusinessPaymentResultPresenter(model,
            BehaviourProvider.getFlowBehaviour(), isMP(this));
    }

    @Override
    public void configureViews(@NonNull final BusinessPaymentResultViewModel model,
        @NonNull final PaymentResultBody.Listener listener) {
        findViewById(R.id.loading).setVisibility(View.GONE);
        final PaymentResultHeader header = findViewById(R.id.header);
        header.setModel(model.headerModel);
        final PaymentResultBody body = findViewById(R.id.body);
        body.init(model.bodyModel, listener);
        //TODO migrate
        BusinessResultLegacyRenderer.render(findViewById(R.id.container), listener, model);
    }

    @Override
    public void onBackPressed() {
        presenter.onAbort();
    }

    @Override
    public void processCustomExit() {
        processCustomExit(new ExitAction("exit", RESULT_OK));
    }

    @Override
    public void processCustomExit(@NonNull final ExitAction action) {
        final Intent intent = action.toIntent();
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }

    @Override
    public void setStatusBarColor(@ColorRes final int color) {
        ViewUtils.setStatusBarColor(ContextCompat.getColor(this, color), getWindow());
    }

    @Override
    public void launchDeepLink(@NonNull final String deepLink) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)));
        } catch (ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void processCrossSellingBusinessAction(@NonNull final String deepLink) {
        try {
            startActivity(getSafeIntent(this, Uri.parse(deepLink)));
        } catch (ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }
}