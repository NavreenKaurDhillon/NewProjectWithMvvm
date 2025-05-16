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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.live.humanmesh.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

abstract class ImagePickerUtilityFragment : Fragment() {

    private var mActivity: Activity? = null
    var mVideoDialog: Boolean = false
    private var mCode = 0
    private lateinit var mImageFile: File
    private var type = ""

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsFor13 = arrayOf(
//        Manifest.permission.READ_EXTERNAL_STORAGE,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA)

    private val permissions1 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA)

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
                val readMediaImage = permissions[Manifest.permission.READ_MEDIA_IMAGES]
                val readMediaVideo = permissions[Manifest.permission.READ_MEDIA_VIDEO]
                val readMediaAudio = permissions[Manifest.permission.READ_MEDIA_AUDIO]

//                if (readStorage == true && writeStorage == true && camera == true) {
//                    Log.e("permissions", "Permission Granted Successfully")
//                    imageDialog()
//                }
//                else if (readMediaImage == true && readMediaVideo == true && readMediaAudio == true && camera == true) {
//                    Log.e("permissions", "Permission Granted Successfully")
//                    imageDialog()
//                }
//
//                else {
//                    Log.e("permissions", "Permission not granted")
//                    checkPermissionDenied(permissions.keys.first())
//                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    if (camera == true) {
                        Log.e("permissions", "Permission Granted Successfully")
                        imageDialog()

                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }

                } else {
                    if (readStorage == true && writeStorage == true && camera == true) {
                        Log.e("permissions", "Permission Granted Successfully")
                        imageDialog()

                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }
                }
            }
        }

    private val videoCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data

                val selectedVideoPath = getPath(contentURI!!)
                selectedImage(selectedVideoPath, mCode, type)
            }
        }

    private val imageCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = Uri.fromFile(mImageFile)
                val picturePath = getAbsolutePath(uri)
                selectedImage(picturePath, mCode, type)
            }
        }

    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data

                val selectedVideoPath = getPath(contentURI!!)
                /* val a = File(contentURI)*/
                val a = "/storage/emulated/0/Movies/Instagram/VID_53050323_203748_988.mp4"
                selectedImage(selectedVideoPath, mCode, type)
            }
        }

    private val imageGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                val picturePath = getAbsolutePath(uri!!)
                selectedImage(picturePath, mCode, type)
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getImage() {

        //*****videoDialog -> put false for pick the Image.*****
        //*****videoDialog -> put true for pick the Video.*****

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasPermissions(permissionsFor13)) {
                imageDialog()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                checkPermissionDenied(Manifest.permission.CAMERA)
            }
            else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) ){
                checkPermissionDenied(Manifest.permission.READ_MEDIA_IMAGES)//00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000103
            }
            else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO) ){
                checkPermissionDenied(Manifest.permission.READ_MEDIA_VIDEO)
//            }  else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ){
//                checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
//            }  else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
//                checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO)
            ) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_AUDIO)
            }else{
                requestPermission()
            }
        }else{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
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

            }else{
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
        mActivity=requireActivity()
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.camera_gallery_popup)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.tvCamera)
        val gallery = dialog.findViewById<TextView>(R.id.tvGallery)
//        val video = dialog.findViewById<TextView>(R.id.tv)
//        val pdf = dialog.findViewById<TextView>(R.id.tvPdf)
        val cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            captureImage()
        }

        gallery.setOnClickListener {
            dialog.dismiss()
            openGalleryForImage()
        }

//        video.setOnClickListener {
//            dialog.dismiss()
//            if (mVideoDialog) {
//                openGalleryForVideo()
//            } else {
//                openGalleryForImage()
//            }
//        }

        dialog.show()
    }

    open fun openPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pdfIntentLauncher.launch(intent)

//        startActivityForResult(Intent.createChooser(intent, "Select a File"), PDF_REQUEST_CODE)
    }

    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)
    }

    private fun captureImage() {
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            Objects.requireNonNull(requireActivity()),
            "com.live.humanmesh.fileprovider", photoFile!!)
        mImageFile= photoFile
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
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageGalleryLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(requireActivity()!!, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionDenied(permissions: String) {
        mActivity = requireActivity()
        if (shouldShowRequestPermissionRationale(permissions)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle("Permissions Required").setMessage(R.string.permissionRequired)
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
                        mActivity!!, R.color.red_color
                    )
                )
            }
            dialog.show()
        }
        else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle("Permissions Required").setMessage(R.string.permissionRequired)
                    .setCancelable(
                        false
                    )
                    .setPositiveButton(R.string.openSettings) { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                "com.live.humanmesh",
                                null
                            )
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.red_color
                    )
                )
            }
            dialog.show()
        }
    }




    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            Log.d("jdmmhbd","hhhhhhhhhhhhh")
            requestMultiplePermissions.launch(permissionsFor13)
        }else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            Log.d("jdmmhbd","hhhhhhhhhhhhh")
            requestMultiplePermissions.launch(permissions)
        }else{
            Log.d("jdmmhbd","kkkkkkkkkkkkkkkkkk")
            requestMultiplePermissions.launch(permissions1)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun requestPermission() {
//        requestMultiplePermissions.launch(permissions)
//    }

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
            // HERE YOU WILL GET A NULL POINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val columnIndex = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    abstract fun selectedImage(imagePath: String?, code: Int, type: String)

    private val pdfIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                Log.e("data", result.toString())
                val uri = result.data?.data


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    try {
//                        grantUriPermission(
//                            com.live.v2k,
//                            uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//                        )
//                    } catch (e: IllegalArgumentException) {
//                        // on Kitkat api only 0x3 is allowed (FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION)
//                        grantUriPermission(
//                            com.live.v2k,
//                            uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        )
//                    } catch (e: SecurityException) {
//                        Log.e("", e.toString())
//                    }
//                    try {
//                        var takeFlags = intent.flags
//                        takeFlags =
//                            takeFlags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                        contentResolver
//                            .takePersistableUriPermission(uri!!, takeFlags)
//                    } catch (e: SecurityException) {
//                        Log.e("", e.toString())
//                    }
//                }

                val file = DocumentUtils.getFile(context, uri!!)//use pdf as file
                Log.e("onActivityResult", file.toString() + " " + uri)
                selectedImage(file.toString(), mCode, type)
            }

        }


    // for PDF File convert PDF to File
    object DocumentUtils {
        fun getFile(mContext: Context?, documentUri: Uri): File {
            val inputStream = mContext?.contentResolver?.openInputStream(documentUri)
            var file = File("")
            inputStream.use { input ->
                file =
                    File(mContext?.cacheDir, System.currentTimeMillis().toString() + ".pdf")
                FileOutputStream(file).use { output ->
                    val buffer =
                        ByteArray(4 * 1024) // or other buffer size
                    var read: Int = -1
                    while (input?.read(buffer).also {
                            if (it != null) {
                                read = it
                            }
                        } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            return file
        }
    }

}