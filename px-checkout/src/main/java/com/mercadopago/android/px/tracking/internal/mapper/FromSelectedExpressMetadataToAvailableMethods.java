package com.mercadopago.android.px.tracking.internal.mapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.AccountMoneyMetadata;
import com.mercadopago.android.px.model.BenefitsMetadata;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.tracking.internal.model.AccountMoneyExtraInfo;
import com.mercadopago.android.px.tracking.internal.model.AvailableMethod;
import com.mercadopago.android.px.tracking.internal.model.CardExtraExpress;
import com.mercadopago.android.px.tracking.internal.model.CreditsExtraInfo;
import com.mercadopago.android.px.tracking.internal.model.PayerCostInfo;
import java.util.Set;

public class FromSelectedExpressMetadataToAvailableMethods extends Mapper<ExpressMetadata, AvailableMethod> {

    @NonNull private final Set<String> cardsWithEsc;
    @Nullable private final PayerCost selectedPayerCost;
    private final boolean isSplit;

    public FromSelectedExpressMetadataToAvailableMethods(@NonNull final Set<String> cardsWithEsc,
        @Nullable final PayerCost selectedPayerCost, final boolean isSplit) {
        this.cardsWithEsc = cardsWithEsc;
        this.selectedPayerCost = selectedPayerCost;
        this.isSplit = isSplit;
    }

    @Override
    public AvailableMethod map(@NonNull final ExpressMetadata expressMetadata) {
        boolean hasInterestFree = false;
        boolean hasReimbursement = false;
        final BenefitsMetadata benefits = expressMetadata.getBenefits();

        if (benefits != null) {
            hasInterestFree = benefits.getInterestFree() != null;
            hasReimbursement = benefits.getReimbursement() != null;
        }

        final AvailableMethod.Builder builder = new AvailableMethod.Builder(
            expressMetadata.getPaymentMethodId(),
            expressMetadata.getPaymentTypeId(),
            hasInterestFree, hasReimbursement);

        if (expressMetadata.isCard()) {
            final CardMetadata card = expressMetadata.getCard();
            builder.setExtraInfo(CardExtraExpress.selectedExpressSavedCard(card, selectedPayerCost,
                cardsWithEsc.contains(card.getId()), isSplit).toMap());
        } else if (expressMetadata.getAccountMoney() != null) {
            final AccountMoneyMetadata accountMoney = expressMetadata.getAccountMoney();
            builder
                .setExtraInfo(new AccountMoneyExtraInfo(accountMoney.getBalance(), accountMoney.isInvested()).toMap());
        } else if (expressMetadata.isConsumerCredits() && selectedPayerCost!= null) {
            builder.setExtraInfo(new CreditsExtraInfo(new PayerCostInfo(selectedPayerCost)).toMap());
        }

        return builder.build();
    }
}
