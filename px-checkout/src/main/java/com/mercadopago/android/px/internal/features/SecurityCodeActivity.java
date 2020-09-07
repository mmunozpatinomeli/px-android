package com.mercadopago.android.px.internal.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.callbacks.card.CardSecurityCodeEditTextCallback;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.card.CardSecurityCodeTextWatcher;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.font.FontHelper;
import com.mercadopago.android.px.internal.font.PxFont;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPEditText;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.CvvInfo;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.ExceptionHandler;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import com.squareup.picasso.Callback;

import static com.mercadopago.android.px.internal.util.AccessibilityUtilsKt.executeIfAccessibilityTalkBackEnable;

public class SecurityCodeActivity extends PXActivity<SecurityCodePresenter> implements SecurityCodeActivityView {

    private static final String EXTRA_PAYMENT_METHOD = "PAYMENT_METHOD";
    private static final String EXTRA_TOKEN = "TOKEN";
    private static final String EXTRA_CARD = "CARD";
    private static final String EXTRA_CARD_INFO = "CARD_INFO";
    private static final String EXTRA_PAYMENT_RECOVERY = "PAYMENT_RECOVERY";
    private static final String EXTRA_REASON = "REASON";

    public static final String ERROR_STATE = "textview_error";
    public static final String NORMAL_STATE = "textview_normal";

    //View controls
    protected ViewGroup mProgressLayout;
    protected MPEditText mSecurityCodeEditText;
    protected AppCompatButton mNextButton;
    protected AppCompatButton mBackButton;
    protected LinearLayout mButtonContainer;
    protected FrameLayout mErrorContainer;
    protected MPTextView mErrorTextView;
    protected String mErrorState;
    protected FrameLayout mBackground;
    protected ImageView mSecurityCodeCardIcon;
    protected Toolbar mToolbar;

    //Normal View
    protected FrameLayout mCardContainer;
    protected CardView mCardView;
    protected MPTextView mTimerTextView;

    public static void startForSavedCard(@NonNull final Fragment fragment, @NonNull final Card card,
        final Reason reason, final int reqCode) {
        //noinspection ConstantConditions
        final Intent intent = createIntent(fragment.getContext(), card);
        intent.putExtra(EXTRA_REASON, reason.name());
        fragment.startActivityForResult(intent, reqCode);
    }

    public static void startForRecovery(@NonNull final Fragment fragment, @NonNull final PaymentRecovery recovery,
        final int reqCode) {
        final Context context = fragment.getContext();
        if (context != null) {
            final Card card = recovery.getCard();
            final Intent intent = card != null ? createIntent(context, card) : createIntent(context, recovery);
            intent.putExtra(EXTRA_PAYMENT_RECOVERY, recovery);
            intent.putExtra(EXTRA_REASON, Reason.from(recovery).name());
            fragment.startActivityForResult(intent, reqCode);
        }
    }

    private static Intent createIntent(@NonNull final Context context, @NonNull final PaymentRecovery recovery) {
        final Intent intent = new Intent(context, SecurityCodeActivity.class);
        intent.putExtra(EXTRA_CARD_INFO, new CardInfo(recovery.getToken()));
        intent.putExtra(EXTRA_TOKEN, recovery.getToken());
        intent.putExtra(EXTRA_PAYMENT_METHOD, (Parcelable) recovery.getPaymentMethod());
        return intent;
    }

    private static Intent createIntent(@NonNull final Context context, @NonNull final Card card) {
        final Intent intent = new Intent(context, SecurityCodeActivity.class);
        intent.putExtra(EXTRA_CARD_INFO, new CardInfo(card));
        intent.putExtra(EXTRA_CARD, (Parcelable) card);
        intent.putExtra(EXTRA_PAYMENT_METHOD, (Parcelable) card.getPaymentMethod());
        return intent;
    }

    @Override
    protected void onCreated(@Nullable final Bundle savedInstanceState) {
        final Session session = Session.getInstance();
        final PaymentSettingRepository paymentSettings = session.getConfigurationModule().getPaymentSettings();
        presenter = new SecurityCodePresenter(paymentSettings, session.getCardTokenRepository(),
            session.getMercadoPagoESC());

        if (savedInstanceState == null) {
            getActivityParameters();
        } else {
            presenter.recoverFromBundle(savedInstanceState);
        }

        setContentView(R.layout.px_activity_security_code);
        presenter.attachView(this);
        presenter.initialize();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        presenter.storeInBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    private void getActivityParameters() {
        final CardInfo cardInfo = (CardInfo) getIntent().getSerializableExtra(EXTRA_CARD_INFO);
        final Card card = (Card) getIntent().getSerializableExtra(EXTRA_CARD);
        final Token token = (Token) getIntent().getSerializableExtra(EXTRA_TOKEN);
        final PaymentMethod paymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);
        final PaymentRecovery paymentRecovery =
            (PaymentRecovery) getIntent().getSerializableExtra(EXTRA_PAYMENT_RECOVERY);
        final Reason reason =
            Reason.valueOf(getIntent().getStringExtra(EXTRA_REASON));

        presenter.setToken(token);
        presenter.setCard(card);
        presenter.setPaymentMethod(paymentMethod);
        presenter.setCardInfo(cardInfo);
        presenter.setPaymentRecovery(paymentRecovery);
        presenter.setReason(reason);
    }

