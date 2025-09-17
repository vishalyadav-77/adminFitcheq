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

                // Prepare master filters map
                val updated = mutableMapOf<String, MutableSet<String>>(
                    "brands" to mutableSetOf(),
                    "categories" to mutableSetOf(),
                    "type" to mutableSetOf(),
                    "fits" to mutableSetOf(),
                    "colors" to mutableSetOf(),
                    "materials" to mutableSetOf(),
                    "tags" to mutableSetOf(),
                    "styles" to mutableSetOf(),
                    "occasions" to mutableSetOf(),
                    "seasons" to mutableSetOf()
                )

                allProducts.forEach { outfit ->
                    outfit.website?.let { updated["brands"]?.add(it) }
                    outfit.category?.let { updated["categories"]?.add(it) }
                    outfit.type?.let { updated["type"]?.add(it) }
                    outfit.fit?.let { updated["fits"]?.add(it) }
                    outfit.color?.let { updated["colors"]?.add(it) }
                    outfit.material?.let { updated["materials"]?.add(it) }
                    outfit.tags?.forEach { updated["tags"]?.add(it) }
                    outfit.style?.forEach { updated["styles"]?.add(it) }
                    outfit.occasion?.forEach { updated["occasions"]?.add(it) }
                    outfit.season?.forEach { updated["seasons"]?.add(it) }
                }

                // Convert sets to lists for Firestore
                val finalMap = updated.mapValues { it.value.toList() }

                // Update filters/master
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