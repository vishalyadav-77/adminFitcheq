package com.admin.fitcheq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admin.fitcheq.data.OutfitData
import com.google.firebase.firestore.FirebaseFirestore

class AddProduct : AppCompatActivity() {
    private lateinit var eturl: EditText
    private lateinit var ettitle: EditText
    private lateinit var etprice: EditText
    private lateinit var etimageUrl: EditText
    private lateinit var etgender: EditText
    private lateinit var etproductId: EditText
    private lateinit var ettags: EditText
    private lateinit var etwebsite: EditText
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
        eturl = findViewById(R.id.etUrl)
        etimageUrl = findViewById(R.id.etImageUrl)
        ettags = findViewById(R.id.etTags)
        ettitle = findViewById(R.id.etTitle)
        etprice = findViewById(R.id.etPrice)
        etgender = findViewById(R.id.etGender)
        etproductId = findViewById(R.id.etProductId)
        etwebsite = findViewById(R.id.etWebsite)
        submitButton = findViewById(R.id.btnSubmit)



        submitButton.setOnClickListener {
            val tagsString = ettags.text.toString()
            // Split by comma, trim spaces, and filter out any empty values
            val tagList: List<String> = tagsString.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val outfits = OutfitData(
                id = etproductId.text.toString(),
             link = eturl.text.toString(),
             imageUrl = etimageUrl.text.toString(),
             title = ettitle.text.toString(),
             price = etprice.text.toString(),
             website = etwebsite.text.toString(),
             gender = etgender.text.toString(),
             tags = tagList
            )
            firestore.collection("outfits").add(outfits)
                .addOnSuccessListener {
                    Toast.makeText(this, "Outfit Added", Toast.LENGTH_SHORT).show()
                    //navigate to dashboard
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