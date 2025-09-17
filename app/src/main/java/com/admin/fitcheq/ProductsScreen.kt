package com.admin.fitcheq

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.admin.fitcheq.viewmodels.AdminProductViewModel
import kotlin.text.replace

class ProductsScreen : AppCompatActivity() {
    private lateinit var allproductsBtn: CardView
    private lateinit var femaleproductsBtn: CardView
    private lateinit var maleproductsBtn: CardView
    private lateinit var findproductsBtn: CardView
    private lateinit var sortIdBtn: CardView
    private lateinit var brandDataBtn: CardView
    private lateinit var productFragmentContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_products_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        allproductsBtn = findViewById(R.id.allproducts)
        maleproductsBtn = findViewById(R.id.maleproducts)
        femaleproductsBtn = findViewById(R.id.femaleproducts)
        findproductsBtn = findViewById(R.id.findByCategory)
        sortIdBtn = findViewById(R.id.sortId)
        brandDataBtn = findViewById(R.id.brandList)
        productFragmentContainer = findViewById(R.id.product_fragment_container)

        allproductsBtn.setOnClickListener {
            openProductListFragment(null, null)
        }
        maleproductsBtn.setOnClickListener {
            openProductListFragment("male", null)
        }
        femaleproductsBtn.setOnClickListener {
            openProductListFragment("female", null)

        }
        findproductsBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_field_filter, null)
            val fieldNameEt = dialogView.findViewById<EditText>(R.id.etFieldName)
            val fieldValueEt = dialogView.findViewById<EditText>(R.id.etFieldValue)

            AlertDialog.Builder(this)
                .setTitle("Filter Products")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val fieldName = fieldNameEt.text.toString().trim()
                    val fieldValue = fieldValueEt.text.toString().trim()
                    if (fieldName.isNotEmpty()) {
                        openProductListFragment(fieldName = fieldName, fieldValue = fieldValue)
                    } else {
                        Toast.makeText(this, "Field is required", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        sortIdBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_field_filter, null)
            val fieldNameEt = dialogView.findViewById<EditText>(R.id.etFieldName)
            val fieldValueEt = dialogView.findViewById<EditText>(R.id.etFieldValue)

            AlertDialog.Builder(this)
                .setTitle("Filter Products")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val fieldName = fieldNameEt.text.toString().trim()
                    val fieldValue = fieldValueEt.text.toString().trim()
                    if (fieldName.isNotEmpty()) {
                        openProductListDescFragment(fieldName = fieldName, fieldValue = fieldValue)
                    } else {
                        Toast.makeText(this, "Field is required", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        brandDataBtn.setOnClickListener {
            val fragment = BrandStats_Fragment()
            productFragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.product_fragment_container, fragment)
                .addToBackStack(null)
                .commit()

        }
    }
    private fun openProductListFragment(
        gender: String? = null,
        tag: String? = null,
        fieldName: String? = null,
        fieldValue: String? = null
    ) {
        val fragment = ProductListFragment().apply {
            arguments = Bundle().apply {
                gender?.let { putString("gender", it) }
                tag?.let { putString("tag", it) }
                fieldName?.let { putString("fieldName", it) }
                fieldValue?.let { putString("fieldValue", it) }
            }
        }
        productFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.product_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun openProductListDescFragment(
        gender: String? = null,
        tag: String? = null,
        fieldName: String? = null,
        fieldValue: String? = null
    ) {
        val fragment = ProductListDesc().apply {
            arguments = Bundle().apply {
                gender?.let { putString("gender", it) }
                tag?.let { putString("tag", it) }
                fieldName?.let { putString("fieldName", it) }
                fieldValue?.let { putString("fieldValue", it) }
            }
        }

        productFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.product_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


}
