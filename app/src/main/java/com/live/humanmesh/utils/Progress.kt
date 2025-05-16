package com.live.humanmesh.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.widget.ProgressBar
import com.live.humanmesh.R


object Progress {
    private var dialog: Dialog? = null
    @SuppressLint("MissingInflatedId")
    fun show(context: Context?) {
        if (isShowing()) {
            hide()
            dialog = null
        }
        dialog = Dialog(context!!)
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window!!.setGravity(Gravity.CENTER)
            setContentView(R.layout.progress_horizontal)
            val progressBar= findViewById<ProgressBar>(R.id.progress)
//            val doubleBounce: Sprite = PulseRing()
//          progressBar.indeterminateDrawable = doubleBounce
            show()
        }
    }


    fun hide() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    private fun isShowing(): Boolean {
        return dialog != null && dialog!!.isShowing
    }
}