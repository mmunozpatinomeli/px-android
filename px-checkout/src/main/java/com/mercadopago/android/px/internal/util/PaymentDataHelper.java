package com.mercadopago.android.px.internal.util;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentInfo;
import com.mercadopago.android.px.model.PaymentData;
import java.math.BigDecimal;
import java.util.Collection;

public final class PaymentDataHelper {

    private PaymentDataHelper() {
    }

    public static BigDecimal getPrettyAmountToPay(@NonNull final PaymentData paymentData) {
        if (paymentData.getPayerCost() != null) {
            return paymentData.getPayerCost().getTotalAmount();
        } else if (paymentData.getDiscount() != null) {
            return paymentData.getRawAmount().subtract(paymentData.getDiscount().getCouponAmount());
        }
        return paymentData.getRawAmount();
    }

    public static BigDecimal getPrettyAmountToPay(@NonNull final PaymentInfo paymentInfo) {
        //TODO MECHI: completar con payerCost y discount
//        if (paymentInfo.getPayerCost() != null) {
//            return paymentInfo.getPayerCost().getTotalAmount();
//        } else if (paymentInfo.getDiscount() != null) {
//            return paymentInfo.getRawAmount().subtract(paymentInfo.getDiscount().getCouponAmount());
//        }
        return paymentInfo.getRawAmount();
    }

    public static boolean isSplitPayment(@NonNull final Collection<PaymentData> paymentDataList) {
        return paymentDataList.size() > 1;
    }

    @NonNull
    public static BigDecimal getTotalDiscountAmount(@NonNull final Iterable<PaymentData> paymentDataList) {
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (final PaymentData paymentData : paymentDataList) {
            if (paymentData.getDiscount() != null) {
                totalDiscountAmount = totalDiscountAmount.add(paymentData.getDiscount().getCouponAmount());
            }
        }
        return totalDiscountAmount;
    }
}