package com.mercadopago.android.px.internal.features.payment_congrats.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    @Nullable public final String lastFourDigits;
    @NonNull public final String paymentMethodId;
    @NonNull public final PaymentMethodType paymentMethodType;

    protected PaymentInfo(final Builder builder) {
        rawAmount = builder.rawAmount;
        paymentMethodName = builder.paymentMethodName;
        lastFourDigits = builder.lastFourDigits;
        paymentMethodId = builder.paymentMethodId;
        paymentMethodType = builder.paymentMethodType;
    }

    protected PaymentInfo(final Parcel in) {
        rawAmount = in.readString();
        paymentMethodName = in.readString();
        lastFourDigits = in.readString();
        paymentMethodId = in.readString();
        paymentMethodType = PaymentMethodType.fromName(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(rawAmount);
        dest.writeString(paymentMethodName);
        dest.writeString(lastFourDigits);
        dest.writeString(paymentMethodId);
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
        PLUGIN("payment_method_plugin");

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
            throw new IllegalStateException("Invalid congratsType");
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Builder {
        /* default */ String rawAmount;
        /* default */ String paymentMethodName;
        /* default */ String lastFourDigits;
        /* default */ String paymentMethodId;
        /* default */ PaymentMethodType paymentMethodType;

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
         * Adds the raw amount of the payment
         *
         * @param rawAmount the value of the raw Amount for the payment
         * @return Builder
         */
        public Builder withRawAmount(final String rawAmount) {
            this.rawAmount = rawAmount;
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
         * Adds the id of the payment method used to pay
         *
         * @param paymentMethodId the id of the payment method
         * @return Builder
         */
        public Builder withPaymentMethodId(final String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
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
    }
}
