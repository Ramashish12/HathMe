package code.videoEditor

import android.app.Activity
import android.app.ProgressDialog
import android.content.CursorLoader
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.view.BaseActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.BuildConfig
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hathme.android.R
import com.hathme.android.databinding.ActivityVideoEditorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Math.abs


@Suppress("DEPRECATION")
class VideoEditorActivity : BaseActivity(), View.OnClickListener {
    private var inputVideoUri: String? = null
    lateinit var binding: ActivityVideoEditorBinding
    val handler = Handler(Looper.getMainLooper())
    private var thumb: File? = null
    private var videoFile: File? = null
    private val GALLERY = 1
    private val CAMERA = 2
    var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var etDesc: EditText
    lateinit var etHashTag: EditText
    private lateinit var pd: ProgressDialog
    private var fileUri: Uri? = null

    //create an intent launcher to save the video file in the device storage
    private val saveVideoLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) {
            it?.let {
                val out = contentResolver.openOutputStream(it)
                val ip: InputStream = FileInputStream(inputVideoUri)

                //com.google.common.io.ByteStreams, also provides a direct method to copy
                // all bytes from the input stream to the output stream. Does not close or
                // flush either stream.
                // copy(ip,out!!)

                out?.let {
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (ip.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                    ip.close()
                    // write the output file (You have now copied the file)
                    out.flush()
                    out.close()
                }
            }
        }

    private val takeVideoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Video capture was successful
            val videoUri = result.data?.data
            // Handle the captured video URI as needed
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.requestFocus()
            binding.videoView.start()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inits()
    }

    private fun inits() {

        binding.btnSelect.setOnClickListener(this)
        binding.slow.setOnClickListener(this)
        binding.fast.setOnClickListener(this)
        binding.reverse.setOnClickListener(this)
        binding.tvUploadVideo.setOnClickListener(this)
        binding.header.ivBack.setOnClickListener(this)
        binding.header.tvUpload.setOnClickListener(this)
        binding.videoView.setOnPreparedListener { mp ->
            //get the duration of the video
            val duration = mp.duration / 1000
            //initially set the left TextView to "00:00:00"
            binding.textleft.text = "00:00:00"
            //initially set the right Text-View to the video length
            //the getTime() method returns a formatted string in hh:mm:ss
            binding.textright.text = getTime(mp.duration / 1000)
            //this will run he video in loop i.e. the video won't stop
            //when it reaches its duration
            mp.isLooping = true

            //set up the initial values of binding.rangeSeekBar
            binding.rangeSeekBar.setRangeValues(0, duration)
            binding.rangeSeekBar.selectedMinValue = 0
            binding.rangeSeekBar.selectedMaxValue = duration
            binding.rangeSeekBar.isEnabled = true
            binding.rangeSeekBar.setOnRangeSeekBarChangeListener { bar, minValue, maxValue ->
                //we seek through the video when the user drags and adjusts the seekbar
                binding.videoView.seekTo(minValue as Int * 1000)
                //changing the left and right TextView according to the minValue and maxValue
                binding.textleft.text = getTime(bar.selectedMinValue.toInt())
                binding.textright.text = getTime(bar.selectedMaxValue.toInt())
            }

            //this method changes the right TextView every 1 second as the video is being played
            //It works same as a time counter we see in any Video Player

            handler.postDelayed(object : Runnable {
                override fun run() {

                    val time: Int = abs(duration - binding.videoView.currentPosition) / 1000
                    binding.textleft.text = getTime(time)

                    //wrapping the video, i.e. once the video reaches its length,
                    // again starts from the current position of left seekbar point
                    if (binding.videoView.currentPosition >= binding.rangeSeekBar.selectedMaxValue.toInt() * 1000) {
                        binding.videoView.seekTo(binding.rangeSeekBar.selectedMinValue.toInt() * 1000)
                    }
                    handler.postDelayed(this, 1000)
                }
            }, 0)

        }
        pd = ProgressDialog(mActivity)
        setLayout()
    }
   private fun setLayout()
   {
       if (AppSettings.getString(AppSettings.isFrom).equals("Long"))
       {
         binding.rlSeecBar.visibility = View.GONE
         binding.rlEffect.visibility = View.GONE
         binding.header.tvUpload.visibility = View.GONE
         binding.tvUploadVideo.visibility = View.VISIBLE
         binding.header.tvHeader.text = getString(R.string.uploadVideo)
       }
       else if (AppSettings.getString(AppSettings.isFrom).equals("Short"))
       {
           binding.header.tvHeader.text = getString(R.string.editAndUploadVideo)
           binding.rlSeecBar.visibility = View.VISIBLE
           binding.rlEffect.visibility = View.VISIBLE
           binding.tvUploadVideo.visibility = View.GONE
           binding.header.tvUpload.visibility = View.VISIBLE
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

    private fun writeToFile(bitmap: Bitmap) {
        thumb = File(mActivity.cacheDir, "thumbnails.png")
        if (!thumb!!.exists()) {
            thumb!!.createNewFile()
        }

        val os: OutputStream
        try {
            os = FileOutputStream(thumb)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)

            os.flush()
            os.close()
        } catch (e: java.lang.Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
//        binding.ivThumbnail.visibility = View.VISIBLE
//        binding.videoView.visibility = View.GONE
//        binding.ivThumbnail.setImageURI(Uri.parse(thumb.toString()))
//        AppUtils.showToastSort(mActivity,thumb.toString())
    }

    /**
     * Method for creating fast motion video
     */
    private fun fastForward(startMs: Int, endMs: Int) {
        /* startMs is the starting time, from where we have to apply the effect.
  	         endMs is the ending time, till where we have to apply effect.
   	         For example, we have a video of 5min and we only want to fast forward a part of video
  	         say, from 1:00 min to 2:00min, then our startMs will be 1000ms and endMs will be 2000ms.
		 */


        //the "exe" string contains the command to process video.The details of command are discussed later in this post.
        // "video_url" is the url of video which you want to edit. You can get this url from intent by selecting any video from gallery.
        val folder = cacheDir
        val file = File(folder, System.currentTimeMillis().toString() + ".mp4")
        val exe =
            ("-y -i $inputVideoUri -filter_complex [0:v]trim=0:${startMs / 1000},setpts=PTS-STARTPTS[v1];[0:v]trim=${startMs / 1000}:${endMs / 1000},setpts=0.5*(PTS-STARTPTS)[v2];[0:v]trim=${endMs / 1000},setpts=PTS-STARTPTS[v3];[0:a]atrim=0:${startMs / 1000},asetpts=PTS-STARTPTS[a1];[0:a]atrim=${startMs / 1000}:${endMs / 1000},asetpts=PTS-STARTPTS,atempo=2[a2];[0:a]atrim=${endMs / 1000},asetpts=PTS-STARTPTS[a3];[v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1 -b:v 2097k -vcodec mpeg4 -crf 0 -preset superfast ${file.absolutePath}")
        executeFfmpegCommand(exe, file.absolutePath)
    }

    /**
     * Method for creating slow motion video for specific part of the video
     * The below code is same as above only the command in string "exe" is changed.
     */
    private fun slowMotion(startMs: Int, endMs: Int) {
        val folder = cacheDir
        val file = File(folder, System.currentTimeMillis().toString() + ".mp4")
        val exe =
            ("-y -i $inputVideoUri -filter_complex [0:v]trim=0:${startMs / 1000},setpts=PTS-STARTPTS[v1];[0:v]trim=${startMs / 1000}:${endMs / 1000},setpts=2*(PTS-STARTPTS)[v2];[0:v]trim=${endMs / 1000},setpts=PTS-STARTPTS[v3];[0:a]atrim=0:${startMs / 1000},asetpts=PTS-STARTPTS[a1];[0:a]atrim=${startMs / 1000}:${endMs / 1000},asetpts=PTS-STARTPTS,atempo=0.5[a2];[0:a]atrim=${endMs / 1000},asetpts=PTS-STARTPTS[a3];[v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1 -b:v 2097k -vcodec mpeg4 -crf 0 -preset superfast ${file.absolutePath}")
        executeFfmpegCommand(exe, file.absolutePath)
    }

    /**
     * Method for reversing the video
     * The below code is same as above only the command is changed.
     */
    private fun reverse(startMs: Int, endMs: Int) {
        val folder = cacheDir
        val file = File(folder, System.currentTimeMillis().toString() + ".mp4")
        val exe =
            "-y -i $inputVideoUri -filter_complex [0:v]trim=0:${endMs / 1000},setpts=PTS-STARTPTS[v1];[0:v]trim=${startMs / 1000}:${endMs / 1000},reverse,setpts=PTS-STARTPTS[v2];[0:v]trim=${startMs / 1000},setpts=PTS-STARTPTS[v3];[v1][v2][v3]concat=n=3:v=1 -b:v 2097k -vcodec mpeg4 -crf 0 -preset superfast ${file.absolutePath}"
        executeFfmpegCommand(exe, file.absolutePath)
    }

    //This method returns the seconds in hh:mm:ss time format
    private fun getTime(seconds: Int): String {
        val hr = seconds / 3600
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d", hr) + ":" + String.format(
            "%02d",
            mn
        ) + ":" + String.format("%02d", sec)
    }

    private fun executeFfmpegCommand(exe: String, filePath: String) {
        //creating the progress dialog
        val progressDialog = ProgressDialog(this@VideoEditorActivity)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    //after successful execution of ffmpeg command,
                    //again set up the video Uri in VideoView
                    binding.videoView.setVideoPath(filePath)
                    //change the video_url to filePath, so that we could do more manipulations in the
                    //resultant video. By this we can apply as many effects as we want in a single video.
                    //Actually there are multiple videos being formed in storage but while using app it
                    //feels like we are doing manipulations in only one video
                    inputVideoUri = filePath
                    // thumbnail = createVideoThumbnail(inputVideoUri!!, Thumbnails.FULL_SCREEN_KIND)
                    //play the result video in VideoView
                    binding.videoView.start()
                    progressDialog.dismiss()
                    Toast.makeText(this@VideoEditorActivity, "Filter Applied", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    progressDialog.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(
                        this@VideoEditorActivity,
                        "Something Went Wrong!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog.setMessage("Please wait...")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            deleteTempFiles(cacheDir)
        }
    }


    private fun deleteTempFiles(file: File): Boolean {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isDirectory) {
                    deleteTempFiles(it)
                } else {
                    it.delete()
                }
            }
        }
        return file.delete()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.reverse -> {
                if (inputVideoUri != null) {
                    reverse(
                        binding.rangeSeekBar.selectedMinValue.toInt() * 1500,
                        binding.rangeSeekBar.selectedMaxValue.toInt() * 1500
                    )
                } else Toast.makeText(
                    this@VideoEditorActivity,
                    "Please upload video",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            binding.slow -> {
                if (inputVideoUri != null) {
                    slowMotion(
                        binding.rangeSeekBar.selectedMinValue.toInt() * 1500,
                        binding.rangeSeekBar.selectedMaxValue.toInt() * 1500
                    )
                } else Toast.makeText(
                    this@VideoEditorActivity,
                    "Please upload video",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            binding.fast -> {
                if (inputVideoUri != null) {
                    fastForward(
                        binding.rangeSeekBar.selectedMinValue.toInt() * 3500,
                        binding.rangeSeekBar.selectedMaxValue.toInt() * 3500
                    )
                } else Toast.makeText(
                    this@VideoEditorActivity,
                    "Please upload video",
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            binding.header.ivBack -> {
                onBackPressed()
            }

            binding.header.tvUpload -> {
                if (videoFile == null) {
                    AppUtils.showMessageDialog(
                        mActivity,
                        getString(R.string.app_name),
                        getString(R.string.pleaseSelectVideo),
                        8
                    )
                } else {

                    showDialog(mActivity)
                }
            }

            binding.tvUploadVideo -> {
                if (videoFile == null) {
                    AppUtils.showMessageDialog(
                        mActivity,
                        getString(R.string.app_name),
                        getString(R.string.pleaseSelectVideo),
                        8
                    )
                } else {
                    showDialog(mActivity)
                }
            }
            binding.btnSelect -> {
                handler.removeCallbacksAndMessages(null)
                showPictureDialog()

//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//                startActivityForResult(intent, GALLERY)
            }

        }
    }
    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(mActivity)
        pictureDialog.setTitle(getString(R.string.selectVideo))
        val pictureDialogItems = arrayOf(getString(R.string.selectVideoFromGallery), getString(R.string.recordVideoFromCamera))
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> chooseVideoFromGallery()
                1 -> recordVideoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun chooseVideoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "video/*"
        startActivityForResult(galleryIntent, GALLERY)
    }


    private fun recordVideoFromCamera()
   {
       val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
       startActivityForResult(takeVideoIntent, CAMERA)
   }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY -> {
                    val videoUri: Uri? = data?.data
                    videoFile = File(getRealPathFromURI(videoUri).toString())
                    if (videoFile!!.name.endsWith("jpg") || videoFile!!.name!!.endsWith("png") || videoFile!!.name!!.endsWith(
                            "jpeg"
                        ) || videoFile!!.name!!.endsWith("webp")
                    ) {
                        AppUtils.showMessageDialog(
                            mActivity,
                            getString(R.string.fileTypeError),
                            getString(R.string.youCanSelect),
                            9
                        )
                    } else {
                        binding.videoView.setVideoURI(videoUri)
                        binding.videoView.start()
                        inputVideoUri =
                            FFmpegKitConfig.getSafParameterForRead(mActivity, videoUri)
                        writeToFile(
                            ThumbnailUtils.createVideoThumbnail(
                                videoFile!!.absolutePath,
                                MediaStore.Video.Thumbnails.MINI_KIND
                            )!!
                        )
                    }

                }
                CAMERA ->
                {
                    val videoUri: Uri? = data?.data
                    videoFile = File(getRealPathFromURI(videoUri).toString())
                    binding.videoView.setVideoURI(videoUri)
                    binding.videoView.start()
                    inputVideoUri =
                        FFmpegKitConfig.getSafParameterForRead(mActivity, videoUri)
                    writeToFile(
                        ThumbnailUtils.createVideoThumbnail(
                            videoFile!!.absolutePath,
                            MediaStore.Video.Thumbnails.MINI_KIND
                        )!!
                    )
                }

            }
        }

    }

    private fun showDialog(mActivity: Activity) {
        bottomSheetDialog = BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.dialog_upload_video)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.setCanceledOnTouchOutside(true)
        bottomSheetDialog!!.show()
        etDesc = bottomSheetDialog!!.findViewById(R.id.etDesc)!!
        etHashTag = bottomSheetDialog!!.findViewById(R.id.etHashTag)!!
        var tvTitle2: TextView = bottomSheetDialog!!.findViewById(R.id.tvTitle2)!!
        val tvContinue: TextView? = bottomSheetDialog!!.findViewById(R.id.tvContinue)
        // tvTitle!!.text = title
        var hint = ""
        var title = ""
        if (AppSettings.getString(AppSettings.isFrom).equals("Long"))
        {
            hint = getString(R.string.hashTag)
            title = getString(R.string.pleaseEnterHash)
        }
        else if (AppSettings.getString(AppSettings.isFrom).equals("Short"))
        {
            hint = getString(R.string.title)
            title = getString(R.string.pleaseEnterTitle)
        }
        etHashTag.hint = hint
        tvTitle2.text = hint
        tvContinue!!.setOnClickListener { v: View? ->
            if (etDesc?.text.toString().isEmpty()) {
                AppUtils.showMessageDialog(
                    mActivity,
                    getString(R.string.app_name),
                    getString(R.string.pleaseEnterDescription),
                    8
                )
            } else if (etHashTag?.text.toString().isEmpty()) {
                AppUtils.showMessageDialog(
                    mActivity,
                    getString(R.string.app_name),
                    title,
                    8
                )
            } else {
                if (AppSettings.getString(AppSettings.isFrom).equals("Long"))
                {
                    hitUploadVideosApi()
                }
                else if (AppSettings.getString(AppSettings.isFrom).equals("Short"))
                {
                    hitUploadShortVideosApi()
                }
            }

        }

    }

    private fun hitUploadVideosApi() {
        val desc = etDesc.text.toString()
        val hashTag = etHashTag.text.toString()
        val deviceType = AppConstants.deviceType
        val versionName = BuildConfig.VERSION_NAME
        val apiVersion = AppConstants.apiVersion
        val deviceId = AppUtils.getDeviceID(mActivity)
        val languageCode = AppSettings.getString(AppSettings.language)
        val token = AppSettings.getString(AppSettings.token)
        val type = "1"
        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(false)
        var progress = 0
        pd.show()
        AndroidNetworking.upload(AppUrls.uploadVideo)
            .addMultipartFile("video", videoFile)
            .addMultipartFile("videoThumbnail", thumb)
            .addMultipartParameter("description", desc)
            .addMultipartParameter("type", type)
            .addMultipartParameter("hashtags", hashTag)
            .addHeaders("deviceType", deviceType)
            .addHeaders("appVersion", versionName)
            .addHeaders("apiVersion", apiVersion)
            .addHeaders("deviceId", deviceId)
            .addHeaders("languageCode", languageCode)
            .addHeaders("loginRegion", "IN")
            .addHeaders("token", token)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                progress = ((bytesUploaded.toFloat() / totalBytes) * 100).toInt()
                pd.setMessage("$progress%")
                //updateProgress(progress)
            }
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                   // AppUtils.hideDialog()
                    try {
                        val jsonObj = JSONObject(response.toString())
                        val jsonObject = jsonObj.getJSONObject(AppConstants.projectName)
                        if (jsonObject.getString("resCode") == "1") {
                            pd.dismiss()
                            AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),jsonObject.getString("resMsg"),1)
                            bottomSheetDialog?.dismiss()
                        }
                        else
                        {
                            pd.dismiss()
                            AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),jsonObject.getString("resMsg"),9)
                            bottomSheetDialog?.dismiss()
                        }
                    }
                    catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    AppUtils.hideDialog()
                    Toast.makeText(mActivity, anError.errorBody, Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun hitUploadShortVideosApi() {
        val desc = etDesc.text.toString()
        val hashTag = etHashTag.text.toString()
        val deviceType = AppConstants.deviceType
        val versionName = BuildConfig.VERSION_NAME
        val apiVersion = AppConstants.apiVersion
        val deviceId = AppUtils.getDeviceID(mActivity)
        val languageCode = AppSettings.getString(AppSettings.language)
        val token = AppSettings.getString(AppSettings.token)
        val type = "2"

        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(false)
       // pd.progress = 0
        pd.show()
        var progress = 0
        AndroidNetworking.upload(AppUrls.uploadVideo)
            .addMultipartFile("video", videoFile)
            .addMultipartFile("videoThumbnail", thumb)
            .addMultipartParameter("description", desc)
            .addMultipartParameter("type", type)
            .addMultipartParameter("hashtags", "")
            .addMultipartParameter("title", hashTag)
            .addHeaders("deviceType", deviceType)
            .addHeaders("appVersion", versionName)
            .addHeaders("apiVersion", apiVersion)
            .addHeaders("deviceId", deviceId)
            .addHeaders("languageCode", languageCode)
            .addHeaders("loginRegion", "IN")
            .addHeaders("token", token)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // Calculate the progress percentage
                 progress = ((bytesUploaded.toFloat() / totalBytes) * 100).toInt()

                // Update your UI with the progress value (0 to 100)
                pd.setMessage("$progress%")
                //updateProgress(progress)
            }
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    try {
                        val jsonObj = JSONObject(response)
                        val jsonObject = jsonObj.getJSONObject(AppConstants.projectName)
                        if (jsonObject.getString("resCode") == "1") {
                            pd.dismiss()
                            AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),jsonObject.getString("resMsg"),1)
                            bottomSheetDialog?.dismiss()
                        }
                        else
                        {
                            pd.dismiss()
                            AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),jsonObject.getString("resMsg"),9)
                            bottomSheetDialog?.dismiss()
                        }
                    }
                    catch (e: JSONException) {
                        e.printStackTrace()
                    }
                   // val jsonObject = JSONObject(response)
                }

                override fun onError(anError: ANError) {
                    AppUtils.hideDialog()
                    Toast.makeText(mActivity, anError.errorBody, Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
        private const val MEDIA_TYPE_VIDEO = 2
    }

}