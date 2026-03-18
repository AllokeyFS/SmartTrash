package com.example.smarttrash.data.remote

import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.data.local.entity.WasteItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_WASTE_ITEMS = "waste_items"
        private const val COLLECTION_RECYCLING_BINS = "recycling_bins"
    }

    // Getting WasteItem from Firestore and mapping into Entity for Room
    suspend fun fetchAllWasteItems(): Result<List<WasteItemEntity>> {
        return try {
            val snapshot = firestore
                .collection(COLLECTION_WASTE_ITEMS)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    WasteItemEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        category = doc.getString("category") ?: return@mapNotNull null,
                        instructions = doc.getString("instructions") ?: "",
                        nameHu = doc.getString("name_hu") ?: "",         // ← добавь
                        instructionsHu = doc.getString("instructions_hu") ?: ""  // ← добавь
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Getting RecycleBin from Firestore
    @Suppress("UNCHECKED_CAST")
    suspend fun fetchAllRecyclingBins(): Result<List<RecyclingBinEntity>> {
        return try {
            val snapshot = firestore
                .collection(COLLECTION_RECYCLING_BINS)
                .get()
                .await()

            val bins = snapshot.documents.mapNotNull { doc ->
                try {
                    RecyclingBinEntity(
                        id = doc.id,
                        latitude = doc.getDouble("latitude") ?: return@mapNotNull null,
                        longitude = doc.getDouble("longitude") ?: return@mapNotNull null,
                        acceptedTypes = (doc.get("acceptedTypes") as? List<String>) ?: emptyList(),
                        address = doc.getString("address") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(bins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}