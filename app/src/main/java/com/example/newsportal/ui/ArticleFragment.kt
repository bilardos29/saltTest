@file:Suppress("DEPRECATION")

package com.example.newsportal.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsportal.R
import com.example.newsportal.adapters.ArticleAdapter
import com.example.newsportal.adapters.ItemClicklistner
import com.example.newsportal.db.Article
import com.example.newsportal.mvvm.NewsDatabase
import com.example.newsportal.mvvm.NewsRepo
import com.example.newsportal.mvvm.NewsViewModel
import com.example.newsportal.mvvm.NewsViewModelFac
import com.example.newsportal.wrapper.Resource

class ArticleFragment : Fragment(), ItemClicklistner, MenuProvider {
    // TODO: Rename and change types of parameters
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: ArticleAdapter
    lateinit var rv: RecyclerView
    lateinit var pb: ProgressBar
    var isClicked: Boolean = false
    var isOpened: Boolean = false
    var addingResponselist = arrayListOf<Article>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setTitle("Article News")
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.CREATED)
        setHasOptionsMenu(true)

        val dao = NewsDatabase.getInstance(requireActivity()).newsDao
        val repository = NewsRepo(dao)
        val factory = NewsViewModelFac(repository, requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]

        rv = view.findViewById(R.id.rvBreakingNews)
        pb = view.findViewById(R.id.paginationProgressBar)

        setUpRecyclerView()
        isClicked = true
        loadBreakingNews()

    }

    private fun loadBreakingNews() {
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsresponse ->
                        addingResponselist = newsresponse.articles as ArrayList<Article>
                        newsAdapter.setlist(newsresponse.articles)
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { messsage ->
                        Log.i("BREAKING FRAG", messsage.toString())
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    fun showProgressBar() {
        pb.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        pb.visibility = View.INVISIBLE
    }

    private fun setUpRecyclerView() {
        newsAdapter = ArticleAdapter()
        newsAdapter.setItemClickListener(this)
        rv.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        }
    }

    override fun onItemClicked(position: Int, article: Article) {

        // GOING TO ANOTHER FRAGMENT
        val action = ArticleFragmentDirections.actionFragmentArticleToFragmentDetail(article)
        view?.findNavController()?.navigate(action)
    }

    private fun newFilterItems(p0: String?) {

        var newfilteredlist = arrayListOf<Article>()
        for (i in addingResponselist) {
            if (i.title!!.contains(p0!!)) {
                newfilteredlist.add(i)
            }
        }
        setUpRecyclerView()
        newsAdapter.filteredList(newfilteredlist)

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menuInflater.inflate(R.menu.menu, menu)

        val deleteIcon = menu.findItem(R.id.deleteAll)
        deleteIcon.setVisible(false)
        val menuItem = menu.findItem(R.id.searchNews)
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search News"

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                newFilterItems(p0)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                newFilterItems(p0)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }
}