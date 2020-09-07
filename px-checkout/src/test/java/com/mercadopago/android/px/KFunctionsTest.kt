package com.mercadopago.android.px

import org.mockito.Mockito


/**
 * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 *
 * See https://medium.com/mobile-app-development-publication/befriending-kotlin-and-mockito-1c2e7b0ef791.
 * See https://stackoverflow.com/questions/30305217/is-it-possible-to-use-mockito-in-kotlin.
 */
fun <T> any(type : Class<T>): T {
    return Mockito.any(type) as T
}

/**
 * Uses a quirk in the bytecode generated by Kotlin
 * to cast [null] to a non-null type.
 *
 * See https://youtrack.jetbrains.com/issue/KT-8135.
 */
@Suppress("UNCHECKED_CAST")
fun <T> castNull(): T = null as T