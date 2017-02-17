package com.mercadopago.presenters;

import com.mercadopago.callbacks.OnConfirmPaymentCallback;
import com.mercadopago.callbacks.OnReviewChange;
import com.mercadopago.controllers.CustomReviewablesHandler;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Item;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.ReviewSubscriber;
import com.mercadopago.model.Reviewable;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.providers.ReviewAndConfirmProvider;
import com.mercadopago.util.MercadoPagoUtil;
import com.mercadopago.views.ReviewAndConfirmView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mreverter on 2/2/17.
 */
public class ReviewAndConfirmPresenter extends MvpPresenter<ReviewAndConfirmView, ReviewAndConfirmProvider> implements ReviewSubscriber {
    private PaymentMethod mPaymentMethod;
    private PayerCost mPayerCost;
    private BigDecimal mAmount;
    private Discount mDiscount;
    private Issuer mIssuer;
    private Token mToken;
    private Site mSite;
    private List<Item> mItems;
    private String mExtraPaymentMethodInfo;
    private DecorationPreference mDecorationPreference;
    private Boolean mEditionEnabled;
    private boolean mTermsAndConditionsEnabled;
    private List<String> reviewOrder;

    public void initialize() {
        try {
            validate();
            showReviewAndConfirm();
        } catch (IllegalStateException exception) {
            getView().showError(exception.getMessage());
        }
    }

    private void validate() throws IllegalStateException {
        if (this.mPaymentMethod == null) {
            throw new IllegalStateException("payment method is null");
        }
        if (this.mItems == null) {
            throw new IllegalStateException("items not set");
        }
        if (this.mSite == null) {
            throw new IllegalStateException("site not set");
        }
        if (this.mAmount == null) {
            throw new IllegalStateException("amount not set");
        }
        if (MercadoPagoUtil.isCard(mPaymentMethod.getPaymentTypeId())) {
            if (this.mPayerCost == null) {
                throw new IllegalStateException("payer cost is null");
            }
            if (this.mToken == null) {
                throw new IllegalStateException("token is null");
            }
        }
    }

    private void showReviewAndConfirm() {

        setReviewTitle();
        setConfirmationMessage();
        setCancelMessage();

        Reviewable summaryReview = getSummary();
        summaryReview.setReviewSubscriber(this);

        Reviewable itemsReview = getItemsReview();
        itemsReview.setReviewSubscriber(this);

        Reviewable paymentMethodReview = getPaymentMethodReview();
        paymentMethodReview.setReviewSubscriber(this);

        List<Reviewable> customReviewables = retrieveCustomReviewables();

        List<Reviewable> reviewablesAvailable = new ArrayList<>();
        reviewablesAvailable.add(summaryReview);
        reviewablesAvailable.add(itemsReview);
        reviewablesAvailable.add(paymentMethodReview);
        reviewablesAvailable.addAll(customReviewables);

        List<Reviewable> orderedReviewables = getOrderedReviewables(reviewablesAvailable, getReviewOrder());
        getView().showReviewables(orderedReviewables);

        if (mTermsAndConditionsEnabled) {
            getView().showTermsAndConditions();
        }
    }

    private List<Reviewable> getOrderedReviewables(List<Reviewable> reviewablesAvailable, List<String> reviewOrder) {

        List<Reviewable> orderedReviewables = new ArrayList<>();
        if (reviewOrder == null || reviewOrder.isEmpty()) {
            orderedReviewables = reviewablesAvailable;
        } else {
            for (String key : reviewOrder) {
                List<Reviewable> toRemove = new ArrayList<>();
                for (Reviewable reviewable : reviewablesAvailable) {
                    if (key.equals(reviewable.getKey())) {
                        orderedReviewables.add(reviewable);
                        toRemove.add(reviewable);
                    }
                }
                reviewablesAvailable.removeAll(toRemove);
            }
        }
        return orderedReviewables;
    }

    private void setReviewTitle() {
        String title = getResourcesProvider().getReviewTitle();
        getView().showTitle(title);
    }

    private void setConfirmationMessage() {
        String message = getResourcesProvider().getConfirmationMessage();
        getView().showConfirmationMessage(message);
    }

    private void setCancelMessage() {
        String message = getResourcesProvider().getCancelMessage();
        getView().showCancelMessage(message);
    }

