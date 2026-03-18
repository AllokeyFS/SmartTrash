package com.example.smarttrash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttrash.data.remote.OpenFoodFactsApi
import com.example.smarttrash.data.repository.WasteRepository
import com.example.smarttrash.ui.screens.barcode.BarcodeUiState
import com.example.smarttrash.ui.screens.barcode.MaterialInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val repository: WasteRepository,
    private val openFoodFactsApi: OpenFoodFactsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<BarcodeUiState>(BarcodeUiState.Scanning)
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    private var lastScannedBarcode = ""

    fun onBarcodeDetected(barcode: String) {
        if (barcode == lastScannedBarcode) return
        if (_uiState.value !is BarcodeUiState.Scanning) return
        lastScannedBarcode = barcode
        _uiState.value = BarcodeUiState.Loading(barcode)
        viewModelScope.launch { lookupBarcode(barcode) }
    }

    private suspend fun lookupBarcode(barcode: String) {
        try {
            val response = openFoodFactsApi.getProduct(barcode)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.status == 1 && body.product != null) {
                    val product = body.product

                    val name = when {
                        !product.productNameHu.isNullOrBlank() -> product.productNameHu
                        !product.productName.isNullOrBlank()   -> product.productName
                        !product.brands.isNullOrBlank()        -> product.brands
                        else -> "Unknown Product"
                    }

                    var materials = parsePackagingMaterials(
                        packaging     = product.packaging,
                        packagingTags = product.packagingTags
                    )

                    // If the packaging is not defined, we guess by context
                    if (materials.isEmpty()) {
                        materials = guessPackagingByContext(
                            productName = name ?: "",
                            categories  = product.categories ?: "",
                            brands      = product.brands ?: ""
                        )
                    }

                    _uiState.value = BarcodeUiState.Found(
                        productName = name ?: "Unknown",
                        barcode     = barcode,
                        materials   = materials,
                        imageUrl    = product.imageUrl
                    )
                } else {
                    _uiState.value = BarcodeUiState.NotFound(barcode)
                }
            } else {
                _uiState.value = BarcodeUiState.NotFound(barcode)
            }
        } catch (e: Exception) {
            _uiState.value = BarcodeUiState.Error(
                barcode = barcode,
                message = "No internet connection"
            )
        }
    }
    //If Open Food Facts doesn't fill in the packaging field.
    // We'll determine the packaging type based on the product name and category.
    private fun guessPackagingByContext(
        productName: String,
        categories: String,
        brands: String
    ): List<MaterialInfo> {

        val combined = "$productName $categories $brands".lowercase()

        return when {
            combined.containsAny(
                "fanta", "coca-cola", "pepsi", "sprite", "7up",
                "water", "víz", "ásványvíz",
                "bottle", "palack", "üdítő", "szénsavas",
                "juice", "lé", "mineral"
            ) -> listOf(
                MaterialInfo(
                    name         = "Plastic Bottle",
                    category     = "Plastic",
                    instructions = "Rinse thoroughly, crush to save space. " +
                            "The cap is attached — recycle together. Yellow bin."
                )
            )
            combined.containsAny(
                "beer", "sör", "can", "doboz", "tin",
                "energy drink", "energiaital", "red bull", "monster"
            ) -> listOf(
                MaterialInfo(
                    name         = "Aluminum Can",
                    category     = "Metal",
                    instructions = "Rinse clean. Crush if possible. Yellow recycling bin."
                )
            )
            combined.containsAny(
                "milk", "tej", "juice", "gyümölcslé",
                "tetra", "carton", "karton", "yogurt", "joghurt"
            ) -> listOf(
                MaterialInfo(
                    name         = "Tetra Pak / Carton",
                    category     = "Paper",
                    instructions = "Rinse clean, flatten. Tetra Pak recycling point."
                )
            )
            combined.containsAny(
                "glass", "üveg", "wine", "bor", "beer bottle",
                "jam", "lekvár", "sauce", "szósz"
            ) -> listOf(
                MaterialInfo(
                    name         = "Glass Bottle/Jar",
                    category     = "Glass",
                    instructions = "Rinse clean, remove lid. Green glass container."
                )
            )
            combined.containsAny(
                "chips", "snack", "crisp", "lay's", "pringles",
                "chocolate", "csokoládé", "candy", "cukorka"
            ) -> listOf(
                MaterialInfo(
                    name         = "Plastic Wrapper",
                    category     = "Plastic",
                    instructions = "Check local recycling. Often general waste if multi-layer."
                ),
                MaterialInfo(
                    name         = "Cardboard Box",
                    category     = "Paper",
                    instructions = "Flatten and place in paper recycling bin."
                )
            )
            else -> listOf(
                MaterialInfo(
                    name         = "Plastic packaging",
                    category     = "Plastic",
                    instructions = "Check recycling symbol on package. Yellow bin if recyclable."
                )
            )
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it, ignoreCase = true) }
    private fun parsePackagingMaterials(
        packaging: String?,
        packagingTags: List<String>?
    ): List<MaterialInfo> {
        val materials = mutableListOf<MaterialInfo>()
        val seen      = mutableSetOf<String>()
        val allTags = mutableListOf<String>()
        packaging?.split(",", " ")?.forEach { allTags.add(it.trim().lowercase()) }
        packagingTags?.forEach { allTags.add(it.lowercase()) }

        allTags.forEach { tag ->
            val material = mapTagToMaterial(tag)
            if (material != null && !seen.contains(material.category)) {
                seen.add(material.category)
                materials.add(material)
            }
        }

        return materials
    }

    private fun mapTagToMaterial(tag: String): MaterialInfo? {
        return when {
            tag.contains("plastic") || tag.contains("pet") ||
                    tag.contains("hdpe") || tag.contains("pvc") ||
                    tag.contains("pp") || tag.contains("ps") ->
                MaterialInfo(
                    name         = "Plastic packaging",
                    category     = "Plastic",
                    instructions = "Rinse clean. Check recycling symbol. Yellow bin."
                )

            tag.contains("glass") ->
                MaterialInfo(
                    name         = "Glass container",
                    category     = "Glass",
                    instructions = "Rinse clean, remove lid. Green glass container."
                )

            tag.contains("cardboard") || tag.contains("paper") ||
                    tag.contains("carton") || tag.contains("tetra") ->
                MaterialInfo(
                    name         = "Paper/Cardboard",
                    category     = "Paper",
                    instructions = "Keep dry. Flatten boxes. Blue paper bin."
                )

            tag.contains("metal") || tag.contains("aluminium") ||
                    tag.contains("aluminum") || tag.contains("steel") ||
                    tag.contains("tin") ->
                if (tag.contains("cap") || tag.contains("lid")) {
                    null
                } else {
                    MaterialInfo(
                        name         = "Metal packaging",
                        category     = "Metal",
                        instructions = "Rinse clean. Crush if possible. Yellow bin."
                    )
                }

            tag.contains("wood") || tag.contains("cork") ->
                MaterialInfo(
                    name         = "Organic material",
                    category     = "Organic",
                    instructions = "Place in organic/compost bin."
                )

            else -> null
        }
    }

    fun resetScanner() {
        lastScannedBarcode = ""
        _uiState.value = BarcodeUiState.Scanning
    }
}