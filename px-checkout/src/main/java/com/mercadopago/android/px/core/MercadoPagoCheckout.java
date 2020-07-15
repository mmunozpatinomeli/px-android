package com.mercadopago.android.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.configuration.TrackingConfiguration;
import com.mercadopago.android.px.internal.callbacks.CallbackHolder;
import com.mercadopago.android.px.internal.datasource.MercadoPagoPaymentConfiguration;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.checkout.CheckoutActivity;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import com.mercadopago.android.px.tracking.internal.events.InitEvent;

/**
 * Main class of this project. It provides access to most of the checkout experience.
 */
@SuppressWarnings("unused")
public final class MercadoPagoCheckout {

    public static final int PAYMENT_RESULT_CODE = 7;
    public static final int SESSION_EXPIRED_RESULT_CODE = 666;
    public static final String EXTRA_PAYMENT_RESULT = "EXTRA_PAYMENT_RESULT";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    @NonNull
    private final String publicKey;

    @NonNull
    private final AdvancedConfiguration advancedConfiguration;

    @Nullable
    private final String preferenceId;

    @Nullable
    private final String privateKey;

    @NonNull
    private final PaymentConfiguration paymentConfiguration;

    @Nullable
    private final CheckoutPreference checkoutPreference;

    @NonNull
    private final TrackingConfiguration trackingConfiguration;

    /* default */ PrefetchService prefetch;

    /* default */ MercadoPagoCheckout(final Builder builder) {
        publicKey = builder.publicKey;
        advancedConfiguration = builder.advancedConfiguration;
        preferenceId = builder.preferenceId;
        privateKey = builder.privateKey;
        paymentConfiguration = builder.paymentConfiguration;
        checkoutPreference = builder.checkoutPreference;
        trackingConfiguration = builder.trackingConfiguration;
        CallbackHolder.getInstance().clean();
    }

    /**
     * Starts checkout experience. When the flows ends it returns a {@link PaymentResult} object that will be returned
     * on {@link Activity#onActivityResult(int, int, Intent)} if success or {@link com.mercadopago.android.px.model.exceptions.MercadoPagoError}
     * <p>
     * will return on {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @param context context needed to start checkout.
     * @param requestCode it's the number that identifies the checkout flow request for {@link
     * Activity#onActivityResult(int, int, Intent)}
     */
    public void startPayment(@NonNull final Context context, final int requestCode) {
        startIntent(context, CheckoutActivity.getIntent(context), requestCode);
    }

    private void startIntent(@NonNull final Context context, @NonNull final Intent checkoutIntent,
        final int requestCode) {

        final Session session = Session.getInstance();
        session.init(this);
        if (prefetch != null && prefetch.getInitResponse() != null) {
            session.getInitRepository().lazyConfigure(prefetch.getInitResponse());
        }
        PrefetchService.onCheckoutStarted();

        MPTracker.getInstance().initializeSessionTime();
        new InitEvent(session.getConfigurationModule().getPaymentSettings()).track();

        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(checkoutIntent, requestCode);
        } else {
            // Since android 9, we are forced to startActivities from an Activity context or use NEW_TASK flag.
            //https://developer.android.com/about/versions/pie/android-9.0-changes-all#fant-required
            checkoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(checkoutIntent);
        }
    }

    @NonNull
    public AdvancedConfiguration getAdvancedConfiguration() {
        return advancedConfiguration;
    }

    @NonNull
    public String getPublicKey() {
        return publicKey;
    }

    @Nullable
    public String getPreferenceId() {
        return preferenceId;
    }

    @Nullable
    public CheckoutPreference getCheckoutPreference() {
        return checkoutPreference;
    }

    @NonNull
    public String getPrivateKey() {
        return TextUtil.isEmpty(privateKey) ? "" : privateKey;
    }

    @NonNull
    public PaymentConfiguration getPaymentConfiguration() {
        return paymentConfiguration;
    }

    @NonNull
    public TrackingConfiguration getTrackingConfiguration() {
        return trackingConfiguration;
    }

    @SuppressWarnings("unused")
    public static final class Builder {

        /* default */ @NonNull final String publicKey;

        /* default */ @Nullable final String preferenceId;

        /* default */ @Nullable final CheckoutPreference checkoutPreference;

        /* default */ @NonNull AdvancedConfiguration advancedConfiguration =
            new AdvancedConfiguration.Builder().build();

        /* default */ @NonNull PaymentConfiguration paymentConfiguration = MercadoPagoPaymentConfiguration.create();

        /* default */ @Nullable String privateKey;

        /* default */ @NonNull TrackingConfiguration trackingConfiguration =
            new TrackingConfiguration.Builder().build();

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout} {@see  <a
         * href="http://developers.mercadopago.com/">our developers site</a>}
         *
         * @param publicKey merchant public key / collector public key {@see <a href="https://www.mercadopago.com/mla/account/credentials">credentials</a>}
         * @param paymentConfiguration the payment configuration for this checkout.
         * @param checkoutPreference the preference that represents the payment information.
         */
        public Builder(@NonNull final String publicKey,
            @NonNull final CheckoutPreference checkoutPreference,
            @NonNull final PaymentConfiguration paymentConfiguration) {
            this.publicKey = publicKey;
            this.paymentConfiguration = paymentConfiguration;
            this.checkoutPreference = checkoutPreference;
            preferenceId = null;
        }

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout} {@see  <a
         * href="http://developers.mercadopago.com/">our developers site</a>}
         *
         * @param publicKey merchant public key / collector public key {@see <a href="https://www.mercadopago.com/mla/account/credentials">credentials</a>}
         * @param paymentConfiguration the payment configuration for this checkout.
         * @param preferenceId the preference id that represents the payment information.
         */
        public Builder(@NonNull final String publicKey,
            @NonNull final String preferenceId,
            @NonNull final PaymentConfiguration paymentConfiguration) {
            this.publicKey = publicKey;
            this.paymentConfiguration = paymentConfiguration;
            this.preferenceId = preferenceId;
            checkoutPreference = null;
        }

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout} For more information check the following
         * links {@see <a href="https://www.mercadopago.com/mla/account/credentials">credentials</a>} {@see <a
         * href="https://www.mercadopago.com.ar/developers/es/reference/preferences/_checkout_preferences/post/">create
         * preference</a>}
         *
         * @param publicKey merchant public key / collector public key
         * @param preferenceId the preference id that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final String preferenceId) {
            this.publicKey = publicKey;
            this.preferenceId = preferenceId;
            checkoutPreference = null;
        }

        /**
         * Private key provides save card capabilities and account money balance.
         *
         * @param privateKey the user private key
         * @return builder to keep operating
         */
        public Builder setPrivateKey(@NonNull final String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour You can enable/disable
         * several functionality.
         *
         * @param advancedConfiguration your configuration.
         * @return builder to keep operating
         */
        public Builder setAdvancedConfiguration(@NonNull final AdvancedConfiguration advancedConfiguration) {
            this.advancedConfiguration = advancedConfiguration;
            return this;
        }

        /**
         * It provides additional configurations to modify tracking and session data.
         *
         * @param trackingConfiguration your configuration.
         * @return builder to keep operating
         */
        public Builder setTrackingConfiguration(@NonNull final TrackingConfiguration trackingConfiguration) {
            this.trackingConfiguration = trackingConfiguration;
            return this;
        }

        /**
         * @return {@link MercadoPagoCheckout} instance
         */
        public MercadoPagoCheckout build() {
            return new MercadoPagoCheckout(this);
        }
    }
}