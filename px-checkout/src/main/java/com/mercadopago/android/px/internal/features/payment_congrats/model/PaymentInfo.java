package com.mercadopago.android.px.internal.features.payment_congrats.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.model.display_info.ResultInfo;
import java.math.BigDecimal;

public class PaymentInfo implements Parcelable {

    public static final Creator<PaymentInfo> CREATOR = new Creator<PaymentInfo>() {
        @Override
        public PaymentInfo createFromParcel(final Parcel in) {
            return new PaymentInfo(in);
        }

        @Override
        public PaymentInfo[] newArray(final int size) {
            return new PaymentInfo[size];
        }
    };
    @NonNull public final String rawAmount;
    @NonNull public final String paymentMethodName;
    @Nullable public final String iconUrl;
    @Nullable public final String lastFourDigits;
    @NonNull public final PaymentMethodType paymentMethodType;
    @NonNull public final String paidAmount;
    @Nullable public final String discountName;
    @NonNull public final Integer installmentsCount;
    @Nullable public final String installmentsAmount;
    @Nullable public final String installmentsTotalAmount;
    @Nullable public final BigDecimal installmentsRate;
    @Nullable public final PaymentResultInfo consumerCreditsInfo;
    @Nullable public final PaymentCongratsText description;

    protected PaymentInfo(final Builder builder) {
        rawAmount = builder.rawAmount;
        paymentMethodName = builder.paymentMethodName;
        iconUrl = builder.iconUrl;
        lastFourDigits = builder.lastFourDigits;
        paymentMethodType = builder.paymentMethodType;
        paidAmount = builder.paidAmount;
        discountName = builder.discountName;
        installmentsCount = builder.installmentsCount;
        installmentsAmount = builder.installmentsAmount;
        installmentsRate = builder.installmentsRate;
        consumerCreditsInfo = builder.consumerCreditsInfo;
        description = builder.description;
        installmentsTotalAmount = builder.installmentsTotalAmount;
    }

