package com.mercadopago.android.px.internal.features.payment_congrats;

import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModel;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsResponse;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentInfo;
import java.math.BigDecimal;
import java.util.ArrayList;

public final class PaymentCongratsMock {

    private PaymentCongratsMock() { }

    public static PaymentCongratsModel getMock() {

        // Discounts
        ArrayList<PaymentCongratsResponse.Discount.Item> itemList = new ArrayList();
        for (int i = 0; i <= 8; i++) {
            PaymentCongratsResponse.Discount.Item discountItem = new PaymentCongratsResponse.Discount.Item("Hasta","20% OFF","https://mla-s1-p.mlstatic.com/952848-MLA41109062105_032020-O.jpg", "mercadopago://discount_center_payers/detail?campaign_id\\u003d1048784\\u0026user_level\\u003d3\\u0026mcc\\u003d561013\\u0026distance\\u003d256\\u0026coupon_used\\u003dfalse\\u0026status\\u003dFULL\\u0026store_id\\u003d30188107\\u0026sections\\u003d%5B%7B%22id%22%3A%22header%22%2C%22type%22%3A%22header%22%2C%22content%22%3A%7B%22logo%22%3A%22https%3A%2F%2Fmla-s1-p.mlstatic.com%2F952848-MLA41109062105_032020-O.jpg%22%2C%22title%22%3A%2220%25%20OFF%22%2C%22subtitle%22%3A%22El%20Noble%22%2C%22level%22%3A%7B%22icon%22%3A%22discount_payers_black_check%22%2C%22label%22%3A%22NIVEL%201%22%2C%22format%22%3A%7B%22text_color%22%3A%22%23000000%22%2C%22background_color%22%3A%22%23EDEDED%22%7D%7D%7D%7D%5D#from\\u003d/px/congrats","1048784");
            itemList.add(discountItem);
        }
        PaymentCongratsResponse.Action action = new PaymentCongratsResponse
            .Action("Ver todos los descuentos", "mercadopago://discount_center_payers/list#from\\u003d/px/congrats");
        PaymentCongratsResponse.Discount.DownloadApp downloadApp =
            new PaymentCongratsResponse.Discount.DownloadApp("Exclusivo con la app de Mercado Pago", action);
        PaymentCongratsResponse.Discount discount =
            new PaymentCongratsResponse.Discount("Descuentos por tu nivel", "", action, downloadApp, null, itemList);

        //Score
        PaymentCongratsResponse.Score.Progress progress =
            new PaymentCongratsResponse.Score.Progress(0.14f, "#1AC2B0", 2);
        PaymentCongratsResponse.Score score =
            new PaymentCongratsResponse.Score(progress, "Sumaste 1 Mercado Punto", action);

        //Payment Methods
        ArrayList<PaymentInfo> paymentList = new ArrayList();
        paymentList.add(
            new PaymentInfo.Builder()
                .withPaymentMethodId("account_money")
                .withPaymentMethodName("Money in Mercado Pago")
                .withPaymentMethodType(PaymentInfo.PaymentMethodType.ACCOUNT_MONEY)
                .withAmountPaid("$100")
                .withDiscountData("50% OFF", "$200")
                .build()
        );
        paymentList.add(
            new PaymentInfo.Builder()
                .withPaymentMethodId("master")
                .withPaymentMethodName("Visa")
                .withPaymentMethodType(PaymentInfo.PaymentMethodType.CREDIT_CARD)
                .withLastFourDigits("8020")
                .withAmountPaid( "$100")
                .withInstallmentsData(3, "$39,90", "$119,70", BigDecimal.valueOf(19.71))
                .build()
        );

        //Congrats
        PaymentCongratsModel congrats = new PaymentCongratsModel.Builder()
            .withCongratsType(PaymentCongratsModel.CongratsType.APPROVED)
            .withTitle("Payment Congrats Example")
            .withImageUrl("https://www.jqueryscript.net/images/Simplest-Responsive-jQuery-Image-Lightbox-Plugin-simple-lightbox.jpg")
            .withExitActionSecondary("Continuar", 13)
            .withPaymentsInfo(paymentList)
            .withShouldPaymentMethod(true)
            .withShouldShowReceipt(true)
            .withReceiptId("12312312")
            .withDiscount(discount)
            .withScore(score)
            .build();

        return congrats;
    }
}
