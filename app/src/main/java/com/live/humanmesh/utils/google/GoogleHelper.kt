package com.live.humanmesh.utils.google

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleHelper(fragment: Fragment, private val callback: GoogleHelperCallback) {

    interface GoogleHelperCallback {
        fun onSuccessGoogle(account: GoogleSignInAccount?)
        fun onErrorGoogle(error: String)
    }

    private val mGoogleSignInClient: GoogleSignInClient?
    private val googleSignRequestLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val data: Intent? = result.data
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                }
                Activity.RESULT_CANCELED -> {
                    callback.onErrorGoogle("Google authentication cancelled!")
                }
                else -> {
                    callback.onErrorGoogle("Google authentication failed!")
                }
            }
        }

    companion object {
        val TAG: String = GoogleHelper::class.java.simpleName
    }

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(fragment.requireActivity(), gso)
    }

    fun signIn() {
        signOut()
        googleSignRequestLauncher.launch(mGoogleSignInClient?.signInIntent)
    }

    private fun signOut() {
        mGoogleSignInClient?.signOut()
            ?.addOnCompleteListener {
                Log.d(TAG, "signOutResult: ${it.isSuccessful}")
            }
            ?.addOnFailureListener {
                Log.d(TAG, "signOutResult: ${it.localizedMessage}")
            }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            callback.onSuccessGoogle(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d(TAG, "signInResult:failed code=" + e.statusCode)
            callback.onErrorGoogle(e.localizedMessage ?: "Google authentication failed!")
        }
        signOut()
    }
}