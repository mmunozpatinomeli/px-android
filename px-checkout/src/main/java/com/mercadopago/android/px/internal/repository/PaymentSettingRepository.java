package com.mercadopago.android.px.internal.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.internal.model.SecurityType;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import com.mercadopago.android.px.model.internal.Configuration;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.List;

public interface PaymentSettingRepository {

    void reset();

    void configure(@NonNull final AdvancedConfiguration advancedConfiguration);

    void configure(@NonNull final String publicKey);

    void configure(@NonNull final Site site);

    void configure(@NonNull final Currency currency);

    void configure(@Nullable final CheckoutPreference checkoutPreference);

    void configure(@NonNull final PaymentConfiguration paymentConfiguration);

    void configure(@NonNull final Configuration configuration);

    void configure(@NonNull final Token token);

    void configure(@NonNull final SecurityType secondFactor);

    void clearToken();

    void configurePreferenceId(@Nullable final String preferenceId);

    void configurePrivateKey(@Nullable final String privateKey);

    @NonNull
    List<PaymentTypeChargeRule> chargeRules();

    @NonNull
    PaymentConfiguration getPaymentConfiguration();

    @Nullable
    CheckoutPreference getCheckoutPreference();

    @Nullable
    String getCheckoutPreferenceId();

    @NonNull
    String getPublicKey();

    @NonNull
    Site getSite();

    @NonNull
    Currency getCurrency();

    @NonNull
    String getTransactionId();

    @NonNull
    AdvancedConfiguration getAdvancedConfiguration();

    @Nullable
    String getPrivateKey();

    @NonNull
    Configuration getConfiguration();

    @Nullable
    Token getToken();

    @NonNull
    SecurityType getSecurityType();

    boolean hasToken();

    boolean isPaymentConfigurationValid();
}