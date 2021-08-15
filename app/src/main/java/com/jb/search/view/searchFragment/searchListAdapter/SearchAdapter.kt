package com.jb.search.view.searchFragment.searchListAdapter

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jb.search.Model.SearchViewModel.SearchResults
import com.jb.search.R
import com.jb.search.utils.SearchItemUtils
import com.jb.search.databinding.SearchFragmentRowItemBinding

class SearchAdapter():RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    var searchResultList =  SearchResults(arrayListOf(), arrayListOf())
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: SearchFragmentRowItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.search_fragment_row_item,parent, false)
       return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filePath = searchResultList.first[position]
        val snippet :String? = searchResultList.second[position]
        holder.binding.fileTypeIcon.setImageImage(filePath)

        holder.binding.name.text = filePath.substringAfterLast('/')
        if (snippet.isNullOrEmpty()){
            holder.binding.snippet.text = " "
        }else{
            holder.binding.snippet.text = HtmlCompat.fromHtml(snippet,HtmlCompat.FROM_HTML_MODE_COMPACT)
        }

        val context = holder.itemView.context
        holder.itemView.setOnClickListener {
            Log.d("Click", "onBindViewHolder: $filePath")
            SearchItemUtils.openFile(filePath,context)
        }
    }

    override fun getItemCount(): Int = searchResultList.first.size


    inner class ViewHolder( val binding:SearchFragmentRowItemBinding) : RecyclerView.ViewHolder(binding.root)


}

