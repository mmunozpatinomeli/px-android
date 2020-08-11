package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.CustomTextsRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.util.ChargeRuleHelper;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryDetailDescriptorFactory;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.SummaryViewDefaultColor;
import com.mercadopago.android.px.internal.viewmodel.TotalLocalized;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import com.mercadopago.android.px.model.internal.ExpressPaymentMethod;
import com.mercadopago.android.px.model.internal.SummaryInfo;
import java.util.List;
import java.util.Objects;

public class SummaryViewModelMapper extends CacheableMapper<ExpressPaymentMethod, SummaryView.Model,
    SummaryViewModelMapper.Key> {

    @NonNull private final Currency currency;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final ElementDescriptorView.Model elementDescriptorModel;
    @NonNull private final AmountDescriptorView.OnClickListener listener;
    @NonNull private final SummaryInfo summaryInfo;
    @NonNull private final ChargeRepository chargeRepository;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull private final CustomTextsRepository customTextsRepository;

    public SummaryViewModelMapper(@NonNull final Currency currency,
        @NonNull final DiscountRepository discountRepository, @NonNull final AmountRepository amountRepository,
        @NonNull final ElementDescriptorView.Model elementDescriptorModel,
        @NonNull final AmountDescriptorView.OnClickListener listener,
        @NonNull final SummaryInfo summaryInfo, @NonNull final ChargeRepository chargeRepository,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository,
        @NonNull final CustomTextsRepository customTextsRepository) {
        this.currency = currency;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.elementDescriptorModel = elementDescriptorModel;
        this.listener = listener;
        this.summaryInfo = summaryInfo;
        this.chargeRepository = chargeRepository;
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.customTextsRepository = customTextsRepository;
    }

    @Override
    protected Key getKey(
        @NonNull final ExpressPaymentMethod expressPaymentMethod) {
        final PaymentTypeChargeRule chargeRule =
            chargeRepository.getChargeRule(expressPaymentMethod.getPaymentTypeId());
        final AmountConfiguration amountConfiguration = getAmountConfiguration(expressPaymentMethod);
        final boolean hasSplit = amountConfiguration != null && amountConfiguration.allowSplit();

        return new Key(discountRepository.getConfigurationFor(expressPaymentMethod.getCustomOptionId()),
            chargeRule, hasSplit);
    }

    @Override
    public SummaryView.Model map(@NonNull final ExpressPaymentMethod expressPaymentMethod) {
        return createModel(expressPaymentMethod.getPaymentTypeId(),
            discountRepository.getConfigurationFor(expressPaymentMethod.getCustomOptionId()),
            getAmountConfiguration(expressPaymentMethod));
    }

    @Nullable
    private AmountConfiguration getAmountConfiguration(@NonNull final ExpressPaymentMethod expressPaymentMethod) {
        return amountConfigurationRepository.getConfigurationFor(expressPaymentMethod.getCustomOptionId());
    }

    @NonNull
    private SummaryView.Model createModel(@NonNull final String paymentTypeId,
        @NonNull final DiscountConfigurationModel discountModel,
        @Nullable final AmountConfiguration amountConfiguration) {
        final PaymentTypeChargeRule chargeRule = chargeRepository.getChargeRule(paymentTypeId);
        final List<AmountDescriptorView.Model> summaryDetailList =
            new SummaryDetailDescriptorFactory(listener, discountModel, amountRepository, summaryInfo, currency,
                chargeRule, amountConfiguration).create();

        final AmountDescriptorView.Model totalRow = new AmountDescriptorView.Model(
            new TotalLocalized(customTextsRepository),
            new AmountLocalized(amountRepository.getAmountToPay(paymentTypeId, discountModel), currency),
            new SummaryViewDefaultColor());

        return new SummaryView.Model(elementDescriptorModel, summaryDetailList, totalRow);
    }

    static final class Key {
        private final DiscountConfigurationModel discountConfigurationModel;
        private final PaymentTypeChargeRule paymentTypeChargeRule;
        private final Boolean hasSplit;

        Key(@NonNull final DiscountConfigurationModel discountConfigurationModel,
            final PaymentTypeChargeRule paymentTypeChargeRule, final boolean hasSplit) {
            this.discountConfigurationModel = discountConfigurationModel;
            this.paymentTypeChargeRule = ChargeRuleHelper.isHighlightCharge(paymentTypeChargeRule) ? null : paymentTypeChargeRule;
            this.hasSplit = hasSplit;
        }

        @Override
        public int hashCode() {
            return (discountConfigurationModel == null ? 0 : discountConfigurationModel.hashCode()) ^
                (paymentTypeChargeRule == null ? 0 : paymentTypeChargeRule.hashCode()) ^
                (hasSplit == null ? 0 : hasSplit.hashCode());
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key k = (Key) obj;
            return Objects.equals(k.discountConfigurationModel, discountConfigurationModel) && Objects.equals(k.paymentTypeChargeRule, paymentTypeChargeRule)
                && Objects.equals(k.hasSplit, hasSplit);
        }
    }
}