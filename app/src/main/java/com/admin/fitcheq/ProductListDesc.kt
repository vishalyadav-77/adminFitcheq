package com.admin.fitcheq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.admin.fitcheq.databinding.FragmentProductListBinding
import com.admin.fitcheq.databinding.FragmentProductListDescBinding
import com.admin.fitcheq.viewmodels.AdminProductViewModel
import com.admin.fitcheq.viewmodels.ProductAdapter

class ProductListDesc : Fragment() {
    private var genderFilter: String? = null
    private var tagFilter: String? = null
    private var fieldNameFilter: String? = null
    private var fieldValueFilter: String? = null
    private lateinit var binding: FragmentProductListBinding
    private lateinit var adapter: ProductAdapter
    private val viewModel: AdminProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genderFilter = it.getString("gender")
            tagFilter = it.getString("tag")
            fieldNameFilter = it.getString("fieldName")
            fieldValueFilter = it.getString("fieldValue")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProductAdapter { productId ->
            val fragment = Edit_outfitFragment().apply {
                arguments = Bundle().apply { putString("productId", productId) }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.product_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val threshold = 3
                if (lastVisibleItemPosition >= totalItemCount - threshold &&
                    viewModel.isLoading.value == false
                ) {
                    viewModel.fetchNextBatchDesc(
                        genderFilter,
                        tagFilter,
                        fieldNameFilter,
                        fieldValueFilter
                    )
                }
            }
        })

        observeViewModel()
        viewModel.fetchTotalProductCount(genderFilter, tagFilter, fieldNameFilter, fieldValueFilter)
        viewModel.fetchNextBatchDesc(genderFilter, tagFilter, fieldNameFilter, fieldValueFilter)

    }
    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.totalItemCount.observe(viewLifecycleOwner) { count ->
            binding.tvResultCount.text = "Total Results: $count"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(gender: String? = null, tag: String? = null): ProductListDesc {
            val fragment = ProductListDesc()
            fragment.arguments = Bundle().apply {
                putString("gender", gender)
                putString("tag", tag)
            }
            return fragment
        }
    }
}