package com.example.smarttrash.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>

    companion object {
        fun create(): OpenFoodFactsApi = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/api/v0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}

data class OpenFoodFactsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: OFFProduct?
)

data class OFFProduct(
    @SerializedName("product_name")    val productName: String?,
    @SerializedName("product_name_hu") val productNameHu: String?,
    @SerializedName("brands")          val brands: String?,
    @SerializedName("packaging")       val packaging: String?,
    @SerializedName("packaging_tags")  val packagingTags: List<String>?,
    @SerializedName("categories")      val categories: String?,
    @SerializedName("categories_tags") val categoriesTags: List<String>?,
    @SerializedName("image_url")       val imageUrl: String?
)