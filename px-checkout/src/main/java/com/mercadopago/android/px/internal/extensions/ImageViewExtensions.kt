package com.mercadopago.android.px.internal.extensions

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.mercadolibre.android.picassodiskcache.PicassoDiskLoader
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Transformation

@JvmOverloads
internal fun ImageView?.loadOrElse(url: String?, @DrawableRes fallback: Int, transformation: Transformation? = null) {
    this?.let {
        val picasso = PicassoDiskLoader.get(it.context)
        val requestCreator: RequestCreator = if (url.isNotNullNorEmpty()) picasso.load(url) else picasso.load(fallback)
        transformation?.let { requestCreator.transform(transformation) }
        requestCreator
            .placeholder(fallback)
            .error(fallback)
            .into(it)
    }
}