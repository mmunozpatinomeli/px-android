package com.mercadopago.android.px.internal.features.payment_congrats;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.google.android.flexbox.FlexboxLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.PayerCost;
import java.math.BigDecimal;
import java.util.Locale;

public class PaymentCongratsResultAmount extends FlexboxLayout {

    private final MPTextView title;
    private final MPTextView detail;
    private final MPTextView noRate;
    private final MPTextView rawAmount;
    private final MPTextView discount;

    public PaymentCongratsResultAmount(final Context context) {
        this(context, null);
    }

    public PaymentCongratsResultAmount(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentCongratsResultAmount(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.px_payment_result_amount, this);
        title = findViewById(R.id.title);
        detail = findViewById(R.id.detail);
        noRate = findViewById(R.id.no_rate);
        rawAmount = findViewById(R.id.raw_amount);
        discount = findViewById(R.id.discount);
    }

    public void setModel(@NonNull final Model model) {
        title.setText(getAmountTitle(model));
        ViewUtils.loadOrGone(getAmountDetail(model), detail);
        ViewUtils.loadOrGone(getNoRate(model.payerCost), noRate);

        final Discount discount = model.discount;
        if (discount != null) {
            rawAmount.setText(model.rawAmount);
            rawAmount.setPaintFlags(rawAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            this.discount.setText(discount.getName());
        } else {
            rawAmount.setVisibility(GONE);
            this.discount.setVisibility(GONE);
        }
    }

    @NonNull
    private String getAmountTitle(@NonNull final Model model) {
        if (hasPayerCostWithMultipleInstallments(model.payerCost)) {
            final String installmentsAmount = getPrettyAmount(model.currency, model.payerCost.getInstallmentAmount());
            return String.format(Locale.getDefault(), "%dx %s", model.payerCost.getInstallments(),
                installmentsAmount);
        } else {
            return getPrettyAmount(model.currency, model.amount);
        }
    }

    @Nullable
    private String getNoRate(@Nullable final PayerCost payerCost) {
        if (hasPayerCostWithMultipleInstallments(payerCost)) {
            final BigDecimal rate = payerCost.getInstallmentRate();
            if (BigDecimal.ZERO.equals(rate)) {
                return getResources().getString(R.string.px_zero_rate).toLowerCase();
            }
        }
        return null;
    }

    @NonNull
    private String getPrettyAmount(@NonNull final Currency currency, @NonNull final BigDecimal amount) {
        return CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(currency, amount);
    }

    @Nullable
    private String getAmountDetail(@NonNull final Model model) {
        if (hasPayerCostWithMultipleInstallments(model.payerCost)) {
            return String.format(Locale.getDefault(), "(%s)",
                getPrettyAmount(model.currency, model.payerCost.getTotalAmount()));
        }
        return null;
    }

    private boolean hasPayerCostWithMultipleInstallments(@Nullable final PayerCost payerCost) {
        return payerCost != null && payerCost.hasMultipleInstallments();
    }

    public static final class Model {
        @NonNull /* default */ final BigDecimal amount;
        @NonNull /* default */ final String rawAmount;
        @NonNull /* default */ final Currency currency;
        @Nullable /* default */ final PayerCost payerCost;
        @Nullable /* default */ final Discount discount;

        /* default */ Model(@NonNull final Builder builder) {
            amount = builder.amount;
            currency = builder.currency;
            payerCost = builder.payerCost;
            discount = builder.discount;
            rawAmount = builder.rawAmount;
        }

        public static class Builder {
            @NonNull /* default */ BigDecimal amount;
            @NonNull /* default */ String rawAmount;
            @NonNull /* default */ Currency currency;
            @Nullable /* default */ PayerCost payerCost;
            @Nullable /* default */ Discount discount;

            public Builder(@NonNull final BigDecimal amount, @NonNull final String rawAmount,
                @NonNull final Currency currency) {
                this.amount = amount;
                this.rawAmount = rawAmount;
                this.currency = currency;
            }

            public Builder setPayerCost(@Nullable final PayerCost payerCost) {
                this.payerCost = payerCost;
                return this;
            }

            public Builder setDiscount(@Nullable final Discount discount) {
                this.discount = discount;
                return this;
            }

            public Model build() {
                return new Model(this);
            }
        }
    }
}