package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsCurrency;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentInfo;
import com.mercadopago.android.px.internal.util.PaymentDataHelper;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.display_info.ResultInfo;
import com.mercadopago.android.px.model.internal.Text;
import java.util.Locale;

public class PaymentResultMethod extends ConstraintLayout {

    private final ImageView icon;
    private final MPTextView description;
    private final MPTextView paymentMethodStatement;
    private final MPTextView statement;
    private final PaymentResultAmount amount;
    private final MPTextView infoTitle;
    private final MPTextView infoSubtitle;

    public PaymentResultMethod(final Context context) {
        this(context, null);
    }

    public PaymentResultMethod(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentResultMethod(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.px_payment_result_method, this);
        icon = findViewById(R.id.icon);
        description = findViewById(R.id.description);
        paymentMethodStatement = findViewById(R.id.pm_statement);
        statement = findViewById(R.id.statement);
        amount = findViewById(R.id.amount);
        infoTitle = findViewById(R.id.info_title);
        infoSubtitle = findViewById(R.id.info_subtitle);
    }

    public void setModel(@NonNull final Model model) {
        icon.setImageResource(ResourceUtil.getIconResource(getContext(), model.paymentMethodId));

        ViewUtils.loadOrGone(getDescription(model), description);
        ViewUtils.loadOrHide(View.GONE, model.paymentMethodDescription, paymentMethodStatement);
        ViewUtils.loadOrGone(getStatement(model), statement);
        amount.setModel(model.amountModel);
        renderInfo(model.info);
    }

    @Nullable
    private String getStatement(@NonNull final Model model) {
        if (PaymentTypes.isCardPaymentType(model.paymentTypeId) && TextUtil.isNotEmpty(model.statement)) {
            return TextUtil.format(getContext(), R.string.px_text_state_account_activity_congrats, model.statement);
        }
        return null;
    }

    @Nullable
    private String getDescription(@NonNull final Model model) {
        if (PaymentTypes.isCardPaymentType(model.paymentTypeId)) {
            return String.format(Locale.getDefault(), "%s %s %s",
                model.paymentMethodName,
                getResources().getString(R.string.px_ending_in),
                model.lastFourDigits);
        } else if(!PaymentTypes.isAccountMoney(model.paymentTypeId)){
            return model.paymentMethodName;
        }
        return null;
    }

    private void renderInfo(@Nullable final ResultInfo info) {
        if (info != null) {
            ViewUtils.loadOrGone(info.getTitle(), infoTitle);
            ViewUtils.loadOrGone(info.getSubtitle(), infoSubtitle);
        } else {
            infoTitle.setVisibility(GONE);
            infoSubtitle.setVisibility(GONE);
        }
    }

    public static final class Model {

        public static Model with(@NonNull final PaymentData paymentData, @NonNull final Currency currency) {

            PaymentInfo paymentInfo = new PaymentInfo.Builder()
                .withLastFourDigits(paymentData.getToken() != null ? paymentData.getToken().getLastFourDigits() : null)
                .withPaymentMethodId(paymentData.getPaymentMethod().getId())
                .withPaymentMethodName(paymentData.getPaymentMethod().getName())
                .withRawAmount(paymentData.getRawAmount())
                .build();

            return with(paymentInfo, null);
        }

