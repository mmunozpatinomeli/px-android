package com.mercadopago.android.px.internal.util;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.uicontrollers.card.BackCardView;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.features.uicontrollers.card.IdentificationCardView;

public final class MPAnimationUtils {

    private static final int FADE_DURATION = 300;
    private static final int ANIMATION_EXTRA_FACTOR = 3;

    private MPAnimationUtils() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void fadeInLollipop(final int color, final ImageView imageView) {
        ViewUtils.runWhenViewIsAttachedToWindow(imageView, () -> {
            imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), color),
                PorterDuff.Mode.SRC_ATOP);

            final int width = imageView.getWidth();

            final Animator anim = ViewAnimationUtils.createCircularReveal(imageView, -width, 0,
                width, ANIMATION_EXTRA_FACTOR * width);
            anim.setDuration(FADE_DURATION);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void fadeOutLollipop(final int color, final ImageView imageView) {
        ViewUtils.runWhenViewIsAttachedToWindow(imageView, () -> {
            final int width = imageView.getWidth();
            final Animator anim = ViewAnimationUtils.createCircularReveal(imageView, -width, 0,
                ANIMATION_EXTRA_FACTOR * width, width);
            anim.setDuration(FADE_DURATION);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(final Animator animation) {
                    //Do something
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), color),
                        PorterDuff.Mode.SRC_ATOP);
                }

                @Override
                public void onAnimationCancel(final Animator animation) {
                    //Do something
                }

                @Override
                public void onAnimationRepeat(final Animator animation) {
                    //Do something
                }
            });
            anim.start();
        });
    }

    public static void fadeIn(final int color, final ImageView imageView) {
        ViewUtils.runWhenViewIsAttachedToWindow(imageView, () -> {
            final Animation mAnimFadeIn = AnimationUtils.loadAnimation(imageView.getContext(), R.anim.px_fade_in);
            mAnimFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                    //Do something
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    imageView.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                    //Do something
                }
            });
            imageView.setBackgroundColor(ContextCompat.getColor(imageView.getContext(), color));
            imageView.startAnimation(mAnimFadeIn);
        });
    }

    public static void fadeOut(final int color, final ImageView imageView) {
        ViewUtils.runWhenViewIsAttachedToWindow(imageView, () -> {
            final Animation mAnimFadeOut = AnimationUtils.loadAnimation(imageView.getContext(), R.anim.px_fade_out);
            mAnimFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                    //Do something
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    imageView.setBackgroundColor(ContextCompat.getColor(imageView.getContext(), color));
                    imageView.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                    //Do something
                }
            });
            imageView.startAnimation(mAnimFadeOut);
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setImageViewColorLollipop(final ImageView imageView, final int color) {
        imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), color),
            PorterDuff.Mode.SRC_ATOP);
    }

    public static void setImageViewColor(final ImageView imageView, final int color) {
        imageView.setBackgroundColor(ContextCompat.getColor(imageView.getContext(), color));
    }

    public static void flipToBack(final float cameraDistance, final View frontView, final View backView,
        final BackCardView backCardView) {

        final AnimatorSet animFront =
            (AnimatorSet) AnimatorInflater.loadAnimator(frontView.getContext(), R.animator.px_card_flip_left_out);
        final AnimatorSet animBack =
            (AnimatorSet) AnimatorInflater.loadAnimator(frontView.getContext(), R.animator.px_card_flip_right_in);

        frontView.setCameraDistance(cameraDistance);
        animFront.setTarget(frontView);
        frontView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        backView.setCameraDistance(cameraDistance);
        animBack.setTarget(backView);
        backView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        animFront.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {
                //Do something
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                frontView.setAlpha(0);
                backView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                //Do something
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
                //Do something
            }
        });

        animFront.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                frontView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        animBack.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animation) {
                super.onAnimationStart(animation);
                if (backCardView != null) {
                    backCardView.show();
                }
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                backView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
        animFront.start();
        animBack.start();
    }

    public static void flipToFront(final float cameraDistance, final View frontView,
        final View backView) {

        final AnimatorSet animFront =
            (AnimatorSet) AnimatorInflater.loadAnimator(frontView.getContext(), R.animator.px_card_flip_left_in);
        final AnimatorSet animBack =
            (AnimatorSet) AnimatorInflater.loadAnimator(frontView.getContext(), R.animator.px_card_flip_right_out);

        frontView.setCameraDistance(cameraDistance);
        animFront.setTarget(frontView);
        frontView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        backView.setCameraDistance(cameraDistance);
        animBack.setTarget(backView);
        backView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        animBack.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {
                //Do something
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                backView.setAlpha(0);
                frontView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                //Do something
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
                //Do something
            }
        });

        animFront.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                frontView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        animBack.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                backView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        animBack.start();
        animFront.start();
    }

    public static void transitionCardAppear(final Context context, final CardView cardView,
        final IdentificationCardView identificationCardView) {

        final Animation animAppear = AnimationUtils.loadAnimation(context, R.anim.px_appear_from_right);
        final Animation animDisappear = AnimationUtils.loadAnimation(context, R.anim.px_dissapear_to_left);

        cardView.getView().startAnimation(animDisappear);

        animAppear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                //Do something
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                cardView.hide();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
                //Do something
            }
        });

        identificationCardView.getView().startAnimation(animAppear);
        identificationCardView.show();
    }

    public static void transitionCardDisappear(final Context context, final CardView cardView,
        final IdentificationCardView identificationCardView) {

        final Animation animAppear = AnimationUtils.loadAnimation(context, R.anim.px_appear_from_left);
        final Animation animDisappear = AnimationUtils.loadAnimation(context, R.anim.px_dissapear_to_right);

        identificationCardView.getView().startAnimation(animDisappear);

        animAppear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
                //Do something
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                identificationCardView.hide();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
                //Do something
            }
        });

        cardView.getView().startAnimation(animAppear);
        cardView.show();
    }
}