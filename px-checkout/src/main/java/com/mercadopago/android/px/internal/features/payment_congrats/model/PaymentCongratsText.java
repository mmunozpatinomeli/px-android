package com.mercadopago.android.px.internal.features.payment_congrats.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentCongratsText implements Parcelable {

    public static final Parcelable.Creator<PaymentCongratsText>
        CREATOR = new Parcelable.Creator<PaymentCongratsText>() {
        @Override
        public PaymentCongratsText createFromParcel(final Parcel in) {
            return new PaymentCongratsText(in);
        }

        @Override
        public PaymentCongratsText[] newArray(final int size) {
            return new PaymentCongratsText[size];
        }
    };

    public static final PaymentCongratsText EMPTY = new PaymentCongratsText();
    private final String message;
    private final String backgroundColor;
    private final String textColor;
    private final String weight;

    public PaymentCongratsText(final String message, final String backgroundColor, final String textColor, final String weight) {
        this.message = message;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.weight = weight;
    }

    private PaymentCongratsText() {
        message = null;
        backgroundColor = null;
        textColor = null;
        weight = null;
    }

    /* default */ PaymentCongratsText(final Parcel in) {
        message = in.readString();
        backgroundColor = in.readString();
        textColor = in.readString();
        weight = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(message);
        dest.writeString(backgroundColor);
        dest.writeString(textColor);
        dest.writeString(weight);
    }

    public String getMessage() {
        return message;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getWeight() {
        return weight;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
