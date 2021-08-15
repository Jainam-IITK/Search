package com.jb.search.view.searchFragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.jb.search.Model.SearchViewModel.SearchViewModel
import com.jb.search.view.searchFragment.searchListAdapter.SearchAdapter
import com.jb.search.databinding.SearchFragmentBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchFragment : Fragment() {

    private var _binding: SearchFragmentBinding? = null
    private var searchResultList = arrayListOf<String>()
    private var searchAdapter:SearchAdapter = SearchAdapter()

    //viewModel
    private lateinit var viewModel: SearchViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val timeStart = System.currentTimeMillis();
        _binding = SearchFragmentBinding.inflate(inflater, container, false)


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeStart = System.currentTimeMillis();
        Log.i("SearchFragment", "Called ViewModelProvider.get")
        viewModel = ViewModelProvider(this@SearchFragment).get(SearchViewModel::class.java)

        Log.d(TAG, "onCreateView: over in ${System.currentTimeMillis()-timeStart}")

       // context?.apply { SearchViewModel.startIndexing(this) }

        Log.d(TAG, "onViewCreated: indexing call over in ${System.currentTimeMillis()-timeStart}")
        //Search View
        val timeStart2 = System.currentTimeMillis();

//        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                context?.apply {
//                   return viewModel.updateSearch(query,this)
//                }
//                return false
//            }
//
//            override fun onQueryTextChange(query: String?): Boolean {
//                context?.apply {
//                    return viewModel.updateSearch(query,this)
//                }
//                return false
//            }
//
//        })
        Log.d(TAG, "onViewCreated: query listener initialised in ${System.currentTimeMillis()-timeStart2}")
        //Search Results list
        //This[searchResultList] is the result list which updates as the user types in the search view and
        //we get a list which shows the results of the search
        val timeStart3 = System.currentTimeMillis();

//        viewModel?.searchResultList.observe(viewLifecycleOwner, Observer {
//            searchAdapter = SearchAdapter(it).also {
//                binding.recyclerView.adapter = it
//                binding.recyclerView.adapter?.notifyDataSetChanged()
//            }
//        })

        Log.d(TAG, "onViewCreated: recyclerview done  ${System.currentTimeMillis()-timeStart3}")


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}