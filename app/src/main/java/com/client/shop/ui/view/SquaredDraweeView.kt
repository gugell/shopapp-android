package com.client.shop.ui.view

import android.content.Context
import android.util.AttributeSet
import com.facebook.drawee.view.SimpleDraweeView

class SquaredDraweeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SimpleDraweeView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}