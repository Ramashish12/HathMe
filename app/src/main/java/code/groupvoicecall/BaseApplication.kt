package code.groupvoicecall

import android.app.Application
import code.groupvoicecall.util.SharedPreferencesManager
import com.sendbird.calls.SendBirdCall

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SendBirdCall.setLoggerLevel(SendBirdCall.LOGGER_INFO)
        SharedPreferencesManager.init(applicationContext)
    }
}
