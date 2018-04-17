package com.mercadopago.plugins.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.mercadopago.R;
import com.mercadopago.util.TextUtils;

public final class BusinessPayment implements PluginPayment, Parcelable {

    private final String help;
    private final int iconId;
    private final String title;
    private final Status status;
    private final boolean shouldShowPaymentMethod;
    private final ExitAction exitActionPrimary;
    private final ExitAction exitActionSecondary;
    private final String statementDescription;
    private final String receiptId;

    private BusinessPayment(Builder builder) {
        help = builder.help;
        title = builder.title;
        status = builder.status;
        iconId = builder.iconId;
        shouldShowPaymentMethod = builder.shouldShowPaymentMethod;
        exitActionPrimary = builder.buttonPrimary;
        exitActionSecondary = builder.buttonSecondary;
        statementDescription = builder.statementDescription;
        receiptId = builder.receiptId;
    }

    protected BusinessPayment(Parcel in) {
        iconId = in.readInt();
        title = in.readString();
        shouldShowPaymentMethod = in.readByte() != 0;
        exitActionPrimary = in.readParcelable(ExitAction.class.getClassLoader());
        exitActionSecondary = in.readParcelable(ExitAction.class.getClassLoader());
        status = Status.fromName(in.readString());
        help = in.readString();
        statementDescription = in.readString();
        receiptId = in.readString();
    }

    public static final Creator<BusinessPayment> CREATOR = new Creator<BusinessPayment>() {
        @Override
        public BusinessPayment createFromParcel(Parcel in) {
            return new BusinessPayment(in);
        }

        @Override
        public BusinessPayment[] newArray(int size) {
            return new BusinessPayment[size];
        }
    };

    @Override
    public void process(final Processor processor) {
        processor.process(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(iconId);
        dest.writeString(title);
        dest.writeByte((byte) (shouldShowPaymentMethod ? 1 : 0));
        dest.writeParcelable(exitActionPrimary, flags);
        dest.writeParcelable(exitActionSecondary, flags);
        dest.writeString(status.name);
        dest.writeString(help);
        dest.writeString(statementDescription);
        dest.writeString(receiptId);
    }

    public boolean hasReceipt() {
        return receiptId != null;
    }

    public Status getStatus() {
        return status;
    }

    public int getIcon() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasHelp() {
        return TextUtils.isNotEmpty(help);
    }

    public ExitAction getSecondaryAction() {
        return exitActionSecondary;
    }

    public ExitAction getPrimaryAction() {
        return exitActionPrimary;
    }

    public String getHelp() {
        return help;
    }

    public boolean shouldShowPaymentMethod() {
        return shouldShowPaymentMethod;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public String getReceipt() {
        return receiptId;
    }

    public enum Status {
        APPROVED("approved", R.color.mpsdk_green_payment_result_background, R.drawable.mpsdk_badge_check, 0),
        REJECTED("rejected", R.color.mpsdk_red_payment_result_background, R.drawable.mpsdk_badge_error, R.string.mpsdk_rejection_label),
        PENDING("pending", R.color.mpsdk_orange_payment_result_background, R.drawable.mpsdk_badge_pending_orange, 0);

        public final String name;
        public final int resColor;
        public final int badge;
        public final int message;

        Status(final String name,
               @ColorRes final int resColor,
               @DrawableRes final int badge,
               @StringRes final int message) {
            this.name = name;
            this.resColor = resColor;
            this.badge = badge;
            this.message = message;
        }

        public static Status fromName(String text) {
            for (Status s : Status.values()) {
                if (s.name.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalStateException("Invalid status");
        }
    }


    public static class Builder {

        // Mandatory values
        @NonNull
        private final Status status;
        @DrawableRes
        private final int iconId;
        @NonNull
        private final String title;

        // Optional values
        private boolean shouldShowPaymentMethod;
        private String statementDescription;
        private ExitAction buttonPrimary;
        private ExitAction buttonSecondary;
        private String help;
        private String receiptId;

        public Builder(@NonNull Status status,
                       @DrawableRes int iconId,
                       @NonNull String title) {
            this.title = title;
            this.status = status;
            this.iconId = iconId;
            shouldShowPaymentMethod = false;
            buttonPrimary = null;
            buttonSecondary = null;
            help = null;
            receiptId = null;
        }

        public BusinessPayment build() {
            if (buttonPrimary == null && buttonSecondary == null)
                throw new IllegalStateException("At least one button should be provided for BusinessPayment");
            return new BusinessPayment(this);
        }

        /**
         * if Exit action is set, then a big primary button
         * will appear and the click action will trigger a resCode
         * that will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setPrimaryButton(@Nullable ExitAction exitAction) {
            buttonPrimary = exitAction;
            return this;
        }

        /**
         * if Exit action is set, then a small secondary button
         * will appear and the click action will trigger a resCode
         * that will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setSecondaryButton(@Nullable ExitAction exitAction) {
            buttonSecondary = exitAction;
            return this;
        }

        /**
         * if help is set, then a small box with help instructions will appear
         *
         * @param help a help message
         * @return builder
         */
        public Builder setHelp(@Nullable String help) {
            this.help = help;
            return this;
        }

        /**
         * If value true is set, then payment method box
         * will appear with the amount value and payment method
         * options that were selected by the user.
         *
         * @param visible visibility mode
         * @return builder
         */
        public Builder setPaymentMethodVisibility(boolean visible) {
            this.shouldShowPaymentMethod = visible;
            return this;
        }

        /**
         * If value true is set on {@link #setPaymentMethodVisibility }
         * and the payment method is credit card
         * then the statementDescription will be shown on payment method view.
         *
         * @param statementDescription disclaimer text
         * @return builder
         */
        public Builder setStatementDescription(final String statementDescription) {
            this.statementDescription = statementDescription;
            return this;
        }

        /**
         * If value is set, then receipt view will appear.
         *
         * @param receiptId the receipt id to be shown.
         * @return builder
         */
        public Builder setReceiptId(final String receiptId) {
            this.receiptId = receiptId;
            return this;
        }
    }


}