        public static Model with(@NonNull final PaymentInfo paymentInfo, @Nullable final String statement) {

            //getPrettyAmountToPay CALCULA CUANTO SE PAGÓ DE VERDAD
//            final PaymentResultAmount.Model amountModel = new PaymentResultAmount.Model.Builder(
//                PaymentDataHelper.getPrettyAmountToPay(paymentInfo), paymentInfo.getRawAmount(), currency)
//                .setPayerCost(paymentInfo.getPayerCost())
//                .setDiscount(paymentInfo.getDiscount())
//                .build();

//            final PaymentMethod paymentMethod = paymentInfo.getPaymentMethod();
//            final Text description =
//                paymentMethod.getDisplayInfo() != null ? paymentMethod.getDisplayInfo().getDescription() : Text.EMPTY;
            return new Builder(paymentInfo.getPaymentMethodId(), paymentInfo.getPaymentMethodName(), Text.EMPTY,
                "account_money")
                .setLastFourDigits(paymentInfo.getLastFourDigits())
                .setStatement(statement)
//                .setAmountModel(amountModel)
//                .setInfo(paymentInfo.getDisplayInfo() != null ? paymentMethod.getDisplayInfo().getResultInfo() : null)
                .build();
        }

//        public static Model with(@NonNull final PaymentInfo paymentInfo, @NonNull final Currency currency,
//            @Nullable final String statement) {
//
//
//            //TODO MECHI hablar con nik este amountModel
//            final PaymentResultAmount.Model amountModel = new PaymentResultAmount.Model.Builder(
//                PaymentDataHelper.getPrettyAmountToPay(paymentInfo), paymentInfo.getRawAmount(), currency)
////                .setPayerCost(paymentInfo.getPayerCost())
////                .setDiscount(paymentInfo.getDiscount())
//                .build();
//
////            final PaymentMethod paymentMethod = paymentInfo.getPaymentMethod();
////            final Text description =
////                paymentMethod.getDisplayInfo() != null ? paymentMethod.getDisplayInfo().getDescription() : Text.EMPTY;
//            //TODO MECHI completar el description del paymentMethod
//            return new Builder(paymentInfo.getPaymentMethodId(), paymentInfo.getPaymentMethodName(), Text.EMPTY,
//                "")
//                .setLastFourDigits(paymentInfo.getLastFourDigits() != null ? paymentInfo.getLastFourDigits() : null)
//                .setStatement(statement)
//                .setAmountModel(amountModel)
//                //TODO MECHI completar el info!
//                .setInfo(null)
//                .build();
//        }

        @NonNull /* default */ final String paymentMethodId;
        @NonNull /* default */ final String paymentMethodName;
        @NonNull /* default */ final Text paymentMethodDescription;
        @NonNull /* default */ final String paymentTypeId;
        @NonNull /* default */ final PaymentResultAmount.Model amountModel;
        @Nullable /* default */ final String lastFourDigits;
        @Nullable /* default */ final String statement;
        @Nullable /* default */ final ResultInfo info;

        /* default */ Model(@NonNull final Builder builder) {
            paymentMethodId = builder.paymentMethodId;
            paymentMethodName = builder.paymentMethodName;
            paymentMethodDescription = builder.paymentMethodDescription;
            paymentTypeId = builder.paymentTypeId;
            amountModel = builder.amountModel;
            lastFourDigits = builder.lastFourDigits;
            statement = builder.statement;
            info = builder.info;
        }

        public static class Builder {
            @NonNull /* default */ String paymentMethodId;
            @NonNull /* default */ String paymentMethodName;
            @NonNull /* default */ final Text paymentMethodDescription;
            @NonNull /* default */ String paymentTypeId;
            /* default */ PaymentResultAmount.Model amountModel;
            @Nullable /* default */ String lastFourDigits;
            @Nullable /* default */ String statement;
            @Nullable /* default */ ResultInfo info;

            public Builder(@NonNull final String paymentMethodId, @NonNull final String paymentMethodName,
                @NonNull final Text paymentMethodDescription, @NonNull final String paymentTypeId) {
                this.paymentMethodId = paymentMethodId;
                this.paymentMethodName = paymentMethodName;
                this.paymentMethodDescription = paymentMethodDescription;
                this.paymentTypeId = paymentTypeId;
            }

            public Builder setAmountModel(@NonNull final PaymentResultAmount.Model amountModel) {
                this.amountModel = amountModel;
                return this;
            }

            public Builder setLastFourDigits(@Nullable final String lastFourDigits) {
                this.lastFourDigits = lastFourDigits;
                return this;
            }

            public Builder setStatement(@Nullable final String statement) {
                this.statement = statement;
                return this;
            }

            public Builder setInfo(@Nullable final ResultInfo info) {
                this.info = info;
                return this;
            }

            public Model build() {
                return new Model(this);
            }
        }
    }
}