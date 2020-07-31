package com.mercadopago.android.px.internal.features.business_result;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsResponse;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.internal.Action;
import com.mercadopago.android.px.model.internal.CongratsResponse;
import java.util.ArrayList;
import java.util.List;

public class CongratsResponseMapper extends Mapper<CongratsResponse, PaymentCongratsResponse> {


    @Override
    public PaymentCongratsResponse map(CongratsResponse congratsResponse) {
        return new PaymentCongratsResponse(getScore(congratsResponse.getScore()),
            getDiscount(congratsResponse.getDiscount()), getMoneySplit(congratsResponse.getMoneySplit()),
            getCrossSelling(congratsResponse.getCrossSellings()),
            getAction(congratsResponse.getViewReceipt()), congratsResponse.hasCustomOrder());
    }

    private PaymentCongratsResponse.Score getScore(final CongratsResponse.Score score) {
        if (score != null) {
            return new PaymentCongratsResponse.Score(
                new PaymentCongratsResponse.Score.Progress(score.getProgress().getPercentage(),
                    score.getProgress().getColor(), score.getProgress().getLevel()), score.getTitle(),
                getAction(score.getAction()));
        }
        return null;
    }

    private List<PaymentCongratsResponse.CrossSelling> getCrossSelling(
        final Iterable<CongratsResponse.CrossSelling> crossSellings) {
        final List<PaymentCongratsResponse.CrossSelling> crossSellingList = new ArrayList<>();
        for (final CongratsResponse.CrossSelling crossSelling : crossSellings) {
            crossSellingList
                .add(new PaymentCongratsResponse.CrossSelling(crossSelling.getTitle(), crossSelling.getIcon(),
                    getAction(crossSelling.getAction()), crossSelling.getContentId()));
        }
        return crossSellingList;
    }

    private PaymentCongratsResponse.Discount getDiscount(final CongratsResponse.Discount discount) {
        if (discount != null) {
            PaymentCongratsResponse.AdditionalEdgeInsets additionalEdgeInsets;
            PaymentCongratsResponse.PXBusinessTouchpoint touchpoint;
            if (discount.getTouchpoint() != null) {
                additionalEdgeInsets =
                    discount.getTouchpoint().getAdditionalEdgeInsets() == null ? null :
                        new PaymentCongratsResponse.AdditionalEdgeInsets(
                            discount.getTouchpoint().getAdditionalEdgeInsets().getTop(),
                            discount.getTouchpoint().getAdditionalEdgeInsets().getLeft(),
                            discount.getTouchpoint().getAdditionalEdgeInsets().getBottom(),
                            discount.getTouchpoint().getAdditionalEdgeInsets().getRight());
                touchpoint =
                    new PaymentCongratsResponse.PXBusinessTouchpoint(discount.getTouchpoint().getId(),
                        discount.getTouchpoint().getType(), discount.getTouchpoint().getContent(),
                        discount.getTouchpoint().getTracking(), additionalEdgeInsets);
            } else {
                additionalEdgeInsets = null;
                touchpoint = null;
            }

            return new PaymentCongratsResponse.Discount(discount.getTitle(), discount.getSubtitle(),
                getAction(discount.getAction())
                , new PaymentCongratsResponse.Discount.DownloadApp(discount.getActionDownload().getTitle(),
                getAction(discount.getActionDownload().getAction())),
                touchpoint, getDiscountItems(discount.getItems()));
        }
        return null;
    }

    @Nullable
    private PaymentCongratsResponse.MoneySplit getMoneySplit(@Nullable final CongratsResponse.MoneySplit moneySplit) {
        if (moneySplit != null) {
            return moneySplit == null ? null
                : new PaymentCongratsResponse.MoneySplit(
                    new PaymentCongratsResponse.Text(moneySplit.getTitle().getMessage(),
                        moneySplit.getTitle().getBackgroundColor(), moneySplit.getTitle().getTextColor(),
                        moneySplit.getTitle().getWeight()), getAction(moneySplit.getAction()),
                    moneySplit.getImageUrl());
        }
        return null;
    }

    @NonNull
    private PaymentCongratsResponse.Action getAction(final Action action) {
        if (action != null) {
            return new PaymentCongratsResponse.Action(action.getLabel(), action.getTarget());
        }
        return null;
    }

    private List<PaymentCongratsResponse.Discount.Item> getDiscountItems(
        final List<CongratsResponse.Discount.Item> items) {
        final List<PaymentCongratsResponse.Discount.Item> discountItems = new ArrayList<>();
        for (final CongratsResponse.Discount.Item item : items) {
            discountItems.add(
                new PaymentCongratsResponse.Discount.Item(item.getTitle(), item.getSubtitle(), item.getIcon(),
                    item.getTarget(), item.getCampaignId()));
        }
        return discountItems;
    }
}