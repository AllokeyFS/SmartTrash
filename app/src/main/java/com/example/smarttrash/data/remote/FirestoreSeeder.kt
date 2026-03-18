package com.example.smarttrash.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun seedIfEmpty() {
        seedWasteItems()
        seedRecyclingBins()
    }

    private suspend fun seedWasteItems() {
        val collection = firestore.collection("waste_items")
        val existing   = collection.limit(1).get().await()
        if (!existing.isEmpty) {
            updateHungarianTranslations()
            return
        }

        val items = listOf(
            mapOf(
                "name"         to "Plastic Bottle",
                "name_hu"      to "Műanyag palack",
                "category"     to "Plastic",
                "instructions" to "Rinse thoroughly, remove cap, crush to save space.",
                "instructions_hu" to "Öblítsd ki alaposan, nyomja össze. A kupak rögzített — reciklálj együtt. Sárga kukába."
            ),
            mapOf(
                "name"         to "Glass Jar",
                "name_hu"      to "Üveg tégely",
                "category"     to "Glass",
                "instructions" to "Rinse clean, remove metal lids. Green container.",
                "instructions_hu" to "Öblítsd ki, távolítsd el a fémes fedőt. Zöld üvegtárolóba."
            ),
            mapOf(
                "name"         to "Newspaper",
                "name_hu"      to "Újság",
                "category"     to "Paper",
                "instructions" to "Keep dry. Remove any plastic wrapping.",
                "instructions_hu" to "Tartsd szárazon. Távolítsd el a műanyag csomagolást."
            ),
            mapOf(
                "name"         to "Cardboard Box",
                "name_hu"      to "Kartondoboz",
                "category"     to "Paper",
                "instructions" to "Flatten completely. Remove all tape and staples.",
                "instructions_hu" to "Lapítsd össze teljesen. Távolítsd el a szalagot és kapcsokat."
            ),
            mapOf(
                "name"         to "Aluminum Can",
                "name_hu"      to "Alumínium doboz",
                "category"     to "Metal",
                "instructions" to "Rinse clean, crush if possible. Yellow recycling bin.",
                "instructions_hu" to "Öblítsd ki, nyomja össze ha lehetséges. Sárga gyűjtőbe."
            ),
            mapOf(
                "name"         to "Battery (AA/AAA)",
                "name_hu"      to "Elem (AA/AAA)",
                "category"     to "Hazardous",
                "instructions" to "NEVER put in regular trash. Take to collection point.",
                "instructions_hu" to "SOHA ne dobd normál kukába. Vidd gyűjtőpontra."
            ),
            mapOf(
                "name"         to "Food Scraps",
                "name_hu"      to "Ételmaradék",
                "category"     to "Organic",
                "instructions" to "Place in brown organic waste bin.",
                "instructions_hu" to "Tedd a barna szerves hulladék kukába."
            ),
            mapOf(
                "name"         to "Styrofoam Cup",
                "name_hu"      to "Hungarocell pohár",
                "category"     to "Plastic",
                "instructions" to "Cannot be recycled in most areas. General waste.",
                "instructions_hu" to "A legtöbb helyen nem recycleálható. Általános hulladékba."
            ),
            mapOf(
                "name"         to "Wine Bottle",
                "name_hu"      to "Borosüveg",
                "category"     to "Glass",
                "instructions" to "Rinse clean, remove cork. Glass recycling bank.",
                "instructions_hu" to "Öblítsd ki, távolítsd el a dugót. Üveg gyűjtőbe."
            ),
            mapOf(
                "name"         to "Mobile Phone",
                "name_hu"      to "Mobiltelefon",
                "category"     to "Hazardous",
                "instructions" to "Take to electronics store. Wipe data first.",
                "instructions_hu" to "Vidd elektronikai boltba. Töröld az adatokat előbb."
            ),
            mapOf(
                "name"         to "Pizza Box",
                "name_hu"      to "Pizzás doboz",
                "category"     to "Paper",
                "instructions" to "If greasy: general waste. If clean: recycle.",
                "instructions_hu" to "Ha zsíros: általános hulladék. Ha tiszta: recycleáld."
            ),
            mapOf(
                "name"         to "Tin Can",
                "name_hu"      to "Konzervdoboz",
                "category"     to "Metal",
                "instructions" to "Rinse clean. Yellow recycling bin.",
                "instructions_hu" to "Öblítsd ki. Sárga gyűjtőbe."
            ),
            mapOf(
                "name"         to "Yogurt Container",
                "name_hu"      to "Joghurtos pohár",
                "category"     to "Plastic",
                "instructions" to "Rinse clean. Check recycling symbol. Yellow bin.",
                "instructions_hu" to "Öblítsd ki. Ellenőrizd a recycling szimbólumot. Sárga kukába."
            ),
            mapOf(
                "name"         to "Light Bulb (LED)",
                "name_hu"      to "LED izzó",
                "category"     to "Hazardous",
                "instructions" to "Take to hardware store recycling point.",
                "instructions_hu" to "Vidd barkácsáruházba gyűjtőpontra."
            ),
            mapOf(
                "name"         to "Apple Core",
                "name_hu"      to "Almacsutak",
                "category"     to "Organic",
                "instructions" to "Place in brown organic bin or compost.",
                "instructions_hu" to "Tedd a barna szerves kukába vagy komposztba."
            )
        )

        val batch = firestore.batch()
        items.forEach { item ->
            batch.set(collection.document(), item)
        }
        batch.commit().await()
    }

    private suspend fun seedRecyclingBins() {
        val collection = firestore.collection("recycling_bins")
        val existing   = collection.limit(1).get().await()
        if (!existing.isEmpty) return

        // Координаты Будапешта 🇭🇺
        val bins = listOf(
            mapOf("address" to "Budapest, Deák Ferenc tér 1",
                "latitude" to 47.4986, "longitude" to 19.0530,
                "acceptedTypes" to listOf("Plastic", "Glass", "Paper", "Metal")),
            mapOf("address" to "Budapest, Keleti pályaudvar",
                "latitude" to 47.5001, "longitude" to 19.0839,
                "acceptedTypes" to listOf("Plastic", "Paper")),
            mapOf("address" to "Budapest, Margit sziget észak",
                "latitude" to 47.5268, "longitude" to 19.0497,
                "acceptedTypes" to listOf("Plastic", "Glass", "Organic")),
            mapOf("address" to "Budapest, Blaha Lujza tér",
                "latitude" to 47.4953, "longitude" to 19.0713,
                "acceptedTypes" to listOf("Plastic", "Metal", "Paper")),
            mapOf("address" to "Budapest, Városliget főbejárat",
                "latitude" to 47.5146, "longitude" to 19.0808,
                "acceptedTypes" to listOf("Plastic", "Glass", "Paper", "Organic")),
            mapOf("address" to "Budapest, Móricz Zsigmond körtér",
                "latitude" to 47.4773, "longitude" to 19.0444,
                "acceptedTypes" to listOf("Plastic", "Glass", "Metal")),
            mapOf("address" to "Budapest, Boráros tér HÉV",
                "latitude" to 47.4834, "longitude" to 19.0652,
                "acceptedTypes" to listOf("Hazardous", "Metal", "Glass"))
        )

        val batch = firestore.batch()
        bins.forEach { bin ->
            batch.set(collection.document(), bin)
        }
        batch.commit().await()
    }
    private suspend fun updateHungarianTranslations() {
        val collection = firestore.collection("waste_items")
        val documents = collection.get().await()

        val translations = mapOf(
            "Plastic Bottle" to Pair("Műanyag palack", "Öblítsd ki, nyomja össze. Sárga kukába."),
            "Glass Jar" to Pair("Üveg tégely", "Öblítsd ki, távolítsd el a fedőt. Zöld tárolóba."),
            "Newspaper" to Pair("Újság", "Tartsd szárazon. Kék papír kukába."),
            "Cardboard Box" to Pair("Kartondoboz", "Lapítsd össze. Kék papír kukába."),
            "Aluminum Can" to Pair("Alumínium doboz", "Öblítsd ki. Sárga gyűjtőbe."),
            "Battery (AA/AAA)" to Pair(
                "Elem (AA/AAA)",
                "SOHA ne dobd normál kukába. Gyűjtőpontra."
            ),
            "Food Scraps" to Pair("Ételmaradék", "Barna szerves kukába."),
            "Styrofoam Cup" to Pair("Hungarocell pohár", "Általános hulladékba."),
            "Wine Bottle" to Pair("Borosüveg", "Öblítsd ki. Üveg gyűjtőbe."),
            "Mobile Phone" to Pair("Mobiltelefon", "Vidd elektronikai boltba."),
            "Pizza Box" to Pair("Pizzás doboz", "Ha tiszta: papír kukába. Ha zsíros: általános."),
            "Tin Can" to Pair("Konzervdoboz", "Öblítsd ki. Sárga gyűjtőbe."),
            "Yogurt Container" to Pair("Joghurtos pohár", "Öblítsd ki. Sárga kukába."),
            "Light Bulb (LED)" to Pair("LED izzó", "Vidd barkácsáruházba gyűjtőpontra."),
            "Apple Core" to Pair("Almacsutak", "Barna szerves kukába vagy komposztba.")
        )

        val batch = firestore.batch()
        documents.forEach { doc ->
            val name = doc.getString("name") ?: return@forEach
            translations[name]?.let { (nameHu, instrHu) ->
                batch.update(
                    doc.reference,
                    "name_hu", nameHu,
                    "instructions_hu", instrHu
                )
            }
        }
        batch.commit().await()
    }
}