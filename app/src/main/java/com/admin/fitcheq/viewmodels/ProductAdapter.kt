package com.admin.fitcheq.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.admin.fitcheq.data.OutfitData
import com.admin.fitcheq.databinding.ItemProductBinding
import com.bumptech.glide.Glide

class ProductAdapter(
    private val onEditClick: (String) -> Unit
) : ListAdapter<OutfitData, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val outfit = getItem(position)
        with(holder.binding) {
            Glide.with(imageView.context)
                .load(outfit.imageUrl)
                .override(500, 620)
                .centerCrop()
                .into(imageView)
            titleTextView.text = outfit.title
            priceTextView.text = "₹${outfit.price}"
            genderTextView.text = "Gender: ${outfit.gender}"
            idTextView.text = "Id: ${outfit.id}"

            if (outfit.imageUrls.isNullOrEmpty()) {
                tvImageUrlsWarning.visibility = View.VISIBLE
            } else {
                tvImageUrlsWarning.visibility = View.GONE
            }
            if (outfit.category.isNullOrEmpty()) {
                tvCategoryWarning.visibility = View.VISIBLE
            } else {
                tvCategoryWarning.visibility = View.GONE
            }
            editButton.setOnClickListener {
                onEditClick(outfit.id)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OutfitData>() {
            override fun areItemsTheSame(oldItem: OutfitData, newItem: OutfitData): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: OutfitData, newItem: OutfitData): Boolean = oldItem == newItem
        }
    }
}