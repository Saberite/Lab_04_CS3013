package edu.msudenver.cs3013.catagentdeployer

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.msudenver.cs3013.catagentdeployer.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    //Added variable slide 13
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    //Added Marker 7.2 slide 13
    private var marker: Marker? = null

    //Added variable from slide 8 Lecture 7.2
    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        //Add
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }



    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //Added mMap change from 7.2 slide 18
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            setOnMapClickListener { latLng ->
                addOrMoveSelectedPositionMarker(latLng)
            }
        }


        //Added when cond, Checks if app has permissions Return true/false slide 15
        when {
            hasLocationPermission() -> getLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    //Requesting Location Permission Slide 8. Added getLocation From Line 96
    //Changed for 7.2 with fusedLocation slide 11 From Line 97 to 103
    //Changed to add Val CO 7.2 lecture from slide 12
    private fun getLocation() {
        //Log.d("MapsActivity", "getLocation() called.")
        //fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                //location: Location? ->
            //location?.let {
               // val userLocation = LatLng(location.latitude, location.longitude)
               // updateMapLocation(userLocation)
               // addMarkerAtLocation(userLocation, "You")
           // }}

        val CO = LatLng(39.0, -105.0)
        updateMapLocation(CO)
        addMarkerAtLocation(CO, "You")


    }

    //Added updateMap Slide 10 Lecture 7.2
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
    }

    //Added addMarker at location slide 10 Lecture 7.2
    //private fun addMarkerAtLocation(location: LatLng, title: String)
    //{
        //mMap.addMarker(MarkerOptions().title(title).position(location))
    //}

    //Added addMarkerAtLocation 7.2 slide 13
    private fun addMarkerAtLocation(location: LatLng, title: String, markerIcon: BitmapDescriptor? = null
    ) = mMap.addMarker(
        MarkerOptions().title(title).position(location)
            .apply { markerIcon?.let { icon(markerIcon) } }
    )

    //Added BitmapDescrip func 7.2 slide 14
    private fun getBitmapDescriptorFromVector(@DrawableRes
                                              vectorDrawableResourceId: Int): BitmapDescriptor? {
        val bitmap = ContextCompat.getDrawable(this,
            vectorDrawableResourceId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight)
            val drawableWithTint = DrawableCompat
                .wrap(vectorDrawable)
            DrawableCompat.setTint(drawableWithTint,
                Color.RED)
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawableWithTint.draw(canvas)
            bitmap
        }?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
            .also { bitmap?.recycle() }
    }

    //Added drawable icon add on, Called on when function above is used. 7.2 slide 18
    private fun addOrMoveSelectedPositionMarker(latLng: LatLng) {
        if (marker == null) {
            marker = addMarkerAtLocation(latLng, "Deploy here",
                getBitmapDescriptorFromVector(R.drawable.baseline_approval_24)
            )
        } else { marker?.apply { position = latLng } }
    }

    //Added Permission location. Allows permission to location Slide 14
    private fun hasLocationPermission() =
//check if ACCESS_FINE_LOCATION permission is granted
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    //Added showPermission Slide 11
    private fun showPermissionRationale(
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveAction()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

}