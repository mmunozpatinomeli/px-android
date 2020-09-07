package com.mercadopago.android.px.internal.features.explode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;

public class ExplodingFragment extends Fragment {

    public static final String TAG = "TAG_EXPLODING_FRAGMENT";
    private static final String BUNDLE_DECORATOR = "BUNDLE_DECORATOR";
    private static final String ARG_PROGRESS_TEXT = "ARG_PROGRESS_TEXT";
    private static final String ARG_TIMEOUT = "ARG_TIMEOUT";

    public static final float ICON_SCALE = 3.0f;

    /* default */ ProgressBar progressBar;
    /* default */ ObjectAnimator animator;
    /* default */ ImageView icon;
    /* default */ ImageView circle;
    /* default */ View reveal;
    private TextView text;
    private ViewGroup rootView;
    private View loadingContainer;

    /* default */ @Nullable ExplodeDecorator explodeDecorator;
    /* default */ @Nullable Animator circularReveal;
    private int buttonHeight;
    private int buttonYPosition;
    private CharSequence buttonText;
    private int maxLoadingTime;

    /* default */ @Nullable Handler handler;

    @NonNull private final View.OnLayoutChangeListener layoutChangeListener =
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (handler != null) {
                updateValuesFromParentView(handler.getParentView());
            }
        };

    public boolean hasFinished() {
        return explodeDecorator == null;
    }

    public interface Handler {
        View getParentView();

        void onAnimationFinished();
    }

    public static ExplodingFragment newInstance(@NonNull final CharSequence progressText, final int timeout) {
        final ExplodingFragment explodingFragment = new ExplodingFragment();
        final Bundle bundle = new Bundle();
        bundle.putCharSequence(ARG_PROGRESS_TEXT, progressText);
        bundle.putInt(ARG_TIMEOUT, timeout);
        explodingFragment.setArguments(bundle);
        return explodingFragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            buttonText = args.getCharSequence(ARG_PROGRESS_TEXT);
            maxLoadingTime = args.getInt(ARG_TIMEOUT);
        } else {
            throw new RuntimeException("Missing explode params");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
        final Bundle savedInstanceState) {
        final ViewGroup view = getActivityContentView();

        if (savedInstanceState != null) {
            explodeDecorator = savedInstanceState.getParcelable(BUNDLE_DECORATOR);
        }

        rootView = (ViewGroup) inflater.inflate(R.layout.px_fragment_exploding, view);
        circle = rootView.findViewById(R.id.cho_loading_buy_circular);
        icon = rootView.findViewById(R.id.cho_loading_buy_icon);
        reveal = rootView.findViewById(R.id.cho_loading_buy_reveal);
        text = rootView.findViewById(R.id.cho_loading_buy_progress_text);
        loadingContainer = rootView.findViewById(R.id.cho_loading_buy_container);
        if (!TextUtil.isEmpty(buttonText)) {
            text.setText(buttonText);
        }

        progressBar = rootView.findViewById(R.id.cho_loading_buy_progress);
        progressBar.setMax(maxLoadingTime);

        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        rootView.addOnLayoutChangeListener(layoutChangeListener);

        if (handler != null) {
            updateValuesFromParentView(handler.getParentView());
        }

        // start loading assuming the worst time possible
        animator = ObjectAnimator.ofInt(progressBar, "progress", 0, maxLoadingTime);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(maxLoadingTime).start();

        if (explodeDecorator != null) {
            finishLoading(explodeDecorator);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (circularReveal != null) {
            circularReveal.cancel();
            circularReveal = null;
        }
        rootView.removeOnLayoutChangeListener(layoutChangeListener);
    }

    @Override
    public void onDestroyView() {
        final ViewGroup view = getActivityContentView();
        view.removeViewAt(view.getChildCount() - 1);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putParcelable(BUNDLE_DECORATOR, explodeDecorator);
        super.onSaveInstanceState(outState);
    }

    private void adjustHeight(final ImageView view) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = buttonHeight;
        params.width = buttonHeight;
        view.setLayoutParams(params);
    }

    /**
     * Notify this view that the loading has finish so as to start the finish anim.
     *
     * @param explodeDecorator information about the order result, useful for styling the view.
     */
    public void finishLoading(@NonNull final ExplodeDecorator explodeDecorator) {
        this.explodeDecorator = explodeDecorator;
        runIfVisibleAndReady(this::doFinishLoading);
    }

    /**
     * Transform the progress bar into the result icon background. The color and the shape are animated.
     */
    /* default */ void createResultAnim() {
        @ColorInt
        final int color = explodeDecorator.getDarkPrimaryColor(getContext());
        circle.setColorFilter(color);
        icon.setImageResource(explodeDecorator.getStatusIcon());
        final int duration = getResources().getInteger(R.integer.px_long_animation_time);
        final int initialWidth = progressBar.getWidth();
        final int finalSize = progressBar.getHeight();
        final int initialRadius = getResources().getDimensionPixelOffset(R.dimen.px_xxxs_margin);
        final int finalRadius = finalSize / 2;

        final GradientDrawable initialBg =
            getProgressBarShape(ContextCompat.getColor(getContext(), R.color.ui_action_button_pressed), initialRadius);
        final GradientDrawable finalBg = getProgressBarShape(color, initialRadius);
        final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[] { initialBg, finalBg });
        progressBar.setProgressDrawable(transitionDrawable);
        transitionDrawable.startTransition(duration);

        final ValueAnimator a = ValueAnimator.ofFloat(0, 1);
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float interpolatedTime = animation.getAnimatedFraction();
                final int radius = getNewRadius(interpolatedTime);
                setRadius(initialBg, radius);
                setRadius(finalBg, radius);
                progressBar.getLayoutParams().width = getNewWidth(interpolatedTime);
                progressBar.requestLayout();
            }

            private int getNewRadius(final float t) {
                return initialRadius + (int) ((finalRadius - initialRadius) * t);
            }

            private int getNewWidth(final float t) {
                return initialWidth + (int) ((finalSize - initialWidth) * t);
            }

            private void setRadius(final Drawable bg, final int value) {
                final GradientDrawable layerBg = (GradientDrawable) bg;
                layerBg.setCornerRadius(value);
            }
        });

        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                animation.removeAllListeners();
                ((ValueAnimator) animation).removeAllUpdateListeners();
                runIfVisibleAndReady(() -> createResultIconAnim());
            }
        });
        a.setInterpolator(new DecelerateInterpolator(2f));
        a.setDuration(duration);
        a.start();
        text.setVisibility(View.GONE);
    }

    /**
     * @return the shape of the progress bar to transform
     */
    private GradientDrawable getProgressBarShape(final int color, final int radius) {
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * Now that the icon background is visible, animate the icon. The icon will start big and transparent and become
     * small and opaque
     */
    /* default */ void createResultIconAnim() {
        progressBar.setVisibility(View.INVISIBLE);
        icon.setVisibility(View.VISIBLE);
        circle.setVisibility(View.VISIBLE);

        icon.setScaleY(ICON_SCALE);
        icon.setScaleX(ICON_SCALE);
        icon.setAlpha(0f);
        icon.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f)
            .setInterpolator(new DecelerateInterpolator(2f))
            .setDuration(getResources().getInteger(R.integer.px_default_animation_time))
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    animation.removeAllListeners();
                    runIfVisibleAndReady(() -> createCircularReveal());
                }
            }).start();
    }

    /**
     * Wait so that the icon is visible for a while.. then fill the whole screen with the appropriate color.
     */
    /* default */ void createCircularReveal() {
        // when the icon anim has finished, paint the whole screen with the result color
        final float finalRadius = (float) Math.hypot(rootView.getWidth(), rootView.getHeight());
        final int startRadius = buttonHeight / 2;
        final int cx = (progressBar.getLeft() + progressBar.getRight()) / 2;
        final int cy = (progressBar.getTop() + progressBar.getBottom()) / 2 + buttonYPosition;

        final int endColor = explodeDecorator.getPrimaryColor(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circularReveal = ViewAnimationUtils.createCircularReveal(reveal, cx, cy, startRadius, finalRadius);
        } else {
            circularReveal = ObjectAnimator.ofFloat(reveal, "alpha", 0, 1);
        }
        circularReveal.setDuration(getResources().getInteger(R.integer.px_long_animation_time));
        circularReveal.setStartDelay(getResources().getInteger(R.integer.px_long_animation_time));
        circularReveal.setInterpolator(new AccelerateInterpolator());
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                circle.setVisibility(View.GONE);
                icon.setVisibility(View.GONE);
                reveal.setVisibility(View.VISIBLE);

                final int startColor = explodeDecorator.getDarkPrimaryColor(getContext());
                final Drawable[] switchColors =
                    { new ColorDrawable(startColor), new ColorDrawable(endColor) };
                final TransitionDrawable colorSwitch = new TransitionDrawable(switchColors);
                reveal.setBackground(colorSwitch);
                colorSwitch.startTransition((int) animation.getDuration());
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                circularReveal = null;
                explodeDecorator = null;
                tintStatusBar(endColor);
                if (handler != null) {
                    handler.onAnimationFinished();
                }
            }
        });

        circularReveal.start();
    }

    /* default */ void tintStatusBar(final int color) {
        final Activity activity = getActivity();
        if (activity != null) {
            ViewUtils.setStatusBarColor(color, activity.getWindow());
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        configureListener(context);
    }

    private void configureListener(final Context context) {
        if (context instanceof Handler) {
            handler = (Handler) context;
        } else if (getTargetFragment() != null) {
            handler = (Handler) getTargetFragment();
        } else if (getParentFragment() != null) {
            handler = (Handler) getParentFragment();
        }
    }

    private void doFinishLoading() {
        // now finish the remaining loading progress
        final int progress = progressBar.getProgress();
        animator.cancel();
        animator = ObjectAnimator.ofInt(progressBar, "progress", progress, maxLoadingTime);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(getResources().getInteger(R.integer.px_long_animation_time));

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                animator.removeListener(this);
                runIfVisibleAndReady(() -> createResultAnim());
            }
        });
        animator.start();
    }

    private void updateValuesFromParentView(final View parentView) {
        final int[] activityLocation = new int[2];
        getActivityContentView().getLocationOnScreen(activityLocation);
        final int[] parentLocation = new int[2];
        parentView.getLocationOnScreen(parentLocation);
        buttonYPosition = parentLocation[1] - activityLocation[1];
        buttonHeight = parentView.getHeight();

        final ViewGroup.MarginLayoutParams progressBarParams =
            (ViewGroup.MarginLayoutParams) progressBar.getLayoutParams();
        progressBarParams.height = buttonHeight;
        progressBarParams.leftMargin = parentLocation[0];
        progressBarParams.rightMargin = parentLocation[0];
        progressBar.setPadding(parentView.getPaddingStart(), parentView.getPaddingTop(), parentView.getPaddingRight(),
            parentView.getPaddingEnd());
        progressBar.setLayoutParams(progressBarParams);
        adjustHeight(circle);
        adjustHeight(icon);
        loadingContainer.setY(buttonYPosition);
    }

    private ViewGroup getActivityContentView() {
        return getActivity().findViewById(android.R.id.content);
    }

    /* default */ void runIfVisibleAndReady(@NonNull final Runnable runnable) {
        if (explodeDecorator == null) {
            ExplodeFrictionTracker.INSTANCE.track();
        } else {
            ViewUtils.runWhenViewIsAttachedToWindow(rootView, runnable);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler = null;
    }
}