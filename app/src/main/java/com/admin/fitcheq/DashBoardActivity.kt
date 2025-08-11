package com.admin.fitcheq

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class DashBoardActivity : AppCompatActivity() {
    private lateinit var addproduct: CardView
    private lateinit var editproduct: CardView
    private lateinit var productsBtn: Button
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.WHITE
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        addproduct = findViewById(R.id.addproduct)
        editproduct = findViewById(R.id.editproduct)
        productsBtn = findViewById(R.id.productsBtn)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Check if user is not logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        addproduct.setOnClickListener {
            startActivity(Intent(this, AddProduct::class.java))
        }
        editproduct.setOnClickListener{
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Edit_outfitFragment())
                .addToBackStack(null)
                .commit()
        }
        productsBtn.setOnClickListener {
            startActivity(Intent(this, ProductsScreen::class.java))
        }
    }
}