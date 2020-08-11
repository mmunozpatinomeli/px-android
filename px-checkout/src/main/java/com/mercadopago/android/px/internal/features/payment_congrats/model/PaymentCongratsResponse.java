package com.mercadopago.android.px.internal.features.payment_congrats.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class PaymentCongratsResponse implements Parcelable {

    public static final PaymentCongratsResponse
        EMPTY = new PaymentCongratsResponse();

    public static final Creator<PaymentCongratsResponse> CREATOR = new Creator<PaymentCongratsResponse>() {
        @Override
        public PaymentCongratsResponse createFromParcel(final Parcel in) {
            return new PaymentCongratsResponse(in);
        }

        @Override
        public PaymentCongratsResponse[] newArray(final int size) {
            return new PaymentCongratsResponse[size];
        }
    };

    @Nullable private final Score score;
    @Nullable private final Discount discount;
    @Nullable private final MoneySplit moneySplit;
    private final List<CrossSelling> crossSellings;
    private final Action viewReceipt;
    private final boolean customOrder;

    public PaymentCongratsResponse(
        @Nullable final Score score,
        @Nullable final Discount discount, @Nullable final
    MoneySplit moneySplit,
        final List<CrossSelling> crossSellings, final Action viewReceipt,
        final boolean customOrder) {
        this.score = score;
        this.discount = discount;
        this.moneySplit = moneySplit;
        this.crossSellings = crossSellings;
        this.viewReceipt = viewReceipt;
        this.customOrder = customOrder;
    }

    private PaymentCongratsResponse() {
        score = null;
        discount = null;
        moneySplit = null;
        crossSellings = Collections.emptyList();
        viewReceipt = null;
        customOrder = false;
    }

    /* default */ PaymentCongratsResponse(final Parcel in) {
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

        private final Score.Progress progress;
        private final String title;
        private final Action action;

        public Score(
            final Progress progress, final String title, final Action action) {
            this.progress = progress;
            this.title = title;
            this.action = action;
        }

        /* default */ Score(final Parcel in) {
            progress = in.readParcelable(Score.Progress.class.getClassLoader());
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

        public Score.Progress getProgress() {
            return progress;
        }

        public String getTitle() {
            return title;
        }

        public Action getAction() {
            return action;
        }

        /* default */ public static final class Progress implements Parcelable {

            public static final Creator<Score.Progress> CREATOR = new Creator<Score.Progress>() {
                @Override
                public Score.Progress createFromParcel(final Parcel in) {
                    return new Score.Progress(in);
                }

                @Override
                public Score.Progress[] newArray(final int size) {
                    return new Score.Progress[size];
                }
            };

            private final float percentage;
            private final String color;
            private final int level;

            public Progress(final float percentage, final String color, final int level) {
                this.percentage = percentage;
                this.color = color;
                this.level = level;
            }

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
        private final Discount.DownloadApp actionDownload;
        private final PXBusinessTouchpoint touchpoint;
        private final List<Discount.Item> items;

        public Discount(final String title, final String subtitle, final Action action,
            final DownloadApp actionDownload,
            final PXBusinessTouchpoint touchpoint,
            final List<Item> items) {
            this.title = title;
            this.subtitle = subtitle;
            this.action = action;
            this.actionDownload = actionDownload;
            this.touchpoint = touchpoint;
            this.items = items;
        }

        /* default */ Discount(final Parcel in) {
            title = in.readString();
            subtitle = in.readString();
            action = in.readParcelable(Action.class.getClassLoader());
            actionDownload = in.readParcelable(
                Discount.DownloadApp.class.getClassLoader());
            touchpoint = in.readParcelable(
                PXBusinessTouchpoint.class.getClassLoader());
            items = in.createTypedArrayList(Discount.Item.CREATOR);
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

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public Action getAction() {
            return action;
        }

        public Discount.DownloadApp getActionDownload() {
            return actionDownload;
        }

        public PXBusinessTouchpoint getTouchpoint() {
            return touchpoint;
        }

        @NonNull
        public List<Discount.Item> getItems() {
            return items != null ? items : Collections.emptyList();
        }

        /* default */ public static final class DownloadApp implements Parcelable {

            public static final Creator<Discount.DownloadApp> CREATOR = new Creator<Discount.DownloadApp>() {
                @Override
                public Discount.DownloadApp createFromParcel(Parcel in) {
                    return new Discount.DownloadApp(in);
                }

                @Override
                public Discount.DownloadApp[] newArray(int size) {
                    return new Discount.DownloadApp[size];
                }
            };
            private final String title;
            private final Action action;

            public DownloadApp(final String title, final Action action) {
                this.title = title;
                this.action = action;
            }

            /* default */ DownloadApp(Parcel in) {
                title = in.readString();
                action = in.readParcelable(Action.class.getClassLoader());
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                dest.writeString(title);
                dest.writeParcelable(action, flags);
            }

            public String getTitle() {
                return title;
            }

            public Action getAction() {
                return action;
            }
        }

        /* default */ public static final class Item implements Parcelable {

            public static final Creator<Discount.Item> CREATOR = new Creator<Discount.Item>() {
                @Override
                public Discount.Item createFromParcel(final Parcel in) {
                    return new Discount.Item(in);
                }

                @Override
                public Discount.Item[] newArray(final int size) {
                    return new Discount.Item[size];
                }
            };

            private final String title;
            private final String subtitle;
            private final String icon;
            private final String target;
            private final String campaignId;

            public Item(final String title, final String subtitle, final String icon, final String target,
                final String campaignId) {
                this.title = title;
                this.subtitle = subtitle;
                this.icon = icon;
                this.target = target;
                this.campaignId = campaignId;
            }

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

        public CrossSelling(final String title, final String icon, final Action action, final String contentId) {
            this.title = title;
            this.icon = icon;
            this.action = action;
            this.contentId = contentId;
        }

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
        @Nullable private final AdditionalEdgeInsets additionalEdgeInsets;

        public PXBusinessTouchpoint(final String id, final String type, final HashMap content,
            @Nullable final HashMap tracking, @Nullable final
        AdditionalEdgeInsets additionalEdgeInsets) {
            this.id = id;
            this.type = type;
            this.content = content;
            this.tracking = tracking;
            this.additionalEdgeInsets = additionalEdgeInsets;
        }

        /* default */ PXBusinessTouchpoint(final Parcel in) {
            id = in.readString();
            type = in.readString();
            content = in.readHashMap(HashMap.class.getClassLoader());
            tracking = in.readHashMap(HashMap.class.getClassLoader());
            additionalEdgeInsets = in.readParcelable(
                AdditionalEdgeInsets.class.getClassLoader());
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

        public AdditionalEdgeInsets(final int top, final int left, final int bottom, final int right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }

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
        public static final Creator<MoneySplit> CREATOR = new Creator<MoneySplit>() {
            @Override
            public MoneySplit createFromParcel(final Parcel in) {
                return new MoneySplit(in);
            }

            @Override
            public MoneySplit[] newArray(final int size) {
                return new MoneySplit[size];
            }
        };
        private final PaymentCongratsText title;
        private final Action action;
        private final String imageUrl;

        public MoneySplit(final PaymentCongratsText title, final Action action, final String imageUrl) {
            this.title = title;
            this.action = action;
            this.imageUrl = imageUrl;
        }

        /* default */ MoneySplit(final Parcel in) {
            title = in.readParcelable(PaymentCongratsText.class.getClassLoader());
            action = in.readParcelable(Action.class.getClassLoader());
            imageUrl = in.readString();
        }

        public PaymentCongratsText getTitle() {
            return title;
        }

        public Action getAction() {
            return action;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(title, flags);
            dest.writeParcelable(action, flags);
            dest.writeString(imageUrl);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    public static final class Action implements Parcelable {

        public static final Creator<Action> CREATOR = new Creator<Action>() {
            @Override
            public Action createFromParcel(final Parcel in) {
                return new Action(in);
            }

            @Override
            public Action[] newArray(final int size) {
                return new Action[size];
            }
        };

        private final String label;
        private final String target;

        public Action(final String label, @Nullable final String target) {
            this.label = label;
            this.target = target;
        }

        /* default */ Action(final Parcel in) {
            label = in.readString();
            target = in.readString();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(label);
            dest.writeString(target);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getLabel() {
            return label;
        }

        @Nullable
        public String getTarget() {
            return target;
        }
    }
}