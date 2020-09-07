package com.mercadopago.android.px.core.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.mercadopago.android.px.configuration.TrackingConfiguration;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.internal.tracking.TrackingRepository;
import com.mercadopago.android.px.tracking.internal.MPTracker;

/**
 * Provides access to card storage flow
 * @deprecated this flow will no longer be available anymore
 */
@Deprecated
@SuppressWarnings("unused")
public final class MercadoPagoCardStorage implements Parcelable {

    public static final Creator<MercadoPagoCardStorage> CREATOR = new Creator<MercadoPagoCardStorage>() {
        @Override
        public MercadoPagoCardStorage createFromParcel(final Parcel in) {
            return new MercadoPagoCardStorage(in);
        }

        @Override
        public MercadoPagoCardStorage[] newArray(final int size) {
            return new MercadoPagoCardStorage[size];
        }
    };

    private final String accessToken;
    private final int requestCode;
    private final boolean skipResultScreen;

    /* default */ MercadoPagoCardStorage(@NonNull final Builder builder) {
        accessToken = builder.accessToken;
        requestCode = builder.requestCode;
        skipResultScreen = builder.skipResultScreen;
    }

    /* default */ MercadoPagoCardStorage(final Parcel in) {
        accessToken = in.readString();
        requestCode = in.readInt();
        skipResultScreen = in.readByte() != 0;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getRequestCode() {
        return requestCode;
    }

    /**
     * Lets us know if we have to skip result screen or not
     *
     * @return true/false to skip Result screen
     */
    public boolean shouldSkipResultScreen() {
        return skipResultScreen;
    }

    /**
     * Starts card storage flow, walking the user through the card details form,
     * with the purpose of storing the card to "My Cards" section, allowing the user to
     * use it anytime he/she wants.
     * <p>
     * The flow can end with a success, returning {@link Activity#RESULT_OK} to the caller,
     * informing that the card association succeeded.
     * <p>
     * In case the flow experiences an error of any kind, the flow will return a {@link Activity#RESULT_CANCELED}
     * informing that the card association failed.
     *
     * @param context Context.
     * @deprecated this flow will no longer be available anymore
     */
    @Deprecated
    public void start(@NonNull final Context context) {
        //start new session id
        final TrackingRepository trackingRepository = Session.getInstance().getConfigurationModule().getTrackingRepository();
        trackingRepository.configure(new TrackingRepository.Model(new TrackingConfiguration.Builder().build().sessionId));
        MPTracker.getInstance().initializeSessionTime();
        GuessingCardActivity.startGuessingCardActivityForStorage(context, this);
    }

    /**
     * Starts card storage flow, walking the user through the card details form,
     * with the purpose of storing the card to "My Cards" section, allowing the user to
     * use it anytime he/she wants.
     * <p>
     * The flow can end with a success, returning {@link Activity#RESULT_OK} to the caller,
     * informing that the card association succeeded.
     * <p>
     * In case the flow experiences an error of any kind, the flow will return a {@link Activity#RESULT_CANCELED}
     * informing that the card association failed.
     *
     * @param context the context that calls the flow
     * @param accessToken logged in user access token
     * @param requestCode it's the number that identifies the checkout flow request for
     * {@link Activity#onActivityResult(int, int, Intent)}
     * @deprecated use {@link #MercadoPagoCardStorage(Builder)}
     */
    @Deprecated
    public static void startCardStorageFlow(@NonNull final Context context, @NonNull final String accessToken,
        final int requestCode) {
        new Builder(accessToken)
            .setRequestCode(requestCode)
            .build()
            .start(context);
    }

    /**
     * Starts card storage flow, walking the user through the card details form, with the purpose of storing the card to
     * "My Cards" section, allowing the user to use it anytime he/she wants.
     * <p>
     * The flow can end with a success, returning {@link Activity#RESULT_OK} to the caller, informing that the card
     * association succeeded.
     * <p>
     * In case the flow experiences an error of any kind, the flow will return a {@link Activity#RESULT_CANCELED}
     * informing that the card association failed.
     *
     * @param callerActivity the activity that calls the flow
     * @param accessToken logged in user access token
     * @param requestCode it's the number that identifies the checkout flow request for {@link
     * Activity#onActivityResult(int, int, Intent)}
     * @deprecated use {@link #MercadoPagoCardStorage(Builder)}
     */
    @Deprecated
    public static void startCardStorageFlow(@NonNull final Activity callerActivity, @NonNull final String accessToken,
        final int requestCode) {
        new Builder(accessToken)
            .setRequestCode(requestCode)
            .build()
            .start(callerActivity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(accessToken);
        dest.writeInt(requestCode);
        dest.writeByte((byte) (skipResultScreen ? 1 : 0));
    }

    /**
     * @deprecated this flow will no longer be available anymore
     */
    @Deprecated
    public static final class Builder {

        /* default */ final String accessToken;
        /* default */ int requestCode;
        /* default */ boolean skipResultScreen;

        public Builder(@NonNull final String accessToken) {
            this.accessToken = accessToken;
        }

        /**
         * Set the request code.
         *
         * @param requestCode Request code.
         * @return Builder to keep operating
         */
        public Builder setRequestCode(final int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        /**
         * Sets if should skip result screen or not
         *
         * @param skipResultScreen Skip congrats.
         * @return Builder to keep operating
         */
        public Builder setSkipResultScreen(final boolean skipResultScreen) {
            this.skipResultScreen = skipResultScreen;
            return this;
        }

        /**
         * @return {@link MercadoPagoCardStorage} instance
         * @deprecated this flow will no longer be available anymore
         */
        @Deprecated
        public MercadoPagoCardStorage build() {
            return new MercadoPagoCardStorage(this);
        }
    }
}