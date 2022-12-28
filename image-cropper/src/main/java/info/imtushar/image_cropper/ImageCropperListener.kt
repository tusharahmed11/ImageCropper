package info.imtushar.image_cropper

import android.net.Uri

interface ImageCropperListener {
    fun imageCropperAction(uri: Uri)
}