    protected PaymentInfo(final Parcel in) {
        rawAmount = in.readString();
        paymentMethodName = in.readString();
        iconUrl = in.readString();
        lastFourDigits = in.readString();
        paymentMethodType = PaymentMethodType.values()[in.readInt()];
        paidAmount = in.readString();
        discountName = in.readString();
        installmentsCount = in.readInt();
        installmentsAmount = in.readString();
        installmentsTotalAmount = in.readString();
        if (in.readByte() == 0) {
            installmentsRate = null;
        } else {
            installmentsRate = new BigDecimal(in.readString());
        }
        consumerCreditsInfo = in.readParcelable(ResultInfo.class.getClassLoader());
        description = in.readParcelable(PaymentCongratsText.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(rawAmount);
        dest.writeString(paymentMethodName);
        dest.writeString(iconUrl);
        dest.writeString(lastFourDigits);
        dest.writeInt(paymentMethodType.ordinal());
        dest.writeString(paidAmount);
        dest.writeString(discountName);
        dest.writeInt(installmentsCount);
        dest.writeString(installmentsAmount);
        dest.writeString(installmentsTotalAmount);
        if (installmentsRate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(installmentsRate.toString());
        }
        dest.writeParcelable(consumerCreditsInfo, flags);
        dest.writeParcelable(description, flags);
    }

    public enum PaymentMethodType {
        CREDIT_CARD("credit_card"),
        DEBIT_CARD("debit_card"),
        PREPAID_CARD("prepaid_card"),
        TICKET("ticket"),
        ATM("atm"),
        DIGITAL_CURRENCY("digital_currency"),
        BANK_TRANSFER("bank_transfer"),
        ACCOUNT_MONEY("account_money"),
        PLUGIN("payment_method_plugin"),
        CONSUMER_CREDITS("consumer_credits");

        public final String value;

        PaymentMethodType(final String value) {
            this.value = value;
        }

        public static PaymentMethodType fromName(final String text) {
            for (final PaymentMethodType paymentMethodType : PaymentMethodType.values()) {
                if (paymentMethodType.name().equalsIgnoreCase(text)) {
                    return paymentMethodType;
                }
            }
            throw new IllegalStateException("Invalid paymentMethodType");
        }
    }

    public static class Builder {
        /* default */ String rawAmount;
        /* default */ String paymentMethodName;
        /* default */ String iconUrl;
        /* default */ String lastFourDigits;
        /* default */ PaymentMethodType paymentMethodType;
        /* default */ String paidAmount;
        /* default */ String discountName;
        /* default */ Integer installmentsCount = 0;
        /* default */ String installmentsAmount;
        /* default */ String installmentsTotalAmount;
        /* default */ BigDecimal installmentsRate;
        /* default */ PaymentResultInfo consumerCreditsInfo;
        /* default */ PaymentCongratsText description;

        /**
         * Instantiates a PaymentInfo object
         *
         * @return PaymentInfo
         */
        public PaymentInfo build() {
            return new PaymentInfo(this);
        }

        /**
         * Adds the name of the payment method used to pay
         *
         * @param paymentMethodName the name of the payment method
         * @return Builder
         */
        public Builder withPaymentMethodName(final String paymentMethodName) {
            this.paymentMethodName = paymentMethodName;
            return this;
        }

        /**
         * Adds the url of the payment method's icon used to pay
         *
         * @param iconUrl the url of the payment method's icon
         * @return Builder
         */
        public Builder withIconUrl(final String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        /**
         * Adds the total amount actually paid by the user
         *
         * @param paidAmount the amount actually paid by the user
         * @return Builder
         */
        public Builder withPaidAmount(final String paidAmount) {
            this.paidAmount = paidAmount;
            return this;
        }

        /**
         * Adds the lastFourDigits of the credit/debit card
         *
         * @param lastFourDigits the last 4 digits of the credit or debit card number
         * @return Builder
         */
        public Builder withLastFourDigits(final String lastFourDigits) {
            this.lastFourDigits = lastFourDigits;
            return this;
        }

        /**
         * Adds the type of the payment method used to pay
         *
         * @param paymentMethodType the type of payment method (account money, credit card, debit card, etc)
         * @return Builder
         */
        public Builder withPaymentMethodType(final PaymentMethodType paymentMethodType) {
            this.paymentMethodType = paymentMethodType;
            return this;
        }

        /**
         * Adds the name of the discount to be displayed (e.g.: 20% OFF)
         *
         * @param discountName the text to be displayed showing the discount (e.g.: 20% OFF)
         * @param rawAmount the value of the raw Amount for the payment without discount
         * @return Builder
         */
        public Builder withDiscountData(final String discountName, final String rawAmount) {
            this.discountName = discountName;
            this.rawAmount = rawAmount;
            return this;
        }

        /**
         * Adds the installments info
         *
         * @param installmentsCount number of installments, if there ara non 0 should be passes as param
         * @param installmentsAmount the amount to be paid for each installment
         * @param installmentsTotalAmount the total amount to pay
         * @param installmentsRate the rate/interest of the installments. If its without a rate or interest "0" should
         * @return
         */
        public Builder withInstallmentsData(final Integer installmentsCount, final String installmentsAmount,
            final String installmentsTotalAmount, final BigDecimal installmentsRate) {
            this.installmentsCount = installmentsCount;
            this.installmentsAmount = installmentsAmount;
            this.installmentsTotalAmount = installmentsTotalAmount;
            this.installmentsRate = installmentsRate;
            return this;
        }

        /**
         * Adds info to be displayed about consumerCredits (e.g. the date when the payer will start paying the credit
         * installments)
         *
         * @param consumerCreditsInfo info shown related to consumerCredits
         * @return Builder
         */
        public Builder withConsumerCreditsInfo(final PaymentResultInfo consumerCreditsInfo) {
            this.consumerCreditsInfo = consumerCreditsInfo;
            return this;
        }

        /**
         * Adds extra info to the paymentMethodName
         *
         * @param description info shown related to paymentMethodName
         * @return Builder
         */
        public Builder withDescription(final PaymentCongratsText description) {
            this.description = description;
            return this;
        }
    }
}