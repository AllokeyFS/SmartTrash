package com.example.smarttrash.ui.screens.barcode

data class MaterialInfo(
    val name: String,
    val category: String,
    val instructions: String
)

sealed interface BarcodeUiState {
    data object Scanning : BarcodeUiState

    data class Loading(
        val barcode: String
    ) : BarcodeUiState

    data class Found(
        val productName: String,
        val barcode: String,
        val materials: List<MaterialInfo>,
        val imageUrl: String? = null
    ) : BarcodeUiState

    data class NotFound(
        val barcode: String
    ) : BarcodeUiState

    // No Internet Connection
    data class Error(
        val barcode: String,
        val message: String
    ) : BarcodeUiState
}