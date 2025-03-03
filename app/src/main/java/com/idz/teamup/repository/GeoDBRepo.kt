package com.idz.teamup.repository

import com.idz.teamup.model.CityResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GeoDBApiService {
    @GET("v1/geo/cities")
    suspend fun getCities(
        @Query("namePrefix") query: String,
        @Query("countryIds") country: String = "IL",
        @Query("limit") limit: Int = 5,
        @Query("types") types: String = "CITY",
        @Query("fields") fields: String = "city",
        @Header("X-RapidAPI-Key") apiKey: String
    ): CityResponse
}


object GeoDBRepo {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wft-geo-db.p.rapidapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeoDBApiService::class.java)

    suspend fun getCities(query: String, apiKey: String): List<String> {
        return try {
            val response = api.getCities(query, apiKey = apiKey)

            response.data.map { it.city }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
