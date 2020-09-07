package com.mercadopago.android.px.internal.features.payment_result;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ScrollView;
import com.mercadolibre.android.andesui.snackbar.AndesSnackbar;
import com.mercadolibre.android.andesui.snackbar.duration.AndesSnackbarDuration;
import com.mercadolibre.android.andesui.snackbar.type.AndesSnackbarType;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.tracking.print.MLBusinessTouchpointListener;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.addons.BehaviourProvider;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.extensions.BaseExtensionsKt;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
import com.mercadopago.android.px.internal.features.pay_button.PayButtonFragment;
import com.mercadopago.android.px.internal.features.payment_result.components.PaymentResultLegacyRenderer;
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesFragment;
import com.mercadopago.android.px.internal.features.payment_result.remedies.view.PaymentResultFooter;
import com.mercadopago.android.px.internal.features.payment_result.viewmodel.PaymentResultViewModel;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.PaymentResultBody;
import com.mercadopago.android.px.internal.view.PaymentResultHeader;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.ChangePaymentMethodPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.PaymentModel;
import com.mercadopago.android.px.internal.viewmodel.RecoverPaymentPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.handlers.PaymentModelHandler;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import kotlin.Unit;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_ACTION;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;
import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.getSafeIntent;
import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.isMP;

public class PaymentResultActivity extends PXActivity<PaymentResultPresenter> implements
    PaymentResultContract.View, PayButton.Handler, RemediesFragment.Listener {

    private static final String TAG = PaymentResultActivity.class.getSimpleName();
    public static final String EXTRA_PAYMENT_MODEL = "extra_payment_model";
    public static final String EXTRA_RESULT_CODE = "extra_result_code";
    private PayButtonFragment payButtonFragment;
    private RemediesFragment remediesFragment;
    private PaymentResultFooter footer;

    public static void startWithForwardResult(@NonNull final Activity activity, @NonNull final PaymentModel model) {
        final Intent intent = new Intent(activity, PaymentResultActivity.class);
        intent.putExtra(EXTRA_PAYMENT_MODEL, model);
        intent.setFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        activity.startActivity(intent);
    }

    public static void start(@NonNull final Fragment fragment, final int requestCode, @NonNull final PaymentModel model) {
        final Activity activity = fragment.getActivity();
        if (activity instanceof PXActivity) {
            ((PXActivity) activity).overrideTransitionIn();
        }
        final Intent intent = new Intent(fragment.getContext(), PaymentResultActivity.class);
        intent.putExtra(EXTRA_PAYMENT_MODEL, model);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreated(@Nullable final Bundle savedInstanceState) {
        setContentView(R.layout.px_activity_payment_result);

        footer = findViewById(R.id.remedies_footer);
        final ScrollView scrollView = findViewById(R.id.scroll_view);
        BaseExtensionsKt.addKeyBoardListener(this, () -> {
            footer.hideQuietButton();
            scrollView.fullScroll(View.FOCUS_DOWN);
            return Unit.INSTANCE;
        }, () -> {
            footer.showQuietButton();
            return Unit.INSTANCE;
        });

        presenter = createPresenter();
        presenter.attachView(this);
        if (savedInstanceState == null) {
            presenter.onFreshStart();
        }
        new MLBusinessTouchpointListener().setOnTouchListener(findViewById(R.id.scroll_view));
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @NonNull
    private PaymentResultPresenter createPresenter() {
        final PaymentModel paymentModel = getIntent().getParcelableExtra(EXTRA_PAYMENT_MODEL);
        final Session session = Session.getInstance();

        return new PaymentResultPresenter(session.getConfigurationModule().getPaymentSettings(),
            session.getInstructionsRepository(), paymentModel, BehaviourProvider.getFlowBehaviour(), isMP(this));
    }

    @Override
    public void configureViews(@NonNull final PaymentResultViewModel model, @NonNull final PaymentModel paymentModel,
        @NonNull final PaymentResultBody.Listener listener) {
        findViewById(R.id.loading).setVisibility(View.GONE);
        final PaymentResultHeader header = findViewById(R.id.header);
        header.setModel(model.headerModel);
        final PaymentResultBody body = findViewById(R.id.body);

        if (model.remediesModel.hasRemedies()) {
            body.setVisibility(View.GONE);
            loadRemedies(paymentModel, model);
        } else {
            body.init(model.bodyModel, listener);
            //TODO migrate
            PaymentResultLegacyRenderer.render(findViewById(R.id.container), listener, model.legacyViewModel);
        }
    }

    private void loadRemedies(@NonNull final PaymentModel paymentModel,
        @NonNull final PaymentResultViewModel model) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            remediesFragment = (RemediesFragment) fragmentManager.findFragmentByTag(RemediesFragment.TAG);
            payButtonFragment = (PayButtonFragment) fragmentManager.findFragmentByTag(PayButtonFragment.TAG);

            if (remediesFragment == null || payButtonFragment == null) {
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (remediesFragment == null) {
                    remediesFragment = RemediesFragment.newInstance(paymentModel, model.remediesModel);
                    transaction.replace(R.id.remedies, remediesFragment, RemediesFragment.TAG);
                }
                if (payButtonFragment == null) {
                    payButtonFragment = new PayButtonFragment();
                    transaction.replace(R.id.pay_button, payButtonFragment, PayButtonFragment.TAG);
                }
                transaction.commitAllowingStateLoss();
            }

            footer.setVisibility(View.VISIBLE);
            footer.init(model.footerModel, remediesFragment);
            findViewById(R.id.remedies).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showApiExceptionError(@NonNull final ApiException exception, @NonNull final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    @Override
    public void showInstructionsError() {
        ErrorUtil.startErrorActivity(this,
            new MercadoPagoError(getString(R.string.px_standard_error_message), false));
    }

    @Override
    public void onBackPressed() {
        presenter.onAbort();
    }

    @Override
    public void setStatusBarColor(@ColorRes final int color) {
        ViewUtils.setStatusBarColor(ContextCompat.getColor(this, color), getWindow());
    }

    @Override
    public void openLink(@NonNull final String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (final ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void finishWithResult(final int resultCode) {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }

    @Override
    public void changePaymentMethod() {
        ViewUtils.hideKeyboard(this);
        final Intent returnIntent = new Intent();
        new ChangePaymentMethodPostPaymentAction().addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @Override
    public void onUserValidation() {
        changePaymentMethod();
    }

    @Override
    public void recoverPayment() {
        final Intent returnIntent = new Intent();
        new RecoverPaymentPostPaymentAction().addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @SuppressLint("Range")
    @Override
    public void copyToClipboard(@NonNull final String content) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("", content);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            final View container = findViewById(R.id.container);
            new AndesSnackbar(container.getContext(), container, AndesSnackbarType.SUCCESS,
                getString(R.string.px_copied_to_clipboard_ack),
                AndesSnackbarDuration.SHORT).show();
        }
    }

    @Override
    public void launchDeepLink(@NonNull final String deepLink) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)));
        } catch (final ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void processCrossSellingBusinessAction(@NonNull final String deepLink) {
        try {
            startActivity(getSafeIntent(this, Uri.parse(deepLink)));
        } catch (final ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void prePayment(@NonNull final PayButton.OnReadyForPaymentCallback callback) {
        ViewUtils.hideKeyboard(this);
        remediesFragment.onPrePayment(callback);
    }

    @Override
    public void enqueueOnExploding(@NonNull final PayButton.OnEnqueueResolvedCallback callback) {
        remediesFragment.onPayButtonPressed(callback);
    }

    @Override
    public void enablePayButton() {
        payButtonFragment.enable();
    }

    @Override
    public void disablePayButton() {
        payButtonFragment.disable();
    }

    @Override
    public void onPaymentFinished(@NonNull final PaymentModel paymentModel,
        @NonNull final PayButton.OnPaymentFinishedCallback callback) {
        paymentModel.process(new PaymentModelHandler() {
            @Override
            public void visit(@NonNull final PaymentModel paymentModel) {
                PaymentResultActivity.startWithForwardResult(PaymentResultActivity.this, paymentModel);
                finish();
            }

            @Override
            public void visit(@NonNull final BusinessPaymentModel businessPaymentModel) {
                BusinessPaymentResultActivity.startWithForwardResult(PaymentResultActivity.this, businessPaymentModel);
                finish();
            }
        });
    }
}