package com.admin.fitcheq

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admin.fitcheq.data.OutfitData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CleanupActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    private lateinit var cleanupButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cleanup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cleanupButton = findViewById(R.id.cleanupButton)
        cleanupButton.setOnClickListener {
            syncFiltersFromAllProducts()
        }
    }
    private fun cleanOutfitsData() {
        db.collection("outfits")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val id = doc.id

                    val category = doc.getString("category")?.trim()?.lowercase()
                    val type = doc.getString("type")?.trim()?.lowercase()
                    val fit = doc.getString("fit")?.trim()?.lowercase()
                    val color = doc.getString("color")?.trim()?.lowercase()
                    val website = doc.getString("website")?.trim()?.uppercase()

                    val updates = hashMapOf<String, Any?>(
                        "category" to category,
                        "type" to type,
                        "fit" to fit,
                        "color" to color,
                        "website" to website,
                    )

                    db.collection("outfits").document(id).update(updates)
                        .addOnSuccessListener {
                            Log.d("Cleanup", "Updated: $id")
                        }
                        .addOnFailureListener {
                            Log.e("Cleanup", "Failed: $id -> ${it.message}")
                        }
                }
                Toast.makeText(this, "Cleanup done", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun syncFiltersFromAllProducts() {
        cleanupButton.isEnabled = false
        Toast.makeText(this, "Sync started...", Toast.LENGTH_SHORT).show()

        db.collection("outfits").get()
            .addOnSuccessListener { snapshot ->
                val allProducts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(OutfitData::class.java)
                }

                val genderKeys = listOf("male", "female", "unisex")

                // Initialize filters map
                val filtersMap = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()

                // Initialize brand map
                val brandMap = genderKeys.associateWith { mutableSetOf<String>() }.toMutableMap()

                allProducts.forEach { outfit ->
                    val gender = outfit.gender?.trim()?.lowercase() ?: return@forEach

                    // --- Brand ---
                    outfit.website?.takeIf { it.isNotBlank() }?.let { brand ->
                        if (!brandMap.containsKey(gender)) brandMap[gender] = mutableSetOf()
                        brandMap[gender]?.add(brand.trim().lowercase())
                    }

                    // --- Other filters ---
                    fun addValue(field: String, value: String?) {
                        if (value.isNullOrBlank()) return

                        // Initialize field map if missing
                        if (!filtersMap.containsKey(field)) filtersMap[field] = mutableMapOf()

                        val genderMap = filtersMap[field]!!

                        // Initialize gender set if missing
                        if (!genderMap.containsKey(gender)) genderMap[gender] = mutableSetOf()

                        genderMap[gender]?.add(value.trim().lowercase())
                    }

                    addValue("categories", outfit.category)
                    addValue("type", outfit.type)
                    addValue("fits", outfit.fit)
                    addValue("colors", outfit.color)
                    addValue("materials", outfit.material)
                    outfit.tags?.forEach { addValue("tags", it) }
                    outfit.style?.forEach { addValue("styles", it) }
                    outfit.occasion?.forEach { addValue("occasions", it) }
                    outfit.season?.forEach { addValue("seasons", it) }
                }

                // Convert sets to lists and skip empty arrays
                val finalMap = mutableMapOf<String, Any>()
                filtersMap.forEach { (field, genderMap) ->
                    val cleanedMap = genderMap
                        .filterValues { it.isNotEmpty() }
                        .mapValues { it.value.toList() }
                    if (cleanedMap.isNotEmpty()) {
                        finalMap[field] = cleanedMap
                    }
                }

                val cleanedBrandMap = brandMap
                    .filterValues { it.isNotEmpty() }
                    .mapValues { it.value.toList() }
                if (cleanedBrandMap.isNotEmpty()) {
                    finalMap["brand"] = cleanedBrandMap
                }

                // Update filters/master in Firestore
                db.collection("filters").document("master")
                    .set(finalMap, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Filters synced successfully", Toast.LENGTH_LONG).show()
                        cleanupButton.isEnabled = true
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to sync filters: ${e.message}", Toast.LENGTH_LONG).show()
                        cleanupButton.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch products: ${e.message}", Toast.LENGTH_LONG).show()
                cleanupButton.isEnabled = true
            }
    }

}