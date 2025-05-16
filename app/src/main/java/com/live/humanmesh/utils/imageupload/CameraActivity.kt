/*
package com.live.humanmesh.utils.imageupload

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.amar.library.BuildConfig.APPLICATION_ID
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.live.humanmesh.R
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

abstract class CameraActivity : Fragment()  {
    var mPhotoFile: File? = null
    var tempPath:String? = null
    var mCompressor: FileCompressor? = null
    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_GALLERY_PHOTO = 2
        const val REQUEST_CROP = 4
    }

    */
/**
     * Alert dialog for capture or select from galley
     *//*

    fun selectImage(context: Context?) {
        mCompressor = FileCompressor(context)
        val imagePickerDialog = Dialog(context!!)

        imagePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        imagePickerDialog.setContentView(R.layout.camera_gallery_popup)

        val tvCancel: TextView = imagePickerDialog.findViewById(R.id.tv_cancel)
        val tvCamera: TextView = imagePickerDialog.findViewById(R.id.tvCamera)
        val tvGallery: TextView = imagePickerDialog.findViewById(R.id.tvGallery)


        imagePickerDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        imagePickerDialog.setCancelable(true)
        imagePickerDialog.setCanceledOnTouchOutside(false)
        imagePickerDialog.window!!.setGravity(Gravity.BOTTOM)

        imagePickerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        tvCancel.setOnClickListener {
            imagePickerDialog.dismiss()

        }


        tvCamera.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestStoragePermission(true)
            }
            else{
                requestStoragePermission11(true)
            }
            imagePickerDialog.dismiss()
        }


        tvGallery.setOnClickListener {

            imagePickerDialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestStoragePermission(false);

            }
            else{
                requestStoragePermission11(false);
            }

        }

        imagePickerDialog.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) != null) { // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "$APPLICATION_ID.provider", photoFile
                )
                mPhotoFile = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when {
                requestCode == REQUEST_TAKE_PHOTO -> {
                    val compressFile = mCompressor?.compressToFile(File(mPhotoFile?.absolutePath.toString()))
                    mCompressor?.setQuality(60)
                    val uri = Uri.fromFile(compressFile)
                    val uCrop = UCrop.of(uri, Uri.fromFile(File(context.cacheDir, UUID.randomUUID().toString()+"CropImage.png")))
                    val option=UCrop.Options()
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(true)
                    uCrop.withOptions(option)
                    uCrop?.start(this)
                }

                requestCode == REQUEST_GALLERY_PHOTO -> {
                    val uCrop = UCrop.of(
                        requireNotNull(data?.data),
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    val option=UCrop.Options()
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(true)
                    uCrop.withOptions(option)
                    uCrop?.start(this)
                }
                requestCode == UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(requireNotNull(data))
                    imagePath(resultUri?.path.toString())
                }
                resultCode == UCrop.RESULT_ERROR -> {
                    val cropError = data?.let { UCrop.getError(it) }
                    cropError?.printStackTrace()
                }
            }
        }

    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestStoragePermission(isCamera: Boolean) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO


        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        }
                        else {
                            dispatchGalleryIntent()
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) { // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                Toast.makeText(
                    applicationContext.applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun requestStoragePermission11(isCamera: Boolean) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        }
                        else {
                            dispatchGalleryIntent()
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) { // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                Toast.makeText(
                    applicationContext.applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun requestStoragePermission1() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        dispatchGalleryIntent()
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) { // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                Toast.makeText(
                    applicationContext.applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    */
/**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings

     *//*

    private fun showSettingsDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            openSettings()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()

    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    */
/**
     * Create file with current timestamp name
     *
     * @return
     * @throws IOException
     *//*

    @Throws(IOException::class)
    private fun createImageFile(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    */
/**
     * Get real file path from URI
     *
     * @param contentUri
     * @return
     *//*

    fun getRealPathFromUri(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver?.query(contentUri!!, proj, null, null, null)
            if (DEBUG && cursor == null) {
                error("Assertion failed")
            }
            val columnIndex: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(requireNotNull(columnIndex))
        } finally {
            cursor?.close()
        }
    }


    @CallSuper
    open fun imagePath(path: String) {

    }
}
*/
