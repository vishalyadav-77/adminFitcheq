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

    fun fetchTotalProductCount(
        gender: String? = null,
        tag: String? = null,
        fieldName: String? = null,
        fieldValue: String? = null
    ) {
        var query: Query = FirebaseFirestore.getInstance()
            .collection("outfits")

        gender?.let { query = query.whereEqualTo("gender", it) }
        tag?.let { query = query.whereArrayContains("tags", it) }

        if (!fieldName.isNullOrEmpty() && !fieldValue.isNullOrEmpty()) {
            query = query.whereEqualTo(fieldName, fieldValue)
        }

        query.get()
            .addOnSuccessListener { result ->
                totalItemCount.postValue(result.size())
            }
            .addOnFailureListener {
                totalItemCount.postValue(0)
                error.postValue(it.message)
            }
    }


    fun fetchNextBatch(
        gender: String? = null,
        tag: String? = null,
        fieldName: String? = null,
        fieldValue: String? = null
    ) {
        if (isFetching || isLastPage) return
        isFetching = true
        isLoading.value = true

        var query: Query = FirebaseFirestore.getInstance()
            .collection("outfits")

        gender?.let { query = query.whereEqualTo("gender", it) }
        tag?.let { query = query.whereArrayContains("tags", it) }

        if (!fieldName.isNullOrEmpty()) {
            query = query.whereEqualTo(fieldName, fieldValue)
        }

        query = query.orderBy("id") // Make sure "id" is indexed

        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.limit(10)
            .get()
            .addOnSuccessListener { result ->
                val newOutfits = result.documents.mapNotNull { it.toObject(OutfitData::class.java) }

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

    fun fetchNextBatchDesc(
        gender: String? = null,
        tag: String? = null,
        fieldName: String? = null,
        fieldValue: String? = null
    ) {
        if (isFetching || isLastPage) return
        isFetching = true
        isLoading.value = true

        var query: Query = FirebaseFirestore.getInstance()
            .collection("outfits") // same as your other function
            .orderBy("id", Query.Direction.DESCENDING) // descending order

        gender?.let { query = query.whereEqualTo("gender", it) }
        tag?.let { query = query.whereArrayContains("tags", it) }
        if (!fieldName.isNullOrEmpty() && !fieldValue.isNullOrEmpty()) {
            query = query.whereEqualTo(fieldName, fieldValue)
        }

        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.limit(10)
            .get()
            .addOnSuccessListener { result ->
                val newOutfits = result.documents.mapNotNull { it.toObject(OutfitData::class.java) }

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