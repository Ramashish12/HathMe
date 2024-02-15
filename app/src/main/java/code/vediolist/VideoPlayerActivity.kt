package code.vediolist

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import code.view.BaseActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.hathme.android.R
import com.hathme.android.databinding.ActivityVideo2Binding


class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideo2Binding
    var playerView: PlayerView? = null
    private var isPortrait = true
    var player: ExoPlayer? = null
    lateinit var videoFile :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ///AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.hide()
        binding= ActivityVideo2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        playerView = findViewById<PlayerView>(R.id.vv)
        videoFile = intent.getStringExtra("url").toString()
        playVideos()
        binding.btn.setOnClickListener {
            if (isPortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                binding.vv.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                binding.rlVideo.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                // else change to Portrait
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                binding.vv.layoutParams.height = 550
                binding.rlVideo.layoutParams.height = 550
            }
            // opposite the value of isPortrait
            isPortrait = !isPortrait
        }

    }

    private fun playVideos() {

        player = SimpleExoPlayer.Builder(this@VideoPlayerActivity).build()
        binding.vv.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(videoFile)
        player!!.setMediaItem(mediaItem)
        // Prepare exoplayer
        player!!.prepare()
        // Start playing media when it is ready
        player!!.playWhenReady = true
    }

    override fun onPause() {
        player!!.pause()
        super.onPause()
    }

}