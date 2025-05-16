package com.live.humanmesh.database

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.live.humanmesh.base.BaseApplication
import com.live.humanmesh.utils.Constants.SP_TUTORIAL_FILE
import androidx.core.content.edit
import com.live.humanmesh.utils.Constants.SP_FILE


@SuppressLint("StaticFieldLeak")
object AppSharedPreferences {
    val context = BaseApplication.appContext
    private const val KEY_LIST = "list"
    private val sharedPreferences : SharedPreferences = context.getSharedPreferences(SP_FILE , Context.MODE_PRIVATE)
    private val sharedTutorialPreferences : SharedPreferences = context.getSharedPreferences(SP_TUTORIAL_FILE , Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val editorT: SharedPreferences.Editor = sharedTutorialPreferences.edit()

    fun saveIntoDatabase(key: String, value: Any?){
        val preference =  BaseApplication.appContext.getSharedPreferences(SP_FILE, 0)
        val editor = preference.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Long -> editor.putLong(key, value)
            is Int -> editor.putInt(key, value)
        }
        editor.apply()
    }


    fun saveTutorialIntoDatabase(key: String, value: Any){
        val preference =  BaseApplication.appContext.getSharedPreferences(SP_TUTORIAL_FILE, 0)
        preference.edit() {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Int -> putInt(key, value)
            }
            editorT.apply()
        }
    }

    fun getFromDatabase(key: String, defaultValue : String): String {
        val value = sharedPreferences.getString(key, defaultValue)
        return value.toString()
    }


    fun getFromDatabase(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    fun getTutorialFromDatabase(key: String, defaultValue: Boolean): Boolean {
        return sharedTutorialPreferences.getBoolean(key, defaultValue)
    }
    fun getTutorialFromDatabase(key: String, defaultValue: String): String? {
        return sharedTutorialPreferences.getString(key, defaultValue)
    }
    fun getFromDatabase(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }


    fun clearDatabase(){
        editor.clear().commit()

    }
    fun clearTutorialDatabase(){
        editorT.clear().commit()

    }
}