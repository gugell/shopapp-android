package com.shopify.ui.checkout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.shopify.ShopifyWrapper
import com.shopify.api.R
import com.shopify.entity.Checkout
import com.shopify.ui.checkout.contract.CheckoutPresenter
import com.shopify.ui.checkout.contract.CheckoutView
import com.shopify.ui.checkout.di.CheckoutModule
import com.shopify.ui.payment.CardPaymentActivity
import com.ui.browser.BrowserActivity
import com.ui.lce.BaseActivity
import kotlinx.android.synthetic.main.activity_checkout.*
import javax.inject.Inject

class CheckoutActivity :
        BaseActivity<Checkout, CheckoutView, CheckoutPresenter>(),
        CheckoutView,
        View.OnClickListener {

    companion object {
        fun getStartIntent(context: Context) = Intent(context, CheckoutActivity::class.java)
    }

    @Inject lateinit var checkoutPresenter: CheckoutPresenter
    private var checkout: Checkout? = null

    //ANDROID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webPaymentButton.setOnClickListener(this)
        cardPaymentButton.setOnClickListener(this)

        loadData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (item.itemId == android.R.id.home) {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //INIT

    override fun inject() {
        ShopifyWrapper.component
                .attachCheckoutComponent(CheckoutModule())
                .inject(this)
    }

    override fun getContentView() = R.layout.activity_checkout

    override fun createPresenter() = checkoutPresenter

    //SETUP

    override fun loadData(pullToRefresh: Boolean) {
        super.loadData(pullToRefresh)
        presenter.createCheckout()
    }

    override fun showContent(data: Checkout) {
        super.showContent(data)
        checkout = data
    }

    //CALLBACK

    override fun onClick(v: View) {
        when (v) {
            webPaymentButton -> {
                checkout?.let {
                    startActivity(BrowserActivity.getStartIntent(this, it.webUrl, getString(R.string.checkout)))
                }
            }
            cardPaymentButton -> {
                checkout?.let {
                    startActivity(CardPaymentActivity.getStartIntent(this, it.checkoutId))
                }
            }
        }
    }
}