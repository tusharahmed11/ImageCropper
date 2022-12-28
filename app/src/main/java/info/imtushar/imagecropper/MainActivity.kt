package info.imtushar.imagecropper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.load
import info.imtushar.image_cropper.CropImage
import java.io.File
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private var tempImageUri: Uri? = null
    private var tempImageFilePath = ""
    val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203
    private lateinit var mImageView: AppCompatImageView


    companion object {
        var weakActivity: WeakReference<MainActivity>? = null

        fun getInstanceActivity(): MainActivity? {
            return weakActivity?.get()
        }
    }

    var PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getImageButton = findViewById<AppCompatButton>(R.id.get_image_btn)
        mImageView = findViewById(R.id.ImageView_image)
        getImageButton.setOnClickListener {
            checkForPermission()
        }

    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                showImagePickerDialog()
            } else {
                permissions.entries.forEach {
                    if (it.key == Manifest.permission.CAMERA){
                        if (!it.value){
                            showToast(this,"Camera permission needed")
                        }

                    }else if(it.key == Manifest.permission.WRITE_EXTERNAL_STORAGE){
                        if (!it.value){
                            showToast(this,"Camera permission needed")
                        }
                    }
                }

            }
        }

    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>(
            "Capture Photo",
            "Open Gallery",
            "Cancel"
        ) //"Take Photo", "Choose from Gallery", "Cancel"
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Capture Photo")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Capture Photo") { //"Take Photo"

                capturePhoto()
            }
            if (options[item] == "Open Gallery") { //"Choose from Gallery"
                openGallery()
            } else if (options[item] == "Cancel") { //"Cancel"
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        getImageFromGallery.launch("image/*")
    }

    private fun capturePhoto() {

        tempImageUri = FileProvider.getUriForFile(
            this, "${packageName}.provider",
            createImageFile().also {
                tempImageFilePath = it.absolutePath
            })

        getImageFromCamera.launch(tempImageUri)

    }

    private val getImageFromCamera = registerForActivityResult(ActivityResultContracts.TakePicture()){success->
        if (success){
            Log.d("getImageFromCamera", "Image: $tempImageUri \n $tempImageFilePath" )
            CropImage.activity(tempImageUri).start(this,startForResult)
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image",".jpg",storageDir)
    }

    private val getImageFromGallery = registerForActivityResult(PhotoActivityContract()){
        Log.d("getImageFromGallery", "Image: $it")


        // start picker to get image for cropping and then use the image in cropping activity

        CropImage.activity(it).start(this,startForResult)


    }
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(result.data)
            val resultUri = result.uri
            Log.d("getImageResult", "ImageResult: $resultUri")
            mImageView.load(resultUri)
            // Handle the Intent
        }
    }
/*
    private val getContentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        Log.d("CroppedImage", "Image: ${result.data?.extras}")

        if (result.data?.data != null){
            Log.d("CroppedImage", "Image: ${result.data?.data!!}")
        }

    }*/



    fun showToast(ctx: Context?, msg: String?) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }

    private fun checkForPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ->{
                permReqLauncher.launch(
                    PERMISSIONS
                )
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                /*   showSnackBarForOpenSettings(
                       requireContext(),
                       getString(R.string.camera_permission_needed),
                       binding.deleteStoreTv
                   )*/
                showToast(this, "Add permission from app settings")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                showToast(this, "Add permission from app settings")
            }
            else -> {
                permReqLauncher.launch(
                    PERMISSIONS
                )
            }
        }
    }



}