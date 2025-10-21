package com.admin.fitcheq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admin.fitcheq.data.OutfitData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AddProduct : AppCompatActivity() {
    private lateinit var eturl: EditText
    private lateinit var etimageUrls: EditText
    private lateinit var ettitle: EditText
    private lateinit var etprice: EditText
    private lateinit var etimageUrl: EditText
    private lateinit var etgender: EditText
    private lateinit var etproductId: EditText
    private lateinit var ettags: EditText
    private lateinit var etwebsite: EditText
    private lateinit var etCategory: EditText
    private lateinit var etType: EditText
    private lateinit var etColor: EditText
    private lateinit var etStyle: EditText
    private lateinit var etOccasion: EditText
    private lateinit var etSeason: EditText
    private lateinit var etFit: EditText
    private lateinit var etMaterial: EditText
    private lateinit var submitButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val firestore = FirebaseFirestore.getInstance()
        etimageUrls = findViewById(R.id.etImageUrls)
        eturl = findViewById(R.id.etUrl)
        etimageUrl = findViewById(R.id.etImageUrl)
        ettags = findViewById(R.id.etTags)
        ettitle = findViewById(R.id.etTitle)
        etprice = findViewById(R.id.etPrice)
        etgender = findViewById(R.id.etGender)
        etproductId = findViewById(R.id.etProductId)
        etwebsite = findViewById(R.id.etWebsite)

        etCategory = findViewById(R.id.etCategory);
        etType = findViewById(R.id.etType);
        etColor = findViewById(R.id.etColor);
        etStyle = findViewById(R.id.etStyle);
        etOccasion = findViewById(R.id.etOccasion);
        etSeason = findViewById(R.id.etSeason);
        etFit = findViewById(R.id.etFit);
        etMaterial = findViewById(R.id.etMaterial);

        submitButton = findViewById(R.id.btnSubmit)

        etimageUrls.addTextChangedListener(object : TextWatcher {
            private var selfChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (selfChange) return

                val text = s.toString()
                if (text.endsWith("\n")) {
                    selfChange = true

                    // Clean current text
                    val trimmed = text.removeSuffix("\n")
                    val lines = trimmed.split("\n")

                    val lastLine = lines.lastOrNull()?.trimEnd()
                    val prefix = lines.dropLast(1).joinToString("\n")

                    val updatedLine = lastLine ?: ""

                    val finalText = (if (prefix.isNotEmpty()) "$prefix\n" else "") + updatedLine + "\n"
                    etimageUrls.setText(finalText)
                    etimageUrls.setSelection(finalText.length)

                    selfChange = false
                }
            }
        })


        submitButton.setOnClickListener {
            val imageUrlsList = etimageUrls.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val tagsString = ettags.text.toString()
            val tagList: List<String> = tagsString.split(",")
                .map { it.trim().lowercase().replace(" ", "") }
                .filter { it.isNotEmpty() }

            val styleList = etStyle.text.toString().split(",")
                .map { it.trim().lowercase().replace(" ", "") }
                .filter { it.isNotEmpty() }
            val occasionList = etOccasion.text.toString().split(",")
                .map { it.trim().lowercase().replace(" ", "") }
                .filter { it.isNotEmpty() }
            val seasonList = etSeason.text.toString().split(",").map { it.trim().lowercase().replace(" ", "") }
                .filter { it.isNotEmpty() }
            val outfit = OutfitData(
                id = etproductId.text.toString().trim().lowercase().replace(" ", ""),
                link = eturl.text.toString().trim(),
                imageUrl = etimageUrl.text.toString(),
                imageUrls = imageUrlsList,
                title = ettitle.text.toString().trim(),
                price = etprice.text.toString().trim().replace(" ", ""),
                website = etwebsite.text.toString().trim().lowercase(),
                gender = etgender.text.toString().trim().lowercase(),
                tags = tagList,
                category = etCategory.text.toString().trim().lowercase(),
                type = etType.text.toString().trim().lowercase(),
                color = etColor.text.toString().trim().lowercase(),
                style = styleList,
                occasion = occasionList,
                season = seasonList,
                fit = etFit.text.toString().trim().lowercase(),
                material = etMaterial.text.toString().trim().lowercase().replace(" ", "")
            )
            // 1. Add new product
            firestore.collection("outfits").add(outfit)
                .addOnSuccessListener {
                    Toast.makeText(this, "Outfit Added", Toast.LENGTH_SHORT).show()
                    // 2. Update filters collection
                    updateFilters(firestore, outfit)
                    // 3. Navigate to dashboard
                    val intent = Intent(this, DashBoardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to Add: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}

private fun updateFilters(firestore: FirebaseFirestore, outfit: OutfitData) {
    val gender = outfit.gender?.trim()?.lowercase() ?: return
    val category = outfit.category?.trim()?.lowercase()
    val styleList = outfit.style?.map { it.trim().lowercase() } ?: emptyList()
    val seasons = outfit.season ?: emptyList()
    val occasions = outfit.occasion ?: emptyList()

    fun addDoc(fieldPrefix: String, valueList: List<String>) {
        valueList.forEach { value ->
            if (value.isBlank()) return@forEach
            val docId = "${fieldPrefix}_${value.trim().lowercase()}_${gender}"
            val docRef = firestore.collection("filters").document(docId)

            val updates = mutableMapOf<String, Any>()

            fun addValue(field: String, value: String?) {
                if (value.isNullOrBlank()) return
                updates[field] = FieldValue.arrayUnion(value.trim().lowercase())
            }

            // Common fields
            addValue("categories", outfit.category)
            addValue("brand", outfit.website)
            addValue("fits", outfit.fit)
            addValue("colors", outfit.color)
            addValue("materials", outfit.material)
            addValue("type", outfit.type)
            outfit.tags?.forEach { tag -> addValue("tags", tag) }
            outfit.occasion?.forEach { occ -> addValue("occasions", occ) }
            outfit.season?.forEach { s -> addValue("seasons", s) }

            // Merge into Firestore (no overwrite, just append unique values)
            if (updates.isNotEmpty()) {
                docRef.set(updates, SetOptions.merge())
            }
        }
    }

    // --- Category docs ---
    if (!category.isNullOrBlank()) addDoc("category", listOf(category))

    // --- Style docs ---
    addDoc("style", styleList)

    // --- Season docs ---
    addDoc("season", seasons)

    // --- Occasion docs ---
    addDoc("occasion", occasions)
}



