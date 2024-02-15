package code.chat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.groupchatting.groupchannel.GroupChannelChatActivity
import code.groupchatting.groupchannel.SelectUserActivity
import code.livestreaming.LiveListActivity
import code.utils.*
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityChatListBinding
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_ID
import com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_NAME
import org.json.JSONObject

class ChatListActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityChatListBinding

    private var arrayList = ArrayList<HashMap<String, String>>()

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inits()

    }

    private fun inits() {

        binding.header.tvHeader.text = getString(R.string.chat)
        binding.header.ivBack.setOnClickListener { onBackPressed() }
        binding.header.rlGoLive.setOnClickListener(this)
        binding.ivCreateGroup.setOnClickListener(this)
        adapter = Adapter(arrayList)
        binding.rvList.adapter = adapter
    }

    private fun hitChatListApi() {

        WebServices.getApi(
            mActivity,
            AppUrls.GetMssageList,
            false,
            false,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJson(response)

                }

                override fun OnFail(response: String?) {

                }
            });
    }

    private fun parseJson(response: JSONObject?) {
        arrayList.clear()
        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (jsonObject!!.getString(AppConstants.resCode) == "1") {
                val jsonArray = jsonObject.getJSONArray("data")

                for (i in 0 until jsonArray.length()) {

                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val hashMap = HashMap<String, String>()
                    if (jsonObject1.getString("messageType").equals("2")) {
                        hashMap["_id"] = jsonObject1.getString("id")
                        hashMap["name"] = jsonObject1.getString("name")
                        hashMap["status"] = jsonObject1.getString("status")
                        hashMap["creator"] = jsonObject1.getString("creator")
                        hashMap["lastMessage"] = jsonObject1.getString("lastMessage")
                        hashMap["channelName"] = jsonObject1.getString("channelName")
                        hashMap["channelUrl"] = jsonObject1.getString("channelUrl")
                        hashMap["isAdmin"] = jsonObject1.getBoolean("isAdmin").toString()
                        hashMap["leaveGroupStatus"] =
                            jsonObject1.getBoolean("leaveGroupStatus").toString()
                        hashMap["messageType"] = jsonObject1.getString("messageType")
                        if (jsonObject1.has("unreadCount")) {
                            hashMap["unreadCount"] = jsonObject1.getString("unreadCount")
                        } else {
                            hashMap["unreadCount"] = "0"
                        }

                    } else {
                        hashMap["senderId"] = jsonObject1.getString("senderId")
                        hashMap["receiverId"] = jsonObject1.getString("receiverId")
                        hashMap["name"] = jsonObject1.getString("name")
                        hashMap["lastMessage"] = jsonObject1.getString("lastMessage")
                        hashMap["channelName"] = jsonObject1.getString("channelName")
                        hashMap["channelUrl"] = jsonObject1.getString("channelUrl")
                        hashMap["messageType"] = jsonObject1.getString("messageType")
                        hashMap["sendDate"] = jsonObject1.getString("createdAt")
                        hashMap["profile"] = jsonObject1.getString("profile")
                        hashMap["unreadCount"] = jsonObject1.getString("unreadCount")

                    }

                    arrayList.add(hashMap)
                }


            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg) ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        adapter.notifyDataSetChanged()

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.rlGoLive -> {
                val intent = Intent(mActivity, LiveListActivity::class.java)
                startActivity(intent)
            }

            binding.ivCreateGroup -> {
                startActivity(Intent(mActivity, SelectUserActivity::class.java))
                intent.putExtra(
                    Constants.INTENT_KEY_BASE_USER,
                    AppSettings.getString(AppSettings.userId)
                )
                intent.putExtra(Constants.INTENT_KEY_SELECT_USER_MODE_CREATE, true)
            }
        }
    }

    inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<Adapter.MyViewHolder?>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.inflate_chat, viewGroup, false)
            return MyViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (data[position]["messageType"].equals("2")) {
                holder.tvName.text = data[position]["name"]
                holder.tvLastMessage.text = data[position]["lastMessage"]
                holder.rlGroupImage.visibility = View.VISIBLE
                holder.ivImage.visibility = View.GONE
            } else {
                holder.tvName.text = data[position]["name"]
                holder.tvLastMessage.text = data[position]["lastMessage"]
                holder.rlGroupImage.visibility = View.GONE
                holder.ivImage.visibility = View.VISIBLE
                AppUtils.loadPicassoImage(data[position]["profile"], holder.ivImage)
            }
            if (data[position]["unreadCount"].equals("0")) {
                holder.tvUnReadCount.visibility = View.GONE
            } else {
                holder.tvUnReadCount.visibility = View.VISIBLE
                holder.tvUnReadCount.text = data[position]["unreadCount"]
            }

            holder.rlMain.setOnClickListener {
                if (data[position]["messageType"].equals("1")) {
                    val intent = Intent(this@ChatListActivity, ChatActivity::class.java)
                    intent.putExtra(INTENT_KEY_CHANNEL_URL, data[position]["channelUrl"])
                    intent.putExtra(INTENT_KEY_CHANNEL_TITLE, data[position]["channelName"])
                    intent.putExtra(INTENT_KEY_RECEIVER_NAME, data[position]["name"])
                    intent.putExtra("receiverId", data[position]["senderId"])
                    if (AppSettings.getString(AppSettings.userId) == data[position]["senderId"]) {
                        intent.putExtra(INTENT_KEY_RECEIVER_ID, data[position]["receiverId"])
                    } else {
                        intent.putExtra(INTENT_KEY_RECEIVER_ID, data[position]["senderId"])
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(mActivity, GroupChannelChatActivity::class.java)
                    intent.putExtra("channelURL", data[position]["channelUrl"])
                    intent.putExtra("channelName", data[position]["name"])
                    intent.putExtra("groupId", data[position]["_id"])
                    intent.putExtra("leaveStatus", data[position]["leaveGroupStatus"])
                    intent.putExtra("isAdmin", data[position]["isAdmin"])
                    startActivity(intent)
                    // AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),data[position]["channelUrl"],9)
                }

            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var rlMain: RelativeLayout
            var rlGroupImage: RelativeLayout
            var ivImage: ImageView
            var tvName: TextView
            var tvLastMessage: TextView
            var tvTime: TextView
            var tvUnReadCount: TextView

            init {
                rlMain = itemView.findViewById(R.id.rlMain)
                ivImage = itemView.findViewById(R.id.ivImage)
                tvName = itemView.findViewById(R.id.tvName)
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage)
                tvTime = itemView.findViewById(R.id.tvTime)
                rlGroupImage = itemView.findViewById(R.id.rlGroupImage)
                tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(mMessageReceiver, IntentFilter("RefreshDetails"))
        hitChatListApi()
    }

    override fun onPause() {
        unregisterReceiver(mMessageReceiver)
        super.onPause()
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            hitChatListApi()
        }

    }

}