package com.mercadopago.android.px

import org.mockito.ArgumentCaptor

/**
 * For call in java code, inline fun not supported in java
 */

fun <T : Any> argumentCaptor(tClass: Class<T>):KArgumentCaptor<T> {
    return KArgumentCaptor(ArgumentCaptor.forClass(tClass))
}


/**
 * See https://stackoverflow.com/questions/45949584/how-does-the-reified-keyword-in-kotlin-work
 */
inline fun <reified T : Any> argumentCaptor(): KArgumentCaptor<T> {
    return KArgumentCaptor(ArgumentCaptor.forClass(T::class.java))
}

class KArgumentCaptor<T : Any?>(
        private val captor: ArgumentCaptor<T>
) {
    val value: T
    get() = captor.value

    @Suppress("UNCHECKED_CAST")
    fun capture(): T {
        return captor.capture() ?: castNull()
    }
}