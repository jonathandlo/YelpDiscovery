package com.example.yelpdiscovery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Debug
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.Console
import kotlin.collections.*


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    val PERMISSION_ID = 42
    val MIN_WAIT = 2000L    // milliseconds
    val MIN_DIST = 10       // meters

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var previousLocation: Location
    lateinit var newestLocation: Location
    var currentBusinesses = mutableMapOf<String, Any>()
    var updateJob: Job = GlobalScope.launch { }

    var currentResultsSearchedText = ""
    var numSearches = 0

    var stopUpdating = false
    var shouldRequestOnce = false // used to force a yelp api call (when user changes search)
    var firstLoading = true // the first load happens on the main thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbarCustom))

        // hook UI events
        buttonStart.setOnClickListener { buttonStartPressed() }
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editTextChanged(s, start, before, count)
            }
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermissions()
        checkLocationEnabled()

        // assume for now we have permissions and gps is enabled
        clearAllResults()
        requestLocationUpdates()
    }

    private fun buttonStartPressed() {
        if (firstLoading) return

        if (updateJob.isCompleted) {
            // launch the update job
            updateCheckerLoop()
            // update the visuals
            buttonStart.text = "Stop tracking"
        } else {
            // stop the update job
            stopUpdating = true
            buttonStart.text = "Stopping..."
        }
    }

    private fun editTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        numSearches++
        val searchNumber = numSearches
        val thisQuery = s.toString().trim()

        if (thisQuery == currentResultsSearchedText) return
        println("new search query: $thisQuery")

        GlobalScope.launch {
            delay(1000)

            // leave if this is not the latest search
            if (searchNumber != numSearches) return@launch
            println("launching search: $thisQuery")

            // trigger a new search
            runOnUiThread {
				clearAllResults()
				textStatus.text = "0 results found"
				shouldRequestOnce = true
				currentResultsSearchedText = thisQuery
			}
        }
    }

    /*
     * Location and Permissions
     */

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // request permissions if we don't have them
            println("No permissions, requesting")
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), PERMISSION_ID
            )
        }
    }

    private fun checkLocationEnabled() {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //request permissions
            println("Location is off, prompting")
            runOnUiThread { Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show() }
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun requestLocationUpdates() {
        println("Requesting constant location updates")
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            onLocationReceived(locationResult.lastLocation)
        }
    }

    private fun onLocationReceived(location: Location) {
        newestLocation = Location(location)

        // display results on first location pull
        if (!::previousLocation.isInitialized || !::newestLocation.isInitialized) {
            previousLocation = Location(location)
            GlobalScope.launch {
                requestLatestYelp()

                // the interface is ready for user input
                firstLoading = false
                runOnUiThread { buttonStartPressed() }
            }
        }
    }

    /*
     * UI and Results
     */

    private fun clearAllResults() {
        picTableLayout.removeAllViews()
        currentBusinesses.clear()
    }

    /*
     * Tracking and API calls
     */

    private fun updateCheckerLoop() {
        updateJob = GlobalScope.launch(Dispatchers.Default) {
            while (!stopUpdating) {
                println("thread updating -----")

                if (checkCoords() || shouldRequestOnce) {
                    // request new Yelp Results
                    requestLatestYelp()
                    shouldRequestOnce = false
                }
                delay(MIN_WAIT)
            }
            println("thread stopping")

            // ready the visuals for another launch
            runBlocking(Dispatchers.Main) {
                buttonStart.text = "Start tracking"
                println("UI knows thread is done")
            }

            stopUpdating = false
            println("thread done")
        }
    }

    private suspend fun checkCoords(): Boolean {
        // wait until location process notifies us, or timeout happens
        runBlocking {
            var counter = 0
            while (isActive && counter < 10) {
                delay(100)
                counter++
            }
        }

        // measure the distance between latest position and last used pos
        var distanceMeters = previousLocation.distanceTo(newestLocation)
        println("GPS Moved $distanceMeters m")
        return distanceMeters > MIN_DIST
    }

    private suspend fun requestLatestYelp() = coroutineScope() {
        val result = yelpSearchHTTP(newestLocation, currentResultsSearchedText)

        // mark the location as used
        previousLocation = Location(newestLocation)

        (result["businesses"] as ArrayList<Map<*, *>>).forEach {
            // track each business found (async)
            launch {
                val id = it["id"].toString()
                val details = yelpDetailsHTTP(id)

                if (!currentBusinesses.containsKey(id)) {
                    // find the associated reviews
                    var reviewData = arrayListOf<LineItem.ReviewData>()
                    (yelpReviewHTTP(id) as Map<*, ArrayList<Map<*, *>>>)["reviews"]!!.forEach { itReview ->
                        reviewData.add(
                            LineItem.ReviewData(
                                (itReview["user"] as Map<*, *>)["name"].toString(),
                                (itReview["user"] as Map<*, *>)["image_url"].toString(),
                                itReview["text"].toString(),
                                itReview["rating"].toString()
                            )
                        )
                    }

                    // add the business in memory and on screen
                    val name = details["name"]!!.toString()
                    val location =
                        (details["location"] as Map<*, ArrayList<*>>)["display_address"]!!.joinToString(
                            " "
                        )
                    val bizURL = details["url"].toString()
                    val reviewCount = details["review_count"]!!.toString().toFloat().toInt()
                    val urls = details["photos"]!! as ArrayList<String>
                    val categories =
                        (details["categories"] as ArrayList<Map<*, *>>).joinToString() { itCategory -> itCategory["title"].toString() }

                    val lineItem = LineItem(
                        this@MainActivity,
                        name,
                        location,
                        bizURL,
                        urls,
                        categories,
                        "4.0",
                        reviewCount,
                        reviewData
                    )
                    currentBusinesses[id] = details
                    runOnUiThread() {
                        lineItem.load()
                        textStatus.text = "${currentBusinesses.count()} results found"
                    }
                }
            }

            delay(230)
        }
    }
}