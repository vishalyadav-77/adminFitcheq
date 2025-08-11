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
import com.google.firebase.firestore.FirebaseFirestore
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
                    title = binding.etTitle.text.toString(),
                    price = binding.etPrice.text.toString(),
                    gender = binding.etGender.text.toString(),
                    imageUrl = binding.etImageUrl.text.toString(),
                    imageUrls =binding.etImageUrls.text.toString()
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty()},
                    link = binding.etLink.text.toString(),
                    id = binding.etId.text.toString(),
                    website = binding.etWebsite.text.toString(),
                    tags = binding.etTags.text.toString()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                    category = binding.etCategory.text.toString(),
                    type = binding.etType.text.toString(),
                    color = binding.etColor.text.toString(),
                    style = binding.etStyle.text.toString().split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty()},
                    occasion = binding.etOccasion.text.toString().split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty()},
                    season = binding.etSeason.text.toString().split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty()},
                    fit = binding.etFit.text.toString(),
                    material = binding.etMaterial.text.toString()
                )

                firestore.collection("outfits").document(safeDocId)
                    .set(updatedOutfit)
                    .addOnSuccessListener {
                        Toast.makeText(view.context, "Outfit updated successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(view.context, "Failed to update outfit: ${e.message}", Toast.LENGTH_SHORT).show()
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


}
