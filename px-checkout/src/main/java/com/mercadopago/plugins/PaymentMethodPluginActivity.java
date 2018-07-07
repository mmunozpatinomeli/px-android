package com.mercadopago.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.BuildConfig;
import com.mercadopago.components.Action;
import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.BackAction;
import com.mercadopago.components.Component;
import com.mercadopago.components.ComponentManager;
import com.mercadopago.components.NextAction;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.internal.datasource.PluginService;
import com.mercadopago.internal.di.ConfigurationModule;
import com.mercadopago.internal.di.UserSelectionComponent;
import com.mercadopago.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.plugins.model.PaymentMethodInfo;
import com.mercadopago.tracker.FlowHandler;
import com.mercadopago.tracker.MPTrackingContext;
import com.mercadopago.android.px.tracking.model.ScreenViewEvent;


public class PaymentMethodPluginActivity extends AppCompatActivity implements ActionDispatcher {

    private static final String SCREEN_NAME_CONFIG_PAYMENT_METHOD_PLUGIN = "CONFIG_PAYMENT_METHOD";
    private static final String PUBLIC_KEY = "public_key";

    private String mPublicKey;

    public static Intent getIntent(@NonNull final Context context, @NonNull final String publicKey) {
        final Intent intent = new Intent(context, PaymentMethodPluginActivity.class);
        intent.putExtra(PUBLIC_KEY, publicKey);
        return intent;
    }

    ComponentManager componentManager;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final UserSelectionComponent configurationModule = new ConfigurationModule(this);
        final UserSelectionRepository userSelectionRepository = configurationModule.getUserSelectionRepository();
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();

        if (paymentMethod == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final PaymentMethodInfo paymentMethodInfo = new PluginService(this)
            .getPaymentMethodInfo(paymentMethod.getId());

        final PaymentMethodPlugin plugin = CheckoutStore
                .getInstance().getPaymentMethodPluginById(paymentMethodInfo.getId());

        final Intent intent = getIntent();
        mPublicKey = intent.getStringExtra(PUBLIC_KEY);

        trackScreen(plugin.getId());

        final PluginComponent.Props props = new PluginComponent.Props.Builder()
                .setData(CheckoutStore.getInstance().getData())
                .setCheckoutPreference(CheckoutStore.getInstance().getCheckoutPreference())
                .build();

        final Component component = plugin.createConfigurationComponent(props, this);
        componentManager = new ComponentManager(this);

        if (component == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        component.setDispatcher(this);
        componentManager.render(component);
    }

    private void trackScreen(final String id) {

        final String screenName = SCREEN_NAME_CONFIG_PAYMENT_METHOD_PLUGIN + "_" + id;

        final MPTrackingContext mTrackingContext = new MPTrackingContext.Builder(this, mPublicKey)
                .setVersion(BuildConfig.VERSION_NAME)
                .build();

        final ScreenViewEvent event = new ScreenViewEvent.Builder()
                .setFlowId(FlowHandler.getInstance().getFlowId())
                .setScreenId(screenName)
                .setScreenName(screenName)
                .build();

        mTrackingContext.trackEvent(event);
    }

    @Override
    public void dispatch(final Action action) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (action instanceof NextAction) {
                    setResult(RESULT_OK);
                    finish();
                } else if (action instanceof BackAction) {
                    onBackPressed();
                } else {
                    componentManager.dispatch(action);
                }
            }
        });
    }
}