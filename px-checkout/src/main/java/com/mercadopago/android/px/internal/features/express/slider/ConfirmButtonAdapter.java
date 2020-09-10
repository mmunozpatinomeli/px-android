package com.mercadopago.android.px.internal.features.express.slider;

import androidx.annotation.NonNull;
import android.view.View;
import com.mercadopago.android.px.internal.features.pay_button.PayButtonFragment;
import com.mercadopago.android.px.internal.viewmodel.ConfirmButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import java.util.List;

public class ConfirmButtonAdapter extends HubableAdapter<List<ConfirmButtonViewModel>, View> {

    private PayButtonFragment payButton;

    public ConfirmButtonAdapter(@NonNull final PayButtonFragment fragment) {
        super(null);
        payButton = fragment;
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        if (data.get(currentIndex).isDisabled()) {
            payButton.disable();
        } else {
            payButton.enable();
        }
    }

    @Override
    public List<ConfirmButtonViewModel> getNewModels(final HubAdapter.Model model) {
        return model.confirmButtonViewModels;
    }
}