package com.mercadopago.android.px.internal.features.express.add_new_card;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.BackHandler;
import com.mercadopago.android.px.internal.base.BaseFragment;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
import com.mercadopago.android.px.internal.features.pay_button.PayButtonFragment;
import com.mercadopago.android.px.internal.font.PxFont;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.model.OfflinePaymentTypesMetadata;

public class OfflineMethodsFragment extends BaseFragment<OfflineMethodsPresenter, OfflinePaymentTypesMetadata>
    implements OfflineMethods.OffMethodsView, BackHandler {

    private static final String TAG_EXPLODING_FRAGMENT = "TAG_EXPLODING_FRAGMENT";

    @Nullable /* default */ Animation fadeInAnimation;
    @Nullable /* default */ Animation fadeOutAnimation;
    /* default */ View panIndicator;
    private PayButtonFragment payButtonFragment;
    private TextView totalAmountTextView;
    private View header;

    @NonNull
    public static OfflineMethodsFragment getInstance(@NonNull final OfflinePaymentTypesMetadata model) {
        final OfflineMethodsFragment instance = new OfflineMethodsFragment();
        instance.storeModel(model);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_offline_methods, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        header = view.findViewById(R.id.header);
        panIndicator = view.findViewById(R.id.pan_indicator);
        ViewUtils.loadOrHide(View.GONE,
            model.getDisplayInfo() != null ? model.getDisplayInfo().getBottomDescription() : null,
            view.findViewById(R.id.bottom_description));
        payButtonFragment = (PayButtonFragment) getChildFragmentManager().findFragmentById(R.id.pay_button);
        if(payButtonFragment != null) {
            payButtonFragment.disable();
        }
        totalAmountTextView = view.findViewById(R.id.total_amount);
        final ImageView closeImage = view.findViewById(R.id.close);
        closeImage.setOnClickListener(v -> {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        });

        configureRecycler(view.findViewById(R.id.methods));

        if (savedInstanceState == null) {
            presenter.trackOfflineMethodsView(model);
        }

        presenter.updateModel();
    }

    private void configureRecycler(@NonNull final RecyclerView recycler) {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(linearLayoutManager);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                final boolean atTop = !recyclerView.canScrollVertically(-1);
                switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    if (atTop) {
                        panIndicator.clearAnimation();
                        panIndicator.startAnimation(fadeOutAnimation);
                    }
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (atTop) {
                        panIndicator.clearAnimation();
                        panIndicator.startAnimation(fadeInAnimation);
                    }
                    break;
                default:
                }
            }

            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        final DividerItemDecoration decoration =
            new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation());
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.px_item_decorator_divider));
        recycler.addItemDecoration(decoration);

        final OnMethodSelectedListener onMethodSelectedListener = selectedItem -> {
            presenter.selectMethod(selectedItem);
            payButtonFragment.enable();
        };

        final OfflineMethodsAdapter offlineMethodsAdapter =
            new OfflineMethodsAdapter(new FromOfflinePaymentTypesMetadataToOfflineItems(getContext()).map(model),
                onMethodSelectedListener);
        recycler.setAdapter(offlineMethodsAdapter);
    }

    @Override
    public void updateTotalView(@NonNull final AmountLocalized amountLocalized) {
        final Editable editable = new SpannableStringBuilder();
        final Editable editableDescription = new SpannableStringBuilder();
        final String totalText = getString(R.string.px_review_summary_total);
        editable.append(totalText);
        editable.append(TextUtil.SPACE);
        editable.append(amountLocalized.get(getContext()));

        ViewUtils.setFontInSpannable(getContext(), PxFont.SEMI_BOLD, editable);

        totalAmountTextView.setText(editable);

        editableDescription
            .append(totalText)
            .append(TextUtil.SPACE)
            .append(String.valueOf(amountLocalized.getAmount().floatValue()))
            .append(getString(R.string.px_money));

        totalAmountTextView.setContentDescription(editableDescription);
    }

    @Override
    protected OfflineMethodsPresenter createPresenter() {
        final Session session = Session.getInstance();
        return new OfflineMethodsPresenter(session.getPaymentRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getAmountRepository(),
            session.getDiscountRepository(),
            session.getConfigurationModule().getProductIdProvider(),
            model.getPaymentTypes().isEmpty() ? TextUtil.EMPTY : model.getPaymentTypes().get(0).getId(),
            session.getInitRepository(), session.getCongratsRepository(),
            session.getConfigurationModule().getCustomTextsRepository());
    }

    @Override
    public boolean handleBack() {
        final boolean isExploding = payButtonFragment.isExploding();
        if (!isExploding) {
            presenter.onBack();
        }
        return isExploding;
    }

    interface OnMethodSelectedListener {
        void onItemSelected(@NonNull final OfflineMethodItem selectedMethod);
    }

    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        final int offset = getResources().getInteger(R.integer.px_long_animation_time);
        final int duration = getResources().getInteger(R.integer.px_shorter_animation_time);
        final Animation animation =
            AnimationUtils.loadAnimation(getContext(), enter ? R.anim.px_fade_in : R.anim.px_fade_out);
        animation.setDuration(duration);
        if (enter) {
            animation.setStartOffset(offset);
        }
        header.startAnimation(animation);
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onSlideSheet(final float offset) {
        header.setAlpha(offset >= 0 ? offset : 0);
    }

    @Override
    public void startKnowYourCustomerFlow(@NonNull final String flowLink) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(flowLink));
        startActivity(intent);
    }

    @Override
    public void prePayment(@NonNull PayButton.OnReadyForPaymentCallback callback) {
        presenter.handlePrePaymentAction(callback);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        final int duration = getResources().getInteger(R.integer.px_shorter_animation_time);
        fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.px_fade_in);
        fadeInAnimation.setDuration(duration);
        fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.px_fade_out);
        fadeOutAnimation.setDuration(duration);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fadeInAnimation = null;
        fadeOutAnimation = null;
    }
}