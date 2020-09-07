package com.mercadopago.android.px.internal.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceEventHandler;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import java.util.List;

public interface PaymentRepository {

    PaymentServiceEventHandler getObservableEvents();

    void startPayment();

    void startExpressPayment(@NonNull final PaymentConfiguration paymentConfiguration);

    boolean isExplodingAnimationCompatible();

    @NonNull
    @Size(min = 1)
    List<PaymentData> getPaymentDataList();

    @NonNull
    PaymentResult createPaymentResult(@NonNull final IPaymentDescriptor genericPayment);

    int getPaymentTimeout();

    void storePayment(@NonNull final IPaymentDescriptor iPayment);

    @Nullable
    IPaymentDescriptor getPayment();

    boolean hasRecoverablePayment();

    @NonNull
    PaymentRecovery createRecoveryForInvalidESC();

    @NonNull
    PaymentRecovery createPaymentRecovery();

    void reset();
}