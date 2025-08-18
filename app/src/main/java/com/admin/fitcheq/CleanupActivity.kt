package com.admin.fitcheq

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class CleanupActivity : AppCompatActivity() {
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cleanup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val cleanupButton: Button = findViewById(R.id.cleanupButton)
        cleanupButton.setOnClickListener {
            cleanOutfitsData()
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
                    val color = doc.getString("color")?.trim()?.lowercase()
                    val website = doc.getString("website")?.trim()?.uppercase()

                    val updates = hashMapOf<String, Any?>(
                        "category" to category,
                        "type" to type,
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
}