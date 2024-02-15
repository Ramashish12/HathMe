package code.groupchatting.groupchannel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.activity.AudioPlayerActivity
import code.activity.ShowPdfActivity
import code.activity.VideoPlayerActivity
import code.activity.ZoomingImageActivity
import code.groupvideocall.VideoAuthenticateViewModel
import code.groupvideocall.main.VideoGroupCallMainActivity
import code.groupvideocall.util.SENDBIRD_APP_ID
import code.groupvideocall.util.Status
import code.groupvoicecall.room.InvitationListActivity
import code.groupvoicecall.room.SelectUserListActivity
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hathme.android.R
import com.hathme.android.databinding.ActivityGroupChannelChatBinding
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.message.*
import com.sendbird.android.params.*
import com.sendbird.calls.BuildConfig
import com.sendbird.calls.SendBirdCall
import com.sendbird.chat.module.ui.ChatInputView
import com.sendbird.chat.module.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class GroupChannelChatActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityGroupChannelChatBinding
    private lateinit var adapter: GroupChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var groupId: String = ""
    private var leaveStatus: String = ""
    private var isAdmin: String = ""
    private var currentGroupChannel: GroupChannel? = null
    private var messageCollection: MessageCollection? = null
    private var channelTSHashMap = ConcurrentHashMap<String, Long>()
    private var isCollectionInitialized = false
    private lateinit var sharedPreferences: SharedPreferenceUtils
    var bottomSheetDialog: BottomSheetDialog? = null
    private val startForResultFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            SendbirdChat.autoBackgroundDetection = true
            if (data.resultCode == RESULT_OK) {
                val uri = data.data?.data
                sendFileMessage(uri)
            }
        }
    private val startForResultInvite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            if (data.resultCode == RESULT_OK) {
                val selectIds = data.data?.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER)
                inviteUser(selectIds)
            }
        }
    private val viewModel: VideoAuthenticateViewModel = VideoAuthenticateViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = SharedPreferenceUtils
        val intent = intent

        channelUrl = intent.getStringExtra("channelURL") ?: ""
        channelTitle = intent.getStringExtra("channelName") ?: ""
        groupId = intent.getStringExtra("groupId") ?: ""
        leaveStatus = intent.getStringExtra("leaveStatus") ?: ""
        isAdmin = intent.getStringExtra("isAdmin") ?: ""

        init()
        initRecyclerView()
        getChannel(channelUrl)
        hitReadCountApi()
    }

    private fun init() {
        observeViewModel()
        binding.header.tvHeader.text = channelTitle
        binding.header.ivBack.setOnClickListener(this)
        binding.header.ivMenu.setOnClickListener(this)
        binding.header.ivGroupAudioCall.setOnClickListener(this)
        binding.header.ivGroupVideoCall.setOnClickListener(this)
        binding.header.ivGroupVideoCall.visibility = View.GONE
        binding.header.ivGroupAudioCall.visibility = View.GONE
        binding.header.ivInvitations.setOnClickListener(this)
        if (leaveStatus == "false") {
            binding.chatInputView.visibility = View.GONE
        }

        binding.chatInputView.setOnSendMessageClickListener(object :
            ChatInputView.OnSendMessageClickListener {
            override fun onUserMessageSend() {
                val message = binding.chatInputView.getText()
                sendMessage(message)
            }

            override fun onFileMessageSend() {
                SendbirdChat.autoBackgroundDetection = false
                FileUtils.selectFile(
                    Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                    startForResultFile,
                    this@GroupChannelChatActivity
                )
            }
        })

    }

    private fun initRecyclerView() {
        adapter = GroupChannelChatAdapter(
            { baseMessage, view ->
                view.setOnCreateContextMenuListener { contextMenu, _, _ ->
                    if (SendbirdChat.currentUser != null && baseMessage.sender?.userId
                        == SendbirdChat.currentUser!!.userId
                    ) {
                        val deleteMenu =
                            contextMenu.add(Menu.NONE, 0, 0, getString(R.string.delete))
                        deleteMenu.setOnMenuItemClickListener {
                            deleteMessage(baseMessage)
                            return@setOnMenuItemClickListener true
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
                                return@setOnMenuItemClickListener true
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
                        1 -> adapter.deletePendingMessages(mutableListOf(it))
                    }
                }
            }
        )
        binding.recyclerviewChat.itemAnimator = null
        binding.recyclerviewChat.adapter = adapter
        recyclerObserver = ChatRecyclerDataObserver(binding.recyclerviewChat, adapter)
        adapter.registerAdapterDataObserver(recyclerObserver)

        binding.recyclerviewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    loadPreviousMessageItems()
                } else if (!recyclerView.canScrollVertically(1)) {
                    loadNextMessageItems()
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
                downloadFile(baseMessage)
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
                downloadFile(baseMessage)
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
                downloadFile(baseMessage)
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
                downloadFile(baseMessage)
            }
        } else {
            AppUtils.showToastSort(mActivity, "This type file not supported")
        }
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("Range")
    private fun downloadFile(baseMessage: FileMessage) {
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

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                currentGroupChannel = groupChannel
                // setChannelTitle()
                createMessageCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
            }
        }
    }

    private fun setChannelTitle() {
        val currentChannel = currentGroupChannel
        if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME && currentChannel != null) {
            binding.toolbar.title = TextUtils.getGroupChannelTitle(currentChannel)
        } else {
            if (currentChannel != null) {
                updateChannelView(channelTitle, currentChannel)
            } else {
                binding.toolbar.title = channelTitle
            }

        }
    }

    private fun createMessageCollection(timeStamp: Long) {
        messageCollection?.dispose()
        isCollectionInitialized = false
        val channel = currentGroupChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            finish()
            return
        }

        val messageListParams = MessageListParams().apply {
            reverse = false
            previousResultSize = 20
            nextResultSize = 20
        }
        val messageCollectionCreateParams =
            MessageCollectionCreateParams(channel, messageListParams).apply {
                startingPoint = timeStamp
                messageCollectionHandler = collectionHandler
            }
        messageCollection =
            SendbirdChat.createMessageCollection(messageCollectionCreateParams).apply {
                initialize(
                    MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
                    object : MessageCollectionInitHandler {
                        override fun onCacheResult(
                            cachedList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                showToast("${e.message}")
                            }
                            adapter.changeMessages(cachedList)
                            adapter.addPendingMessages(this@apply.pendingMessages)
                        }

                        override fun onApiResult(
                            apiResultList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                showToast("${e.message}")
                            }
                            adapter.changeMessages(apiResultList, false)
                            markAsRead()
                            isCollectionInitialized = true
                        }
                    }
                )
            }
    }

    private fun loadPreviousMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasPrevious) {
            collection.loadPrevious { messages, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadPrevious
                }
                adapter.addPreviousMessages(messages)
            }
        }
    }

    private fun loadNextMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasNext) {
            collection.loadNext { messages, e ->
                if (e != null) {
                    showToast("${e.message}")
                    return@loadNext
                }
                adapter.addNextMessages(messages)
                markAsRead()
            }
        }
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        currentGroupChannel?.deleteMessage(baseMessage) {
            if (it != null) {
                showToast("${it.message}")
            }
        }
    }

    private fun updateMessage(msg: String, baseMessage: BaseMessage) {
        if (msg.isBlank()) {
            // showToast(R.string.enter_message_msg)
            return
        }
        val params = UserMessageUpdateParams().apply {
            message = msg
        }
        currentGroupChannel?.updateUserMessage(
            baseMessage.messageId, params
        ) { _, e ->
            if (e != null) {
                showToast("${e.message}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.leave -> {
                showAlertDialog(
                    getString(R.string.delete_channel),
                    getString(R.string.sure_to_delete_channel),
                    getString(R.string.delete),
                    getString(R.string.cancel),
                    { leaveChannel() },
                )
                true
            }

            R.id.member_list -> {
                val intent = Intent(this, ChatMemberListActivity::class.java)
                intent.putExtra(Constants.INTENT_KEY_CHANNEL_URL, channelUrl)
                startActivity(intent)
                true
            }

            R.id.invite -> {
                selectInviteUser()
                true
            }

            R.id.update_channel_name -> {
                val channel = currentGroupChannel ?: return true
                showInputDialog(
                    getString(R.string.update),
                    null,
                    channel.name,
                    getString(R.string.update),
                    getString(R.string.cancel),
                    {

                        updateChannelView(it, channel)
                    },
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

    private fun deleteChannel() {
//        if (profSession.GetSharedPreferences(PF300kfjs3.KEY_is_operator).equals(currentGroupChannel!!.creator!!.userId))
//        {
//            currentGroupChannel?.delete {
//                if (it != null) {
//                    showToast("${it.message}")
//                    return@delete
//                }
//                finish()
//            }
////            currentGroupChannel?.leave {
////                if (it != null) {
////                    showToast("Group Deleted")
////                }
////                finish()
////            }
//        }
//        else
//        {
//            showToast("You are not Admin for delete group")
//        }
        currentGroupChannel?.delete {
            if (it != null) {
                showToast("${it.message}")
                return@delete
            }
            finish()
        }
    }

    private fun leaveChannel() {
        currentGroupChannel?.leave {
            if (it != null) {
                showToast("${it.message}")
            }
            finish()
        }
    }

    private fun selectInviteUser() {
        val channel = currentGroupChannel
        if (channel == null) {
            showToast(R.string.channel_error)
            return
        }
        val memberIds = ArrayList(channel.members.map { it.userId })

        val intent = Intent(this, InviteMembersListActivity::class.java)
        intent.putExtra("channelURL", channelUrl)
        intent.putExtra("channelName", channelTitle)
        intent.putExtra("groupId", groupId)
        intent.putExtra(Constants.INTENT_KEY_BASE_USER, memberIds)
        startForResultInvite.launch(intent)
    }

    private fun inviteUser(selectIds: List<String>?) {
        if (!selectIds.isNullOrEmpty()) {
            val channel = currentGroupChannel ?: return
            channel.invite(selectIds.toList()) {
                if (it != null) {
                    showToast("${it.message}")
                }
            }
        }
    }

    private fun updateChannelView(name: String, channel: GroupChannel) {
        if (name.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (channel.name != name) {
            val params = GroupChannelUpdateParams()
                .apply { this.name = name }
            channel.updateChannel(
                params
            ) { _, e ->
                if (e != null) {
                    showToast("${e.message}")
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            showToast(R.string.enter_message_msg)
            return
        }
        if (!isCollectionInitialized) {
            showToast(R.string.message_collection_init_msg)
            return
        }
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return

        val params = UserMessageCreateParams().apply {
            this.message = message.trim()
        }
        hitNotifyApi("1", message)
        binding.chatInputView.clearText()
        recyclerObserver.scrollToBottom(true)
        channel.sendUserMessage(params, null)
        if (collection.hasNext) {
            createMessageCollection(Long.MAX_VALUE)
        }
    }

    private fun sendFileMessage(imgUri: Uri?) {
        if (imgUri == null) {
            showToast(R.string.file_transfer_error)
            return
        }
        if (!isCollectionInitialized) {
            showToast(R.string.message_collection_init_msg)
            return
        }
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return

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
            if (fileInfo.mime.toString() == "mp4")
                hitNotifyApi("2", fileInfo.name)
            else
                hitNotifyApi("3", fileInfo.name)
            recyclerObserver.scrollToBottom(true)

            channel.sendFileMessage(
                params,
            ) { _, _ -> }
            if (collection.hasNext) {
                createMessageCollection(Long.MAX_VALUE)
            }
        } else {
            showToast(R.string.file_transfer_error)
        }
    }

    private fun resendMessage(baseMessage: BaseMessage) {
        val channel = currentGroupChannel ?: return
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

    private fun markAsRead() {
        currentGroupChannel?.markAsRead { e1 -> e1?.printStackTrace() }
    }

    private fun updateChannelView(groupChannel: GroupChannel) {
        currentGroupChannel = groupChannel
//        binding.toolbar.title =
//            if (groupChannel.name.isBlank() || groupChannel.name == TextUtils.CHANNEL_DEFAULT_NAME)
//                TextUtils.getGroupChannelTitle(groupChannel)
//            else groupChannel.name
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onPause() {
        val lastMessage = adapter.currentList.lastOrNull()
        if (lastMessage != null && channelUrl.isNotBlank()) {
            channelTSHashMap[channelUrl] = lastMessage.createdAt
            //SharedPreferenceutils.channelTSMap = channelTSHashMap
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageCollection?.dispose()
        SendbirdChat.autoBackgroundDetection = true
    }

    private val collectionHandler = object : MessageCollectionHandler {
        override fun onMessagesAdded(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> {
                    adapter.addMessages(messages)
                    markAsRead()
                }

                SendingStatus.PENDING -> adapter.addPendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onMessagesUpdated(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> {
                    adapter.updateSucceedMessages(messages)
                }

                SendingStatus.PENDING -> {
                    adapter.updatePendingMessages(messages)
                }

                SendingStatus.FAILED -> {
                    adapter.updatePendingMessages(messages)
                }

                SendingStatus.CANCELED -> {
                    adapter.deletePendingMessages(messages)
                }// The cancelled messages in the sample will be deleted

                else -> {
                }
            }
        }

        override fun onMessagesDeleted(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> adapter.deleteMessages(messages)

                SendingStatus.FAILED -> adapter.deletePendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
            // updateChannelView(channel)
        }

        override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
            showToast(R.string.channel_deleted_event_msg)
            finish()
        }


        override fun onHugeGapDetected() {
            val collection = messageCollection
            if (collection == null) {
                showToast(R.string.channel_error)
                finish()
                return
            }
            val startingPoint = collection.startingPoint
            collection.dispose()
            val position: Int =
                (binding.recyclerviewChat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (position >= 0) {
                val message: BaseMessage = adapter.currentList[position]
                createMessageCollection(message.createdAt)
            } else {
                createMessageCollection(startingPoint)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.permission_granted))
                    SendbirdChat.autoBackgroundDetection = false
                    FileUtils.selectFile(
                        Constants.DATA_TYPE_IMAGE_AND_VIDEO,
                        startForResultFile,
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

    override fun onClick(v: View?) {
        when (v) {
            binding.header.ivBack -> {
                onBackPressed()
            }

            binding.header.ivGroupVideoCall -> {
                onSignInButtonClicked()
            }

            binding.header.ivGroupAudioCall -> {
                val intent = Intent(this, SelectUserListActivity::class.java)
                startActivity(intent)
            }
            binding.header.ivInvitations -> {
                val intent = Intent(this, InvitationListActivity::class.java)
                startActivity(intent)
            }

            binding.header.ivMenu -> {
                val popup = PopupMenu(mActivity, v)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_group_chat, popup.menu)
                val leaveMenuItem = popup.menu.findItem(R.id.leave)
                val deleteMenuItem = popup.menu.findItem(R.id.delete)
                val inviteMenuItem = popup.menu.findItem(R.id.invite)
                val updateGroupNameMenuItem = popup.menu.findItem(R.id.update_channel_name)
                val isShow = true /* Your condition to show the Edit menu item */
                val isNotShow = false/* Your condition to show the Delete menu item */
                if (leaveStatus == "true") {
                    leaveMenuItem.isVisible = isShow
                    if (isAdmin == "true") {
                        inviteMenuItem.isVisible = isShow
                    } else {
                        inviteMenuItem.isVisible = isNotShow
                    }
                    updateGroupNameMenuItem.isVisible = isShow
                    deleteMenuItem.isVisible = isNotShow
                } else {
                    leaveMenuItem.isVisible = isNotShow
                    inviteMenuItem.isVisible = isNotShow
                    updateGroupNameMenuItem.isVisible = isNotShow
                    deleteMenuItem.isVisible = isShow
                    binding.chatInputView.visibility = View.GONE
                }
                popup.setOnMenuItemClickListener { item ->

                    when (item.itemId) {
                        R.id.delete -> {
                            showDeleteGroupAlert()
                            popup.dismiss()
                        }

                        R.id.update_channel_name -> {
                            if (isAdmin == "true") {
                                if (leaveStatus == "true") {
                                    showDialogUpdateGroupName(mActivity, channelTitle)
                                } else {
                                    AppUtils.showToastSort(
                                        mActivity,
                                        getString(R.string.update_group)
                                    )
                                }

                            } else {
                                AppUtils.showToastSort(mActivity, getString(R.string.update_group))
                            }
                            popup.dismiss()
                        }

                        R.id.member_list -> {
                            val intent = Intent(mActivity, ChatMemberListActivity::class.java)
                            intent.putExtra("channelURL", channelUrl)
                            intent.putExtra("channelName", channelTitle)
                            intent.putExtra("groupId", groupId)
                            startActivity(intent)
                            popup.dismiss()
                        }

                        R.id.invite -> {
//                            val intent = Intent(mActivity, InviteMembersListActivity::class.java)
//                            intent.putExtra("channelURL", channelUrl)
//                            intent.putExtra("channelName", channelTitle)
//                            intent.putExtra("groupId", groupId)
//                            startActivity(intent)
                            selectInviteUser()
                            true
                            popup.dismiss()
                        }

                        R.id.leave -> {
                            showLeaveGroupAlert()
                            popup.dismiss()
                        }

                    }
                    false
                }
                popup.show() //
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authenticationLiveData.observe(this) { resource ->
            Log.d("SignInActivity", "observe() resource: $resource")
            when (resource.status) {
                Status.LOADING -> {
                    // TODO : show loading view
                }

                Status.SUCCESS -> {
                    goToMainActivity()
                }

                Status.ERROR -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, VideoGroupCallMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onSignInButtonClicked() {
        val appId = SENDBIRD_APP_ID
        if (appId.isEmpty()) {
            return
        }

        SendBirdCall.init(applicationContext, appId)
        val userId = AppSettings.getString(AppSettings.userId)
        val accessToken = ""

        viewModel.authenticate(userId, accessToken)
    }

    private fun getVersion() =
        "Quickstart ${BuildConfig.VERSION_NAME}   SDK ${SendBirdCall.VERSION}"


    private fun showLeaveGroupAlert() {
        val alertDialog = AlertDialog.Builder(mActivity).create()
        alertDialog.setTitle(getString(R.string.leave))
        alertDialog.setMessage(getString(R.string.leaveGroup))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.yes)
        ) { dialog: DialogInterface, _: Int ->
            hitLeaveGroupApi()
            dialog.dismiss()
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.no)
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun showDeleteGroupAlert() {
        val alertDialog = AlertDialog.Builder(mActivity).create()
        alertDialog.setTitle(getString(R.string.delete))
        alertDialog.setMessage(getString(R.string.deleteGroup))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.yes)
        ) { dialog: DialogInterface, _: Int ->
            hitDeleteGroupApi()
            dialog.dismiss()
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.no)
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun hitDeleteGroupApi() {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("groupId", groupId)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.deleteApi(
            mActivity,
            AppUrls.deleteGroup,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseDeleteGroupJson(response)
                }

                override fun OnFail(response: String) {}
            })
    }

    private fun parseDeleteGroupJson(response: JSONObject) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                //send broadcast message

                finish()
            } else {
                AppUtils.showMessageDialog(
                    mActivity, getString(R.string.friends),
                    jsonObject.getString(AppConstants.resMsg), 2
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    ///leave group
    private fun hitLeaveGroupApi() {

        val jsonObject = JSONObject()
        val json = JSONObject()

        try {
            jsonObject.put("groupId", groupId)

            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity, AppUrls.leaveGroup, json, true, true,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJson(response)

                }

                override fun OnFail(response: String?) {

                }
            });
    }

    private fun parseJson(response: JSONObject?) {

        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)
            if (jsonObject!!.getString(AppConstants.resCode) == "1") {

                leaveStatus = "false"
                if (leaveStatus == "false") {
                    binding.chatInputView.visibility = View.GONE
                }
                finish()
            } else {

                // AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg) ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        adapter.notifyDataSetChanged()

    }

   //update group name

    private fun hitUpdateGroupNameApi(groupName: String) {

        val jsonObject = JSONObject()
        val json = JSONObject()

        try {
            jsonObject.put("groupId", groupId)
            jsonObject.put("name", groupName)

            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.putApi(
            mActivity, AppUrls.updateGroupName, json, true, true,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJsonUpdateGroupName(response)

                }

                override fun OnFail(response: String?) {

                }
            })
    }
    private fun parseJsonUpdateGroupName(response: JSONObject?) {

        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)
            if (jsonObject!!.getString(AppConstants.resCode) == "1") {
                val job = jsonObject.getJSONObject("data")
                channelTitle = job.getString("name")
                binding.header.tvHeader.text = channelTitle
                if (bottomSheetDialog != null) {
                    bottomSheetDialog!!.dismiss()
                }
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg) ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        adapter.notifyDataSetChanged()

    }

    private fun showDialogUpdateGroupName(mActivity: Activity, title: String?) {
        bottomSheetDialog = BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.dialog_message_group_name)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.setCanceledOnTouchOutside(true)
        bottomSheetDialog!!.show()
        val tvTitle: TextView? = bottomSheetDialog!!.findViewById(R.id.tvTitle)
        val etGroupName: EditText? = bottomSheetDialog!!.findViewById(R.id.etGroupName)
        val tvContinue: TextView? = bottomSheetDialog!!.findViewById(R.id.tvContinue)
        tvTitle!!.text = getString(R.string.update_channel_name)
        etGroupName!!.setText(title)
        tvContinue!!.setOnClickListener { v: View? ->
            if (etGroupName?.text.toString().isEmpty()) {
                AppUtils.showToastSort(mActivity, getString(R.string.please_enter_group_name))
            } else {
                hitUpdateGroupNameApi(etGroupName?.text.toString())
            }
        }
    }


    private fun hitNotifyApi(type: String, message: String) {

        val jsonObject = JSONObject()
        val json = JSONObject()
        jsonObject.put("senderId", groupId)
        jsonObject.put("message", message)
        jsonObject.put("name", channelTitle)
       // jsonObject.put("type", type)
        jsonObject.put("messageType", "2")
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
                    parseJsonCount(response!!,"Notify")
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
            jsonObject.put("senderId", "")

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
                    parseJsonCount(response,"")
                }

                override fun OnFail(response: String) {}
            })
    }
    private fun parseJsonCount(response: JSONObject,notifys: String) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
              if (notifys.equals("Notify"))
              {
                  hitReadCountApi()
              }
                else
              {

              }

            } else {

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}