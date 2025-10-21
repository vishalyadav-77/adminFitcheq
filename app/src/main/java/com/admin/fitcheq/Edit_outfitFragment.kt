package com.admin.fitcheq

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.admin.fitcheq.data.OutfitData
import com.admin.fitcheq.databinding.FragmentEditOutfitBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.collections.get
import kotlin.text.get


class Edit_outfitFragment : Fragment() {


    private var _binding: FragmentEditOutfitBinding? = null
    private val binding get() = _binding!!

    private var documentId: String? = null  // Firestore document ID
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditOutfitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val productId = arguments?.getString("productId")
        Log.d("Edit", "Received productId: $productId")
        if (!productId.isNullOrEmpty()) {
            binding.etSearchId.setText(productId)
            loadProductData(productId)
            binding.editFieldsLayout.visibility = View.VISIBLE
        }

        binding.btnSearch.setOnClickListener {
            val id = binding.etSearchId.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(requireContext(), "Enter ID to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                loadProductData(id)
            }
        }

        binding.btnEdit.setOnClickListener {
            binding.editFieldsLayout.visibility = View.VISIBLE
        }

        binding.btnDelete.setOnClickListener {
            val safeDocId = documentId ?: ""
            if (safeDocId.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Outfit")
                    .setMessage("Are you sure you want to delete this outfit? This action cannot be undone.")
                    .setPositiveButton("Delete") { dialog, _ ->
                        // Proceed with deletion
                        firestore.collection("outfits")
                            .document(safeDocId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Outfit deleted successfully", Toast.LENGTH_SHORT).show()
//                                documentId = "" // Reset ID if needed
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to delete document: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Toast.makeText(view.context, "No documentId available to delete", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpdate.setOnClickListener {
            val safeDocId = documentId ?: ""
            if (safeDocId.isNotEmpty()) {
                val updatedOutfit = OutfitData(
                    title = binding.etTitle.text.toString().trim(),
                    price = binding.etPrice.text.toString().trim(),
                    gender = binding.etGender.text.toString().trim(),
                    imageUrl = binding.etImageUrl.text.toString().trim(),
                    imageUrls = binding.etImageUrls.text.toString()
                        .split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                    link = binding.etLink.text.toString().trim(),
                    id = binding.etId.text.toString().trim(),
                    website = binding.etWebsite.text.toString().trim(),
                    tags = binding.etTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    category = binding.etCategory.text.toString().trim(),
                    type = binding.etType.text.toString().trim(),
                    color = binding.etColor.text.toString().trim(),
                    style = binding.etStyle.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    occasion = binding.etOccasion.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    season = binding.etSeason.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    fit = binding.etFit.text.toString().trim(),
                    material = binding.etMaterial.text.toString().trim()
                )

                firestore.collection("outfits").document(safeDocId).get()
                    .addOnSuccessListener { snapshot ->
                        val oldOutfit = snapshot.toObject(OutfitData::class.java)

                        // 🔎 Compare only filter-related fields
                        val filterFieldsChanged = oldOutfit?.let {
                            it.category != updatedOutfit.category ||
                                    it.type != updatedOutfit.type ||
                                    it.color != updatedOutfit.color ||
                                    it.gender != updatedOutfit.gender ||
                                    it.website != updatedOutfit.website ||
                                    it.fit != updatedOutfit.fit ||
                                    it.material != updatedOutfit.material ||
                                    it.tags?.toSet() != updatedOutfit.tags?.toSet() ||
                                    it.style?.toSet() != updatedOutfit.style?.toSet() ||
                                    it.occasion?.toSet() != updatedOutfit.occasion?.toSet() ||
                                    it.season?.toSet() != updatedOutfit.season?.toSet()
                        } ?: true // if no old outfit, treat as changed

                        // ✅ Update outfit
                        firestore.collection("outfits").document(safeDocId)
                            .set(updatedOutfit)
                            .addOnSuccessListener {
                                Toast.makeText(view.context, "Outfit updated successfully", Toast.LENGTH_SHORT).show()

                                // Only update filters if something changed
                                if (filterFieldsChanged) {
                                    updateFilters(firestore, updatedOutfit)
                                }

                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(view.context, "Failed to update outfit: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
            } else {
                Toast.makeText(view.context, "No document selected to update", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun loadProductData(id: String) {
        // Search in 'outfits' collection where field "id" matches
        firestore.collection("outfits")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                binding.itemFoundStatus.visibility = View.VISIBLE
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    documentId = document.id  // Save actual Firestore doc ID

                    // Set values to EditTexts
                    binding.etTitle.setText(document.getString("title") ?: "")
                    binding.etPrice.setText(document.getString("price") ?: "")
                    binding.etGender.setText(document.getString("gender") ?: "")
                    binding.etImageUrl.setText(document.getString("imageUrl") ?: "")
                    val imageList = (document.get("imageUrls") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    binding.etImageUrls.setText(imageList.joinToString("\n"))

                    binding.etImageUrls.addTextChangedListener(object : TextWatcher {
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
                                binding.etImageUrls.setText(finalText)
                               binding.etImageUrls.setSelection(finalText.length)

                                selfChange = false
                            }
                        }
                    })
                    binding.etLink.setText(document.getString("link") ?: "")
                    binding.etId.setText(document.getString("id") ?: "")
                    binding.etWebsite.setText(document.getString("website") ?: "")
                    val tagsList = (document.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    binding.etTags.setText(tagsList.joinToString(","))

                    binding.etCategory.setText(document.getString("category") ?: "")
                    binding.etType.setText(document.getString("type") ?: "")
                    binding.etColor.setText(document.getString("color") ?: "")
                    val styleList = (document.get("style") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    binding.etStyle.setText(styleList.joinToString(","))
                    val occasionList = (document.get("occasion") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    binding.etOccasion.setText(occasionList.joinToString(","))
                    val seasonList = (document.get("season") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    binding.etSeason.setText(seasonList.joinToString(","))
                    binding.etFit.setText(document.getString("fit") ?: "")
                    binding.etMaterial.setText(document.getString("material") ?: "")

                    binding.btnEdit.visibility = View.VISIBLE
                    binding.btnDelete.visibility = View.VISIBLE

                    // Keep editable layout hidden until "Edit" is clicked
                    binding.editFieldsLayout.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "No outfit found with this ID", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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


}
