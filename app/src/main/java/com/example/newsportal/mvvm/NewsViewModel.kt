@file:Suppress("DEPRECATION")

package com.example.newsportal.mvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsportal.db.News
import com.example.newsportal.db.SavedArticle
import com.example.newsportal.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(val newsRepo: NewsRepo, application: Application) :
    AndroidViewModel(application) {


    //adding news data
    val breakingNews: MutableLiveData<Resource<News>> = MutableLiveData()
    val pageNumber = 1

    // to get saved news
    val getSavedNews = newsRepo.getAllSavedNews()

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(code: String) = viewModelScope.launch {
        checkInternetandBreakingNews(code)
    }

    private suspend fun checkInternetandBreakingNews(code: String) {
        breakingNews.postValue(Resource.Loading())

        try {
            val response = newsRepo.getBreakingNews(code, pageNumber)
            breakingNews.postValue(handleNewsResponse(response))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("NETWORK FAILER"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleNewsResponse(response: Response<News>): Resource<News>? {
        if (response.isSuccessful) {
            response.body()?.let { resultresponse ->
                return Resource.Success(resultresponse)
            }
        }
        return Resource.Error(response.message())
    }

    // get category
    fun insertArticle(savedArt: SavedArticle) {
        insertNews(savedArt)
    }

    fun insertNews(savedArt: SavedArticle) = viewModelScope.launch(Dispatchers.IO) {
        newsRepo.insertNews(savedArt)
    }

    fun deleteAllArtciles() {
        deleteAll()
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        newsRepo.deleteAll()
    }
}