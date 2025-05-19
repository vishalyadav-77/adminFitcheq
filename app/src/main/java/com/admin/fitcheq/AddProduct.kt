package com.admin.fitcheq

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddProduct : AppCompatActivity() {
    private lateinit var eturl: EditText
    private lateinit var ettitle: EditText
    private lateinit var etprice: EditText
    private lateinit var etimageUrl: EditText
    private lateinit var etgender: EditText
    private lateinit var etproductId: EditText
    private lateinit var ettags: EditText
    private lateinit var etwebsite: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        eturl = findViewById(R.id.etUrl)
        etimageUrl = findViewById(R.id.etImageUrl)
        ettags = findViewById(R.id.etTags)
        ettitle = findViewById(R.id.etTitle)
        etprice = findViewById(R.id.etPrice)
        etgender = findViewById(R.id.etGender)
        etproductId = findViewById(R.id.etProductId)
        etwebsite = findViewById(R.id.etWebsite)

        val url = eturl.text.toString()
        val imageUrl = eturl.text.toString()
        val tags = eturl.text.toString()
        val title = eturl.text.toString()
        val price = eturl.text.toString()
        val gender = eturl.text.toString()
        val productId = eturl.text.toString()
        val website = eturl.text.toString()



    }
}