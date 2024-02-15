package code.groupvideocall.room

import android.os.Bundle
import code.groupvideocall.util.BaseActivity
import com.hathme.android.R
import code.groupvideocall.util.EXTRA_IS_NEWLY_CREATED
import code.groupvideocall.util.EXTRA_ROOM_ID

class VideoRoomActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_video)
    }

    fun getRoomId(): String {
        return intent.getStringExtra(EXTRA_ROOM_ID) ?: throw IllegalStateException()
    }

    fun isNewlyCreated(): Boolean {
        return intent.getBooleanExtra(EXTRA_IS_NEWLY_CREATED, false)
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_room).let {
            it?.childFragmentManager?.fragments?.firstOrNull()
        }

        if (currentFragment is VideoGroupCallFragment) {
            // ignore event
        } else {
            super.onBackPressed()
        }
    }
}