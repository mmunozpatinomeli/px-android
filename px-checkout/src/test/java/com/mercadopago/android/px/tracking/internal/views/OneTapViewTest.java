package com.mercadopago.android.px.tracking.internal.views;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.core.FlowIdProvider;
import com.mercadopago.android.px.internal.di.NetworkModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.PXTracker;
import com.mercadopago.android.px.tracking.PXTrackingListener;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import com.mercadopago.android.px.tracking.internal.model.DiscountInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(Session.class)
@RunWith(PowerMockRunner.class)
public class OneTapViewTest {

    private static final String EXPECTED_PATH = "/px_checkout/review/one_tap";
    private static final int DISABLED_METHODS_QUANTITY = 0;

    @Mock
    private DiscountConfigurationModel discountModel;

    @Mock private CheckoutPreference checkoutPreference;

    @Test
    public void verifyPath() {
        assertEquals(EXPECTED_PATH,
            new OneTapViewTracker(Collections.EMPTY_LIST, checkoutPreference, discountModel, Collections.emptySet(),
                Collections.emptySet(), DISABLED_METHODS_QUANTITY).getTrack().getPath());
    }

    @Test
    public void verifyListenerCalled() {
        final Session session = mock(Session.class);
        final NetworkModule networkModule = mock(NetworkModule.class);
        PowerMockito.mockStatic(Session.class);
        when(Session.getInstance()).thenReturn(session);
        when(session.getNetworkModule()).thenReturn(networkModule);
        when(networkModule.getFlowIdProvider()).thenReturn(mock(FlowIdProvider.class));
        MPTracker.getInstance().hasExpressCheckout(true);

        final PXTrackingListener listener = mock(PXTrackingListener.class);
        PXTracker.setListener(listener);
        final OneTapViewTracker tracker =
            new OneTapViewTracker(Collections.EMPTY_LIST, checkoutPreference, discountModel, Collections.emptySet(),
                Collections.emptySet(), DISABLED_METHODS_QUANTITY);
        tracker.track();
        verify(listener).onView(eq(EXPECTED_PATH), eq(expectedOneTapData()));
    }

    @NonNull
    private Map<String, Object> expectedOneTapData() {
        final Map<String, Object> data = new HashMap<>();
        data.put("discount", JsonUtil.fromJson("{}", DiscountInfo.class).toMap());
        data.put("available_methods", Collections.EMPTY_LIST);
        data.put("available_methods_quantity", 0);
        data.put("disabled_methods_quantity", 0);
        data.put("items", Collections.EMPTY_LIST);
        data.put("flow", null);
        data.put("preference_amount", null);
        data.put("session_id", null);
        data.put("flow_detail", Collections.EMPTY_MAP);
        data.put("session_time", 0L);
        data.put("checkout_type", "one_tap");
        data.put("security_enabled", false);
        data.put("experiments", "");
        return data;
    }
}