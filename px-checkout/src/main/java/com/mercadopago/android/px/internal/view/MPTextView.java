package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsText;
import com.mercadopago.android.px.internal.font.FontHelper;
import com.mercadopago.android.px.internal.font.PxFont;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.internal.Text;

public class MPTextView extends AppCompatTextView {

    public MPTextView(final Context context) {
        this(context, null);
    }

    public MPTextView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MPTextView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MPTextView);
        final PxFont font = PxFont.from(a.getInt(R.styleable.MPTextView_customStyle, PxFont.REGULAR.id));
        a.recycle();

        if (!isInEditMode()) {
            FontHelper.setFont(this, font);
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                configureEllipsize();
            }
        });
    }

    public void setText(@NonNull final Text text) {
        setText(text.getMessage());
        ViewUtils.setTextColor(this, text.getTextColor());
        if (TextUtil.isNotEmpty(text.getWeight())) {
            FontHelper.setFont(this, PxFont.from(text.getWeight()));
        }
    }

    public void setText(@NonNull final PaymentCongratsText text) {
        setText(text.getMessage());
        ViewUtils.setTextColor(this, text.getTextColor());
        if (TextUtil.isNotEmpty(text.getWeight())) {
            FontHelper.setFont(this, PxFont.from(text.getWeight()));
        }
    }

    private void configureEllipsize() {
        final TextUtils.TruncateAt truncateAt = getEllipsize();
        if (truncateAt != null && truncateAt.equals(TextUtils.TruncateAt.END) && getLineCount() > getMaxLines()) {

            final int indexLastLine = getLayout().getLineEnd(getMaxLines() - 1);
            final String text = getText().subSequence(0, indexLastLine - 3) + "...";
            setText(text);
        }
    }
}