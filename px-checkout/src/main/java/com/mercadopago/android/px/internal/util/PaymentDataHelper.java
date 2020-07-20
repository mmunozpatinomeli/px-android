package com.mercadopago.android.px.internal.util;

import androidx.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentData;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

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

    public static <T> boolean isSplitPayment(@NonNull final Collection<T> paymentDataList) {
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