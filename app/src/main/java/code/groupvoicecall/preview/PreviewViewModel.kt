package code.groupvoicecall.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import code.groupvoicecall.util.Resource
import com.sendbird.calls.EnterParams
import com.sendbird.calls.SendBirdCall
import com.sendbird.calls.SendBirdException
import com.sendbird.calls.handler.CompletionHandler

class PreviewViewModel : ViewModel() {
    private val _enterResult: MutableLiveData<Resource<Unit>> = MutableLiveData()
    val enterResult: LiveData<Resource<Unit>> = _enterResult

    fun enter(roomId: String, isAudioEnabled: Boolean, isVideoEnabled: Boolean) {
        val room = SendBirdCall.getCachedRoomById(roomId) ?: return
        _enterResult.postValue(Resource.loading(null))
        val enterParams = EnterParams()
            .setAudioEnabled(isAudioEnabled)
            .setVideoEnabled(isVideoEnabled)

        room.enter(enterParams, object : CompletionHandler {
            override fun onResult(e: SendBirdException?) {
                if (e == null) {
                    _enterResult.postValue(Resource.success(null))
                } else {
                    _enterResult.postValue(Resource.error(e.message, e.code, null))
                }
            }
        })
    }
}
