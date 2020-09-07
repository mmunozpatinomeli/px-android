package com.mercadopago

import android.content.Context
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.mercadopago.android.px.core.SplitPaymentProcessor
import com.mercadopago.android.px.model.IParcelablePaymentDescriptor
import com.mercadopago.android.px.model.IPayment
import com.mercadopago.android.px.model.internal.GenericPaymentDescriptor
import com.mercadopago.android.px.preferences.CheckoutPreference

open class SamplePaymentProcessor @JvmOverloads constructor(private val visualProcessor: Boolean = false,
    private vararg val payments: IParcelablePaymentDescriptor) : SplitPaymentProcessor {
    private val handler = Handler()
    private var index = 0

    @JvmOverloads constructor(visualProcessor: Boolean = false, vararg payments: IPayment) : this(
        visualProcessor,
        *payments.map { GenericPaymentDescriptor.with(it) }.toTypedArray())

    private constructor(parcel: Parcel) : this(
        parcel.readInt() == 1,
        *mutableListOf<IParcelablePaymentDescriptor>().apply {
            parcel.readList(this as List<*>, IParcelablePaymentDescriptor::class.java.classLoader)
        }.toTypedArray()) {
        index = parcel.readInt()
    }

    override fun startPayment(context: Context, data: SplitPaymentProcessor.CheckoutData,
        paymentListener: SplitPaymentProcessor.OnPaymentListener) {
        handler.postDelayed({ paymentListener.onPaymentFinished(getCurrentPayment()) }, LOADING_TIME.toLong())
    }

    override fun getPaymentTimeout(checkoutPreference: CheckoutPreference) = TIMEOUT

    override fun shouldShowFragmentOnPayment(checkoutPreference: CheckoutPreference) = visualProcessor

    override fun supportsSplitPayment(checkoutPreference: CheckoutPreference?) = true

    override fun getFragment(data: SplitPaymentProcessor.CheckoutData, context: Context): Fragment? {
        return SamplePaymentProcessorFragment.with(getCurrentPayment())
    }

    private fun getCurrentPayment() : IParcelablePaymentDescriptor {
        val currentIndex = index++
        val fixedIndex = currentIndex % payments.size
        return payments[fixedIndex]
    }

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (visualProcessor) 1 else 0)
        parcel.writeList(payments.toList())
        parcel.writeInt(index)
    }

    companion object {
        private const val TIMEOUT = 20000
        private const val LOADING_TIME = 2000
        @JvmField val CREATOR = object : Parcelable.Creator<SamplePaymentProcessor> {
            override fun createFromParcel(parcel: Parcel) = SamplePaymentProcessor(parcel)
            override fun newArray(size: Int) = arrayOfNulls<SamplePaymentProcessor>(size)
        }
    }
}