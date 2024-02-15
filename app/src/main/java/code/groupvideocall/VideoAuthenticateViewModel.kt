package code.groupvideocall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import code.groupvideocall.util.Resource
import code.groupvideocall.util.SharedPreferencesManager
import com.sendbird.calls.AuthenticateParams
import com.sendbird.calls.SendBirdCall
import com.sendbird.calls.User

class VideoAuthenticateViewModel : ViewModel() {
    private val _authenticateLiveData: MutableLiveData<Resource<User>> = MutableLiveData()
    val authenticationLiveData: LiveData<Resource<User>> = _authenticateLiveData

    fun authenticate(userId: String, accessToken: String? = null) {
        _authenticateLiveData.postValue(Resource.loading(null))
        if (userId.isEmpty()) {
            _authenticateLiveData.postValue(Resource.error("User ID is empty", null, null))
            return
        }

        val authenticateParams = AuthenticateParams(userId)
        if (accessToken != null) {
            authenticateParams.setAccessToken(accessToken)
        }

        SendBirdCall.authenticate(authenticateParams
        ) { user, e ->
            val resource = if (e == null) {
                SharedPreferencesManager.userId = userId
                SharedPreferencesManager.accessToken = accessToken
                Resource.success(user)
            } else {
                Resource.error(e.message, e.code, null)
            }
            _authenticateLiveData.postValue(resource)
        }
    }
}
