package com.mercadopago.android.px.internal.features;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.adapters.IssuersAdapter;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.callbacks.RecyclerItemClickListener;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.internal.font.FontHelper;
import com.mercadopago.android.px.internal.font.PxFont;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IssuersActivity extends PXActivity<IssuersPresenter> implements IssuersActivityView {

    private static final String EXTRA_ISSUERS = "issuers";
    private static final String EXTRA_ISSUER_ID = "issuerId";
    private static final String EXTRA_CARD_INFO = "cardInfo";
    private static final String EXTRA_PAYMENT_METHOD = "paymentMethod";

    public static void start(@NonNull final AppCompatActivity from, final int requestCode,
        @NonNull final List<Issuer> issuers, @NonNull final Serializable cardInformation) {
        final Intent intent = new Intent(from, IssuersActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_ISSUERS, new ArrayList<>(issuers));
        intent.putExtra(EXTRA_CARD_INFO, cardInformation);
        from.startActivityForResult(intent, requestCode);
        from.overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    public static void startWithPaymentMethod(@NonNull final AppCompatActivity from, final int requestCode,
        @NonNull final List<Issuer> issuers, @NonNull final Serializable cardInformation,
        @NonNull final PaymentMethod paymentMethod) {
        final Intent intent = new Intent(from, IssuersActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_ISSUERS, new ArrayList<>(issuers));
        intent.putExtra(EXTRA_CARD_INFO, cardInformation);
        intent.putExtra(EXTRA_PAYMENT_METHOD, (Parcelable) paymentMethod);
        from.startActivityForResult(intent, requestCode);
        from.overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    public static long extractIssuerIdFromIntent(@NonNull final Intent intent) {
        return intent.getExtras().getLong(EXTRA_ISSUER_ID);
    }

    // Local vars
    protected boolean mActivityActive;

    protected IssuersAdapter mIssuersAdapter;
    protected RecyclerView mIssuersRecyclerView;

    //ViewMode
    protected boolean mLowResActive;

    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mLowResTitleToolbar;
    protected MPTextView mTimerTextView;

    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected Toolbar mNormalToolbar;
    protected FrontCardView mFrontCardView;
    protected ViewGroup mProgressLayout;

    @Override
    protected void onCreated(@Nullable final Bundle savedInstanceState) {
        getActivityParameters();

        presenter.attachView(this);

        mActivityActive = true;

        analyzeLowRes();
        setContentView();
        initializeControls();

        initialize();
        presenter.initialize();
    }

    private void getActivityParameters() {
        final Session session = Session.getInstance();
        final List<Issuer> issuers = getIntent().getParcelableArrayListExtra(EXTRA_ISSUERS);

        final PaymentMethod paymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        if (paymentMethod != null) {
            // Comes from card storage flow
            presenter =
                new IssuersPresenter(session.getIssuersRepository(), paymentMethod, true);
        } else {
            presenter =
                new IssuersPresenter(
                    session.getIssuersRepository(),
                    session.getConfigurationModule().getUserSelectionRepository().getPaymentMethod(),
                    false);
        }

        presenter.setCardInfo((CardInfo) getIntent().getSerializableExtra(EXTRA_CARD_INFO));
        presenter.setIssuers(issuers);
    }

    public void analyzeLowRes() {
        if (presenter.isRequiredCardDrawn()) {
            mLowResActive = ScaleUtil.isLowRes(this);
        } else {
            mLowResActive = true;
        }
    }

    public void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.px_activity_issuers_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.px_activity_issuers_normal);
    }

    private void initializeControls() {
        mIssuersRecyclerView = findViewById(R.id.mpsdkActivityIssuersView);
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);

        if (mLowResActive) {
            initializeLowResControls();
        } else {
            initializeNormalControls();
        }
    }

    private void initializeLowResControls() {
        mLowResToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);

        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            final Toolbar.LayoutParams marginParams =
                new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginParams.setMargins(0, 0, 0, 0);
            mLowResTitleToolbar.setLayoutParams(marginParams);
            mLowResTitleToolbar.setTextSize(17);
            mTimerTextView.setTextSize(15);
        }

        mLowResToolbar.setVisibility(View.VISIBLE);
    }

    private void initializeNormalControls() {
        mCollapsingToolbar = findViewById(R.id.mpsdkCollapsingToolbar);
        mAppBar = findViewById(R.id.mpsdkIssuersAppBar);
        mCardContainer = findViewById(R.id.mpsdkActivityCardContainer);
        mNormalToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mNormalToolbar.setVisibility(View.VISIBLE);
    }

    private void initialize() {
        loadViews();
        hideHeader();
        showTimer();
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.px_card_issuers_title));
    }

    private void loadToolbarArrow(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.px_card_issuers_title));
        setCustomFontNormal();

        mFrontCardView = new FrontCardView(this, CardRepresentationModes.SHOW_FULL_FRONT_ONLY);
        mFrontCardView.setSize(CardRepresentationModes.MEDIUM_SIZE);
        mFrontCardView.setPaymentMethod(presenter.getPaymentMethod());
        if (presenter.getCardInfo() != null) {
            mFrontCardView.setCardNumberLength(presenter.getCardInfo().getCardNumberLength());
            mFrontCardView.setLastFourDigits(presenter.getCardInfo().getLastFourDigits());
        }
        mFrontCardView.inflateInParent(mCardContainer, true);
        mFrontCardView.initializeControls();
        mFrontCardView.draw();
        mFrontCardView.enableEditingCardNumber();
    }

    private void setCustomFontNormal() {
        FontHelper.setFont(mCollapsingToolbar, PxFont.REGULAR);
    }

    private void hideHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.GONE);
        } else {
            mNormalToolbar.setTitle("");
        }
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    private void initializeAdapter(final OnSelectedCallback<Integer> onSelectedCallback) {
        mIssuersAdapter = new IssuersAdapter(onSelectedCallback);
        initializeAdapterListener(mIssuersAdapter, mIssuersRecyclerView);
    }

    private void initializeAdapterListener(final RecyclerView.Adapter adapter, final RecyclerView view) {
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addOnItemTouchListener(new RecyclerItemClickListener(this,
            (view1, position) -> presenter.onItemSelected(position)));
    }

    @Override
    public void showHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mNormalToolbar.setTitle(getString(R.string.px_card_issuers_title));
            setCustomFontNormal();
        }
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error.isApiException()) {
            showApiException(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void showEmptyIssuersError(final String requestOrigin) {
        final String message = getString(R.string.px_standard_error_message);
        final String detail = getString(R.string.px_error_message_detail_issuers);
        final MercadoPagoError error = new MercadoPagoError(message, detail, false);
        showError(error, requestOrigin);
    }

    public void showApiException(final ApiException apiException, final String requestOrigin) {
        if (mActivityActive) {
            ErrorUtil.showApiExceptionError(this, apiException, requestOrigin);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    @Override
    public void showIssuers(final List<Issuer> issuersList, final OnSelectedCallback<Integer> onSelectedCallback) {
        initializeAdapter(onSelectedCallback);
        mIssuersAdapter.addResults(issuersList);
        stopLoadingView();
    }

    @Override
    public void showLoadingView() {
        mIssuersRecyclerView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoadingView() {
        mIssuersRecyclerView.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void finishWithResult(final Issuer issuer) {
        setResult(RESULT_OK);
        Session.getInstance().getConfigurationModule().getUserSelectionRepository().select(issuer);
        finish();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void finishWithResultForCardStorage(final Long issuerId) {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_ISSUER_ID, issuerId);
        setResult(RESULT_OK, intent);
        finish();
        animateTransitionSlideInSlideOut();
    }

    public void animateTransitionSlideInSlideOut() {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
