package com.admin.fitcheq

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
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
    }
        private fun openProductListFragment(gender: String?, tag: String?) {
            val fragment = ProductListFragment().apply {
                arguments = Bundle().apply {
                    gender?.let { putString("gender", it) }
                    tag?.let { putString("tag", it) }
                }
            }
            productFragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.product_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
}
