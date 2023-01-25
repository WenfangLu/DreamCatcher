package edu.vt.cs.cs5254.gallery

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.vt.cs.cs5254.gallery.api.GalleryItem

private val TAG = "PhotoMapFragment"


class PhotoMapFragment : MapViewFragment(), GoogleMap.OnMarkerClickListener  {

    // init PhotoMapViewModel
    private val viewModel: PhotoMapViewModel by viewModels()
    private lateinit var thumbnailDownloader: ThumbnailDownloader<Marker>
    var geoGalleryMap = emptyMap<String,GalleryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler){
                marker, bitmap ->
                setMarkerIcon(marker, bitmap)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        // call parent's onCreateMapView
        val mapView = super.onCreateMapView(inflater,
            container,
            savedInstanceState,
            R.layout.fragment_photo_map,
            R.id.map_view)
        Log.d("#########", "CALL PhotoMapFragment !!!!!!!!!")
        return mapView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onMapViewCreated(view,savedInstanceState, block = {
           googleMap -> Log.i(TAG, "Google Map acquired")

           googleMap.setOnMarkerClickListener(this@PhotoMapFragment)
           viewModel.geoGalleryItemMapLiveData.observe(
               viewLifecycleOwner
           ){
                   geoGalleryItemMapArg -> Log.i(TAG, "Gallery Items acquired")
                    geoGalleryMap = geoGalleryItemMapArg
                    updateUI()
           }
       })

    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    private fun updateUI() {

        if( super.mapIsInitialized() == false){
            return
        }
        // if the fragment is not currently added to its activity, or
        // if there are not gallery items, do not update the UI
        if (!isAdded || geoGalleryMap.isEmpty()) {
            return
        }

        Log.i(TAG, "Gallery has has " + geoGalleryMap.size + " items")

        // remove all markers, overlays, etc. from the map
        googleMap.clear()

        val bounds = LatLngBounds.Builder()

        for (item in geoGalleryMap.values) {
            // log the information of each gallery item with a valid lat-lng
            Log.i(
                TAG,
                "Item id=${item.id} " +
                        "lat=${item.latitude} long=${item.longitude} " +
                        "title=${item.title}"
            )
            // create a lan-lng point for the item and add it to the lat-lng bounds
            val itemPoint = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
            bounds.include(itemPoint)

            // create a marker for the item and add it to the map
            val itemMarker = MarkerOptions().position(itemPoint).title(item.title)
            val marker = googleMap.addMarker(itemMarker)
            if (marker != null) {
                marker.tag = item.id
                thumbnailDownloader.queueThumbnail(marker, item.url)
            }

        }

        Log.i(TAG, "Expecting ${geoGalleryMap.size} markers on the map")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        googleMap.clear()
        viewModel.reloadPhotos()
        updateUI()
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
    }

    override fun onMarkerClick(marker : Marker): Boolean {
       val galleryItemId = marker.tag as String
        Log.d(TAG, "Click on marker $galleryItemId")
        val item = geoGalleryMap[galleryItemId]
        val uri = item?.photoPageUri
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
        return true
    }
}