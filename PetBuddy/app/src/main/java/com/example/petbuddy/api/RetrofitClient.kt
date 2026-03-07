package com.example.petbuddy.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val DOG_BASE_URL = "https://dog.ceo/api/"
    private const val CAT_BASE_URL = "https://api.thecatapi.com/"

    val dogApi: DogApiService by lazy {

        Retrofit.Builder()
            .baseUrl(DOG_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogApiService::class.java)

    }

    val catApi: CatApiService by lazy {

        Retrofit.Builder()
            .baseUrl(CAT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatApiService::class.java)

    }
}