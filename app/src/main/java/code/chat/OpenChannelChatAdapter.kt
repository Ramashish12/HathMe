package code.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hathme.android.databinding.*
import com.sendbird.android.SendbirdChat
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.chat.module.utils.ListUtils
import com.sendbird.chat.module.utils.toTime

class OpenChannelChatAdapter(
    private val longClickListener: OnItemLongClickListener,
    private val onItemClickListener: OnItemClickListener,
    private val failedItemClickListener: OnFailedItemClickListener
) : ListAdapter<BaseMessage, RecyclerView.ViewHolder>(diffCallback) {

    fun interface OnItemLongClickListener {
        fun onItemLongClick(baseMessage: BaseMessage, view: View, position: Int)
    }
    fun interface OnItemClickListener {
        fun onItemClick(baseMessage: BaseMessage, view: View, position: Int)
    }

    fun interface OnFailedItemClickListener {
        fun onItemClick(baseMessage: BaseMessage)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BaseMessage>() {
            override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return if (oldItem.messageId > 0 && newItem.messageId > 0) {
                    oldItem.messageId == newItem.messageId
                } else {
                    oldItem.requestId == newItem.requestId
                }
            }

            override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return oldItem.message == newItem.message
                        && oldItem.sender?.nickname == newItem.sender?.nickname
                        && oldItem.sendingStatus == newItem.sendingStatus
                        && oldItem.updatedAt == newItem.updatedAt
            }
        }
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
        const val VIEW_TYPE_SEND_IMAGE = 2
        const val VIEW_TYPE_RECEIVE_IMAGE = 3
        const val VIEW_TYPE_SEND_FILE = 4
        const val VIEW_TYPE_RECEIVE_FILE = 5
    }

    private val baseMessageList = mutableListOf<BaseMessage>()
    private val pendingMessageList = mutableListOf<BaseMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_SEND -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE -> return GroupChatReceiveViewHolder(
                ListItemChatReceiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_SEND_IMAGE -> return GroupChatImageSendViewHolder(
                ListItemChatImageSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_RECEIVE_IMAGE -> return GroupChatImageReceiveViewHolder(
                ListItemChatImageReceiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_RECEIVE_FILE -> return FileReceiveViewHolder(
                ListItemChatFileReceiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_SEND_FILE -> return FileSendViewHolder(
                ListItemChatFileSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> return GroupChatSendViewHolder(
                ListItemChatSendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupChatSendViewHolder -> holder.bind(getItem(position))

            is GroupChatReceiveViewHolder -> holder.bind(getItem(position))

            is GroupChatImageSendViewHolder -> holder.bind(getItem(position) as FileMessage)

            is GroupChatImageReceiveViewHolder -> holder.bind(getItem(position) as FileMessage)

            is FileReceiveViewHolder -> holder.bind(getItem(position) as FileMessage)

            is FileSendViewHolder -> holder.bind(getItem(position) as FileMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentUser = SendbirdChat.currentUser
        val message = getItem(position)
        return if (currentUser != null) {
            if (message.sender?.userId == currentUser.userId) {
                if (message is FileMessage) {
                    if (message.type.contains("image")) {
                        VIEW_TYPE_SEND_IMAGE
                    } else {
                        VIEW_TYPE_SEND_FILE
                    }
                } else {
                    VIEW_TYPE_SEND
                }
            } else {
                if (message is FileMessage) {
                    if (message.type.contains("image")) {
                        VIEW_TYPE_RECEIVE_IMAGE
                    } else {
                        VIEW_TYPE_RECEIVE_FILE
                    }
                } else {
                    VIEW_TYPE_RECEIVE
                }
            }
        } else {
            if (message is FileMessage) {
                if (message.type.contains("image")) {
                    VIEW_TYPE_RECEIVE_IMAGE
                } else {
                    VIEW_TYPE_RECEIVE_FILE
                }
            } else {
                VIEW_TYPE_RECEIVE
            }
        }
    }

    fun addPendingMessage(message: BaseMessage?) {
        if (message != null) {
            pendingMessageList.add(message)
            mergeList()
        }
    }

    fun updateSucceedMessage(message: BaseMessage?) {
        if (message != null) {
            pendingMessageList.removeIf { it.requestId == message.requestId }
            baseMessageList.add(message)
            mergeList()
        }
    }

    fun updatePendingMessage(message: BaseMessage?) {
        if (message != null) {
            pendingMessageList.forEachIndexed { index, baseMessage ->
                if (baseMessage.requestId == message.requestId) {
                    pendingMessageList[index] = message
                    return@forEachIndexed
                }
            }
            mergeList()
        }
    }

    fun deletePendingMessage(message: BaseMessage?) {
        if (message != null) {
            pendingMessageList.removeIf { it.requestId == message.requestId }
            mergeList()
        }
    }

    fun addMessage(message: BaseMessage?) {
        if (message != null) {
            baseMessageList.add(message)
            mergeList()
        }
    }

    fun addNextMessages(messages: List<BaseMessage>?) {
        if (!messages.isNullOrEmpty()) {
            messages.forEach {
                ListUtils.findAddMessageIndex(baseMessageList, it).apply {
                    if (this > -1) {
                        baseMessageList.add(this, it)
                    }
                }
            }
            mergeList()
        }
    }

    fun addPreviousMessages(messages: List<BaseMessage>?) {
        if (!messages.isNullOrEmpty()) {
            baseMessageList.addAll(0, messages)
            mergeList()
        }
    }

    fun updateMessages(messages: List<BaseMessage>?) {
        if (!messages.isNullOrEmpty()) {
            val idIndexMap =
                baseMessageList.mapIndexed { index, baseMessage ->
                    baseMessage.messageId to index
                }.toMap()
            messages.forEach {
                idIndexMap[it.messageId]?.let { index ->
                    baseMessageList[index] = it
                }
            }
            mergeList()
        }
    }

    fun deleteMessages(messageIds: List<Long>?) {
        if (!messageIds.isNullOrEmpty()) {
            baseMessageList.removeAll { it.messageId in messageIds }
            mergeList()
        }
    }

    private fun mergeList() = submitList(baseMessageList + pendingMessageList)

    open inner class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnLongClickListener {
                longClickListener.onItemLongClick(getItem(adapterPosition), it, adapterPosition)
                return@setOnLongClickListener false
            }

            itemView.setOnClickListener{
                onItemClickListener.onItemClick(getItem(adapterPosition), it, adapterPosition)

            }
        }
    }

    inner class GroupChatSendViewHolder(private val binding: ListItemChatSendBinding) :
        BaseViewHolder(binding) {
        fun bind(message: BaseMessage) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                binding.progressSend.visibility = View.GONE
                binding.chatErrorButton.visibility = View.GONE
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
                    binding.progressSend.visibility = View.VISIBLE
                    binding.chatErrorButton.visibility = View.GONE
                } else {
                    binding.progressSend.visibility = View.GONE
                    binding.chatErrorButton.visibility = View.VISIBLE
                    binding.chatErrorButton.setOnClickListener {
                        failedItemClickListener.onItemClick(message)
                    }
                }
            }
            binding.chatBubbleSend.setText(message.message)
        }
    }

    inner class GroupChatReceiveViewHolder(private val binding: ListItemChatReceiveBinding) :
        BaseViewHolder(binding) {
        fun bind(message: BaseMessage) {
            binding.chatBubbleReceive.setText(message.message)
            binding.textviewTime.text = message.createdAt.toTime()
            binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
        }
    }

    inner class GroupChatImageSendViewHolder(private val binding: ListItemChatImageSendBinding) :
        BaseViewHolder(binding) {
        fun bind(message: FileMessage) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {

                binding.chatBubbleImageSend.setImageUrl(message.url, message.plainUrl)
                binding.progressImageSend.visibility = View.GONE
                binding.chatImageErrorButton.visibility = View.GONE
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.chatBubbleImageSend.setImageFile(message.messageCreateParams?.file)
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
                    binding.progressImageSend.visibility = View.VISIBLE
                    binding.chatImageErrorButton.visibility = View.GONE
                } else {
                    binding.progressImageSend.visibility = View.GONE
                    binding.chatImageErrorButton.visibility = View.VISIBLE
                    binding.chatImageErrorButton.setOnClickListener {
                        failedItemClickListener.onItemClick(message)
                    }
                }
            }
        }
    }

    inner class GroupChatImageReceiveViewHolder(private val binding: ListItemChatImageReceiveBinding) :
        BaseViewHolder(binding) {
        fun bind(message: FileMessage) {
            binding.chatBubbleImageReceive.setImageUrl(message.url, message.plainUrl)
            binding.textviewTime.text = message.createdAt.toTime()
            binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
        }
    }

    inner class FileReceiveViewHolder(private val binding: ListItemChatFileReceiveBinding) :
        BaseViewHolder(binding) {
        fun bind(message: FileMessage) {
            binding.chatBubbleReceive.setText(message.name)
            binding.textviewTime.text = message.createdAt.toTime()
            binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
        }
    }

    inner class FileSendViewHolder(private val binding: ListItemChatFileSendBinding) :
        BaseViewHolder(binding) {
        fun bind(message: FileMessage) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                binding.progressSend.visibility = View.GONE
                binding.chatErrorButton.visibility = View.GONE
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
                    binding.progressSend.visibility = View.VISIBLE
                    binding.chatErrorButton.visibility = View.GONE
                } else {
                    binding.progressSend.visibility = View.GONE
                    binding.chatErrorButton.visibility = View.VISIBLE
                    binding.chatErrorButton.setOnClickListener {
                        failedItemClickListener.onItemClick(message)
                    }
                }
            }
            binding.chatBubbleSend.setText(message.name)
        }
    }
}