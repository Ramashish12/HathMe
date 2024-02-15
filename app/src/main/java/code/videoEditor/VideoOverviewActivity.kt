package code.videoEditor

import android.content.Context
import android.content.CursorLoader
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.view.BaseActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.androidnetworking.interfaces.UploadProgressListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import com.hathme.android.BuildConfig
import com.hathme.android.R
import com.hathme.android.databinding.ActivityVideoOverviewBinding
import org.apache.commons.io.FilenameUtils.getPath
import java.io.File


class VideoOverviewActivity : BaseActivity(), View.OnClickListener {
    private lateinit var b: ActivityVideoOverviewBinding
    private var thumb: File? = null
    private var videoFile: File? = null
    private lateinit var video: String
    private lateinit var thumbnail: String
    private lateinit var simpleExoPlayer: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityVideoOverviewBinding.inflate(layoutInflater)
        setContentView(b.root)

        inits()
    }

    private fun inits() {
        b.header.tvHeader.text = getString(R.string.upload)
        b.header.ivBack.setOnClickListener(this)
        b.tvUpload.setOnClickListener(this)
        video = intent.getStringExtra("video")!!
        thumbnail = intent.getStringExtra("thumb")!!
        // videoFile = File(getRealPathFromURI(Uri.parse(video)).toString())

        //AppUtils.showToastSort(mActivity,thumbnail)
    }

    override fun onClick(view: View?) {
        when (view) {
            b.header.ivBack -> {
                onBackPressed()
            }
            b.tvUpload -> {
                if (b.etDesc.text.equals(""))
                {
                    AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),getString(R.string.pleaseEnterDescription),8)
                }
                else if (b.etHashTag.text.equals(""))
                {
                    AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),getString(R.string.pleaseEnterHash),8)
                }
                else {
                    hitUploadVideosApi()
                }
            }
        }
    }

    private fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(
            mActivity, contentUri!!, proj, null, null, null
        )
        val cursor = loader.loadInBackground()
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }


    private fun initializePlayer() {
        // val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(video)))
        val mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)
        simpleExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        simpleExoPlayer.addMediaSource(mediaSource)
        simpleExoPlayer.prepare(mediaSource)
        simpleExoPlayer.playWhenReady = true
        b.videoView.player = simpleExoPlayer
        b.videoView.requestFocus()
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
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }
    private fun hitUploadVideoApi() {
        val videoFile = File(getPath(video))
        val thumbnailFile = File(getPath(thumbnail))
       AndroidNetworking.upload(AppUrls.uploadVideo)
            .addMultipartFile("video", videoFile)

            .addMultipartFile("videoThumbnail", thumbnailFile)
            .addMultipartParameter("description", b.etDesc.text.toString())
            .addMultipartParameter("type", "1")
            .addMultipartParameter("hashtags", b.etHashTag.text.toString())

            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener(UploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
            })
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    // below code will be executed in the executor provided
                    // do anything with response

//                    val  jaresponse = response.getJSONObject("data")
                    AppUtils.showToastSort(mActivity,"uploaded")
                }

                override fun onError(error: ANError) {
                    error.printStackTrace()

                    AppUtils.showToastSort(mActivity,"Error")
                    // handle error
                }
            })
    }


    fun uriToFile(context: Context, uri: Uri): File? {
        var filePath: String? = null

        // For MediaStore Uris
        if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                it.moveToFirst()
                filePath = it.getString(columnIndex)
            }
        }
        // For other Uris, including file:// and android.resource://
        else if (uri.scheme == "file") {
            filePath = uri.path
        }

        // Create a File object from the file path
        filePath?.let {
            return File(it)
        }

        return null
    }


    private fun hitUploadVideosApi() {
        val desc = b.etDesc.text.toString()
        val hashtags = b.etHashTag.text.toString()
        val deviceType = AppConstants.deviceType
        val VERSION_NAME = BuildConfig.VERSION_NAME
        val apiVersion = AppConstants.apiVersion
        val deviceId = AppUtils.getDeviceID(mActivity)
        val languageCode = AppSettings.getString(AppSettings.language)
        val token = AppSettings.getString(AppSettings.token)
        val type = "1"
        AppUtils.showRequestDialog(mActivity)
        AndroidNetworking.upload(AppUrls.uploadVideo)
            .addMultipartFile("video", File(video))
            .addMultipartFile("videoThumbnail", File(thumbnail))
            .addMultipartParameter("description", desc)
            .addMultipartParameter("type", type)
            .addMultipartParameter("hashtags", hashtags)
            .addHeaders("deviceType", deviceType)
            .addHeaders("appVersion", VERSION_NAME)
            .addHeaders("apiVersion", apiVersion)
            .addHeaders("deviceId", deviceId)
            .addHeaders("languageCode", languageCode)
            .addHeaders("loginRegion", "IN")
            .addHeaders("token", token)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded: Long, totalBytes: Long ->

            }
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    AppUtils.hideDialog()
                    AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),getString(R.string.uploadMsg),1)
                }

                override fun onError(anError: ANError) {
                    AppUtils.hideDialog()
                    Toast.makeText(mActivity, anError.errorBody, Toast.LENGTH_SHORT).show()
                }
            })
    }
}