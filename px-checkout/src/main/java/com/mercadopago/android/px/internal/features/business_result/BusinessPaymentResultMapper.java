package com.mercadopago.android.px.internal.features.business_result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.dummy_result.RedirectHelper;
import com.mercadopago.android.px.internal.features.payment_congrats.model.CongratsViewModelMapper;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentInfo;
import com.mercadopago.android.px.internal.view.PaymentResultBody;
import com.mercadopago.android.px.internal.view.PaymentResultHeader;
import com.mercadopago.android.px.internal.view.PaymentResultMethod;
import com.mercadopago.android.px.internal.viewmodel.GenericLocalized;
import com.mercadopago.android.px.internal.viewmodel.PaymentResultType;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import java.util.ArrayList;
import java.util.List;

public class BusinessPaymentResultMapper extends Mapper<PaymentCongratsModel, BusinessPaymentResultViewModel> {

    @Nullable private final String autoReturn;

    /* default */ BusinessPaymentResultMapper(@Nullable final String autoReturn) {
        this.autoReturn = autoReturn;
    }

    @Override
    public BusinessPaymentResultViewModel map(@NonNull final PaymentCongratsModel model) {
        final PaymentResultHeader.Model headerModel = getHeaderModel(model);
        final PaymentResultBody.Model bodyModel = getBodyModel(model);
        return new BusinessPaymentResultViewModel(headerModel, bodyModel,
            model.getFooterMainAction(), model.getFooterSecondaryAction(),
            RedirectHelper.INSTANCE.shouldAutoReturn(autoReturn, model.getCongratsType().name));
    }

    @NonNull
    private PaymentResultBody.Model getBodyModel(@NonNull final PaymentCongratsModel model) {
        final List<PaymentResultMethod.Model> methodModels = new ArrayList<>();
        if (model.getShouldShowPaymentMethod()) {
            for (final PaymentInfo paymentInfo : model.getPaymentsInfo()) {
                methodModels.add(PaymentResultMethod.Model.with(paymentInfo,
                    model.getStatementDescription()));
            }
        }
        final PaymentCongratsModel.CongratsType type = model.getCongratsType();
        return new PaymentResultBody.Model.Builder()
            .setMethodModels(methodModels)
            .setCongratsViewModel(new CongratsViewModelMapper(new BusinessPaymentResultTracker())
                .map(model.getPaymentCongratsResponse()))
            .setReceiptId((type == PaymentCongratsModel.CongratsType.APPROVED && model.getShouldShowReceipt()) ? String
                .valueOf(model.getPaymentId()) : null)
            .setHelp(model.getHelp())
            .setStatement(model.getStatementDescription())
            .setTopFragment(model.getTopFragment())
            .setBottomFragment(model.getBottomFragment())
            .setImportantFragment(model.getImportantFragment())
            .build();
    }

    @NonNull
    private PaymentResultHeader.Model getHeaderModel(@NonNull final PaymentCongratsModel model) {
        final PaymentResultHeader.Model.Builder builder = new PaymentResultHeader.Model.Builder();
        builder.setIconImage(model.getIconId() == 0 ? R.drawable.px_icon_product : model.getIconId());
        builder.setIconUrl(model.getImageUrl());
        final PaymentResultType type = PaymentResultType.from(model.getCongratsType());
        return builder
            .setBackground(type.resColor)
            .setBadgeImage(type.badge)
            .setTitle(new GenericLocalized(model.getTitle(), 0))
            .setLabel(new GenericLocalized(model.getSubtitle(), type.message))
            .build();
    }
}