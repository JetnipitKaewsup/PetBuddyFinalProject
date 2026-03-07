package com.example.petbuddy.api

import retrofit2.Call
import retrofit2.http.GET

interface DogApiService {

    @GET("breeds/list/all")
    fun getDogBreeds(): Call<DogResponse>

}