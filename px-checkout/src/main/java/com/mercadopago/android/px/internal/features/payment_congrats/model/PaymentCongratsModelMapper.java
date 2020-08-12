package com.mercadopago.android.px.internal.features.payment_congrats.model;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.business_result.CongratsResponseMapper;
import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import com.mercadopago.android.px.internal.util.PaymentDataHelper;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.PaymentData;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentCongratsModelMapper {

    /**
     * Takes a BusinessPaymentModel and outputs a PaymentCongratsModel
     *
     * @param businessPaymentModel the data to be converted
     * @return a paymentCongratsModel built from a businessPaymentModel
     */
    public PaymentCongratsModel map(final BusinessPaymentModel businessPaymentModel) {
        final PaymentCongratsResponse paymentCongratsResponse =
            new CongratsResponseMapper().map(businessPaymentModel.getCongratsResponse());
        final BusinessPayment businessPayment = businessPaymentModel.getPayment();
        final PaymentCongratsModel.Builder builder = new PaymentCongratsModel.Builder()
            .withCongratsType(
                PaymentCongratsModel.CongratsType.fromName(businessPayment.getPaymentStatus()))
            .withCrossSelling(paymentCongratsResponse.getCrossSellings())
            .withHeader(businessPayment.getTitle(),businessPayment.getImageUrl())
            .withShouldShowPaymentMethod(businessPayment.shouldShowPaymentMethod())
            .withIconId(businessPayment.getIcon())
            .withPaymentMethodInfo(getPaymentsInfo(businessPaymentModel.getPaymentResult().getPaymentDataList(),
                businessPaymentModel.getCurrency()).get(0))
            .withPaymentMethodInfo(getPaymentsInfo(businessPaymentModel.getPaymentResult().getPaymentDataList(),
                businessPaymentModel.getCurrency()).get(1));

        if (businessPayment.getPrimaryAction() != null && businessPayment.getPrimaryAction().getName() != null) {
            builder.withFooterMainAction(businessPayment.getPrimaryAction().getName(),
                businessPayment.getPrimaryAction().getResCode());
        }
        if (businessPayment.getSecondaryAction() != null && businessPayment.getSecondaryAction().getName() != null) {
            builder.withFooterSecondaryAction(businessPayment.getSecondaryAction().getName(),
                businessPayment.getSecondaryAction().getResCode());
        }
        if (businessPayment.getHelp() != null) {
            builder.withHelp(businessPayment.getHelp());
        }
        if (paymentCongratsResponse.getDiscount() != null) {
            builder.withDiscount(paymentCongratsResponse.getDiscount());
        }
        if (businessPayment.getBottomFragment() != null) {
            builder.withBottomFragment(businessPayment.getBottomFragment());
        }
        if (businessPayment.getTopFragment() != null) {
            builder.withTopFragment(businessPayment.getTopFragment());
        }
        if (businessPayment.getImportantFragment() != null) {
            builder.withImportantFragment(businessPayment.getImportantFragment());
        }
        if (paymentCongratsResponse.getMoneySplit() != null) {
            builder.withMoneySplit(paymentCongratsResponse.getMoneySplit());
        }
        if (paymentCongratsResponse.getScore() != null) {
            builder.withScore(paymentCongratsResponse.getScore());
        }
        if (businessPayment.getReceipt() != null) {
            builder.withReceipt(businessPayment.getReceipt(),paymentCongratsResponse.getViewReceipt());
        }
        if (businessPayment.getStatementDescription() != null) {
            builder.withStatementDescription(businessPayment.getStatementDescription());
        }
        if (businessPayment.getSubtitle() != null) {
            builder.withSubtitle(businessPayment.getSubtitle());
        }
//        if (paymentCongratsResponse.getViewReceipt() != null) {
//            builder.withViewReceipt(paymentCongratsResponse.getViewReceipt());
//        }

        return builder.build();
    }

    private List<PaymentInfo> getPaymentsInfo(final Iterable<PaymentData> paymentDataList, final Currency currency) {
        final List<PaymentInfo> paymentsInfoList = new ArrayList<>();
        for (final PaymentData paymentData : paymentDataList) {
            final PaymentInfo.Builder paymentInfo = new PaymentInfo.Builder()
                .withPaymentMethodType(
                    PaymentInfo.PaymentMethodType.fromName(paymentData.getPaymentMethod().getPaymentTypeId()))
                .withPaymentMethodId(paymentData.getPaymentMethod().getId())
                .withPaymentMethodName(paymentData.getPaymentMethod().getName())
                .withPaidAmount(getPrettyAmount(currency,
                    PaymentDataHelper.getPrettyAmountToPay(paymentData)));
            if (paymentData.getToken() != null && paymentData.getToken().getLastFourDigits() != null) {
                paymentInfo.withLastFourDigits(paymentData.getToken().getLastFourDigits());
            }
            if (paymentData.getPayerCost() != null) {
                paymentInfo.withInstallmentsData(
                    paymentData.getPayerCost().getInstallments(),
                    getPrettyAmount(currency, paymentData.getPayerCost().getInstallmentAmount()),
                    getPrettyAmount(currency, paymentData.getPayerCost().getTotalAmount()),
                    paymentData.getPayerCost().getInstallmentRate());
            }
            paymentsInfoList.add(paymentInfo.build());
        }
        return paymentsInfoList;
    }

    private String getPrettyAmount(@NonNull final Currency currency, @NonNull final BigDecimal amount) {
        return CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(currency, amount);
    }
}
