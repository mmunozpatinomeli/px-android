package com.mercadopago.android.px.model.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class CongratsResponse implements Parcelable {

    public static final CongratsResponse EMPTY = new CongratsResponse();

    public static final Creator<CongratsResponse> CREATOR = new Creator<CongratsResponse>() {
        @Override
        public CongratsResponse createFromParcel(final Parcel in) {
            return new CongratsResponse(in);
        }

        @Override
        public CongratsResponse[] newArray(final int size) {
            return new CongratsResponse[size];
        }
    };

    @SerializedName("mpuntos")
    @Nullable private final Score score;
    @SerializedName("discounts")
    @Nullable private final Discount discount;
    @SerializedName("expense_split")
    @Nullable private final MoneySplit moneySplit;
    @SerializedName("cross_selling")
    private final List<CrossSelling> crossSellings;
    private final Action viewReceipt;
    private final boolean customOrder;

    private CongratsResponse() {
        score = null;
        discount = null;
        moneySplit = null;
        crossSellings = Collections.emptyList();
        viewReceipt = null;
        customOrder = false;
    }

    /* default */ CongratsResponse(final Parcel in) {
        score = in.readParcelable(Score.class.getClassLoader());
        discount = in.readParcelable(Discount.class.getClassLoader());
        moneySplit = in.readParcelable(MoneySplit.class.getClassLoader());
        crossSellings = in.createTypedArrayList(CrossSelling.CREATOR);
        viewReceipt = in.readParcelable(Action.class.getClassLoader());
        customOrder = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(score, flags);
        dest.writeParcelable(discount, flags);
        dest.writeParcelable(moneySplit, flags);
        dest.writeTypedList(crossSellings);
        dest.writeParcelable(viewReceipt, flags);
        dest.writeInt(customOrder ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Nullable
    public Score getScore() {
        return score;
    }

    @Nullable
    public Discount getDiscount() {
        return discount;
    }

    @Nullable
    public MoneySplit getMoneySplit() {
        return moneySplit;
    }

    @NonNull
    public List<CrossSelling> getCrossSellings() {
        return crossSellings != null ? crossSellings : Collections.emptyList();
    }

    @Nullable
    public Action getViewReceipt() {
        return viewReceipt;
    }

    public boolean hasCustomOrder() {
        return customOrder;
    }

    /* default */public static final class Score implements Parcelable {

        public static final Creator<Score> CREATOR = new Creator<Score>() {
            @Override
            public Score createFromParcel(final Parcel in) {
                return new Score(in);
            }

            @Override
            public Score[] newArray(final int size) {
                return new Score[size];
            }
        };

        private final Progress progress;
        private final String title;
        private final Action action;

        /* default */ Score(final Parcel in) {
            progress = in.readParcelable(Progress.class.getClassLoader());
            title = in.readString();
            action = in.readParcelable(Action.class.getClassLoader());
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(progress, flags);
            dest.writeString(title);
            dest.writeParcelable(action, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /* default */ public static final class Progress implements Parcelable {

            public static final Creator<Progress> CREATOR = new Creator<Progress>() {
                @Override
                public Progress createFromParcel(final Parcel in) {
                    return new Progress(in);
                }

                @Override
                public Progress[] newArray(final int size) {
                    return new Progress[size];
                }
            };

            private final float percentage;
            @SerializedName("level_color")
            private final String color;
            @SerializedName("level_number")
            private final int level;

            /* default */ Progress(final Parcel in) {
                percentage = in.readFloat();
                color = in.readString();
                level = in.readInt();
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                dest.writeFloat(percentage);
                dest.writeString(color);
                dest.writeInt(level);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public float getPercentage() {
                return percentage;
            }

            public String getColor() {
                return color;
            }

            public int getLevel() {
                return level;
            }
        }

        public Progress getProgress() {
            return progress;
        }

        public String getTitle() {
            return title;
        }

        public Action getAction() {
            return action;
        }
    }

    /* default */ public static final class Discount implements Parcelable {

        public static final Creator<Discount> CREATOR = new Creator<Discount>() {
            @Override
            public Discount createFromParcel(final Parcel in) {
                return new Discount(in);
            }

            @Override
            public Discount[] newArray(final int size) {
                return new Discount[size];
            }
        };

        private final String title;
        private final String subtitle;
        private final Action action;
        @SerializedName("action_download")
        private final DownloadApp actionDownload;
        private final PXBusinessTouchpoint touchpoint;
        private final List<Item> items;


        /* default */ Discount(final Parcel in) {
            title = in.readString();
            subtitle = in.readString();
            action = in.readParcelable(Action.class.getClassLoader());
            actionDownload = in.readParcelable(DownloadApp.class.getClassLoader());
            touchpoint = in.readParcelable(PXBusinessTouchpoint.class.getClassLoader());
            items = in.createTypedArrayList(Item.CREATOR);
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(title);
            dest.writeString(subtitle);
            dest.writeParcelable(action, flags);
            dest.writeParcelable(actionDownload, flags);
            dest.writeParcelable(touchpoint, flags);
            dest.writeTypedList(items);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /* default */ public static final class DownloadApp implements Parcelable {

            public static final Creator<DownloadApp> CREATOR = new Creator<DownloadApp>() {
                @Override
                public DownloadApp createFromParcel(Parcel in) {
                    return new DownloadApp(in);
                }

                @Override
                public DownloadApp[] newArray(int size) {
                    return new DownloadApp[size];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                dest.writeString(title);
                dest.writeParcelable(action, flags);
            }

            /* default */ DownloadApp(Parcel in) {
                title = in.readString();
                action = in.readParcelable(Action.class.getClassLoader());
            }

            private final String title;
            private final Action action;

            public String getTitle() {
                return title;
            }

            public Action getAction() {
                return action;
            }
        }

        /* default */ public static final class Item implements Parcelable {

            public static final Creator<Item> CREATOR = new Creator<Item>() {
                @Override
                public Item createFromParcel(final Parcel in) {
                    return new Item(in);
                }

                @Override
                public Item[] newArray(final int size) {
                    return new Item[size];
                }
            };

            private final String title;
            private final String subtitle;
            private final String icon;
            private final String target;
            private final String campaignId;

            /* default */ Item(final Parcel in) {
                title = in.readString();
                subtitle = in.readString();
                icon = in.readString();
                target = in.readString();
                campaignId = in.readString();
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                dest.writeString(title);
                dest.writeString(subtitle);
                dest.writeString(icon);
                dest.writeString(target);
                dest.writeString(campaignId);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public String getTitle() {
                return title;
            }

            public String getSubtitle() {
                return subtitle;
            }

            public String getIcon() {
                return icon;
            }

            public String getTarget() {
                return target;
            }

            public String getCampaignId() {
                return campaignId;
            }
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public Action getAction() {
            return action;
        }

        public DownloadApp getActionDownload() {
            return actionDownload;
        }

        public PXBusinessTouchpoint getTouchpoint() {
            return touchpoint;
        }

        @NonNull
        public List<Item> getItems() {
            return items != null ? items : Collections.emptyList();
        }
    }

    /* default */ public static final class CrossSelling implements Parcelable {

        public static final Creator<CrossSelling> CREATOR = new Creator<CrossSelling>() {
            @Override
            public CrossSelling createFromParcel(final Parcel in) {
                return new CrossSelling(in);
            }

            @Override
            public CrossSelling[] newArray(final int size) {
                return new CrossSelling[size];
            }
        };

        private final String title;
        private final String icon;
        private final Action action;
        private final String contentId;

        /* default */ CrossSelling(final Parcel in) {
            title = in.readString();
            icon = in.readString();
            action = in.readParcelable(Action.class.getClassLoader());
            contentId = in.readString();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(title);
            dest.writeString(icon);
            dest.writeParcelable(action, flags);
            dest.writeString(contentId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }

        public Action getAction() {
            return action;
        }

        public String getContentId() {
            return contentId;
        }
    }

    /* default */ public static final class PXBusinessTouchpoint implements Parcelable {

        public static final Creator<PXBusinessTouchpoint> CREATOR = new Creator<PXBusinessTouchpoint>() {
            @Override
            public PXBusinessTouchpoint createFromParcel(final Parcel in) {
                return new PXBusinessTouchpoint(in);
            }

            @Override
            public PXBusinessTouchpoint[] newArray(final int size) {
                return new PXBusinessTouchpoint[size];
            }
        };

        private final String id;
        private final String type;
        private final HashMap content;
        @Nullable private final HashMap tracking;
        @SerializedName("additional_edge_insets")
        @Nullable private final AdditionalEdgeInsets additionalEdgeInsets;


        /* default */ PXBusinessTouchpoint(final Parcel in) {
            id = in.readString();
            type = in.readString();
            content = in.readHashMap(HashMap.class.getClassLoader());
            tracking = in.readHashMap(HashMap.class.getClassLoader());
            additionalEdgeInsets = in.readParcelable(AdditionalEdgeInsets.class.getClassLoader());
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(id);
            dest.writeString(type);
            dest.writeMap(content);
            dest.writeMap(tracking);
            dest.writeParcelable(additionalEdgeInsets, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public HashMap getContent() {
            return content;
        }

        @Nullable
        public HashMap getTracking() {
            return tracking;
        }

        @Nullable
        public AdditionalEdgeInsets getAdditionalEdgeInsets() {
            return additionalEdgeInsets;
        }
    }

    /* default */ public static final class AdditionalEdgeInsets implements Parcelable {

        public static final Creator<AdditionalEdgeInsets> CREATOR = new Creator<AdditionalEdgeInsets>() {
            @Override
            public AdditionalEdgeInsets createFromParcel(final Parcel in) {
                return new AdditionalEdgeInsets(in);
            }

            @Override
            public AdditionalEdgeInsets[] newArray(final int size) {
                return new AdditionalEdgeInsets[size];
            }
        };

        private final int top;
        private final int left;
        private final int bottom;
        private final int right;

        /* default */ AdditionalEdgeInsets(final Parcel in) {
            top = in.readInt();
            left = in.readInt();
            bottom = in.readInt();
            right = in.readInt();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(top);
            dest.writeInt(left);
            dest.writeInt(bottom);
            dest.writeInt(right);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public int getTop() {
            return top;
        }

        public int getLeft() {
            return left;
        }

        public int getBottom() {
            return bottom;
        }

        public int getRight() {
            return right;
        }
    }

    public static final class MoneySplit implements Parcelable {
        private final Text title;
        private final Action action;
        private final String imageUrl;

        public Text getTitle() {
            return title;
        }

        public Action getAction() {
            return action;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        /* default */ MoneySplit(Parcel in) {
            title = in.readParcelable(Text.class.getClassLoader());
            action = in.readParcelable(Action.class.getClassLoader());
            imageUrl = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(title, flags);
            dest.writeParcelable(action, flags);
            dest.writeString(imageUrl);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<MoneySplit> CREATOR = new Creator<MoneySplit>() {
            @Override
            public MoneySplit createFromParcel(Parcel in) {
                return new MoneySplit(in);
            }

            @Override
            public MoneySplit[] newArray(int size) {
                return new MoneySplit[size];
            }
        };
    }
}