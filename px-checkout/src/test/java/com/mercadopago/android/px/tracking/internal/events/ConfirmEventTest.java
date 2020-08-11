package com.mercadopago.android.px.tracking.internal.events;

import com.mercadopago.android.px.model.AccountMoneyMetadata;
import com.mercadopago.android.px.model.CardDisplayInfo;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.tracking.internal.mapper.FromSelectedExpressMetadataToAvailableMethods;
import com.mercadopago.android.px.tracking.internal.model.ConfirmData;
import java.math.BigDecimal;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmEventTest {

    private static final String EXPECTED_PATH = "/px_checkout/review/confirm";
    private static final String EXPECTED_JUST_CARD =
        "{review_type=one_tap, payment_method_selected_index=2, payment_method_id=visa, payment_method_type=credit_card, extra_info={has_interest_free=false, issuer_id=0, has_split=false, has_reimbursement=false, card_id=123, selected_installment={quantity=1, installment_amount=10, visible_total_price=10, interest_rate=10}, has_esc=false}}";
    private static final String EXPECTED_JUST_AM =
        "{review_type=one_tap, payment_method_selected_index=2, payment_method_id=account_money, payment_method_type=account_money, extra_info={has_interest_free=false, balance=10, has_reimbursement=false, invested=true}}";
    private static final int PAYMENT_METHOD_SELECTED_INDEX = 2;

    @Mock private ExpressMetadata expressMetadata;
    @Mock private Set<String> cardIdsWithEsc;

    @NotNull
    private ConfirmEvent getConfirmEvent(final PayerCost payerCost) {
        final ConfirmData
            confirmTrackerData = new ConfirmData(ConfirmEvent.ReviewType.ONE_TAP, PAYMENT_METHOD_SELECTED_INDEX,
            new FromSelectedExpressMetadataToAvailableMethods(cardIdsWithEsc,
                payerCost, false)
                .map(expressMetadata));
        return new ConfirmEvent(confirmTrackerData);
    }

    @Test
    public void whenGetEventPathVerifyIsCorrect() {
        final ConfirmEvent event = getConfirmEvent(mock(PayerCost.class));
        assertEquals(EXPECTED_PATH, event.getTrack().getPath());
    }

    @Test
    public void whenExpressMetadataHasAccountMoneyThenShowItInMetadata() {
        final AccountMoneyMetadata am = mock(AccountMoneyMetadata.class);
        when(expressMetadata.getPaymentMethodId()).thenReturn("account_money");
        when(expressMetadata.getPaymentTypeId()).thenReturn("account_money");
        when(expressMetadata.getAccountMoney()).thenReturn(am);
        when(am.getBalance()).thenReturn(BigDecimal.TEN);
        when(am.isInvested()).thenReturn(true);
        final ConfirmEvent event = getConfirmEvent(mock(PayerCost.class));
        assertEquals(EXPECTED_JUST_AM, event.getTrack().getData().toString());
    }

    @Test
    public void whenExpressMetadataHasSavedCardThenShowItInMetadata() {
        final CardMetadata card = mock(CardMetadata.class);
        final PayerCost payerCost = mock(PayerCost.class);
        final CardDisplayInfo cardDisplayInfo = mock(CardDisplayInfo.class);

        when(payerCost.getTotalAmount()).thenReturn(BigDecimal.TEN);
        when(payerCost.getInstallmentAmount()).thenReturn(BigDecimal.TEN);
        when(payerCost.getInstallments()).thenReturn(1);
        when(payerCost.getInstallmentRate()).thenReturn(BigDecimal.TEN);
        when(card.getId()).thenReturn("123");
        when(card.getDisplayInfo()).thenReturn(cardDisplayInfo);

        when(expressMetadata.getPaymentMethodId()).thenReturn("visa");
        when(expressMetadata.getPaymentTypeId()).thenReturn("credit_card");
        when(expressMetadata.getCard()).thenReturn(card);
        when(expressMetadata.isCard()).thenReturn(true);

        final ConfirmEvent event = getConfirmEvent(payerCost);

        assertEquals(EXPECTED_JUST_CARD, event.getTrack().getData().toString());
    }
}