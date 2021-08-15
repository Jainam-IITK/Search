package com.jb.search.view

import android.content.ContentValues.TAG
import android.inputmethodservice.Keyboard
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.jb.lucenecompose.Utils.startIndexing
//import com.jb.lucenecompose.Utils.startIndexing
import com.jb.search.Model.SearchViewModel.SearchViewModel
import com.jb.search.utils.checkPermissionRecur
import com.jb.search.utils.isStoragePermissionGranted
import com.jb.search.view.searchFragment.searchListAdapter.SearchAdapter
import com.jb.search.databinding.ActivityMainBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.runBlocking
import java.util.*
import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var searchAdapter: SearchAdapter = SearchAdapter()
    private var timer: Timer? = null
    //viewModel
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.addLogAdapter(AndroidLogAdapter())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if(isStoragePermissionGranted(applicationContext,this)){
           startIndexing(application,this)
        }else{
            runBlocking {
                checkPermissionRecur(context = this@MainActivity,activity = this@MainActivity)
            }
           startIndexing(application,this)
        }


    }

    override fun onStart() {
        super.onStart()
        val timeStart = System.currentTimeMillis()
        binding.searchText.requestFocus()

        Log.i("SearchFragment", "Called ViewModelProvider.get")
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        Log.d(TAG, "onCreateView: over in ${System.currentTimeMillis()-timeStart}")


        Log.d(TAG, "onViewCreated: indexing call over in ${System.currentTimeMillis()-timeStart}")
        //Search View
        val timeStart2 = System.currentTimeMillis()
        binding.searchText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: ")
                if(timer != null){
                    timer?.cancel()
                }
                viewModel.stopIfSearching()

            }

            override fun afterTextChanged(query: Editable?) {
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        // you will probably need to use
                        // runOnUiThread(Runnable action) for some specific
                        viewModel.updateSearch(query?.toString(),context = this@MainActivity,activity = this@MainActivity)
                        // actions
                    }
                }, 300)
                Log.d(TAG, "afterTextChanged: ")
            }
        })

        Log.d(TAG, "onViewCreated: query listener initialised in ${System.currentTimeMillis()-timeStart2}")

        //Search Results list
        //This[searchResultList] is the result list which updates as the user types in the search view and
        //we get a list which shows the results of the search
        val timeStart3 = System.currentTimeMillis()
        binding.recyclerView.adapter = searchAdapter
        binding.recyclerView.addOnScrollListener(object: OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    SCROLL_STATE_IDLE -> println("The RecyclerView is not scrolling")
                    SCROLL_STATE_DRAGGING -> println("Scrolling now")
                    SCROLL_STATE_SETTLING -> println("Scroll Settling")
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                when {
                    dx > 0 -> {
                        System.out.println("Scrolled Right");
                    }
                    dx < 0 -> {
                        System.out.println("Scrolled Left");
                    }
                    else -> {
                        System.out.println("No Horizontal Scrolled");
                    }
                }

                when {
                    dy > 0 -> {
                        System.out.println("Scrolled Downwards");
                        hideKeyboard(this@MainActivity)
                    }
                    dy < 0 -> {
                        System.out.println("Scrolled Upwards");
                    }
                    else -> {
                        System.out.println("No Vertical Scrolled");
                    }
                }
            }
        })
        viewModel.searchResultList.observe(this, {
            searchAdapter.searchResultList = it
        })

        Log.d(TAG, "onViewCreated: recyclerview done  ${System.currentTimeMillis()-timeStart3}")
    }


}

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
}