package com.tolgahantutar.getlocationkotlin

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.jar.Manifest
//We are creating variables that we will need
lateinit var fusedLocationProviderClient: FusedLocationProviderClient
lateinit var locationRequest: LocationRequest

//the permission id is just an int that must be unique so you can use any number you want
private var PERMISSION_ID = 36

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Now let's initiate the fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //Now add the event to our Button
        getPos.setOnClickListener {
            getLastLocation()
        }

    }
    //function to get the city name
    private fun getCityName(lat:Double,long:Double) : String{

        var geoCoder = Geocoder(this, Locale.getDefault())
        var adress : MutableList<Address> = geoCoder.getFromLocation(lat,long,1)

        val cityName = adress.get(0).adminArea
        return cityName
    }
    //function to get the countryname
    private fun getCountryName(lat:Double,long:Double) : String{

        var geoCoder = Geocoder(this, Locale.getDefault())
        var adress = geoCoder.getFromLocation(lat,long,1)

        val countryName = adress.get(0).countryName
        return countryName
    }
    //Now we will create a function that will allow us to get the last location

    private fun getLastLocation(){
        //first we check permission
        if (checkPermission()){
        // Now we check the location service is enabled
            if (isLocationEnabled()){
                //Now let's get the location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener{task ->
                    var location : Location? = task.result
                    if(location == null){
                    //If the location is null we will get the new user location
                        //so we need to create a new function
                        getNewLocation()
                    }else{
                        Locationtxt.text = "Your current coordinates are :\nLat:"+location.latitude+"; Long:"+location.longitude+
                                "\n Your City: "+getCityName(location.latitude,location.longitude)+", your country: "+getCountryName(location.latitude,location.longitude)
                    }
                }
            }else{
                Toast.makeText(this, "Please enable your location service", Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
        }
    }
    //First we need to create a function that will check the uses permission
    private fun checkPermission():Boolean{
        if(
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager
                .PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager
                .PERMISSION_GRANTED
                ){
            return true
        }
        return false
    }
    //Now we need to create a function that will allow us to get user's permission
    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    // Now we need a function that check if the location service of the device is enabled

    private fun isLocationEnabled(): Boolean{
        var locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //this is a built in function that check the permission result
        //we will use it just for debugging our code
        if(requestCode== PERMISSION_ID){
            if (grantResults.isNotEmpty()&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug","You have the permission")
            }
        }
    }
    private fun getNewLocation() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallBack, Looper.myLooper()
        )
    }
    private var locationCallBack = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            Locationtxt.text = "Your current coordinates are :\nLat:"+lastLocation.latitude+"; Long:"+lastLocation.longitude+
                    "\n Your City: "+getCityName(lastLocation.latitude,lastLocation.longitude)+", your country: "+getCountryName(lastLocation.latitude,lastLocation.longitude)

        }
    }
}