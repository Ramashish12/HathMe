package code.groupchatting.groupchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import code.groupchatting.adapter.AdapterUsreList
import code.utils.AppConstants
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hathme.android.R
import com.hathme.android.databinding.ActivitySelectUserBinding
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.ApplicationUserListQueryParams
import com.sendbird.android.params.GroupChannelCreateParams
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class SelectUserActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var madapter: AdapterUsreList
    private lateinit var userName: List<String>
    var bottomSheetDialog: BottomSheetDialog? = null
    private var userListQuery = SendbirdChat.createApplicationUserListQuery(
        ApplicationUserListQueryParams()
    )
    var memberID = ""
    var arrayList = ArrayList<HashMap<String, String>>()
    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    private fun init() {
        binding.header.tvHeader.text = getString(R.string.select_user)
        binding.header.tvTitle.visibility = View.VISIBLE
        binding.header.ivBack.setOnClickListener(this)
        binding.header.tvTitle.setOnClickListener(this)
        isCreateMode = intent.getBooleanExtra(Constants.INTENT_KEY_SELECT_USER_MODE_CREATE,true)
        if (isCreateMode) {
           binding.header.tvTitle.text= getString(R.string.create)
        } else {
            binding.header.tvTitle.text=  getString(R.string.select)
        }
        hitGetFriendListApi()
        madapter = AdapterUsreList(
            arrayList, { _, _ -> },
            true
        )
        binding.recyclerviewUser.adapter = madapter
    }

    private fun initRecyclerView() {
//        adapter = SelectUserAdapter(
//            { _, _ -> },
//            true,
//            intent.getStringArrayListExtra(Constants.INTENT_KEY_SELECT_USER),
//            intent.getStringArrayListExtra(Constants.INTENT_KEY_BASE_USER)
//        )
        binding.recyclerviewUser.adapter = madapter
        binding.recyclerviewUser.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
        binding.recyclerviewUser.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadNextUsers()
                }
            }
        })
    }

    private fun loadNextUsers() {
        if (userListQuery.hasNext) {
            userListQuery.next { users, e ->
                if (e != null) {
                    showToast("Loading.....")
                    return@next
                }
                if (!users.isNullOrEmpty()) {
                   // madapter.addUsers(users)
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.select)
        item.title =
            if (isCreateMode) {
                getString(R.string.create)
            } else {
                getString(R.string.select)
            }
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.select -> {
                if (isCreateMode)
                {

                    if (madapter.selectUserIdSet.isEmpty()) {
                        AppUtils.showToastSort(mActivity, getString(R.string.select_user))
                    } else {

                        showDialog(mActivity, getString(R.string.group_name))
                    }

                    //
                }
                else {
                    selectUser()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createChannel(groupName: String) {

        if (madapter.selectUserIdSet.isEmpty()) {
            showToast(R.string.select_user_msg)
            return
        }
        val params = GroupChannelCreateParams()
            .apply {
                userIds = madapter.selectUserIdSet.toList()
            }
        GroupChannel.createChannel(params) createChannelLabel@{ groupChannel, e ->
            if (e != null) {
                showToast("${e.message}")
                return@createChannelLabel
            }
            if (groupChannel != null) {
               hitCreateGroupApi(groupName,groupName,groupChannel.url)
            }
        }
    }

    private fun selectUser() {
        val intent = intent
        val arrayList = arrayListOf<String>()
        arrayList.addAll(madapter.selectUserIdSet)
        intent.putExtra(Constants.INTENT_KEY_SELECT_USER, arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }


    private fun hitGetFriendListApi() {

        WebServices.getApi(mActivity, AppUrls.myFriends, true, true, object : WebServicesCallback {
            override fun OnJsonSuccess(response: JSONObject) {
                parseFriendListJson(response)
            }

            override fun OnFail(response: String) {}
        })
    }

    private fun parseFriendListJson(response: JSONObject) {
        arrayList.clear()
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val jsonArray = jsonObject.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap["userId"] = jsonObject1.getString("userId")
                    hashMap["name"] = jsonObject1.getString("name")
                    hashMap["profileImage"] = jsonObject1.getString("profileImage")
                    hashMap["mutualFriend"] = jsonObject1.getString("mutualFriend")
                    hashMap["count"] = jsonObject1.getString("count")
                    hashMap["userChatId"] = jsonObject1.getString("userChatId")
                    hashMap["AverageRating"] = jsonObject1.getString("AverageRating")
                    hashMap["selfRating"] = jsonObject1.getString("selfRating")
                    hashMap["remark"] = jsonObject1.getString("remark")
                    hashMap["totalRating"] = jsonObject1.getString("totalRating")
                    hashMap["channelName"] = jsonObject1.getString("channelName")
                    hashMap["channelUrl"] = jsonObject1.getString("channelUrl")
                    hashMap["type"] = "1"
                    arrayList.add(hashMap)
                }
            }
           } catch (e: JSONException) {
            e.printStackTrace()
        }
        madapter.notifyDataSetChanged()
    }

   private fun showDialog(mActivity: Activity, title: String?) {
        bottomSheetDialog = BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme)
        bottomSheetDialog!!.setContentView(R.layout.dialog_message_group_name)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.setCanceledOnTouchOutside(true)
        bottomSheetDialog!!.show()
        val tvTitle: TextView? = bottomSheetDialog!!.findViewById(R.id.tvTitle)
        val etGroupName: EditText? = bottomSheetDialog!!.findViewById(R.id.etGroupName)
        val tvContinue: TextView? = bottomSheetDialog!!.findViewById(R.id.tvContinue)
        tvTitle!!.text = title

        tvContinue!!.setOnClickListener { v: View? ->
            if (etGroupName?.text.toString().isEmpty()) {
                AppUtils.showToastSort(mActivity, getString(R.string.please_enter_group_name))
            } else {
                createChannel(etGroupName?.text.toString())
            }
        }
    }

    //create group
    private fun hitCreateGroupApi(groupName: String,channelName: String,channelUrl: String) {
        val jsonObject = JSONObject()
        val json = JSONObject()
        // Convert the MutableSet to a JSONArray
        val jsonArray = JSONArray(madapter.selectUserIdSet)


        try {
            jsonObject.put("name", groupName)
            jsonObject.put("members", jsonArray)
            jsonObject.put("channelUrl", channelUrl)
            jsonObject.put("channelName", channelName)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity,
            AppUrls.createGroup,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJsonObject(response,groupName,channelUrl)
                }

                override fun OnFail(response: String) {
                    AppUtils.showToastSort(mActivity,response)
                }
            })
    }

    private fun parseJsonObject(response: JSONObject,groupName:String,channelUrl:String) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val data = jsonObject.getJSONObject("data")
                val intent = Intent(mActivity, GroupChannelChatActivity::class.java)
                intent.putExtra("channelURL", channelUrl)
                intent.putExtra("channelName", groupName)
                intent.putExtra("groupId", data.getString("_id"))
                intent.putExtra("leaveStatus", "true")
                intent.putExtra("isAdmin", "true")
                startActivity(intent)
                finish()
                if (bottomSheetDialog != null) {
                    bottomSheetDialog!!.dismiss()
                }
            } else AppUtils.showMessageDialog(
                mActivity, getString(R.string.create_group), jsonObject.getString(AppConstants.resMsg), 2
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    override fun onBackPressed() {
        if (bottomSheetDialog!=null)
        {
            if (bottomSheetDialog!!.isShowing)
            {
                bottomSheetDialog!!.dismiss()
            }
            else
            {
                finish()
            }
        }
        else
        {
            finish()
        }
    }

    override fun onClick(v: View?) {
        when(v)
        {
           binding.header.ivBack-> {
               finish()
           }
           binding.header.tvTitle->
           {
               if (isCreateMode)
               {

                   if (madapter.selectUserIdSet.isEmpty()) {
                       AppUtils.showToastSort(mActivity, getString(R.string.select_user))
                   } else {
                       showDialog(mActivity, getString(R.string.group_name))
                       //hitCreateGroupApi("abcd","abcd","abcd")
                   }

               }
               else {
                   selectUser()
               }
           }
        }
    }
}