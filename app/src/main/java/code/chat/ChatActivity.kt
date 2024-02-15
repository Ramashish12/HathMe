package code.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import code.activity.AudioPlayerActivity
import code.activity.ShowPdfActivity
import code.activity.VideoPlayerActivity
import code.activity.ZoomingImageActivity
import code.utils.*
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityChatBinding
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.android.handler.OpenChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.ThumbnailSize
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.*
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class ChatActivity : BaseActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: OpenChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private var currentOpenChannel: OpenChannel? = null
    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var receiverId: String = ""
    private var hasPrevious: Boolean = true
    private var isMessageLoading: Boolean = false
    private var changelogToken: String? = null
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendbirdChat.autoBackgroundDetection = true
            if (data.resultCode == RESULT_OK) {
                val uri = data.data?.data
                sendFileMessage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        channelUrl = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_URL) ?: ""
        channelTitle = intent.getStringExtra(Constants.INTENT_KEY_CHANNEL_TITLE) ?: ""
        receiverId = intent.getStringExtra("receiverId")!!
        init()
        initRecyclerView()
        enterChannel(channelUrl)
        addHandler()
    }

    private fun init() {
//        binding.toolbar.title = channelTitle


        binding.header.ivBack.setOnClickListener { onBackPressed() }
        binding.header.tvHeader.text =
            intent.getStringExtra(Constants.INTENT_KEY_RECEIVER_NAME) ?: ""

        binding.chatInputView.setOnSendMessageClickListener(object :
            ChatInputView.OnSendMessageClickListener {
            override fun onUserMessageSend() {
                val message = binding.chatInputView.getText()
                sendMessage(message)
            }

            override fun onFileMessageSend() {
                SendbirdChat.autoBackgroundDetection = false

                FileUtils.selectFile(
                    startForResult,
                    this@ChatActivity
                )
            }
        })
        //  intent.putExtra(INTENT_KEY_RECEIVER_ID, merchantId);


        hitReadCountApi()
    }

    private fun initRecyclerView() {
        adapter = OpenChannelChatAdapter(
            { baseMessage, view, _ ->
                view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                    val currentUser = SendbirdChat.currentUser
                    if (currentUser != null && baseMessage.sender?.userId == currentUser.userId) {
                        val deleteMenu =
                            contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                        deleteMenu.setOnMenuItemClickListener {
                            deleteMessage(baseMessage)
                            return@setOnMenuItemClickListener false
                        }
                        if (baseMessage is UserMessage) {
                            val updateMenu =
                                contextMenu.add(Menu.NONE, 1, 1, getString(R.string.update))
                            updateMenu.setOnMenuItemClickListener {
                                showInputDialog(
                                    getString(R.string.update),
                                    null,
                                    baseMessage.message,
                                    getString(R.string.update),
                                    getString(R.string.cancel),
                                    { updateMessage(it, baseMessage) },
                                )
                                return@setOnMenuItemClickListener false
                            }
                        }
                    }
                    if (baseMessage is UserMessage) {
                        val copyMenu = contextMenu.add(Menu.NONE, 2, 2, getString(R.string.copy))
                        copyMenu.setOnMenuItemClickListener {
                            copy(baseMessage.message)
                            return@setOnMenuItemClickListener true
                        }
                    }
                }
            },
            { baseMessage, view, _ ->

                if (baseMessage is FileMessage) {

                    //val url = baseMessage.url
                    downloadMedia(baseMessage)

                }


            },
            {
                showListDialog(
                    listOf(getString(R.string.retry), getString(R.string.delete))
                ) { _, position ->
                    when (position) {
                        0 -> resendMessage(it)
                        1 -> adapter.deletePendingMessage(it)
                    }
                }
            }
        )
        binding.recyclerviewChat.adapter = adapter
        binding.recyclerviewChat.itemAnimator = null
        recyclerObserver = ChatRecyclerDataObserver(binding.recyclerviewChat, adapter)
        adapter.registerAdapterDataObserver(recyclerObserver)
        binding.recyclerviewChat.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    if (hasPrevious && !isMessageLoading && adapter.currentList.isNotEmpty()) {
                        loadMessagesPreviousMessages(adapter.currentList.first().createdAt)
                    }
                }
            }
        })
    }

    private fun downloadMedia(baseMessage: FileMessage) {

        if (baseMessage.message.endsWith("jpg") || baseMessage.message.endsWith("jpeg")
            || baseMessage.message.endsWith("png") || baseMessage.message.endsWith("webp")
        ) {
            val fileName =
                baseMessage.url.substring(baseMessage.url.lastIndexOf('/') + 1).split("?")[0]
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val files = File(downloadDir, fileName)
            if (files.exists()) {
                val pdffilepath = files.absolutePath
                AppSettings.putString(AppSettings.KEY_selected_image, pdffilepath)
                val i = Intent(applicationContext, ZoomingImageActivity::class.java)
                startActivity(i)
            } else {
                // The file does not exist in the download directory, download it
                AppSettings.putString(AppSettings.KEY_selected_type, Environment.DIRECTORY_PICTURES)
                AppSettings.putString(AppSettings.KEY_selected_filename, fileName)
                downloadfile(baseMessage)
            }
        } else if (baseMessage.message.endsWith("mp3")) {

            val fileName =
                baseMessage.url.substring(baseMessage.url.lastIndexOf('/') + 1).split("?")[0]
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val files = File(downloadDir, fileName)
            if (files.exists()) {
                val pdffilepath = files.absolutePath
                AppSettings.putString(AppSettings.KEY_selected_url, pdffilepath)
                val i = Intent(applicationContext, AudioPlayerActivity::class.java)
                startActivity(i)
            } else {
                AppSettings.putString(AppSettings.KEY_selected_type, Environment.DIRECTORY_MUSIC)
                AppSettings.putString(AppSettings.KEY_selected_filename, fileName)
                downloadfile(baseMessage)
            }
        } else if (baseMessage.message.endsWith("mp4")) {

            val fileName =
                baseMessage.url.substring(baseMessage.url.lastIndexOf('/') + 1).split("?")[0]
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val files = File(downloadDir, fileName)
            if (files.exists()) {
                val pdffilepath = files.absolutePath
                AppSettings.putString(AppSettings.KEY_selected_url, pdffilepath)
                val i = Intent(applicationContext, VideoPlayerActivity::class.java)
                startActivity(i)
                // The file exists in the download directory, perform necessary operations on the file
            } else {
                AppSettings.putString(AppSettings.KEY_selected_type, Environment.DIRECTORY_MOVIES)
                AppSettings.putString(AppSettings.KEY_selected_filename, fileName)
                downloadfile(baseMessage)
                // The file does not exist in the download directory, download it
            }

        } else if (baseMessage.message.endsWith("pdf")) {
            val fileName =
                baseMessage.url.substring(baseMessage.url.lastIndexOf('/') + 1).split("?")[0]
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val files = File(downloadDir, fileName)
            if (files.exists()) {
                val pdffilepath = files.absolutePath
                AppSettings.putString(AppSettings.KEY_selected_pdfurl, pdffilepath)
                val i = Intent(applicationContext, ShowPdfActivity::class.java)
                startActivity(i)
            } else {
                // The file does not exist in the download directory, download it
                AppSettings.putString(
                    AppSettings.KEY_selected_type,
                    Environment.DIRECTORY_DOCUMENTS
                )
                AppSettings.putString(AppSettings.KEY_selected_filename, fileName)
                downloadfile(baseMessage)
            }
        } else {
            AppUtils.showToastSort(mActivity, "This type file not supported")
        }
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("Range")
    private fun downloadfile(baseMessage: FileMessage) {
        val type = AppSettings.getString(AppSettings.KEY_selected_type)
        val fileName = AppSettings.getString(AppSettings.KEY_selected_filename)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait while your file is downloading...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val request = DownloadManager.Request(Uri.parse(baseMessage.url))
        request.setDestinationInExternalPublicDir(type, fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread(Runnable {
            var downloading = true
            while (downloading) {
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    == DownloadManager.STATUS_SUCCESSFUL
                ) {
                    downloading = false
                    progressDialog.dismiss()

                    if (AppSettings.getString(AppSettings.KEY_selected_type).equals(
                            Environment.DIRECTORY_PICTURES
                        )
                    ) {
                        val downloadDir = Environment.getExternalStoragePublicDirectory(type)
                        val files = File(downloadDir, fileName)
                        val path = files.absolutePath
                        AppSettings.putString(AppSettings.KEY_selected_image, path)
                        val i = Intent(applicationContext, ZoomingImageActivity::class.java)
                        startActivity(i)
                    } else if (AppSettings.getString(AppSettings.KEY_selected_type).equals(
                            Environment.DIRECTORY_MOVIES
                        )
                    ) {
                        val downloadDir = Environment.getExternalStoragePublicDirectory(type)
                        val files = File(downloadDir, fileName)
                        val path = files.absolutePath
                        AppSettings.putString(AppSettings.KEY_selected_url, path)
                        val i = Intent(applicationContext, VideoPlayerActivity::class.java)
                        startActivity(i)
                    } else if (AppSettings.getString(AppSettings.KEY_selected_type).equals(
                            Environment.DIRECTORY_MUSIC
                        )
                    ) {
                        val downloadDir = Environment.getExternalStoragePublicDirectory(type)
                        val files = File(downloadDir, fileName)
                        val path = files.absolutePath
                        AppSettings.putString(AppSettings.KEY_selected_url, path)
                        val i = Intent(applicationContext, AudioPlayerActivity::class.java)
                        startActivity(i)
                    } else if (AppSettings.getString(AppSettings.KEY_selected_type).equals(
                            Environment.DIRECTORY_DOCUMENTS
                        )
                    ) {
                        val downloadDir = Environment.getExternalStoragePublicDirectory(type)
                        val files = File(downloadDir, fileName)
                        val path = files.absolutePath
                        AppSettings.putString(AppSettings.KEY_selected_pdfurl, path)
                        val i = Intent(applicationContext, ShowPdfActivity::class.java)
                        startActivity(i)
                    } else {

                    }
                }
                val progress = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                runOnUiThread {
                    progressDialog.progress = progress
                }
                cursor.close()
            }
        }).start()
    }


    private fun enterChannel(channelUrl: String) {
        if (channelUrl.isBlank()) {
            // showToast(getString(R.string.channel_url_error))
            finish()
            return
        }
        OpenChannel.getChannel(channelUrl) { openChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannel
            }
            openChannel?.enter { e2 ->
                if (e2 != null) {
                    showToast("${e2.message}")
                    return@enter
                }
                currentOpenChannel = openChannel
                loadMessagesPreviousMessages(Long.MAX_VALUE)
            }
        }
    }

    private fun addHandler() {
        SendbirdChat.addConnectionHandler(
            Constants.CONNECTION_HANDLER_ID,
            object : ConnectionHandler {
                override fun onReconnectStarted() {}

                override fun onReconnectSucceeded() {
                    if (changelogToken != null) {
                        getMessageChangeLogsSinceToken()
                    } else {
                        val lastMessage = adapter.currentList.lastOrNull()
                        if (lastMessage != null) {
                            getMessageChangeLogsSinceTimestamp(lastMessage.createdAt)
                        }
                    }
                    loadToLatestMessages(adapter.currentList.lastOrNull()?.createdAt ?: 0)
                }

                override fun onConnected(userId: String) {}

                override fun onDisconnected(userId: String) {}

                override fun onReconnectFailed() {}
            })

        SendbirdChat.addChannelHandler(Constants.CHANNEL_HANDLER_ID, object : OpenChannelHandler() {

            override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.addMessage(message)
                    hitReadCountApi()
                }
            }

            override fun onMessageDeleted(channel: BaseChannel, msgId: Long) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.deleteMessages(listOf(msgId))
                }
            }

            override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {
                if (channel.url == currentOpenChannel?.url) {
                    adapter.updateMessages(listOf(message))
                }
            }

            override fun onChannelDeleted(
                channelUrl: String,
                channelType: ChannelType
            ) {
                showToast(R.string.channel_deleted_event_msg)
                finish()
            }

            override fun onChannelChanged(channel: BaseChannel) {
                updateChannel(channel as OpenChannel)
            }

        })
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        currentOpenChannel?.deleteMessage(baseMessage) {
            if (it != null) {
                showToast("${it.message}")
            }
        }
    }

    private fun updateMessage(msg: String, baseMessage: BaseMessage) {
        if (msg.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val params = UserMessageUpdateParams()
            .apply {
                message = msg
            }
        currentOpenChannel?.updateUserMessage(
            baseMessage.messageId,
            params
        ) { message, e ->
            if (e != null) {
                showToast("${e.message}")
                return@updateUserMessage
            }
            if (message != null) {
                adapter.updateMessages(
                    listOf(message)
                )
            }
        }
    }

    /*
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.delete -> {
                    showAlertDialog(
                        getString(R.string.delete_channel),
                        getString(R.string.channel_delete_msg),
                        getString(R.string.delete),
                        getString(R.string.cancel),
                        { deleteChannel() },
                    )
                    true
                }

                R.id.update_channel_name -> {
                    val channel = currentOpenChannel ?: return true
                    showInputDialog(
                        getString(R.string.update),
                        null,
                        channel.name,
                        getString(R.string.update),
                        getString(R.string.cancel),
                        { updateChannel(it, channel) },
                    )
                    true
                }

                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    */

    private fun deleteChannel() {
        currentOpenChannel?.delete {
            if (it != null) {
                showToast("$it")
            }
            finish()
        }
    }

    private fun updateChannel(name: String, channel: OpenChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val params = OpenChannelUpdateParams()
            .apply { this.name = name }
        channel.updateChannel(params) { _, e ->
            if (e != null) {
                showToast("${e.message}")
            }
        }
    }

    private fun loadMessagesPreviousMessages(
        timeStamp: Long,
    ) {
        val channel = currentOpenChannel ?: return
        isMessageLoading = true
        val params = MessageListParams().apply {
            previousResultSize = 20
            nextResultSize = 0
            reverse = false
        }
        channel.getMessagesByTimestamp(timeStamp, params) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            }
            if (messages != null) {
                if (messages.isNotEmpty()) {
                    hasPrevious = messages.size >= params.previousResultSize
                    adapter.addPreviousMessages(messages)
                } else {
                    hasPrevious = false
                }
            }
            isMessageLoading = false
        }
    }

    private fun loadToLatestMessages(timeStamp: Long) {
        val channel = currentOpenChannel ?: return
        isMessageLoading = true
        val params = MessageListParams().apply {
            nextResultSize = 100
            reverse = false
        }
        channel.getMessagesByTimestamp(timeStamp, params) { messages, e ->
            if (e != null) {
                showToast("${e.message}")
            }
            if (!messages.isNullOrEmpty()) {
                adapter.addNextMessages(messages)
                if (messages.size >= params.nextResultSize) {
                    loadToLatestMessages(messages.last().createdAt)
                } else {
                    isMessageLoading = false
                }
            } else {
                isMessageLoading = false
            }
        }
    }

    private fun sendMessage(msg: String) {
        if (msg.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        val channel = currentOpenChannel ?: return
        val params = UserMessageCreateParams()
            .apply {
                message = msg.trim()
            }
        binding.chatInputView.clearText()
        val pendingMessage = channel.sendUserMessage(params) { message, e ->
            if (e != null) {
                //failed
                showToast("${e.message}")
                adapter.updatePendingMessage(message)
                return@sendUserMessage
            }
            //succeeded
            adapter.updateSucceedMessage(message)
            hitNotifyApi("1", msg)

        }
        //pending
        adapter.addPendingMessage(pendingMessage)
        recyclerObserver.scrollToBottom(true)
    }

    private fun sendFileMessage(imgUri: Uri?) {
        if (imgUri == null) {
            // showToast(R.string.file_transfer_error)
            return
        }
        val channel = currentOpenChannel ?: return
        val thumbnailSizes = listOf(
            ThumbnailSize(100, 100),
            ThumbnailSize(200, 200)
        )
        val fileInfo = FileUtils.getFileInfo(imgUri, applicationContext)
        if (fileInfo != null) {
            val params = FileMessageCreateParams().apply {
                file = fileInfo.file
                fileName = fileInfo.name
                fileSize = fileInfo.size
                this.thumbnailSizes = thumbnailSizes
                mimeType = fileInfo.mime
            }
            val pendingMessage = channel.sendFileMessage(
                params
            ) sendFileMessageLabel@{ fileMessage, e ->
                if (e != null) {
                    //failed
                    showToast("${e.message}")
                    adapter.updatePendingMessage(fileMessage)
                    return@sendFileMessageLabel
                }
                //succeeded
                adapter.updateSucceedMessage(fileMessage)
                if (fileInfo.mime.toString() == "mp4")
                    hitNotifyApi("2", fileInfo.name)
                else
                    hitNotifyApi("3", fileInfo.name)

            }
            //pending
            adapter.addPendingMessage(pendingMessage)
            recyclerObserver.scrollToBottom(true)
        } else {
            //  showToast(R.string.file_transfer_error)
        }
    }

    private fun resendMessage(baseMessage: BaseMessage) {
        val channel = currentOpenChannel ?: return
        when (baseMessage) {
            is UserMessage -> {
                channel.resendMessage(baseMessage, null)
            }

            is FileMessage -> {
                val params = baseMessage.messageCreateParams
                if (params != null) {
                    channel.resendMessage(
                        baseMessage,
                        params.file
                    ) { _, _ -> }
                }
            }
        }
    }

    private fun getMessageChangeLogsSinceTimestamp(timeStamp: Long) {
        val channel = currentOpenChannel ?: return
        val params = MessageChangeLogsParams()
        channel.getMessageChangeLogsSinceTimestamp(
            timeStamp,
            params
        ) getMessageChangeLogsSinceTimestampLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTimestampLabel
            }
            adapter.updateMessages(updatedMessages)
            adapter.deleteMessages(deletedMessageIds)
            changelogToken = token
            if (hasMore) {
                getMessageChangeLogsSinceToken()
            }

        }
    }

    private fun getMessageChangeLogsSinceToken() {
        if (changelogToken == null) return
        val channel = currentOpenChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            return
        }
        val params = MessageChangeLogsParams()
        channel.getMessageChangeLogsSinceToken(
            changelogToken,
            params
        ) getMessageChangeLogsSinceTokenLabel@{ updatedMessages, deletedMessageIds, hasMore, token, e ->
            if (e != null) {
                showToast("$e")
                return@getMessageChangeLogsSinceTokenLabel
            }
            adapter.updateMessages(updatedMessages)
            adapter.deleteMessages(deletedMessageIds)
            changelogToken = token
            if (hasMore) {
                getMessageChangeLogsSinceToken()
            }
        }
    }

    private fun updateChannel(openChannel: OpenChannel) {
        currentOpenChannel = openChannel
//        binding.toolbar.title = openChannel.name
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }*/

    override fun onDestroy() {
        super.onDestroy()
        SendbirdChat.removeConnectionHandler(Constants.CONNECTION_HANDLER_ID)
        SendbirdChat.removeChannelHandler(Constants.CHANNEL_HANDLER_ID)
        SendbirdChat.autoBackgroundDetection = true
        currentOpenChannel?.exit {}
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showToast(getString(R.string.permission_granted))
                        SendbirdChat.autoBackgroundDetection = false
                        FileUtils.selectFile(
                            Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                            startForResult,
                            this
                        )
                    } else {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                Constants.PERMISSION_REQUEST_CODE
                            )
                        } else {
                            showToast(getString(R.string.permission_denied))
                        }
                    }
                }
            }
        }
    }

    private fun hitNotifyApi(type: String, message: String) {

        val jsonObject = JSONObject()
        val json = JSONObject()

        jsonObject.put("senderId", intent.getStringExtra(Constants.INTENT_KEY_RECEIVER_ID) ?: "")
        jsonObject.put("message", message)
        jsonObject.put("name", intent.getStringExtra(Constants.INTENT_KEY_RECEIVER_NAME) ?: "")
        /// jsonObject.put("type", type)
        jsonObject.put("messageType", type)
        jsonObject.put("channelName", channelTitle)
        jsonObject.put("channelUrl", channelUrl)
        json.put(AppConstants.projectName, jsonObject)

        WebServices.postApi(
            mActivity,
            AppUrls.SendNotification,
            json,
            false,
            false,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {
                    parseJsonCount(response!!)
                }

                override fun OnFail(response: String?) {

                }
            })

    }


    private fun hitReadCountApi() {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("channelUrl", channelUrl)
            jsonObject.put("senderId", receiverId)

            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity,
            AppUrls.messageCount,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJsonCount(response)
                }

                override fun OnFail(response: String) {}
            })
    }

    private fun parseJsonCount(response: JSONObject) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {


            } else {

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        GlobalData.isChatOpen = true
    }

    override fun onPause() {
        super.onPause()
        GlobalData.isChatOpen = false

    }

}