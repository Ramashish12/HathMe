package com.sendbird.chat.module.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.chat.module.utils.SharedPreferenceUtils
import com.sendbird.chat.module.utils.changeValue

//const val SENDBIRD_APP_ID = "8C036D78-4BAC-4B4B-9226-682153DBA913" // US-1 Demo
const val SENDBIRD_APP_ID = "AA312247-14B1-4979-AD7A-FB5378F1AB8E" // US-1 Demo

open class BaseApplication : Application() {
    protected val initMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { changeValue(false) }
    val initLiveData: LiveData<Boolean>
        get() = initMutableLiveData

    override fun onCreate() {
        super.onCreate()
        sendbirdChatInit()
        SharedPreferenceUtils.init(applicationContext)
    }

    open fun sendbirdChatInit(){
        val initParams = InitParams(SENDBIRD_APP_ID, applicationContext, false)
        initParams.logLevel = LogLevel.ERROR
        SendbirdChat.init(
            initParams,
            object : InitResultHandler {
                override fun onInitFailed(e: SendbirdException) {
                    initMutableLiveData.changeValue(true)
                    // If useLocalCaching is true and init fails, the SDK will turn off the useLocalCaching flag so that you can still proceed with your app
                }

                override fun onMigrationStarted() {
                    // This won't be called if useLocalCaching is set to false.
                }

                override fun onInitSucceed() {
                    initMutableLiveData.changeValue(true)
                }
            })
    }

}
