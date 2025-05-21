package com.admin.fitcheq

import android.app.AlertDialog
import android.os.Bundle
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

        binding.btnSearch.setOnClickListener {
            val id = binding.etSearchId.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(requireContext(), "Enter ID to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                        binding.etLink.setText(document.getString("link") ?: "")
                        binding.etId.setText(document.getString("id") ?: "")
                        binding.etWebsite.setText(document.getString("website") ?: "")
                        val tagsList = document.get("tags") as? List<*> ?: emptyList<String>()
                        binding.etTags.setText(tagsList.joinToString(","))

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

        binding.btnEdit.setOnClickListener {
            binding.editFieldsLayout.visibility = View.VISIBLE
        }

        binding.btnDelete.setOnClickListener {
            val safeDocId = documentId?.toString() ?: ""
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
                                documentId = "" // Reset ID if needed
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
            val safeDocId = documentId?.toString() ?: ""
            if (safeDocId.isNotEmpty()) {
                val updatedOutfit = OutfitData(
                    title = binding.etTitle.text.toString(),
                    price = binding.etPrice.text.toString(),
                    gender = binding.etGender.text.toString(),
                    imageUrl = binding.etImageUrl.text.toString(),
                    link = binding.etLink.text.toString(),
                    id = binding.etId.text.toString(),
                    website = binding.etWebsite.text.toString(),
                    tags = binding.etTags.text.toString()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )

                firestore.collection("outfits").document(safeDocId)
                    .set(updatedOutfit)
                    .addOnSuccessListener {
                        Toast.makeText(view.context, "Outfit updated successfully", Toast.LENGTH_SHORT).show()

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

}