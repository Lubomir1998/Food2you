package com.example.food2you.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.food2you.R
import com.example.food2you.databinding.TrackOrderFragmentBinding
import com.example.food2you.other.Constants.COARSE_LOCATION
import com.example.food2you.other.Constants.FINE_LOCATION
import com.example.food2you.other.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.example.food2you.viewmodels.TrackViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TrackOrderFragment: Fragment(), OnMapReadyCallback {

    private lateinit var binding: TrackOrderFragmentBinding
    private val viewModel: TrackViewModel by viewModels()
    private val args: TrackOrderFragmentArgs by navArgs()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false
    private var map: GoogleMap? = null
    var pathLines = listOf<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TrackOrderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.map.onCreate(savedInstanceState)

        getLocationPermission()

        collectCoordinates()

    }

    private fun animateCamera(){
        if(pathLines.isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathLines.last(), 15f))
        }
    }

    private fun collectCoordinates() {
        lifecycleScope.launchWhenStarted {
            viewModel.coordinates.collect {
                it?.let { track ->
                    val coordinates = track.coordinates

                    val list = mutableListOf<LatLng>()

                    for(position in coordinates) {
                        list.add(LatLng(position.latitude, position.longitude))
                        Log.d("TAG", "coords ->  ${position.latitude}   ${position.longitude}\n")
                    }

                    pathLines = list

                    addLatestPolyline(pathLines)
                    animateCamera()
                }

            }
        }
    }

    private fun initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        binding.map.getMapAsync {
            map = it
            getDeviceLocation()
            viewModel.getCoordinates(args.orderId)
            addAllPolylines()
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        map!!.isMyLocationEnabled = true
        map!!.uiSettings.isMyLocationButtonEnabled = true
    }


    private fun getLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                requestPermissions(
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            requestPermissions(
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("VisibleForTests")
    private fun getDeviceLocation(){
        mFusedLocationClient = FusedLocationProviderClient(requireContext())

        if(mLocationPermissionsGranted){
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val location = mFusedLocationClient.lastLocation
            location.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    if(task.result != null) {
                        val currentLocation = task.result as Location

                        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                        val zoomLevel = 15f

                        moveCamera(latLng, zoomLevel, "My Location")
                    }
                }
            }

        }

    }

    private fun addAllPolylines(){
        val polylineOptions = PolylineOptions()
                .color(R.color.orange)
                .width(10f)
                .addAll(pathLines)
        map?.addPolyline(polylineOptions)
    }

    private fun addLatestPolyline(pathLines: List<LatLng>){
        if(pathLines.size > 1){
            val preLastLatLng = pathLines[pathLines.size - 2]
            val lastLatLng = pathLines.last()
            val polylineOptions = PolylineOptions()
                    .color(R.color.orange)
                    .width(10f)
                    .add(preLastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCamera(latLng: LatLng, zoomLevel: Float, title: String){
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        // add marker on recent location
        val marker = MarkerOptions().position(latLng).title(title)
        map!!.addMarker(marker)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                mLocationPermissionsGranted = true
                initMap()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapView: MapView? = requireActivity().findViewById(R.id.map)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }
}