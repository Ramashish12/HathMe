package code.groupchatting.groupchannel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityChatMemberListBinding
import com.sendbird.android.channel.GroupChannel
import com.sendbird.chat.module.utils.showToast
import org.json.JSONException
import org.json.JSONObject

class ChatMemberListActivity : BaseActivity() ,View.OnClickListener{
    private lateinit var binding: ActivityChatMemberListBinding

    private var currentChannel: GroupChannel? = null
    private var channelUrl: String? = ""
    private var channelTitle: String? = ""
    private var groupId: String? = ""
    private var arrayList = ArrayList<HashMap<String, String>>()

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        groupId = intent.getStringExtra("groupId") ?: ""
        channelUrl = intent.getStringExtra("channelURL") ?: ""
        channelTitle = intent.getStringExtra("channelName") ?: ""



        init()
        getGroupChannel()
    }

    private fun init() {
        binding.header.tvHeader.text = getString(R.string.members_list)
        binding.header.ivBack.setOnClickListener(this)
        adapter = Adapter(arrayList)
        binding.recyclerviewMember.adapter = adapter
        hitChatMemberListApi()
    }

    private fun initRecyclerView() {
        //Adapter = ChatMemberListAdapter { _, _ -> currentChannel}
        //binding.recyclerviewMember.adapter = Adapter
        binding.recyclerviewMember.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun getGroupChannel() {
        val url = channelUrl
        if (url.isNullOrBlank()) {
            showToast(getString(R.string.channel_url_error))
            return
        }
        GroupChannel.getChannel(url) { groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                finish()
                return@getChannel
            }
            if (groupChannel != null) {
                currentChannel = groupChannel
                // Adapter.submitList(groupChannel.members)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hitChatMemberListApi() {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("groupId", groupId)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity, AppUrls.getGroupMembers, json, true, true,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJson(response)

                }

                override fun OnFail(response: String?) {

                }
            })
    }

    private fun parseJson(response: JSONObject?) {

        arrayList.clear()
        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (jsonObject!!.getString(AppConstants.resCode) == "1") {

                val jsonArray = jsonObject.getJSONObject("data")
                val jsonArrayGroup = jsonArray.getJSONObject("group")
                val jsonArrayGroupMembers = jsonArrayGroup.getJSONArray("members")
                for (i in 0 until jsonArrayGroupMembers.length()) {
                    val jsonObject1 = jsonArrayGroupMembers.getJSONObject(i)
                    val hashMap = HashMap<String, String>()
                    hashMap["_id"] = jsonObject1.getString("_id")
                    hashMap["name"] = jsonObject1.getString("name")
                    hashMap["isAdminAllow"] = jsonObject1.getBoolean("isAdminAllow").toString()
                    hashMap["profileImage"] = jsonObject1.getString("profileImage")
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

    inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<Adapter.MyViewHolder?>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_item_member, viewGroup, false)
            return MyViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (data[position]["isAdminAllow"]!! == "true")
            {
                holder.tvName.text = data[position]["name"]
                holder.tvIsAdminStatus.visibility = View.VISIBLE
                holder.tvIsAdminStatus.text = getString(R.string.isAdmin)
            }
           else
            {
                holder.tvIsAdminStatus.visibility = View.GONE
                holder.tvName.text = data[position]["name"]
            }
//            if (data[position]["profileImage"]==""||data[position]["profileImage"]==null||data[position]["profileImage"]=="null")
//            {
//
//            }
//            else
//            {
//                AppUtils.loadPicassoImage(data[position]["profileImage"], holder.ivImage)
//            }
            AppUtils.loadPicassoImage(data[position]["profileImage"], holder.ivImage)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivImage: ImageView
            var tvName: TextView
            var tvIsAdminStatus: TextView

            init {
                ivImage = itemView.findViewById(R.id.ivImage)
                tvName = itemView.findViewById(R.id.tvName)
                tvIsAdminStatus = itemView.findViewById(R.id.tvIsAdminStatus)
            }
        }
    }
//    override fun onResume() {
//        super.onResume()
//        hitChatMemberListApi()
//
//    }

    override fun onClick(v: View?) {
        when(v)
        {
            binding.header.ivBack-> {
             onBackPressed()
            }
        }
    }
}