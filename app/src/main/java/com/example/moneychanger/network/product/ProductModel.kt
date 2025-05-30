package com.example.moneychanger.network.product

import android.os.Parcelable
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel(
    val productId: Long,
    val listId: Long, // 외래 키 추가 - 유빈
    val name: String,
    val originPrice: Double,
    val deletedYn : Boolean,
    val createdAt: String
): Parcelable
data class ProductRequestDto(
    val listId: Long,
    val originPrice: Double,
    val deletedYn : Boolean
)
@Parcelize
data class ProductResponseDto(
    @SerializedName("productId") val productId: Long,
    @SerializedName("listId") val listId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originPrice") val originPrice: Double,
    @SerializedName("deletedYn") val deletedYn: Boolean,
    @SerializedName("createdAt") val createdAt : String
): Parcelable

@Parcelize
data class ProductWithCurrencyDto(
    @SerializedName("productId") val productId: Long,
    @SerializedName("listId") val listId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originPrice") val originPrice: Double,
    @SerializedName("deletedYn") val deletedYn: Boolean,
    @SerializedName("createdAt") val createdAt : String,
    @SerializedName("currencyId") val currencyId : Long
): Parcelable

data class ImageProductResponseDto(
    val name: String,
    val price: Double,
)

data class CreateProductRequestDto(
    @SerializedName("listId") val listId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originPrice") val originPrice: Double,
)

data class CreateProductResponseDto(
    @SerializedName("productId") val productId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originPrice") val originPrice: Double,
    @SerializedName("listId") val listId: Long,
    @SerializedName("deletedYn") val deletedYn: Boolean,
    @SerializedName("createdAt") val createdAt : String
)

data class DeleteProductsRequestDto(
    @SerializedName("product_ids")val productIds: List<Long>
)

data class UpdateProductRequestDto(
    @SerializedName("productId") val productId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originPrice") val originPrice: Double,
)