    private List<Reviewable> retrieveCustomReviewables() {
        List<Reviewable> customReviewables = CustomReviewablesHandler.getInstance().getReviewables();

        for (Reviewable reviewable : customReviewables) {
            reviewable.setReviewSubscriber(this);
        }

        return customReviewables;
    }

    private Reviewable getSummary() {
        Reviewable summary = getResourcesProvider().getSummaryReviewable(mPaymentMethod, mPayerCost, mAmount, mDiscount, mSite, mDecorationPreference, new OnConfirmPaymentCallback() {
            @Override
            public void confirmPayment() {
                getView().confirmPayment();
            }
        });
        return summary;
    }

    private Reviewable getItemsReview() {
        Reviewable itemsReview = getResourcesProvider().getItemsReviewable(mSite.getCurrencyId(), mItems);
        return itemsReview;
    }

    private Reviewable getPaymentMethodReview() {
        Reviewable paymentMethodReview;
        if (MercadoPagoUtil.isCard(mPaymentMethod.getPaymentTypeId())) {
            paymentMethodReview = getPaymentMethodOnReview();
        } else {
            paymentMethodReview = getPaymentMethodOffReview();
        }
        return paymentMethodReview;
    }

    private Reviewable getPaymentMethodOffReview() {
        BigDecimal finalAmount;
        if (mDiscount == null) {
            finalAmount = mAmount;
        } else {
            finalAmount = mAmount.subtract(mDiscount.getCouponAmount());
        }
        return getResourcesProvider().getPaymentMethodOffReviewable(mPaymentMethod, mExtraPaymentMethodInfo, finalAmount, mSite, mDecorationPreference, mEditionEnabled, new OnReviewChange() {
            @Override
            public void onChangeSelected() {
                getView().changePaymentMethod();
            }
        });
    }

    private Reviewable getPaymentMethodOnReview() {
        CardInfo cardInfo = new CardInfo(mToken);
        return getResourcesProvider().getPaymentMethodOnReviewable(mPaymentMethod, mPayerCost, cardInfo, mSite, mDecorationPreference, mEditionEnabled, new OnReviewChange() {
            @Override
            public void onChangeSelected() {
                getView().changePaymentMethod();
            }
        });
    }

    public void setAmount(BigDecimal amount) {
        this.mAmount = amount;
    }

    public void setDiscount(Discount discount) {
        this.mDiscount = discount;
    }

    public void setPayerCost(PayerCost mPayerCost) {
        this.mPayerCost = mPayerCost;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.mPaymentMethod = paymentMethod;
    }

    public void setToken(Token token) {
        this.mToken = token;
    }

    public void setIssuer(Issuer issuer) {
        this.mIssuer = issuer;
    }

    public void setSite(Site site) {
        this.mSite = site;
    }

    public void setItems(List<Item> items) {
        this.mItems = items;
    }

    public void setExtraPaymentMethodInfo(String extraPaymentMethodInfo) {
        this.mExtraPaymentMethodInfo = extraPaymentMethodInfo;
    }

    public void setDecorationPreference(DecorationPreference decorationPreference) {
        this.mDecorationPreference = decorationPreference;
    }

    public void setEditionEnabled(Boolean editionEnabled) {
        this.mEditionEnabled = editionEnabled;
    }

    public void setTermsAndConditionsEnabled(boolean termsAndConditionsEnabled) {
        this.mTermsAndConditionsEnabled = termsAndConditionsEnabled;
    }

    public void setReviewOrder(List<String> reviewOrder) {
        this.reviewOrder = reviewOrder;
    }

    @Override
    public void changeRequired(Reviewable reviewable) {
        if (reviewable.getReviewableCallback() != null) {
            PaymentData paymentData = new PaymentData();
            paymentData.setPaymentMethod(mPaymentMethod);
            paymentData.setPayerCost(mPayerCost);
            paymentData.setDiscount(mDiscount);
            paymentData.setIssuer(mIssuer);
            paymentData.setToken(mToken);

            reviewable.getReviewableCallback().onChangeRequired(paymentData);
        }
        getView().cancelPayment();
    }

    public List<String> getReviewOrder() {
        return reviewOrder;
    }
}