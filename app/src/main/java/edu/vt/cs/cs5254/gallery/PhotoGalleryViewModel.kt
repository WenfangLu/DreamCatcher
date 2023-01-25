package edu.vt.cs.cs5254.gallery

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel : ViewModel() {

    val galleryItemsLiveData = FlickrFetchr.galleryItemsLiveData
    fun loadPhotos() = FlickrFetchr.fetchPhotos()
    fun storeThumbnail(id: String, drawable: Drawable) {
        FlickrFetchr.storeThumbnail(id, drawable)
    }
    fun reloadPhotos(){
        FlickrFetchr.fetchPhotos()
    }
}