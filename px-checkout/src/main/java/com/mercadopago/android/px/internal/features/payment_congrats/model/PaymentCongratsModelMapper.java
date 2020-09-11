package com.mercadopago.android.px.internal.features.payment_congrats.model;

import androidx.annotation.NonNull;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.business_result.PaymentCongratsResponseMapper;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import com.mercadopago.android.px.internal.util.PaymentDataHelper;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.internal.CongratsResponse;
import com.mercadopago.android.px.tracking.internal.views.ResultViewTrack;
import java.math.BigDecimal;

public class PaymentCongratsModelMapper extends Mapper<BusinessPaymentModel, PaymentCongratsModel> {

    /**
     * Takes a BusinessPaymentModel and outputs a PaymentCongratsModel
     *
     * @param businessPaymentModel the data to be converted
     * @return a paymentCongratsModel built from a businessPaymentModel
     */
    @Override
    public PaymentCongratsModel map(final BusinessPaymentModel businessPaymentModel) {

        final PaymentSettingRepository paymentSettings =
            Session.getInstance().getConfigurationModule().getPaymentSettings();

        final PaymentCongratsResponse paymentCongratsResponse =
            new PaymentCongratsResponseMapper().map(businessPaymentModel.getCongratsResponse());
        final BusinessPayment businessPayment = businessPaymentModel.getPayment();
        final PXPaymentCongratsTracking tracking = new PXPaymentCongratsTracking(
            businessPaymentModel.getPaymentResult().getPaymentData().getCampaign() != null ? businessPaymentModel
                .getPaymentResult().getPaymentData().getCampaign().getId() : "",
            businessPaymentModel.getCurrency().getId(),
            businessPayment.getPaymentStatusDetail(),
            businessPaymentModel.getPaymentResult().getPaymentId(),
            Session.getInstance().getConfigurationModule().getPaymentSettings().getCheckoutPreference()
                .getTotalAmount(),
            Session.getInstance().getConfigurationModule().getTrackingRepository().getFlowDetail(),
            Session.getInstance().getConfigurationModule().getTrackingRepository().getFlowId(),
            Session.getInstance().getConfigurationModule().getTrackingRepository().getSessionId(),
            businessPaymentModel.getPayment().getPaymentMethodId()
        );
        final PaymentCongratsModel.Builder builder = new PaymentCongratsModel.Builder()
            .withTracking(tracking)
            .withDiscountCouponsAmount(
                PaymentDataHelper.getTotalDiscountAmount(businessPaymentModel.getPaymentResult().getPaymentDataList()))
            .withCongratsType(
                PaymentCongratsModel.CongratsType.fromName(businessPayment.getPaymentStatus()))
            .withCrossSelling(paymentCongratsResponse.getCrossSellings())
            .withHeader(businessPayment.getTitle(), businessPayment.getImageUrl())
            .withShouldShowPaymentMethod(businessPayment.shouldShowPaymentMethod())
            .withIconId(businessPayment.getIcon())
            .withPaymentData(businessPaymentModel.getPaymentResult().getPaymentData())
            .withIconId(businessPayment.getIcon())
            .withAutoReturn(paymentSettings.getCheckoutPreference().getAutoReturn())
            .withCustomSorting(businessPaymentModel.getCongratsResponse().hasCustomOrder());

        if (!businessPaymentModel.getPaymentResult().getPaymentDataList().isEmpty()) {
            builder.withPaymentMethodInfo(
                getPaymentsInfo(businessPaymentModel.getPaymentResult().getPaymentDataList().get(0),
                    businessPaymentModel.getCurrency(), businessPaymentModel.getCongratsResponse()));
        }
        if (businessPaymentModel.getPaymentResult().getPaymentDataList().size() > 1) {
            builder.withPaymentMethodInfo(
                getPaymentsInfo(businessPaymentModel.getPaymentResult().getPaymentDataList().get(1),
                    businessPaymentModel.getCurrency(), businessPaymentModel.getCongratsResponse()));
        }
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
            builder.withDiscounts(paymentCongratsResponse.getDiscount());
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
        if (paymentCongratsResponse.getExpenseSplit() != null) {
            builder.withExpenseSplit(paymentCongratsResponse.getExpenseSplit());
        }
        if (paymentCongratsResponse.getLoyalty() != null) {
            builder.withLoyalty(paymentCongratsResponse.getLoyalty());
        }
        if (businessPaymentModel.getPaymentResult().getPaymentId() != null) {
            builder.withReceipt(businessPaymentModel.getPaymentResult().getPaymentId(),
                businessPayment.shouldShowReceipt(),
                paymentCongratsResponse.getViewReceipt());
        }
        if (businessPayment.getStatementDescription() != null) {
            builder.withStatementDescription(businessPayment.getStatementDescription());
        }
        if (businessPayment.getSubtitle() != null) {
            builder.withSubtitle(businessPayment.getSubtitle());
        }

        return builder.build();
    }


    private PaymentInfo getPaymentsInfo(final PaymentData paymentData, final Currency currency,
        final CongratsResponse congratsResponse) {
        final PaymentInfo.Builder paymentInfo = new PaymentInfo.Builder()
            .withPaymentMethodType(
                PaymentInfo.PaymentMethodType.fromName(paymentData.getPaymentMethod().getPaymentTypeId()))
            .withPaymentMethodName(paymentData.getPaymentMethod().getName())
            .withPaidAmount(getPrettyAmount(currency,
                PaymentDataHelper.getPrettyAmountToPay(paymentData)))
            .withIconUrl(
                congratsResponse.getPaymentMethodsImages().get(paymentData.getPaymentMethod().getId()));

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

        return paymentInfo.build();
    }

    private String getPrettyAmount(@NonNull final Currency currency, @NonNull final BigDecimal amount) {
        return CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(currency, amount);
    }

    private String getTrackingPaymentStatus(final PaymentResult paymentResult) {
        final String status;
        if (paymentResult.isApproved() || paymentResult.isInstructions()) {
            status = ResultViewTrack.SUCCESS;
        } else if (paymentResult.isRejected()) {
            status = ResultViewTrack.ERROR;
        } else if (paymentResult.isPending()) {
            status = ResultViewTrack.PENDING;
        } else {
            status = ResultViewTrack.UNKNOWN;
        }
        return status;
    }
}
