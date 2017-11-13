package com.daocore

import android.content.Context
import com.daocore.adapter.CartProductAdapter
import com.daocore.adapter.CustomerAdapter
import com.daocore.entity.CartProductData
import com.daocore.entity.CartProductDataEntity
import com.daocore.entity.CustomerDataEntity
import com.daocore.entity.Models
import com.domain.entity.CartProduct
import com.domain.entity.Customer
import com.domain.entity.Error
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.reactivex.KotlinReactiveEntityStore
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode

class DaoImpl(context: Context) : Dao {

    private val store: KotlinReactiveEntityStore<Persistable>

    init {
        val source = DatabaseSource(context, Models.DEFAULT, 1)
        source.setTableCreationMode(TableCreationMode.DROP_CREATE)
        store = KotlinReactiveEntityStore(KotlinEntityDataStore(source.configuration))
    }

    override fun getCartDataList(): Observable<List<CartProduct>> {
        return store.select(CartProductDataEntity::class)
                .get()
                .observableResult()
                .map { it -> it.asIterable() }
                .map {
                    val list: MutableList<CartProduct> = mutableListOf()
                    it.iterator().forEach { list.add(CartProductAdapter.adaptFromStore(it)) }
                    list
                }
    }

    override fun addCartProduct(cartProduct: CartProduct): Single<CartProduct> {
        val productVariant = cartProduct.productVariant
        var storeItem: CartProductData? = store.select(CartProductDataEntity::class)
                .where(CartProductDataEntity.ID.eq(productVariant.id))
                .get()
                .singleOrNull()
        if (storeItem != null) {
            storeItem.quantity = storeItem.quantity + cartProduct.quantity
        } else {
            storeItem = CartProductAdapter.adaptToStore(cartProduct)
        }
        return store.upsert(storeItem).map { CartProductAdapter.adaptFromStore(it) }
    }

    override fun deleteProductFromCart(productVariantId: String): Completable {
        val storeItem: CartProductData? = store.select(CartProductDataEntity::class)
                .where(CartProductDataEntity.ID.eq(productVariantId))
                .get()
                .singleOrNull()

        return storeItem?.let { store.delete(storeItem) } ?: Completable.complete()
    }

    override fun changeCartProductQuantity(productVariantId: String, newQuantity: Int): Single<CartProduct> {
        val storeItem: CartProductData? = store.select(CartProductDataEntity::class)
                .where(CartProductDataEntity.ID.eq(productVariantId))
                .get()
                .singleOrNull()

        return storeItem?.let {
            it.quantity = newQuantity
            store.update(storeItem).map { CartProductAdapter.adaptFromStore(it) }
        } ?: Single.error(Error.NonCritical("Product not found"))
    }

    override fun saveCustomer(customer: Customer): Single<Customer> {
        store.delete(CustomerDataEntity::class).get().value()
        return store.insert(CustomerAdapter.adaptToStore(customer)).map { customer }
    }

    override fun isLoggedIn(): Single<Boolean> {

        return Single.create<Boolean> {
            val result = store.select(CustomerDataEntity::class).get().toList()
            it.onSuccess(result.isNotEmpty())
        }
    }
}