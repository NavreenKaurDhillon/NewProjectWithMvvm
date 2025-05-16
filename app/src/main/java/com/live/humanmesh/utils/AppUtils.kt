package com.live.humanmesh.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.live.humanmesh.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object AppUtils {
    fun View.visible() {
        visibility = VISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    /*fun View.invisibe() {
        visibility = View.INVISIBLE
    }*/
    fun Activity.transparentStatusBarStyle(){
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

    }
    fun isValidEmail(email: String): Boolean {
        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        } else {
            return false
        }
    }


    fun showMaterialDatePicker(supportFragmentManager: FragmentManager, textView: TextView?): String {
        var selectedDate = ""

        // Calculate maximum selectable date: today - 18 years
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val maxSelectableDate = calendar.timeInMillis
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(maxSelectableDate) // Set today's date as default selected
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(maxSelectableDate) // Prevent selection after 18-year cutoff
                    .build()
            )
            .build()

        datePicker.show(supportFragmentManager, "MaterialDatePicker")
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = formatDate(selection)
            textView?.text = selectedDate
        }

        return selectedDate
    }

    fun distanceBetweenInMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }


    fun showCurrentDatePicker(supportFragmentManager: FragmentManager, textView: TextView?): String {
        var selectedDate = ""

       /* // Calculate maximum selectable date: today - 18 years
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH))
        val todaySelected = calendar.timeInMillis
*/
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .build()
            )
            .build()

        datePicker.show(supportFragmentManager, "MaterialDatePicker")

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = formatDate(selection)
            textView?.text = selectedDate
        }

        return selectedDate
    }

    fun isValidDateRange(startDateStr: String, endDateStr: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return try {
            val startDate = LocalDate.parse(startDateStr, formatter)
            val endDate = LocalDate.parse(endDateStr, formatter)
            val today = LocalDate.now()

            // endDate must be same or after startDate, and not greater than today
            (endDate.isEqual(startDate) || endDate.isAfter(startDate)) && !endDate.isAfter(today) && !startDate.isAfter(today)
        } catch (e: Exception) {
            false
        }
    }

    fun convertToAmPm(timeStr: String): String {
        val inputFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
        return LocalTime.parse(timeStr, inputFormat).format(outputFormat)
    }
    fun isValidTimeRange(startTimeStr: String, endTimeStr: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        return try {
            val startTime = LocalTime.parse(startTimeStr, formatter)
            val endTime = LocalTime.parse(endTimeStr, formatter)

            endTime.isAfter(startTime)
        } catch (e: Exception) {
            false
        }
    }
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }



    fun showTimePickerDialog(context: Context, onTimeSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val hoursPicker = dialogView.findViewById<NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<NumberPicker>(R.id.minutesPicker)
        val amPmPicker = dialogView.findViewById<NumberPicker>(R.id.secondsPicker)

        // Set ranges for 12-hour format
        hoursPicker.minValue = 1
        hoursPicker.maxValue = 12

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59

       /* secondsPicker.minValue = 0
        secondsPicker.maxValue = 59*/

        amPmPicker.minValue = 0
        amPmPicker.maxValue = 1
        amPmPicker.displayedValues = arrayOf("AM", "PM")

        val builder = AlertDialog.Builder(context)
            .setTitle("Pick Time")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val hour = hoursPicker.value
                val minute = minutesPicker.value
//                val second = secondsPicker.value
                val amPm = if (amPmPicker.value == 0) "AM" else "PM"

                val selectedTime = String.format("%02d:%02d %s", hour, minute,  amPm)
                onTimeSelected(selectedTime)
            }
            .setNegativeButton("Cancel", null)

        builder.create().show()
    }


    fun isEmailValid(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true

        } else {
            false
        }
    }

    fun prepareMultiPart(partName: String, image: Any?): MultipartBody.Part {

        /*    var   imageFileBody = MultipartBody.Part.createFormData(partName, "image_"+".jpg", requestBody);
              imageArrayBody.add(imageFileBody);*/
        var requestFile: RequestBody? = null
        if (image is File) {

            requestFile = image
                .asRequestBody("*/*".toMediaTypeOrNull())
        } else if (image is ByteArray) {
            requestFile = image
                .toRequestBody(
                    "*/*".toMediaTypeOrNull(),
                    0, image.size
                )
        }
        if (image is String) {
            val attachmentEmpty = "".toRequestBody("text/plain".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, "", attachmentEmpty)
        } else
            return MultipartBody.Part.createFormData(partName, (image as File).name, requestFile!!)
    }
    fun createRequestBody(param: String): RequestBody {
        return param.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    //display date
    fun String.toFormattedDate(): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = parser.parse(this)
            if (date != null) formatter.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    //display time
    fun String.toFormattedTime(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Handle Zulu time

            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault() // Local time zone

            val date = inputFormat.parse(this)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getChatTimeAgo(time: String,dateTV: TextView): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val messageDate: Date = try {
            sdf.parse(time) ?: return ""
        } catch (e: Exception) {
            return ""
        }

        val now = Date()
        val diffMillis = now.time - messageDate.time
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)

        // Set label in TextView based on the time difference
        when {
            diffMillis < TimeUnit.HOURS.toMillis(24) -> {
                dateTV.text = "Today"
            }
            diffMillis < TimeUnit.HOURS.toMillis(48) -> {
                dateTV.text = "Yesterday"
            }
            else -> {
                val dateFormat = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault()) // 18 April, 2025
                dateTV.text = dateFormat.format(messageDate)
            }
        }

        // Return appropriate time text
        return when {
            diffMillis <= TimeUnit.HOURS.toMillis(1) -> {
                if (diffMinutes < 1) "Just now" else "$diffMinutes minutes ago"
            }
            else -> {
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 04:20 PM
                timeFormat.format(messageDate)
            }
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timeInMillis: Long): Boolean {
        val messageCal = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        val yesterdayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(messageCal.timeInMillis, yesterdayCal.timeInMillis)
    }

    fun isToday(isoTime: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoTime) ?: return false

        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal2.time = date

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(isoTime: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoTime) ?: return false

        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal2.time = date

        cal1.add(Calendar.DAY_OF_YEAR, -1)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }


    fun maskString(name: String): String {
        if (name.isEmpty()) return ""
        if (name.length == 1) return name.first().toString()
        if (name.length == 2) return name.first().toString() + name.last()

        val first = name.first()
        val last = name.last()
        val maskedMiddle = "*".repeat(name.length - 2)

        return "$first$maskedMiddle$last"
    }

    /*  private fun createThumbnail(imagePath: String?) {
      val thumb = ThumbnailUtils.createVideoThumbnail(
          imagePath.toString(), MediaStore.Images.Thumbnails.MINI_KIND)
      val imageThumb = saveToFile(thumb)
      thumbnailPart = prepareMultiPart("thumbnail", File(imageThumb))
  }

  private fun saveToFile(bmp: Bitmap?): String {
      val bytes = ByteArrayOutputStream()
      bmp?.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
      val f = File(requireContext().cacheDir, System.currentTimeMillis().toString() + ".jpg")
      f.createNewFile()
      val fo = FileOutputStream(f)
      fo.write(bytes.toByteArray())
      fo.close()
      return f.absolutePath
  }
*/

}