    private void initializeControls() {
        initializeToolbar();
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        mSecurityCodeEditText = findViewById(R.id.mpsdkCardSecurityCode);
        mNextButton = findViewById(R.id.mpsdkNextButton);
        mBackButton = findViewById(R.id.mpsdkBackButton);
        mButtonContainer = findViewById(R.id.mpsdkButtonContainer);
        mErrorContainer = findViewById(R.id.mpsdkErrorContainer);
        mErrorTextView = findViewById(R.id.mpsdkErrorTextView);
        mBackground = findViewById(R.id.mpsdkSecurityCodeActivityBackground);
        mCardContainer = findViewById(R.id.mpsdkCardViewContainer);
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout.setVisibility(View.GONE);
        mSecurityCodeCardIcon = findViewById(R.id.mpsdkSecurityCodeCardIcon);

        FontHelper.setFont(mNextButton, PxFont.REGULAR);
        FontHelper.setFont(mBackButton, PxFont.REGULAR);
        setListeners();
    }

    private void initializeToolbar() {
        mToolbar = findViewById(R.id.mpsdkToolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeActionContentDescription(R.string.px_label_back);
        }
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        presenter.trackAbort();
        configureFinish();
        finish();
    }

    @Override
    public void onBackButtonPressed() {
        configureFinish();
        super.onBackPressed();
    }

    private void configureFinish() {
        final Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        overrideTransitionOut();
    }

    private void setListeners() {
        setSecurityCodeListeners();
        setButtonsListeners();
    }

    @Override
    public void showLoadingView() {
        ViewUtils.hideKeyboard(this);
        mProgressLayout.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void stopLoadingView() {
        mProgressLayout.setVisibility(View.GONE);
    }

    private void loadViews() {
        if (!presenter.shouldShowCvvInfo()) {
            showCvv();
        } else {
            showCustomView();
        }
        presenter.setSecurityCodeCardType();
    }

    private void showCvv() {
        mCardView = new CardView(this);
        final String lastFourDigits = presenter.getCardInfo().getLastFourDigits();
        mCardView.setSize(CardRepresentationModes.BIG_SIZE);
        mCardView.inflateInParent(mCardContainer, true);
        mCardView.initializeControls();
        mCardView.setPaymentMethod(presenter.getPaymentMethod());
        mCardView.setSecurityCodeLength(presenter.getSecurityCodeLength());
        mCardView.setSecurityCodeLocation(presenter.getSecurityCodeLocation());
        mCardView.setCardNumberLength(presenter.getCardNumberLength());
        mCardView.setLastFourDigits(lastFourDigits);
        mCardView.draw(CardView.CARD_SIDE_FRONT);
        mCardView.drawFullCard();
        mCardView.drawEditingSecurityCode("");

        executeIfAccessibilityTalkBackEnable(this, () -> {
            mCardContainer.setContentDescription(new SpannableStringBuilder()
                .append(getString(R.string.px_card_number_label))
                .append(TextUtil.SPACE)
                .append(getString(R.string.px_ending_in))
                .append(TextUtil.SPACE)
                .append(lastFourDigits));
            return null;
        });
    }

    private void showCustomView() {
        final CvvInfo cvvInfo = presenter.getCvvInfo();
        final View view = LayoutInflater.from(this).inflate(R.layout.px_cvv_info, mCardContainer, true);

        final MPTextView title = view.findViewById(R.id.title);
        title.setText(cvvInfo.getTitle());
        ViewUtils.loadOrGone(cvvInfo.getMessage(), (TextView) view.findViewById(R.id.message));
    }

    private void setSecurityCodeCardColorFilter() {
        final int color = ResourceUtil.getCardColor(presenter.getPaymentMethod().getId(), this);
        mSecurityCodeCardIcon.setColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.DST_OVER);
    }

    @Override
    public void initialize() {
        initializeControls();
        presenter.initializeSettings();
        loadViews();
        ViewUtils.openKeyboard(mSecurityCodeEditText);
    }

    @Override
    public void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void showBackSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void showFrontSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_amex_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void showUrlSecurityCodeCardView(@Nullable final String securityCodeUrl) {
        ViewUtils.loadOrCallError(securityCodeUrl, mSecurityCodeCardIcon, new Callback.EmptyCallback());
    }

