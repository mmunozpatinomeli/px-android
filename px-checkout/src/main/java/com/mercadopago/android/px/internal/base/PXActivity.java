package com.mercadopago.android.px.internal.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.addons.BehaviourProvider;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.font.FontHelper;

public abstract class PXActivity<P extends BasePresenter> extends AppCompatActivity implements MvpView {

    protected P presenter;

    @Override
    protected final void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FontHelper.init(getApplicationContext());
        if (!Session.getInstance().isInitialized()) {
            onHalted();
            setResult(MercadoPagoCheckout.SESSION_EXPIRED_RESULT_CODE);
            finish();
        } else {
            onCreated(savedInstanceState);
        }
    }

    protected abstract void onCreated(@Nullable final Bundle savedInstanceState);

    protected void onHalted() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    public void attachBaseContext(@NonNull final Context context) {
        super.attachBaseContext(BehaviourProvider.getLocaleBehaviour().attachBaseContext(context));
    }

    @Override
    public void onBackPressed() {
        if (presenter != null) {
            presenter.tracker.trackBack();
        }
        super.onBackPressed();
    }

    public void overrideTransitionIn() {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    public void overrideTransitionOut() {
        overridePendingTransition(R.anim.px_slide_left_to_right_in, R.anim.px_slide_left_to_right_out);
    }

    public void overrideTransitionFadeInFadeOut() {
        overridePendingTransition(R.anim.px_fade_in_seamless, R.anim.px_fade_out_seamless);
    }

    public void overrideTransitionWithNoAnimation() {
        overridePendingTransition(R.anim.px_no_change_animation, R.anim.px_no_change_animation);
    }
}