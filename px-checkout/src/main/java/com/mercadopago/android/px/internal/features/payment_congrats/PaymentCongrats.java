package com.mercadopago.android.px.internal.features.payment_congrats;

import android.app.Activity;
import android.content.Intent;
import com.mercadopago.android.px.core.internal.MercadoPagoCardStorage;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;

public class PaymentCongrats {

    private static final String PAYMENT_CONGRATS = "payment_congrats";

    /**
     * Allows to execute a congrats activity
     *
     * @param paymentCongratsModel model with the needed data to show a PaymentCongrats
     * @param activity caller activity
     * @param requestCode requestCode for ActivityForResult
     */
    public static void show(final PaymentCongratsModel paymentCongratsModel, final Activity activity,
        final int requestCode) {
        Session.getInstance().init((MercadoPagoCardStorage) null);
        final Intent intent = new Intent(activity, BusinessPaymentResultActivity.class);
        intent.putExtra(PAYMENT_CONGRATS, paymentCongratsModel);
        activity.startActivityForResult(intent, requestCode);
    }
}