    @Override
    public void showStandardErrorMessage() {
        final String standardErrorMessage = getString(R.string.px_standard_error_message);
        showError(MercadoPagoError.createNotRecoverable(standardErrorMessage), TextUtil.EMPTY);
    }

    @Override
    public void setSecurityCodeInputMaxLength(final int length) {
        setInputMaxLength(mSecurityCodeEditText, length);
    }

    private void setInputMaxLength(final MPEditText text, final int maxLength) {
        final InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    private void setSecurityCodeListeners() {
        mSecurityCodeEditText
            .addTextChangedListener(new CardSecurityCodeTextWatcher(new CardSecurityCodeEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    ViewUtils.openKeyboard(mSecurityCodeEditText);
                }

                @Override
                public void saveSecurityCode(final CharSequence s) {
                    presenter.saveSecurityCode(s.toString());
                    if (mCardView != null) {
                        mCardView.setSecurityCodeLocation(presenter.getSecurityCodeLocation());
                        mCardView.drawEditingSecurityCode(s.toString());
                    }
                }

                @Override
                public void changeErrorView() {
                    clearErrorView();
                }

                @Override
                public void toggleLineColorOnError(final boolean toggle) {
                    mSecurityCodeEditText.toggleLineColorOnError(toggle);
                }
            }));
        mSecurityCodeEditText.setOnEditorActionListener((v, actionId, event) -> onNextKey(actionId, event));
    }

    private boolean onNextKey(final int actionId, final KeyEvent event) {
        if (isNextKey(actionId, event)) {
            presenter.validateSecurityCodeInput();
            return true;
        }
        return false;
    }

    private boolean isNextKey(final int actionId, final KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
            (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    private void setButtonsListeners() {
        setNextButtonListeners();
        setBackButtonListeners();
    }

    private void setNextButtonListeners() {
        mNextButton.setOnClickListener(v -> presenter.validateSecurityCodeInput());
    }

    public void setBackButtonListeners() {
        mBackButton.setOnClickListener(v -> onBackButtonPressed());
    }

    private void setErrorState(final String mErrorState) {
        this.mErrorState = mErrorState;
    }

    @Override
    public void setErrorView(final CardTokenException exception) {
        mSecurityCodeEditText.toggleLineColorOnError(true);
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        final String errorText = ExceptionHandler.getErrorMessage(this, exception);
        mErrorTextView.setText(errorText);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void clearErrorView() {
        mSecurityCodeEditText.toggleLineColorOnError(false);
        mButtonContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        mErrorTextView.setText("");
        setErrorState(NORMAL_STATE);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                setResult(RESULT_CANCELED, data);
                finish();
            }
        }
    }

    @Override
    public void finishWithResult() {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * @deprecated Use static factory methods
     */
    @Deprecated
    public static final class Builder {
        private CardInfo cardInformation;
        private PaymentMethod paymentMethod;
        private Card card;
        private Token token;
        private PaymentRecovery paymentRecovery;
        private Reason reason;

        public Builder() {
        }

        public Builder setCardInfo(final CardInfo cardInformation) {
            this.cardInformation = cardInformation;
            return this;
        }

        public Builder setPaymentRecovery(final PaymentRecovery paymentRecovery) {
            this.paymentRecovery = paymentRecovery;
            return this;
        }

        public Builder setPaymentMethod(final PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder setCard(final Card card) {
            this.card = card;
            return this;
        }

        public Builder setToken(final Token token) {
            this.token = token;
            return this;
        }

        public Builder setReason(final Reason reason) {
            this.reason = reason;
            return this;
        }

        public void startActivity(@NonNull final Activity activity, final int requestCode) {
            if (reason == null) {
                throw new IllegalStateException("reason is null");
            }
            if (cardInformation == null) {
                throw new IllegalStateException("card info is null");
            }
            if (paymentMethod == null) {
                throw new IllegalStateException("payment method is null");
            }
            if (card != null && token != null && paymentRecovery == null) {
                throw new IllegalStateException(
                    "can't start with card and token at the same time if it's not recoverable");
            }
            if (card == null && token == null) {
                throw new IllegalStateException("card and token can't both be null");
            }

            startSecurityCodeActivity(activity, requestCode);
        }

        private void startSecurityCodeActivity(@NonNull final Activity activity, final int requestCode) {
            final Intent intent = new Intent(activity, SecurityCodeActivity.class);
            intent.putExtra(EXTRA_PAYMENT_METHOD, (Parcelable) paymentMethod);
            intent.putExtra(EXTRA_TOKEN, token);
            intent.putExtra(EXTRA_CARD, (Parcelable) card);
            intent.putExtra(EXTRA_CARD_INFO, cardInformation);
            intent.putExtra(EXTRA_PAYMENT_RECOVERY, paymentRecovery);
            intent.putExtra(EXTRA_REASON, reason.name());
            activity.startActivityForResult(intent, requestCode);
        }
    }
}