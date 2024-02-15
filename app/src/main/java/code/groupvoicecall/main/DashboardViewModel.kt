package code.groupvoicecall.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import code.groupvoicecall.util.Resource
import code.groupvoicecall.util.Status
import com.sendbird.calls.*
import com.sendbird.calls.handler.RoomHandler

class DashboardViewModel : ViewModel() {
    private val _createdRoomId: MutableLiveData<Resource<String>> = MutableLiveData()
    val createdRoomId: LiveData<Resource<String>> = _createdRoomId
    private val _fetchedRoomId: MutableLiveData<Resource<String>> = MutableLiveData()
    val fetchedRoomId: LiveData<Resource<String>> = _fetchedRoomId

    fun createAndEnterRoom() {
        if (_createdRoomId.value?.status == Status.LOADING) {
            return
        }

        _createdRoomId.postValue(Resource.loading(null))
        val params = RoomParams(RoomType.LARGE_ROOM_FOR_AUDIO_ONLY)
        SendBirdCall.createRoom(params) { room, e ->
            if (e != null) {

                _createdRoomId.postValue(Resource.error(e.message, e.code, null))
            } else {
                room?.enter(EnterParams().setAudioEnabled(true).setVideoEnabled(false)
                ) { e ->
                    if (e != null) {

                        _createdRoomId.postValue(Resource.error(e.message, e.code, null))
                    } else {
                        _createdRoomId.postValue(Resource.success(room.roomId))
                    }
                }
            }
        }

    }
    fun fetchRoomById(roomId: String) {
        if (roomId.isEmpty()) {
            return
        }

        if (_fetchedRoomId.value?.status == Status.LOADING) {
            return
        }

        _fetchedRoomId.postValue(Resource.loading(null))
        SendBirdCall.fetchRoomById(roomId, object : RoomHandler {
            override fun onResult(room: Room?, e: SendBirdException?) {
                if (e != null) {
                    _fetchedRoomId.postValue(Resource.error(e.message, e.code, null))
                } else {
                    _fetchedRoomId.postValue(Resource.success(room?.roomId))
                    val rooms = SendBirdCall.getCachedRoomById(room?.roomId.toString())

                }
            }


        })


    }



}
