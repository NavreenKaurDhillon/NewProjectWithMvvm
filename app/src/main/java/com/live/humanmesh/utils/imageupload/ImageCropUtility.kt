package com.live.humanmesh.utils.imageupload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.live.humanmesh.R
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

abstract class ImageCropUtility : AppCompatActivity() {

    private var mActivity: Activity? = null
    var mVideoDialog: Boolean = false
    private var mCode = 0
    private lateinit var mImageFile: File
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>
    private lateinit var imageGalleryLauncher: ActivityResultLauncher<Intent>

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )
    private val permissionsFor13 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA
    )

    private val permissions1 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->

            Log.d("wjkfjkw", "onCreate: result = $result")
            if (result.isSuccessful) {
                val croppedImageUri = result.uriContent
//                selectedImage(croppedImageUri.toString(), mCode)
                Log.d("wjkfjkw", "onCreate: result223 = $croppedImageUri")

                /*  //new
                  var resultUri = CropImage.getPickImageResultUriContent(this, intent)
                  if (Build.VERSION.SDK_INT>28) {
                      resultUri = CropImage.getPickImageResultUriFilePath(this, intent).toUri()
                  }*/
                /* val croppedImagePath = croppedImageUri?.let { getRealPathFromURI(it, mActivity!!) }
                 if (croppedImagePath != null) {
                     // Use the path as needed
                     Log.d("CroppedImagePath", croppedImagePath)
                    Log.d("wjkfjkw", "onCreate: result24 = $croppedImagePath")

                 } else {
                     Log.e("CroppedImagePath", "Failed to get file path from URI")
                 }*/
//                val uri = Uri.fromFile(mImageFile)
//                val picturePath = getAbsolutePath(uri)
//                selectedImage(picturePath, mCode)

                /*  val uri = Uri.fromFile(File(result.uriContent?.path))
                  val picturePath = getAbsolutePath(uri)
                  Log.d("wjkfjkw", "onCreate: result24 = $picturePath")
                  selectedImage(picturePath, mCode)*/

                val croppedImageUri2 = result.uriContent // Your cropped image URI
                val savedImagePath = saveImageFromUri(croppedImageUri2!!, mActivity!!)
                Log.d("wjkfjkw", "onCreateeee: $savedImagePath")

                if (savedImagePath != null) {
                    Log.d("SavedImagePath", savedImagePath)

                    compressAndSetImage(Uri.fromFile(File(savedImagePath)))
//                    selectedImage(savedImagePath, mCode)
                } else {
                    Log.e("SavedImagePath", "Failed to save image")
                }


            } else {
                val exception = result.error
                Log.e("ImagePicker", "Crop failed: $exception")
            }
        }

        imageGalleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    cropImageLauncher.launch(
                        CropImageContractOptions(
                            uri,
                            CropImageOptions(guidelines = CropImageView.Guidelines.ON)
                        )
                    )
                }
            }
    }

    @SuppressLint("Recycle")
    fun getRealPathFromURI(uri: Uri, context: Context): String? {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity!!.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.M)
    val externalStorageLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.isNotEmpty()) {
                permissions.entries.forEach {
                    Log.d("permissions", "${it.key} = ${it.value}")
                }

                val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                val camera = permissions[Manifest.permission.CAMERA]
                val images = permissions[Manifest.permission.READ_MEDIA_IMAGES]

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (camera == true && images == true) {
                        Log.e("permissions", "Permission Granted Successfully")
                        imageDialog()

                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }

                } else {
                    if (readStorage == true && writeStorage == true && camera == true) {
                        imageDialog()
                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }
                }
            }
        }

