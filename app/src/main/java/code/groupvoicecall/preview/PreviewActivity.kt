package code.groupvoicecall.preview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import code.groupvoicecall.room.RoomActivity
import code.groupvoicecall.util.BaseActivity
import code.groupvoicecall.util.EXTRA_ENTER_ERROR_CODE
import code.groupvoicecall.util.EXTRA_ENTER_ERROR_MESSAGE
import code.groupvoicecall.util.EXTRA_IS_NEWLY_CREATED
import code.groupvoicecall.util.EXTRA_ROOM_ID
import code.groupvoicecall.util.RESULT_ENTER_FAIL
import code.groupvoicecall.util.Status
import com.google.common.util.concurrent.ListenableFuture
import com.hathme.android.R
import com.hathme.android.databinding.ActivityPreviewBinding

class PreviewActivity : BaseActivity() {
    private val viewModel: PreviewViewModel = PreviewViewModel()
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private var preview: Preview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview)
        previewView = binding.previewPreviewView
        setViewEventListeners()
        observeViewModel()
        initCameraPreview()
    }

    private fun initCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setViewEventListeners() {
        binding.previewEnterButton.setOnClickListener(this::onEnterButtonClicked)
        binding.previewImageViewClose.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.enterResult.observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> goToRoomActivity()
                Status.ERROR -> {
                    setResult(RESULT_ENTER_FAIL, Intent().apply {
                        putExtra(EXTRA_ENTER_ERROR_CODE, resource.errorCode)
                        putExtra(EXTRA_ENTER_ERROR_MESSAGE, resource.message)
                    })
                    finish()
                }

                else -> {

                }
            }
        }
    }

    private fun onEnterButtonClicked(v: View) {
        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: throw IllegalStateException("Room ID must not be null")
        val isAudioEnabled = !binding.previewAudioCheckbox.isChecked
        val isVideoEnabled = !binding.previewVideoCheckbox.isChecked
        viewModel.enter(roomId, isAudioEnabled, isVideoEnabled)
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        preview = Preview.Builder().build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview?.setSurfaceProvider(previewView.surfaceProvider)

        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }

    private fun goToRoomActivity() {
        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: throw IllegalStateException("Room ID must not be null")
        val intent = Intent(this, RoomActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_IS_NEWLY_CREATED, false)
        }

        startActivity(intent)
        finish()
    }
}
