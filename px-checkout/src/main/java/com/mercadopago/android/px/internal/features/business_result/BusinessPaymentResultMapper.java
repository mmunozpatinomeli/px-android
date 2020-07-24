package com.mercadopago.android.px.internal.features.business_result;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;
import com.mercadopago.android.px.internal.view.PaymentResultBody;
import com.mercadopago.android.px.internal.view.PaymentResultHeader;
import com.mercadopago.android.px.internal.view.PaymentResultMethod;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.GenericLocalized;
import com.mercadopago.android.px.internal.viewmodel.PaymentResultType;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.PaymentData;
import java.util.ArrayList;
import java.util.List;

public class BusinessPaymentResultMapper extends Mapper<BusinessPaymentModel, BusinessPaymentResultViewModel> {

    @Override
    public BusinessPaymentResultViewModel map(@NonNull final PaymentCongratsModel model) {
        final PaymentResultHeader.Model headerModel = getHeaderModel(model);
        final PaymentResultBody.Model bodyModel = getBodyModel(model);
        return new BusinessPaymentResultViewModel(headerModel, bodyModel,
            model.getExitActionPrimary(), model.getExitActionSecondary());
    }

    @NonNull
    private PaymentResultBody.Model getBodyModel(@NonNull final BusinessPaymentModel model) {
        final BusinessPayment payment = model.getPayment();
        final List<PaymentResultMethod.Model> methodModels = new ArrayList<>();
        if (payment.shouldShowPaymentMethod()) {
            for (final PaymentData paymentData : model.getPaymentResult().getPaymentDataList()) {
                methodModels.add(PaymentResultMethod.Model.with(paymentData, model.getCurrency(),
                    payment.getStatementDescription()));
            }
        }

        final PaymentResultType type = PaymentResultType.from(payment.getDecorator());
        return new PaymentResultBody.Model.Builder()
            .setMethodModels(methodModels)
            .setCongratsViewModel(new CongratsResponseMapper(new BusinessPaymentResultTracker())
                .map(model.getCongratsResponse()))
            .setReceiptId((type == PaymentResultType.APPROVED && payment.shouldShowReceipt()) ? payment.getReceipt() : null)
            .setHelp(payment.getHelp())
            .setStatement(payment.getStatementDescription())
            .setTopFragment(payment.getTopFragment())
            .setBottomFragment(payment.getBottomFragment())
            .setImportantFragment(payment.getImportantFragment())
            .build();
    }

    @NonNull
    private PaymentResultHeader.Model getHeaderModel(@NonNull final PaymentCongratsModel payment) {
        final PaymentResultHeader.Model.Builder builder = new PaymentResultHeader.Model.Builder();

        builder.setIconImage(payment.getIconId() == 0 ? R.drawable.px_icon_product : payment.getIconId());
        builder.setIconUrl(payment.getImageUrl());

        final PaymentResultType type = PaymentResultType.from(payment.getDecorator());

        return builder
            .setBackground(type.resColor)
            .setBadgeImage(type.badge)
            .setTitle(new GenericLocalized(payment.getTitle(), 0))
            .setLabel(new GenericLocalized(payment.getSubtitle(), type.message))
            .build();
    }
}