//    private val requestMultiplePermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//
//            if (permissions.isNotEmpty()) {
//                permissions.entries.forEach {
//                    Log.d("permissions", "${it.key} = ${it.value}")
//                }
//
//                val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
//                val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
//                val camera = permissions[Manifest.permission.CAMERA]
//
//                if (readStorage == true && writeStorage == true && camera == true) {
//                    Log.e("permissions", "Permission Granted Successfully")
//                    imageDialog()
//
//                } else {
//                    Log.e("permissions", "Permission not granted")
//                    checkPermissionDenied(permissions.keys.first())
//                }
//            }
//        }

    private val imageCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = Uri.fromFile(mImageFile)
                cropImageLauncher.launch(
                    CropImageContractOptions(
                        uri,
                        CropImageOptions(guidelines = CropImageView.Guidelines.ON)
                    )
                )
            }
        }

    private val videoCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data
                val selectedVideoPath = getPath(contentURI!!)
                selectedImage(selectedVideoPath, mCode)
            }
        }

    /*
        private val imageCameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = Uri.fromFile(mImageFile)
                    val picturePath = getAbsolutePath(uri)
    //                selectedImage(picturePath, mCode)
                    compressAndSetImage(uri)
                }
            }
    */


    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data

                val selectedVideoPath = getPath(contentURI!!)
                /* val a = File(contentURI)*/
                val a = "/storage/emulated/0/Movies/Instagram/VID_53050323_203748_988.mp4"
                selectedImage(selectedVideoPath, mCode)
            }
        }


    private fun compressAndSetImage(result: Uri) {
        val job = Job()
        val uiScope = CoroutineScope(Dispatchers.IO + job)
        val fileUri = getAbsolutePath(result)
        uiScope.launch {
            val compressedImageFile = Compressor.compress(mActivity!!, File(fileUri)) {
                quality(50) // combine with compressor constraint
                format(Bitmap.CompressFormat.JPEG)
            }
            val resultUri = Uri.fromFile(compressedImageFile)
            mActivity!!.runOnUiThread {
                resultUri?.let {
                    //set image here
                    Log.d("elgkjkjeg", "compressAndSetImage: ${it.path}")
                    selectedImage(it.path, mCode)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    open fun getImage(activity: Activity, code: Int, videoDialog: Boolean) {
        //*****videoDialog -> put false for pick the Image.*****
        mActivity = activity
        mCode = code
        mVideoDialog = videoDialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasPermissions(permissionsFor13)) {
                imageDialog()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                checkPermissionDenied(Manifest.permission.CAMERA)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_IMAGES)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO)) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_VIDEO)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO)
            ) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                requestPermission()
            }
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (hasPermissions(permissions)) {
                    Log.e("Permissionsgregregrgrg", "Permissions Granted")
                    imageDialog()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.e("Permissionsgregregrgrg", "read for Permissions")

                    checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.e("Permissionsgregregrgrg", "write for Permissions")

                    checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Log.e("Permissionsgregregrgrg", "write sdvd for Permissions")

                    checkPermissionDenied(Manifest.permission.CAMERA)
                } else {
                    Log.e("Permissionsgregregrgrg", "Request for Permissions")
                    requestPermission()
                }

            } else {
                if (hasPermissions(permissions1)) {
                    Log.e("Permissionsgregregrgrg", "Permissions yhhhhhhhhhGranted")
                    imageDialog()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.e("Permissionsgregregrgrg", "read forrthhhhhhhhhhhh Permissions")

                    checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.e("Permissionsgregregrgrg", "write for rfffffffff Permissions")

                    checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Log.e("Permissionsgregregrgrg", "write sdvd forrtttttttttttt Permissions")

                    checkPermissionDenied(Manifest.permission.CAMERA)
                } else {
                    Log.e("Permissionsgregregrgrg", "Request forrttttttttttt Permissions")
                    requestPermission()
                }
            }
        }
    }


    private fun imageDialog() {
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.camera_gallery_popup)
//        dialog.window!!.attributes.windowAnimations = R.style.Theme_Dialog
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.tvCamera)
        val cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        val gallery = dialog.findViewById<TextView>(R.id.tvGallery)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                captureVideo()
            } else {
                captureImage()
            }

        }

        gallery.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                openGalleryForVideo()
            } else {
                openGalleryForImage()
            }
        }
        dialog.show()
    }


    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)

    }

    private fun captureImage() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        try {
            createImageFile(mActivity!!, imageFileName, ".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            Objects.requireNonNull(mActivity!!), "com.live.humanmesh.fileprovider",
            mImageFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        imageCameraLauncher.launch(intent)
    }


    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        videoGalleryLauncher.launch(
            Intent.createChooser(intent, "Select Video")
        )

    }


    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageGalleryLauncher.launch(intent)
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String, extension: String) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) {
            storageDir.mkdirs()
        }
        mImageFile = File.createTempFile(
            name,
            extension,
            storageDir
        )
    }


    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity!!, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionDenied(permissions: String) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle(R.string.permissionRequired).setMessage(R.string.permissionRequired)
                    .setPositiveButton(
                        R.string.ok
                    ) { dialog, which -> requestPermission() }
                    .setNegativeButton(
                        R.string.cancel
                    ) { dialog, which ->

                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.white
                    )
                )
            }
            dialog.show()
        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle(R.string.permissionRequired).setMessage(R.string.permissionRequired)
                    .setCancelable(
                        false
                    )
                    .setPositiveButton(R.string.openSettings) { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                "com.live.dwello",
                                null
                            )
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.white
                    )
                )
            }
            dialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            Log.d("jdmmhbd", "hhhhhhhhhhhhh")
            requestMultiplePermissions.launch(permissionsFor13)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Log.d("jdmmhbd", "hhhhhhhhhhhhh")
            requestMultiplePermissions.launch(permissions)
        } else {
            Log.d("jdmmhbd", "kkkkkkkkkkkkkkkkkk")
            requestMultiplePermissions.launch(permissions1)
        }
    }

//    private fun requestPermission() {
//        requestMultiplePermissions.launch(permissions)
//    }

    @Throws(IOException::class)
    fun saveImageFromUri(uri: Uri, context: Context): String? {
        // Create a temporary image file
        val fileName = "temp_image_" + System.currentTimeMillis()
        createImageFile(context, fileName, ".jpg") // Adjust the extension as needed

        // Get the output file
        val outputFile = mImageFile

        // Use content resolver to open an input stream and save to the output file
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                // Buffer for reading/writing
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
        }

        // Return the absolute path of the saved file
        return outputFile.absolutePath
    }

    //------------------------Return Uri file to String Path ------------------//
    @SuppressLint("Recycle")
    private fun getAbsolutePath(uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity!!.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    private fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? =
            mActivity!!.contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    abstract fun selectedImage(imagePath: String?, code: Int)
}