package com.live.humanmesh.utils

object Constants {

    //live url
    const val BASE_URL = "http://122.176.141.23:1613/api/"
    const val IMAGES_BASE_URL = "http://122.176.141.23:1613/images/"
    const val SOCKET_URL = "http://122.176.141.23:1613/"
    const val DEEP_LINK_URL = "http://122.176.141.23:1613/shareApp"

    //local url
/*
    const val BASE_URL = "192.168.1.36:3002/"
*/
    //shared preference keys
    const val SP_FILE = "humanmesh"
    const val SP_TUTORIAL_FILE = "humanmeshTutorial"
    const val DEVICE_TOKEN = "device_token"
    const val AUTH_TOKEN = "auth_token"
    const val REMEMBER_KEY = "remember_key"
    const val EMAIL_KEY = "email"
    const val SAVED_EMAIL = "saved_email"
    const val PASSWORD_KEY = "password"
    const val SAVED_PASSWORD = "saved_password"
    const val NAME = "name"
    const val NICK_NAME = "nick_name"
    const val COUNTRY_CODE = "country_code"
    const val USER_ID = "user_id"
    const val USER_LOCATION = "user_loc"
    const val USER_PRIVATE_ACCOUNT = "user_account_privacy"
    const val USER_SHOW_NICK_NAME = "user_show_nick_name"
    const val USER_PHOTO_VISIBLE = "user_photo_visiblity"
    const val USER_SHOW_EVENT_DETAILS = "user_show_event_details"
    const val USER_MAIL = "user_email"
    const val USER_PHONE = "user_phone"
    const val USER_COUNTRY_CODE = "user_country_code"
    const val USER_GENDER = "user_gender"
    const val IS_LOGIN = "is_login"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val IS_TUTORIAL_SHOWN = "is_shown"
    var isOnChat = false


    //apis endpoints
    const val LOG_IN = "login"
    const val CHECK_SOCIAL_LOG_IN = "checkSocialLogin"
    const val VERIFY_OTP = "verifyotp"
    const val RESEND_OTP = "resendotp"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "passwordforget"
    const val LOGOUT = "logout"
    const val GET_PROFILE = "profile"
    const val EDIT_PROFILE = "editprofile"
    const val DELETE_ACCOUNT = "account_deleted"
    const val ACCOUNT_PRIVACY = "account_privacy"
    const val TERMS_CONDITIONS = "termsAndConditions"
    const val PRIVACY_POLICY = "privacypolicy"
    const val FAQS = "faqsListing"
    const val ABOUT_US = "aboutus"
    const val CONTACT_US = "contactus"
    const val NOTIFICATIONS_LIST = "notificationlist"
    const val MY_EVENTS = "myEvents"
    const val ADD_EVENT = "addEvent"
    const val HOME = "home"
    const val EDIT_EVENT = "editEvent/"
    const val DELETE_EVENT = "deleteEvents/"
    const val REQUESTS_LIST = "requestsList"
    const val BANNERS_LIST = "bannerList"
    const val UPDATE_LAT_LNG = "updateUserLatLong"
    const val REQUEST_DETAILS = "requestDetails"
    const val ACCEPT_REJECT_REQUEST = "requestStatus"
    const val MATCHED_USERS = "matchedUsers"
    const val OTHER_USER_PROFILE = "user_profile"
    const val IMAGE_UPLOAD = "socketImageUploaded"
    const val NOTIFICATION_LIST = "notificationlist"
    const val REWARDS_LIST = "rewardsList"
    const val ADD_SUBSCRIPTION = "AddSubscription"

}