package com.mercadopago.android.px.internal.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.util.ChargeRuleHelper;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.ChargeLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountDetailColor;
import com.mercadopago.android.px.internal.viewmodel.IDetailColor;
import com.mercadopago.android.px.internal.viewmodel.ItemLocalized;
import com.mercadopago.android.px.internal.viewmodel.SummaryViewDefaultColor;
import com.mercadopago.android.px.internal.viewmodel.SummaryViewDetailDrawable;
import com.mercadopago.android.px.internal.viewmodel.mappers.AmountDescriptorMapper;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import com.mercadopago.android.px.model.DiscountOverview;
import com.mercadopago.android.px.model.internal.SummaryInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SummaryDetailDescriptorFactory {

    @NonNull private final AmountDescriptorView.OnClickListener listener;
    @NonNull private final DiscountConfigurationModel discountModel;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final SummaryInfo summaryInfo;
    @NonNull private final Currency currency;
    @Nullable private final PaymentTypeChargeRule chargeRule;
    @Nullable private final AmountConfiguration amountConfiguration;

    public SummaryDetailDescriptorFactory(@NonNull final AmountDescriptorView.OnClickListener listener,
        @NonNull final DiscountConfigurationModel discountModel, @NonNull final AmountRepository amountRepository,
        @NonNull final SummaryInfo summaryInfo, @NonNull final Currency currency,
        @Nullable final PaymentTypeChargeRule chargeRule, @Nullable final AmountConfiguration amountConfiguration) {
        this.listener = listener;
        this.discountModel = discountModel;
        this.amountRepository = amountRepository;
        this.summaryInfo = summaryInfo;
        this.currency = currency;
        this.chargeRule = chargeRule;
        this.amountConfiguration = amountConfiguration;
    }

    public List<AmountDescriptorView.Model> create() {
        final List<AmountDescriptorView.Model> list = new ArrayList<>();

        addDiscountRow(list);
        if (chargeRule != null && !ChargeRuleHelper.isHighlightCharge(chargeRule)) {
            addChargesRow(list);
        }
        addPurchaseRow(list);

        return list;
    }

    private void addDiscountRow(@NonNull final Collection<AmountDescriptorView.Model> list) {
        final DiscountOverview discountOverview = discountModel.getDiscountOverview();
        final IDetailColor detailColor = new DiscountDetailColor();
        final boolean hasSplit = amountConfiguration != null && amountConfiguration.allowSplit();
        if (discountOverview != null) {
            list.add(new AmountDescriptorView.Model(new AmountDescriptorMapper().map(discountOverview), detailColor,
                hasSplit)
                .setDetailDrawable(new SummaryViewDetailDrawable(), detailColor)
                .setListener(v -> listener.onDiscountAmountDescriptorClicked(discountModel)));
        }
    }

    private void addChargesRow(@NonNull final Collection<AmountDescriptorView.Model> list) {
        final AmountDescriptorView.Model model = new AmountDescriptorView.Model(new ChargeLocalized(summaryInfo),
            new AmountLocalized(chargeRule.charge(), currency), new SummaryViewDefaultColor());
        if (chargeRule.hasDetailModal()) {
            model.setDetailDrawable(new SummaryViewDetailDrawable(), new SummaryViewDefaultColor())
                .setListener(v -> listener.onChargesAmountDescriptorClicked(chargeRule.getDetailModal()));
        }
        list.add(model);
    }

    private void addPurchaseRow(@NonNull final List<AmountDescriptorView.Model> list) {
        if (!list.isEmpty()) {
            list.add(0, new AmountDescriptorView.Model(new ItemLocalized(summaryInfo),
                new AmountLocalized(amountRepository.getItemsAmount(), currency), new SummaryViewDefaultColor()));
        }
    }
}