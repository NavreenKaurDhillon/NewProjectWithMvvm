package com.live.humanmesh.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.tapadoo.alerter.Alerter
import com.live.humanmesh.R
import com.live.humanmesh.databinding.CommonBottomSheetBinding
import com.live.humanmesh.databinding.DialogWithOkButtonBinding
import org.jetbrains.annotations.UnknownNullability


//show alert
fun Activity.showErrorAlert(msg: String) {
    Alerter.create(this)
        .setTitle("")
        .setTitleAppearance(R.style.AlertTextAppearanceTitle)
        .setText(msg)
        .setTextAppearance(R.style.AlertTextAppearanceText)
        .setBackgroundColorRes(R.color.red_color)
        .show()
}

fun Fragment.showErrorAlert(msg: String) {
    Alerter.create(requireActivity())
        .setTitle("")
        .setTitleAppearance(R.style.AlertTextAppearanceTitle)
        .setText(msg)
        .setTextAppearance(R.style.AlertTextAppearanceText)
        .setBackgroundColorRes(R.color.red_color)
        .show()
}

fun checkNetworkConnection(ctx: Context): Boolean {
    val connectivityManager =
        ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Define the network callback
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Network is available
        }

        override fun onLost(network: Network) {
            // Network is lost
        }
    }

    // Request network callbacks
    val networkRequest = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

    // Check network availability
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val isConnected = networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

    // Unregister network callback to release resources
    connectivityManager.unregisterNetworkCallback(networkCallback)

    return isConnected
}

fun Fragment.showSuccessAlert(msg: String) {
    Alerter.create(requireActivity())
        .setTitle("")
        .setTitleAppearance(R.style.AlertTextAppearanceTitle)
        .setText(msg)
        .setTextAppearance(R.style.AlertTextAppearanceText)
        .setBackgroundColorRes(R.color.green_color)
        .show()
}

fun Activity.showSuccessAlert(msg: String) {
    Alerter.create(this)
        .setTitle("")
        .setTitleAppearance(R.style.AlertTextAppearanceTitle)
        .setText(msg)
        .setTextAppearance(R.style.AlertTextAppearanceText)
        .setBackgroundColorRes(R.color.green_color)
        .show()
}

//singleClick
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    setOnClickListener(SafeClickListener { v ->
        onSafeClick(v)
    })
}

fun EditText.setupPasswordToggle(imageView: ImageView, visibleIcon: Int, hiddenIcon: Int) {
    var isPasswordVisible = false

    // Set click listener for the show/hide password ImageView
    imageView.setSafeOnClickListener {
        if (isPasswordVisible) {
            // Hide password
            this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(hiddenIcon) // Set closed-eye icon
        } else {
            // Show password
            this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(visibleIcon) // Set open-eye icon
        }

        // Move cursor to the end of the text
        this.setSelection(this.text.length)

        // Toggle visibility flag
        isPasswordVisible = !isPasswordVisible
    }
}
 fun Activity.okDialog(title : String,message:String, onYesClick: () -> Unit )  {
    val dialogBinding = DialogWithOkButtonBinding.inflate(layoutInflater)
    val dialog = Dialog(this)  // Use Dialog instead of AlertDialog
    dialog.setContentView(dialogBinding.root)

    dialog.window?.let { window ->
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    with(dialogBinding) {
        tvHeading.text = title
        tvSubHeading.text = message
        btnYes.setOnClickListener {
            dialog.dismiss()
            onYesClick?.invoke()
        }
    }

    dialog.show()

}


