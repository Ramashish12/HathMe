package code.activity

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import code.utils.AppSettings
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import com.hathme.android.databinding.ActivityVideoPlayersBinding

class VideoPlayerActivity: Activity() {
    private lateinit var simpleExoPlayer: ExoPlayer
    private lateinit var binding: ActivityVideoPlayersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayersBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun initializePlayer() {
        // val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(AppSettings.getString(AppSettings.KEY_selected_url)))
        val mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)
        simpleExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        simpleExoPlayer.addMediaSource(mediaSource)
        simpleExoPlayer.prepare(mediaSource)
        simpleExoPlayer.playWhenReady = true
        binding.playerView.player = simpleExoPlayer
        binding.playerView.requestFocus()
    }

    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        if (simpleExoPlayer!=null)
        {
            releasePlayer()
        }
        super.onPause()
    }

    public override fun onStop() {
        if (simpleExoPlayer!=null)
        {
            releasePlayer()
        }
        super.onStop()
    }
}
