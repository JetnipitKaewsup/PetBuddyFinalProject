package com.example.petbuddy.api

import retrofit2.Call
import retrofit2.http.GET

interface CatApiService {

    @GET("v1/breeds")
    fun getCatBreeds(): Call<List<CatBreed>>

}