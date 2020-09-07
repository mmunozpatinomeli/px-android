package com.mercadopago.android.px.internal.mapper

import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodMapper
import com.mercadopago.android.px.mocks.InitResponseStub
import com.mercadopago.android.px.mocks.PaymentMethodStub
import com.mercadopago.android.px.model.internal.InitResponse
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PaymentMethodMapperTest {

    private lateinit var initResponse: InitResponse

    @Test
    fun whenPaymentMethodIdIsNull() {
        initResponse = InitResponseStub.ONLY_TICKET_MLA.get()
        val paymentMethodMock = PaymentMethodStub.VISA_CREDIT.get()
        val actual = PaymentMethodMapper(initResponse).map(Pair(paymentMethodMock.id, paymentMethodMock.paymentTypeId))

        assertNull(actual)
    }

    @Test
    fun whenPaymentMethodIdIsNotNull() {
        initResponse = InitResponseStub.FULL.get()

        val paymentMethodMock = PaymentMethodStub.VISA_CREDIT.get()
        val actual = PaymentMethodMapper(initResponse).map(Pair(paymentMethodMock.id, paymentMethodMock.paymentTypeId))

        assertEquals(actual.id, paymentMethodMock.id)
        assertEquals(actual.paymentTypeId, paymentMethodMock.paymentTypeId)
    }
}