package com.example.yelpdiscovery

import android.location.Location
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLEncoder

// Yelp client ID: YGh0bM9sFV_7en0MrckH5g
// Yelp API key: 0SrG207I75QqhYo3xk6wsORy8Nq6IU5BgCYz3FxYi26xgIHMeBrRpaq23vSKTF_8hTMns0-hIzuUlmYZsANh1Tw3Tcj7LXkehXmJBiEOT9P8_r32ityqjyCVBaxZXnYx
// Google API key: AIzaSyCphOPNXz2Dyk3P3pOUWpr8Hgxtivi5_Ko

private val gson = Gson()
private const val YELP_API_KEY = "Bearer 0SrG207I75QqhYo3xk6wsORy8Nq6IU5BgCYz3FxYi26xgIHMeBrRpaq23vSKTF_8hTMns0-hIzuUlmYZsANh1Tw3Tcj7LXkehXmJBiEOT9P8_r32ityqjyCVBaxZXnYx"

public suspend fun yelpSearchHTTP(location: Location, searchTerms: String = ""): Map<String, Any> {
    var urlSearchTerms = URLEncoder.encode(searchTerms, "utf-8")

    val (request, response, result) = ("https://api.yelp.com/v3/businesses/search?"
            + (if(searchTerms.isNotEmpty()) "&term=$urlSearchTerms" else "") +
            "&latitude=" + location.latitude +
            "&longitude=" + location.longitude)
        .httpGet()
        .header("Authorization", YELP_API_KEY)
        .responseString()

    return when (result) {
        is Result.Failure -> {
	        val errString = "Error" to result.getException().toString() + " Response: " + response.toString()
	        println(errString)
	        mapOf(errString)
        }
        is Result.Success -> {
            val data = result.get()
            gson.fromJson(data, object : TypeToken<Map<String, Any>>() {}.type)
        }
    }
}

public suspend fun yelpDetailsHTTP(id: String): Map<String, Any> {
	val (request, response, result) = "https://api.yelp.com/v3/businesses/${id}"
			.httpGet()
			.header("Authorization", YELP_API_KEY)
			.responseString()

	return when (result) {
		is Result.Failure -> {
			val errString = "Error" to result.getException().toString() + " Response: " + response.toString()
			println(errString)
			mapOf(errString)
		}
		is Result.Success -> {
			val data = result.get()
			gson.fromJson(data, object : TypeToken<Map<String, Any>>() {}.type)
		}
	}
}

public suspend fun yelpReviewHTTP(id: String): Map<String, Any> {
    val (request, response, result) = "https://api.yelp.com/v3/businesses/${id}/reviews"
        .httpGet()
	    .header("Authorization", YELP_API_KEY)
	    .responseString()

    return when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            println(ex)
            mapOf("Error" to ex.toString())
        }
        is Result.Success -> {
            val data = result.get()
            gson.fromJson(data, object : TypeToken<Map<String, Any>>() {}.type)
        }
    }
}
