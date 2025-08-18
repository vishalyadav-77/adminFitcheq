package com.admin.fitcheq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.admin.fitcheq.databinding.FragmentBrandStatsBinding
import com.google.firebase.firestore.FirebaseFirestore

class BrandStats_Fragment : Fragment() {
    private lateinit var binding: FragmentBrandStatsBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrandStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBrandStats()
    }

    private fun fetchBrandStats() {
        db.collection("outfits")
            .get()
            .addOnSuccessListener { documents ->
                val brandCountMap = mutableMapOf<String, Int>()

                for (doc in documents) {
                    val brand = doc.getString("website") ?: "Unknown"
                    brandCountMap[brand] = (brandCountMap[brand] ?: 0) + 1
                }

                val sortedList = brandCountMap.entries.sortedBy { it.key}
                val totalBrands = brandCountMap.size
                val displayText = sortedList.joinToString("\n\n") {
                    "${it.key}: ${it.value} products"
                }

                binding.totalBrands.text = totalBrands.toString()
                binding.textViewBrands.text = displayText

            }
            .addOnFailureListener { e ->
                binding.textViewBrands.text = "Error: ${e.message}"
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BrandStats_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }
}