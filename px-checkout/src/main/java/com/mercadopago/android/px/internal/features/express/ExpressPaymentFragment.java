package com.mercadopago.android.px.internal.features.express;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.mercadolibre.android.cardform.internal.CardFormWithFragment;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.BackHandler;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.di.CheckoutConfigurationModule;
import com.mercadopago.android.px.internal.di.MapperProvider;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.experiments.Variant;
import com.mercadopago.android.px.internal.features.disable_payment_method.DisabledPaymentMethodDetailDialog;
import com.mercadopago.android.px.internal.features.express.add_new_card.OfflineMethodsFragment;
import com.mercadopago.android.px.internal.features.express.add_new_card.OtherPaymentMethodFragment;
import com.mercadopago.android.px.internal.features.express.animations.ExpandAndCollapseAnimation;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnimationListener;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnimator;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentRowHolder;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentsAdapter;
import com.mercadopago.android.px.internal.features.express.slider.ConfirmButtonAdapter;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragment;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodHeaderAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SummaryViewAdapter;
import com.mercadopago.android.px.internal.features.express.slider.TitlePagerAdapter;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialog;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialogAction;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialogItem;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
import com.mercadopago.android.px.internal.features.pay_button.PayButtonFragment;
import com.mercadopago.android.px.internal.util.CardFormWithFragmentWrapper;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.DynamicHeightViewPager;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.OfflinePaymentTypesMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.StatusMetadata;
import com.mercadopago.android.px.model.internal.DisabledPaymentMethod;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapter.RenderMode.HIGH_RES;
import static com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapter.RenderMode.LOW_RES;

