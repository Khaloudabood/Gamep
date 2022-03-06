package com.example.android.pokemon

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.android.pokemon.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        loadPokemon()
        checkPermissions()

    }//end onCreate()
    val accessLocation = 123
    fun checkPermissions(){

        if(Build.VERSION.SDK_INT >= 23){

            if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), accessLocation)
                return
            }
        }
        getUserLocation()
    }//end checkPermissions()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            accessLocation->{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getUserLocation()
                }else{
                    Toast.makeText(this, "Location access is deny", Toast.LENGTH_LONG).show()
                }
            }
        }//end when
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }//end onRequestPermissionsResult()

    fun getUserLocation(){
        Toast.makeText(this, "Location access now", Toast.LENGTH_LONG).show()
        //TODO: access user location

        val myLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,3f,myLocation)

        val myThread = MyThread()
        myThread.start()
    }//end getUserLocation()

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


    }//end onMapReady

     var myLocation: Location? = null
     inner class MyLocationListener: LocationListener{
         constructor(){
             myLocation = Location("me")
             myLocation!!.longitude = 0.0
             myLocation!!.latitude = 0.0
         }//end constructor()
         override fun onLocationChanged(location: Location) {
             myLocation= location
         }

         override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
             super.onStatusChanged(provider, status, extras)
         }

         override fun onProviderEnabled(provider: String) {
             super.onProviderEnabled(provider)
         }

         override fun onProviderDisabled(provider: String) {
             super.onProviderDisabled(provider)
         }
     }//end MyLocationListener class
    var oldLocation: Location? = null
    inner class MyThread: Thread{
        constructor():super(){
            oldLocation = Location("oldLocation")
            oldLocation!!.longitude = 0.0
            oldLocation!!.latitude = 0.0
        }//end constructor()

        override fun run() {
           while (true){
               try {

                   if (oldLocation!!.distanceTo(myLocation) == 0f)
                   {continue}
                   oldLocation = myLocation
               runOnUiThread(){
                   mMap.clear()
                   // Add a marker in Sydney and move the camera
                   val sydney = LatLng(myLocation!!.latitude, myLocation!!.longitude)
                   mMap.addMarker(MarkerOptions()
                       .position(sydney)
                       .title("Me")
                       .snippet("here is my location")
                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario_foreground)))
                   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,14f))

                   //show pokemons

                   for(i in 0..listOfPokemons.size-1){

                       var newPokemon = listOfPokemons[i]

                       if (newPokemon.isCatch == false){

                           val pockLocation = LatLng(newPokemon.location!!.latitude, newPokemon.location!!.longitude)
                           mMap.addMarker(MarkerOptions()
                               .position(pockLocation)
                               .title(newPokemon.name)
                               .snippet(newPokemon.describtion + ", power: " + newPokemon.power)
                               .icon(BitmapDescriptorFactory.fromResource(newPokemon.image!!)))

                           if (myLocation!!.distanceTo(newPokemon.location) < 2){
                           myPower += newPokemon.power!!
                           newPokemon.isCatch = true
                               listOfPokemons[i]=newPokemon
                               Toast.makeText(applicationContext, "You catch new pockemon, your new power is"
                                       + myPower, Toast.LENGTH_LONG).show()
                           }
                       }
                   }//end for
               }//end runOnUiThread()

                   Thread.sleep(1000)
               }catch (ex:Exception){}
           }//end while
        }//end run
    }//end MyThread

    var myPower:Double = 0.0

    var listOfPokemons = ArrayList<Pokemon>()
    fun loadPokemon(){
        listOfPokemons.add(
            Pokemon(R.drawable.cc_foreground,
        "Charmander", "Charmander living in japan", 55.0, 37.7789994893035, -122.401846647263)
        )
        listOfPokemons.add(Pokemon(R.drawable.bb_foreground,
            "Bulbasaur", "Bulbasaur living in USA", 90.5, 37.7949502667, -122.410494089127))
        listOfPokemons.add(Pokemon(R.drawable.ss_foreground,
            "Squirtle", "Squirtle living in Iraq", 33.5, 37.7816621152613, -122.41225361824))

    }//end loadPokemon
}//end Class