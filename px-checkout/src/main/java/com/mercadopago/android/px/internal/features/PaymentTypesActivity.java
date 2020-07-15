package com.mercadopago.android.px.internal.features;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.adapters.PaymentTypesAdapter;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.callbacks.RecyclerItemClickListener;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.internal.font.FontHelper;
import com.mercadopago.android.px.internal.font.PxFont;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.exceptions.ApiException;
import java.util.ArrayList;
import java.util.List;

public class PaymentTypesActivity extends PXActivity implements PaymentTypesActivityView {

    private static final String EXTRA_PAYMENT_METHODS = "paymentMethods";
    private static final String EXTRA_PAYMENT_TYPES = "paymentTypes";
    private static final String EXTRA_CARD_INFO = "cardInfo";
    public static final String EXTRA_PAYMENT_TYPE = "paymentType";

    protected PaymentTypesPresenter mPresenter;
    //ViewMode
    protected boolean mLowResActive;
    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mTimerTextView;
    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected Toolbar mNormalToolbar;
    protected FrontCardView mFrontCardView;
    private Activity mActivity;
    //View controls
    private PaymentTypesAdapter mPaymentTypesAdapter;
    private RecyclerView mPaymentTypesRecyclerView;
    private ViewGroup mProgressLayout;
    private MPTextView mLowResTitleToolbar;

    public static void start(@NonNull final Activity activity, final int requestCode,
        @NonNull final List<PaymentMethod> paymentMethods, @NonNull final List<PaymentType> paymentTypes,
        final CardInfo cardInfo) {
        final Intent intent = new Intent(activity, PaymentTypesActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_PAYMENT_METHODS, new ArrayList<>(paymentMethods));
        intent.putParcelableArrayListExtra(EXTRA_PAYMENT_TYPES, new ArrayList<>(paymentTypes));
        intent.putExtra(EXTRA_CARD_INFO, cardInfo);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    protected void onCreated(@Nullable final Bundle savedInstanceState) {
        if (mPresenter == null) {
            mPresenter = new PaymentTypesPresenter();
        }
        mPresenter.setView(this);
        mActivity = this;
        getActivityParameters();

        analizeLowRes();
        setContentView();
        mPresenter.validateActivityParameters();
    }

    private void getActivityParameters() {
        final List<PaymentMethod> paymentMethods = getIntent().getParcelableArrayListExtra(EXTRA_PAYMENT_METHODS);
        final List<PaymentType> paymentTypes = getIntent().getParcelableArrayListExtra(EXTRA_PAYMENT_TYPES);
        final CardInfo cardInfo = (CardInfo) getIntent().getSerializableExtra(EXTRA_CARD_INFO);
        mPresenter.setPaymentMethodList(paymentMethods);
        mPresenter.setPaymentTypesList(paymentTypes);
        mPresenter.setCardInfo(cardInfo);
    }

    public void analizeLowRes() {
        if (mPresenter.isCardInfoAvailable()) {
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

    @Override
    public void onValidStart() {
        mPresenter.initializePaymentMethod();
        initializeViews();
        loadViews();
        showTimer();
        initializeAdapter();
        mPresenter.loadPaymentTypes();
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void onInvalidStart(String message) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.px_activity_payment_types_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.px_activity_payment_types_normal);
    }

    private void initializeViews() {
        mPaymentTypesRecyclerView = findViewById(R.id.mpsdkActivityPaymentTypesRecyclerView);
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        if (mLowResActive) {
            mLowResToolbar = findViewById(R.id.mpsdkRegularToolbar);
            mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mCollapsingToolbar = findViewById(R.id.mpsdkCollapsingToolbar);
            mAppBar = findViewById(R.id.mpsdkPaymentTypesAppBar);
            mCardContainer = findViewById(R.id.mpsdkActivityCardContainer);
            mNormalToolbar = findViewById(R.id.mpsdkRegularToolbar);
            mNormalToolbar.setVisibility(View.VISIBLE);
        }
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout.setVisibility(View.GONE);
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    private void initializeAdapter() {
        mPaymentTypesAdapter = new PaymentTypesAdapter(getDpadSelectionCallback());
        initializeAdapterListener(mPaymentTypesAdapter, mPaymentTypesRecyclerView);
    }

    protected OnSelectedCallback<Integer> getDpadSelectionCallback() {
        return new OnSelectedCallback<Integer>() {
            @Override
            public void onSelected(Integer position) {
                mPresenter.onItemSelected(position);
            }
        };
    }

    private void initializeAdapterListener(RecyclerView.Adapter adapter, RecyclerView view) {
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addOnItemTouchListener(new RecyclerItemClickListener(this,
            new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    mPresenter.onItemSelected(position);
                }
            }));
    }

    @Override
    public void initializePaymentTypes(List<PaymentType> paymentTypes) {
        mPaymentTypesAdapter.addResults(paymentTypes);
    }

    @Override
    public void showApiExceptionError(ApiException exception, String requestOrigin) {
        ErrorUtil.showApiExceptionError(mActivity, exception, requestOrigin);
    }

    @Override
    public void startErrorView(String message, String errorDetail) {
        ErrorUtil.startErrorActivity(mActivity, message, errorDetail, false);
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.px_payment_types_title));
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.px_payment_types_title));
        setCustomFontNormal();
        mFrontCardView = new FrontCardView(mActivity, CardRepresentationModes.SHOW_FULL_FRONT_ONLY);
        mFrontCardView.setSize(CardRepresentationModes.MEDIUM_SIZE);
        mFrontCardView.setPaymentMethod(mPresenter.getPaymentMethod());
        if (mPresenter.isCardInfoAvailable()) {
            mFrontCardView.setCardNumberLength(mPresenter.getCardInfo().getCardNumberLength());
            mFrontCardView.setLastFourDigits(mPresenter.getCardInfo().getLastFourDigits());
        }
        mFrontCardView.inflateInParent(mCardContainer, true);
        mFrontCardView.initializeControls();
        mFrontCardView.draw();
        mFrontCardView.enableEditingCardNumber();
    }

    private void setCustomFontNormal() {
        FontHelper.setFont(mCollapsingToolbar, PxFont.REGULAR);
    }

    private void loadToolbarArrow(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    public void showLoadingView() {
        mPaymentTypesRecyclerView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoadingView() {
        mPaymentTypesRecyclerView.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void finishWithResult(PaymentType paymentType) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_PAYMENT_TYPE, (Parcelable) paymentType);
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.px_hold, R.anim.px_hold);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPresenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        }
    }
}