public class ExpressPaymentFragment extends Fragment implements ExpressPayment.View, ViewPager.OnPageChangeListener,
    InstallmentsAdapter.ItemListener, SplitPaymentHeaderAdapter.SplitListener,
    PaymentMethodFragment.DisabledDetailDialogLauncher, OtherPaymentMethodFragment.OnOtherPaymentMethodClickListener,
    TitlePagerAdapter.InstallmentChanged, PayButton.Handler, GenericDialog.Listener, BackHandler {

    private static final String TAG = ExpressPaymentFragment.class.getSimpleName();
    public static final String TAG_OFFLINE_METHODS_FRAGMENT = "TAG_OFFLINE_METHODS_FRAGMENT";
    private static final String TAG_HEADER_DYNAMIC_DIALOG = "TAG_HEADER_DYNAMIC_DIALOG";
    private static final String EXTRA_RENDER_MODE = "render_mode";

    private static final int REQ_CODE_DISABLE_DIALOG = 105;
    public static final int REQ_CODE_CARD_FORM = 106;
    private static final float PAGER_NEGATIVE_MARGIN_MULTIPLIER = -1.5f;

    @Nullable private CallBack callback;

    /* default */ ExpressPaymentPresenter presenter;

    private SummaryView summaryView;
    private View payButtonContainer;
    private RecyclerView installmentsRecyclerView;
    /* default */ DynamicHeightViewPager paymentMethodPager;
    /* default */ View pagerAndConfirmButtonContainer;
    private ScrollingPagerIndicator indicator;
    @Nullable private ExpandAndCollapseAnimation expandAndCollapseAnimation;
    @Nullable private FadeAnimator fadeAnimation;
    @Nullable private Animation slideUpAndFadeAnimation;
    @Nullable private Animation slideDownAndFadeAnimation;
    private InstallmentsAdapter installmentsAdapter;
    private PaymentMethodHeaderView paymentMethodHeaderView;
    private LabeledSwitch splitPaymentView;
    private PaymentMethodFragmentAdapter paymentMethodFragmentAdapter;
    private PaymentMethodFragmentAdapter.RenderMode renderMode;
    private View loading;

    private HubAdapter hubAdapter;
    /* default */ View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private PayButtonFragment payButtonFragment;
    private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks;

    public static Fragment getInstance() {
        return new ExpressPaymentFragment();
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        presenter.onSplitChanged(isChecked);
    }

    @Override
    public void prePayment(@NonNull final PayButton.OnReadyForPaymentCallback callback) {
        presenter.handlePrePaymentAction(callback);
    }

    @Override
    public void onPostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        presenter.onPostPaymentAction(postPaymentAction);
    }

    @Override
    public boolean handleBack() {
        final boolean isExploding = payButtonFragment.isExploding();
        if (!isExploding) {
            presenter.onBack();
        }
        return isExploding;
    }

    public interface CallBack {
        void onOneTapCanceled();
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null && fragmentManager.findFragmentByTag(CardFormWithFragment.TAG) != null) {
            final int duration = getResources().getInteger(R.integer.cf_anim_duration);
            final int offset = getResources().getInteger(R.integer.px_card_form_animation_offset);
            if (enter) {
                final Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_slide_up_in);
                slideUp.setStartOffset(offset);
                paymentMethodPager
                    .startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_slide_up_in));

                final Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.px_fade_in);
                fadeIn.setDuration(duration);
                fadeIn.setStartOffset(offset);

                paymentMethodHeaderView.startAnimation(fadeIn);
                splitPaymentView.startAnimation(fadeIn);
                indicator.startAnimation(fadeIn);
                payButtonContainer.startAnimation(slideUp);

                summaryView.animateEnter(duration);
            } else {
                final Animation slideDown =
                    AnimationUtils.loadAnimation(getContext(), R.anim.px_summary_slide_down_out);
                paymentMethodPager.startAnimation(slideDown);

                final Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.px_fade_out);
                fadeOut.setDuration(duration);

                paymentMethodHeaderView.startAnimation(fadeOut);
                indicator.startAnimation(fadeOut);
                payButtonContainer.startAnimation(slideDown);
                if (splitPaymentView.getVisibility() == VISIBLE) {
                    splitPaymentView.startAnimation(fadeOut);
                }

                summaryView.animateExit(offset);
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_express_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViews(view);

        presenter = createPresenter();
        if (savedInstanceState != null) {
            renderMode =
                (PaymentMethodFragmentAdapter.RenderMode) savedInstanceState.getSerializable(EXTRA_RENDER_MODE);
            presenter.recoverFromBundle(savedInstanceState);
        } else {
            presenter.onFreshStart();
        }

        presenter.attachView(this);

        summaryView.setOnLogoClickListener(v -> presenter.onHeaderClicked());

        paymentMethodPager.addOnPageChangeListener(this);
    }

    private void configureViews(@NonNull final View view) {
        payButtonFragment = (PayButtonFragment) getChildFragmentManager().findFragmentById(R.id.pay_button);
        payButtonContainer = view.findViewById(R.id.pay_button);
        splitPaymentView = view.findViewById(R.id.labeledSwitch);
        summaryView = view.findViewById(R.id.summary_view);
        loading = view.findViewById(R.id.loading);

        pagerAndConfirmButtonContainer = view.findViewById(R.id.container);
        paymentMethodPager = view.findViewById(R.id.payment_method_pager);
        indicator = view.findViewById(R.id.indicator);
        installmentsRecyclerView = view.findViewById(R.id.installments_recycler_view);
        expandAndCollapseAnimation = new ExpandAndCollapseAnimation(installmentsRecyclerView);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation()));

        paymentMethodPager.setPageMargin(
            ((int) (getResources().getDimensionPixelSize(R.dimen.px_m_margin) * PAGER_NEGATIVE_MARGIN_MULTIPLIER)));
        paymentMethodPager.setOffscreenPageLimit(2);
        slideDownAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, INVISIBLE));
        slideUpAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, VISIBLE));

        paymentMethodHeaderView = view.findViewById(R.id.installments_header);

        paymentMethodHeaderView.setListener(new PaymentMethodHeaderView.Listener() {
            @Override
            public void onDescriptorViewClicked() {
                presenter.onInstallmentsRowPressed();
            }

            @Override
            public void onInstallmentsSelectorCancelClicked() {
                presenter.onInstallmentSelectionCanceled();
            }

            @Override
            public void onDisabledDescriptorViewClick() {
                presenter.onDisabledDescriptorViewClick();
            }
        });

        if (getActivity() instanceof AppCompatActivity) {
            summaryView.configureToolbar((AppCompatActivity) getActivity(), v -> presenter.cancel());
        }

        final TitlePager titlePager = view.findViewById(R.id.title_pager);
        final TitlePagerAdapter titlePagerAdapter = new TitlePagerAdapter(titlePager, this);
        titlePager.setAdapter(titlePagerAdapter);

        hubAdapter = new HubAdapter(Arrays.asList(titlePagerAdapter,
            new SummaryViewAdapter(summaryView),
            new SplitPaymentHeaderAdapter(splitPaymentView, this),
            new PaymentMethodHeaderAdapter(paymentMethodHeaderView),
            new ConfirmButtonAdapter(payButtonFragment)
        ));

        configureBottomSheet();
        registerFragmentLifecycleCallbacks();
    }

    private void configureBottomSheet() {
        final FragmentManager manager = getFragmentManager();
        final Activity activity = getActivity();
        if (activity != null) {
            bottomSheet = activity.findViewById(R.id.off_methods_fragment);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull final View view, final int state) {
                    if (manager != null && state == BottomSheetBehavior.STATE_HIDDEN) {
                        manager.popBackStackImmediate();
                    }
                }

                @Override
                public void onSlide(@NonNull final View view, final float v) {
                    if (manager != null) {
                        final OfflineMethodsFragment fragment = FragmentUtil
                            .getFragmentByTag(manager, TAG_OFFLINE_METHODS_FRAGMENT,
                                OfflineMethodsFragment.class);
                        if (fragment != null) {
                            fragment.onSlideSheet(v);
                        }
                    }
                }
            });
        }
    }

    private void registerFragmentLifecycleCallbacks() {
        final FragmentManager manager = getFragmentManager();
        fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentAttached(@NonNull final FragmentManager fm, @NonNull final Fragment fragment,
                @NonNull final Context context) {
                if (fragment instanceof OfflineMethodsFragment) {
                    presenter.onOtherPaymentMethodClickableStateChanged(false);
                }
                super.onFragmentAttached(fm, fragment, context);
            }

            @Override
            public void onFragmentDetached(@NonNull final FragmentManager fm, @NonNull final Fragment fragment) {
                if (fragment instanceof ExpressPaymentFragment) {
                    fm.unregisterFragmentLifecycleCallbacks(this);
                } else if (fragment instanceof OfflineMethodsFragment) {
                    bottomSheet.setVisibility(GONE);
                    presenter.onOtherPaymentMethodClickableStateChanged(true);
                }
                super.onFragmentDetached(fm, fragment);
            }
        };

        if (manager != null) {
            manager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
        }
    }

    @Override
    public void onDestroyView() {
        final FragmentManager manager = getFragmentManager();
        if (manager != null) {
            manager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
        }
        super.onDestroyView();
    }

    private ExpressPaymentPresenter createPresenter() {
        final Session session = Session.getInstance();
        final CheckoutConfigurationModule configurationModule = session.getConfigurationModule();
        return new ExpressPaymentPresenter(session.getPaymentRepository(),
            configurationModule.getPaymentSettings(),
            configurationModule.getDisabledPaymentMethodRepository(),
            configurationModule.getPayerCostSelectionRepository(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getInitRepository(),
            session.getAmountConfigurationRepository(),
            session.getConfigurationModule().getChargeRepository(),
            session.getMercadoPagoESC(),
            MapperProvider.INSTANCE.getPaymentMethodDrawableItemMapper(getContext()),
            session.getCongratsRepository(),
            session.getExperimentsRepository(),
            configurationModule.getPayerComplianceRepository(),
            configurationModule.getTrackingRepository(),
            MapperProvider.INSTANCE.getPaymentMethodDescriptorMapper(),
            configurationModule.getCustomTextsRepository());
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putSerializable(EXTRA_RENDER_MODE, renderMode);
        if (presenter != null) {
            super.onSaveInstanceState(presenter.storeInBundle(outState));
        } else {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        fadeAnimation = new FadeAnimator(context);
        slideDownAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_down_and_fade);
        slideUpAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_up_and_fade);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        fadeAnimation = null;
        expandAndCollapseAnimation = null;
        slideDownAndFadeAnimation = null;
        slideUpAndFadeAnimation = null;
        if(presenter!=null) {
            presenter.detachView();
        }
        super.onDetach();
    }

    @Override
    public void clearAdapters() {
        paymentMethodPager.setAdapter(null);
    }

    @Override
    public void configureAdapters(@NonNull final Site site, @NonNull final Currency currency) {
        installmentsAdapter = new InstallmentsAdapter(this);
        installmentsRecyclerView.setAdapter(installmentsAdapter);
        installmentsRecyclerView.setVisibility(GONE);

        // Order is important, should update all others adapters before update paymentMethodAdapter

        if (paymentMethodPager.getAdapter() == null) {
            paymentMethodFragmentAdapter = new PaymentMethodFragmentAdapter(getChildFragmentManager());
            if (renderMode == null) {
                summaryView.setMeasureListener((itemsClipped) -> {
                    renderMode = itemsClipped ? LOW_RES : HIGH_RES;
                    onRenderModeDecided();
                });
            } else {
                onRenderModeDecided();
            }
            paymentMethodPager.setAdapter(paymentMethodFragmentAdapter);
            indicator.attachToPager(paymentMethodPager);
        }
    }

    @Override
    public void updateAdapters(@NonNull final HubAdapter.Model model) {
        hubAdapter.update(model);
    }

    @Override
    public void updatePaymentMethods(@NonNull final List<DrawableFragmentItem> items) {
        paymentMethodFragmentAdapter.setItems(items);
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    @Override
    public void showInstallmentsList(final int selectedIndex, @NonNull final List<InstallmentRowHolder.Model> models) {
        animateViewPagerDown();
        installmentsRecyclerView.scrollToPosition(selectedIndex);
        installmentsAdapter.setModels(models);
        installmentsAdapter.setPayerCostSelected(selectedIndex);
        installmentsAdapter.notifyDataSetChanged();
        hubAdapter.showInstallmentsList();
        expandAndCollapseAnimation.expand();
    }

    @Override
    public void installmentSelectedChanged(final int installment) {
        paymentMethodFragmentAdapter.updateInstallment(installment);
    }

    private void animateViewPagerDown() {
        paymentMethodPager.startAnimation(slideDownAndFadeAnimation);
        fadeAnimation.fadeOutFast(payButtonContainer);
        fadeAnimation.fadeOutFast(indicator);
    }

    @Override
    public void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        summaryView.showToolbarElementDescriptor(elementDescriptorModel);
    }

    @Override
    public void showDisabledPaymentMethodDetailDialog(@NonNull final DisabledPaymentMethod disabledPaymentMethod,
        @NonNull final StatusMetadata currentStatus) {
        DisabledPaymentMethodDetailDialog
            .showDialog(this, REQ_CODE_DISABLE_DIALOG, disabledPaymentMethod.getPaymentStatusDetail(), currentStatus);
    }

    @Override
    public void collapseInstallmentsSelection() {
        paymentMethodPager.startAnimation(slideUpAndFadeAnimation);
        fadeAnimation.fadeIn(payButtonContainer);
        fadeAnimation.fadeIn(indicator);
        expandAndCollapseAnimation.collapse();
        paymentMethodFragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_DISABLE_DIALOG) {
            setPagerIndex(0);
        } else if (requestCode == REQ_CODE_CARD_FORM) {
            handleCardFormResult(resultCode);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void handleCardFormResult(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onCardFormResult();
        }
    }

    @Override
    public void updateViewForPosition(final int paymentMethodIndex,
        final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        hubAdapter.updateData(paymentMethodIndex, payerCostSelected, splitSelectionState);
    }

    @Override
    public void onClick(final PayerCost payerCostSelected) {
        presenter.onPayerCostSelected(payerCostSelected);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        hubAdapter.updatePosition(positionOffset, position);
    }

    @Override
    public void onPageSelected(final int position) {
        presenter.onSliderOptionSelected(position);
        VibrationUtils.smallVibration(getContext());
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // do nothing.
    }

    @Override
    public void showDiscountDetailDialog(@NonNull final Currency currency,
        @NonNull final DiscountConfigurationModel discountModel) {
        DiscountDetailDialog.showDialog(getChildFragmentManager(), discountModel);
    }

    @Override
    public void setPagerIndex(final int index) {
        paymentMethodPager.setCurrentItem(index);
    }

    @Override
    public void showDynamicDialog(@NonNull final DynamicDialogCreator creator,
        @NonNull final DynamicDialogCreator.CheckoutData checkoutData) {
        final Context context;
        if ((context = getContext()) != null && creator.shouldShowDialog(context, checkoutData)) {
            creator.create(getContext(), checkoutData).show(getChildFragmentManager(),
                TAG_HEADER_DYNAMIC_DIALOG);
        }
    }

    private void onRenderModeDecided() {
        //Workaround to pager not updating the fragments
        paymentMethodPager.post(() -> paymentMethodFragmentAdapter.setRenderMode(renderMode));
    }

    @Override
    public int getRequestCode() {
        return REQ_CODE_DISABLE_DIALOG;
    }

    @Override
    public void onOtherPaymentMethodClicked(@NonNull final OfflinePaymentTypesMetadata offlineMethods) {
        presenter.onOtherPaymentMethodClicked(offlineMethods);
    }

    @Override
    public void showOfflineMethods(@NonNull final OfflinePaymentTypesMetadata offlineMethods) {
        final FragmentManager manager = getFragmentManager();
        bottomSheet.setVisibility(VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        if (manager != null && FragmentUtil.getFragmentByTag(manager, TAG_OFFLINE_METHODS_FRAGMENT) == null) {
            final OfflineMethodsFragment instance = OfflineMethodsFragment.getInstance(offlineMethods);
            manager.beginTransaction()
                .setCustomAnimations(R.anim.px_off_methods_slide_up_in, 0, 0, R.anim.px_off_methods_slide_down_out)
                .replace(R.id.off_methods_fragment, instance,
                    TAG_OFFLINE_METHODS_FRAGMENT)
                .addToBackStack(TAG_OFFLINE_METHODS_FRAGMENT)
                .commitAllowingStateLoss();
            instance.setTargetFragment(this, 1);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void updateBottomSheetStatus(final boolean hasToExpand) {
        if (hasToExpand) {
            bottomSheet.setVisibility(VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onAction(@NonNull final GenericDialogAction action) {
        if (action instanceof GenericDialogAction.DeepLinkAction) {
            startDeepLink(((GenericDialogAction.DeepLinkAction) action).getDeepLink());
        } else if (action instanceof GenericDialogAction.CustomAction) {
            presenter.handleGenericDialogAction(((GenericDialogAction.CustomAction) action).getType());
        }
    }

    @Override
    public void showGenericDialog(@NonNull final GenericDialogItem item) {
        GenericDialog.showDialog(getChildFragmentManager(), item);
    }

    @Override
    public void startAddNewCardFlow(final CardFormWithFragmentWrapper cardFormWithFragmentWrapper) {
        final FragmentManager manager = getFragmentManager();
        if (manager != null) {
            cardFormWithFragmentWrapper.getCardFormWithFragment()
                .start(manager, REQ_CODE_CARD_FORM, R.id.one_tap_fragment);
        }
    }

    @Override
    public void startDeepLink(@NonNull final String deepLink) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)));
        } catch (final ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void onDeepLinkReceived() {
        presenter.handleDeepLink();
    }

    @Override
    public void showLoading() {
        loading.setVisibility(VISIBLE);
    }

    @Override
    public void hideLoading() {
        loading.setVisibility(GONE);
    }

    @Override
    public void configurePaymentMethodHeader(@NonNull final List<Variant> variants) {
        paymentMethodHeaderView.configureExperiment(variants);
    }
}