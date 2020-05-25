package com.radiant.cityguide

import android.app.Activity
import android.app.ListActivity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
//                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        createLocationRequest()
        fetchJson()
    }

    class HomeFeed(val results:List<Result>)

    class Result(val series:List<Serie>)

    class Serie(val name: String, values:List<Value>)

    class Value(val fullness: Int)

    fun fetchJson() {
        println("Attempting to Fetch JSON")

        val url = "http://162.243.166.72:8086/query?db=trash&q=SELECT*FROM%20bishkek"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response?.body?.string()
                println(body)

                val gson = GsonBuilder().create()

                val homeFeed = gson.fromJson(body, HomeFeed::class.java)

            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execude request")
            }
        })
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMarkets()
    }

    val bishkek = LatLng(42.8774274, 74.5956251)
    val bishkek2 = LatLng(42.8579532, 74.6133573)
    val bishkek3 = LatLng(42.868378, 74.576871)
    val bishkek4 = LatLng(42.828726, 74.605638)
    val bishkek5 = LatLng(42.856391, 74.636490)


    fun setUpMarkets() {

//        for ()

//        val bishkek = LatLng(42.8774274, 74.5956251)
        map.addMarker(MarkerOptions().position(bishkek).title("Marker in Bishkek").snippet("75"))

//        val bishkek2 = LatLng(42.8579532, 74.6133573)
        map.addMarker(MarkerOptions().position(bishkek2).title("Marker in Bishkek")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

//        val bishkek3 = LatLng(42.868378, 74.576871)
        map.addMarker(MarkerOptions().position(bishkek3).title("Marker in Bishkek").snippet("43"))

//        val bishkek4 = LatLng(42.828726, 74.605638)
        map.addMarker(MarkerOptions().position(bishkek4).title("Marker in Bishkek").snippet("67"))

//        val bishkek5 = LatLng(42.856391, 74.636490)
        map.addMarker(MarkerOptions().position(bishkek5).title("Marker in Bishkek").snippet("88"))

//        val URL = getDirectionURL(bishkek, bishkek, bishkek3, bishkek4, bishkek5)
//        GetDirection(URL).execute()

        setUpMap()
    }




    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                buildPath(currentLatLng, currentLatLng, bishkek4, bishkek, bishkek2, bishkek3, bishkek5)

//                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    private fun buildPath(start:LatLng, destination:LatLng, way1: LatLng, way2: LatLng, way3: LatLng, way4: LatLng, way5: LatLng) {
        val URL = getDirectionURL(start, destination, way1, way2, way3, way4, way5)
        GetDirection(URL).execute()
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    fun getDirectionURL(origin:LatLng, dest:LatLng, waypnt:LatLng, waypnt2:LatLng, waypnt3:LatLng, waypnt4:LatLng, waypnt5:LatLng): String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}" +
                "&waypoints=optimize:true|${waypnt.latitude},${waypnt.longitude} |" +
                "${waypnt2.latitude},${waypnt2.longitude} |" +
                "${waypnt3.latitude},${waypnt3.longitude}" +
                "${waypnt4.latitude},${waypnt4.longitude}\" " +
                "${waypnt5.latitude},${waypnt5.longitude}\" " +
                "&key=AIzaSyBez6_TeUsnQ0_FA0P1o2v5v9PtrYbrsqY\n"
    }

    inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            val result = ArrayList<List<LatLng>>()

            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()


                for (j in 0 until respObj.routes[0].legs.size) {
                    for (i in 0 until respObj.routes[0].legs[j].steps.size) {
//                        val startLatLng = LatLng(respObj.routes[0].legs[j].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//
//                        path.add(startLatLng)
//
//                        val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())

                        path.addAll(decodePolyline(respObj.routes[0].legs[j].steps[i].polyline.points))
                    }
                    result.add(path)
                }
//                result.add(path)
            } catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            map.addPolyline(lineoption)
        }

        public fun decodePolyline(encoded: String) : List<LatLng> {

            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
                poly.add(latLng)
            }

            return poly
        }
    }
}
