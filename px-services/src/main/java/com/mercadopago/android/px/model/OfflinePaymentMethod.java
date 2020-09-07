package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.model.internal.Text;
import java.io.Serializable;

public final class OfflinePaymentMethod implements Parcelable, Serializable {

    private final String id;
    private final String instructionId;
    private final Text name;
    private final Text description;
    @SerializedName("has_additional_info_needed")
    private final boolean additionalInfoNeeded;
    private final StatusMetadata status;
    private final String imageUrl;

    public static final Creator<OfflinePaymentMethod> CREATOR = new Creator<OfflinePaymentMethod>() {
        @Override
        public OfflinePaymentMethod createFromParcel(final Parcel in) {
            return new OfflinePaymentMethod(in);
        }

        @Override
        public OfflinePaymentMethod[] newArray(final int size) {
            return new OfflinePaymentMethod[size];
        }
    };

    protected OfflinePaymentMethod(final Parcel in) {
        id = in.readString();
        instructionId = in.readString();
        name = in.readParcelable(Text.class.getClassLoader());
        description = in.readParcelable(Text.class.getClassLoader());
        additionalInfoNeeded = in.readByte() != 0;
        status = in.readParcelable(StatusMetadata.class.getClassLoader());
        imageUrl = in.readString();
    }

    public String getId() {
        return id;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public Text getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    public boolean isAdditionalInfoNeeded() {
        return additionalInfoNeeded;
    }

    public StatusMetadata getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeString(instructionId);
        dest.writeParcelable(name, flags);
        dest.writeParcelable(description, flags);
        dest.writeByte((byte) (additionalInfoNeeded ? 1 : 0));
        dest.writeParcelable(status, flags);
        dest.writeString(imageUrl);
    }
}