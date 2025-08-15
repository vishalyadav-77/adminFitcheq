package com.admin.fitcheq.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.admin.fitcheq.data.OutfitData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.jvm.java

class AdminProductViewModel: ViewModel() {
    private val _products = MutableLiveData<List<OutfitData>>()
    val products: MutableLiveData<List<OutfitData>> = _products

    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>()
    val totalItemCount = MutableLiveData<Int>()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isFetching = false
    private var isLastPage = false

    fun resetPagination() {
        lastVisibleDocument = null
        _products.value = emptyList()
        isLastPage = false // reset
    }

    fun fetchTotalProductCount(gender: String? = null, tag: String? = null) {
        var query: Query = FirebaseFirestore.getInstance()
            .collection("outfits")

        gender?.let { query = query.whereEqualTo("gender", it) }
        tag?.let { query = query.whereArrayContains("tags", it) }

        query.get()
            .addOnSuccessListener { result ->
                totalItemCount.postValue(result.size())
            }
            .addOnFailureListener {
                totalItemCount.postValue(0)
                error.postValue(it.message)
            }
    }

    fun fetchNextBatch(gender: String? = null, tag: String? = null) {
        if (isFetching || isLastPage) return
        isFetching = true
        isLoading.value = true

        var query: Query = FirebaseFirestore.getInstance()
            .collection("outfits")
//            .orderBy("id") // Use indexed field like "id" or "timestamp"

        gender?.let { query = query.whereEqualTo("gender", it) }
        tag?.let { query = query.whereArrayContains("tags", it) }

        query = query.orderBy("id")

        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.limit(10)
            .get()
            .addOnSuccessListener { result ->
                val newOutfits = result.documents.mapNotNull { it.toObject(OutfitData::class.java) }

                // If no new data, mark last page
                if (newOutfits.isEmpty()) {
                    isLastPage = true
                } else {
                    val currentList = products.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(newOutfits)
                    products.postValue(currentList)

                    lastVisibleDocument = result.documents.last()
                }

                isFetching = false
                isLoading.postValue(false)
            }
            .addOnFailureListener {
                isFetching = false
                isLoading.postValue(false)
                error.postValue(it.message)
            }
    }

}