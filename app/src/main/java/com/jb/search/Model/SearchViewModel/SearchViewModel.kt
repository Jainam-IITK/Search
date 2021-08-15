package com.jb.search.Model.SearchViewModel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.lucenecompose.Utils.startIndexing
//import com.jb.lucenecompose.Utils.startIndexing
import com.jb.search.INDEX
import com.jb.search.searchUtil.Searcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.lucene.index.IndexNotFoundException
typealias SearchResults = Pair<List<String>,List<String>>
class SearchViewModel:ViewModel() {
    init {
        Log.i("SearchViewModel", "SearchViewModel created!")

    }

    override fun onCleared() {
        super.onCleared()
        Log.i("SearchViewModel", "SearchViewModel destroyed!")
    }

    val searchResultList:MutableLiveData<SearchResults> by lazy {
        MutableLiveData<SearchResults>()
    }

    fun updateSearch(query:String?,context: Context,activity: Activity){

        viewModelScope.launch {
            withContext(Dispatchers.Default){
                if (query==null) return@withContext

                if (query=="") return@withContext

                val path = context.filesDir?.absolutePath + '/'+ INDEX
                try {
                    val result = Searcher.main(path,query)
                    //val result = Searcher.main(indexDir = path,query=query)
                    val filePaths :List<String> = result.first
                    val snippets :List<String> = result.second
                    searchResultList.postValue(Pair(filePaths,snippets))
                    return@withContext

                }catch (e:IndexNotFoundException){
                   // startIndexing(context,activity)
                }
                return@withContext
            }
        }

    }

    fun stopIfSearching() {
        return
        if (Searcher.isSearching()){
            Searcher.stopSearching()
        }
    }

}


