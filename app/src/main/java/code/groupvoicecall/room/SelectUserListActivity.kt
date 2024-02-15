package code.groupvoicecall.room

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.groupvoicecall.main.DashboardViewModel
import code.groupvoicecall.preview.PreviewActivity
import code.groupvoicecall.util.EXTRA_ENTER_ERROR_CODE
import code.groupvoicecall.util.EXTRA_ENTER_ERROR_MESSAGE
import code.groupvoicecall.util.EXTRA_IS_NEWLY_CREATED
import code.groupvoicecall.util.EXTRA_ROOM_ID
import code.groupvoicecall.util.REQUEST_CODE_PREVIEW
import code.groupvoicecall.util.RESULT_ENTER_FAIL
import code.groupvoicecall.util.Status
import code.groupvoicecall.util.UNKNOWN_SENDBIRD_ERROR
import code.groupvoicecall.util.hideKeyboard
import code.groupvoicecall.util.showAlertDialog
import code.utils.AppConstants
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivitySelectUserListBinding
import com.sendbird.calls.SendBirdError
import com.sendbird.chat.module.utils.showToast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SelectUserListActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySelectUserListBinding
    private lateinit var madapter: Adapter
    private val viewModel: DashboardViewModel = DashboardViewModel()
    var arrayList = ArrayList<HashMap<String, String>>()

    companion object {
        lateinit var activity: SelectUserListActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inite()
        activity = this
    }

    private fun inite() {
        binding.ivBack.setOnClickListener(this)
        binding.btnCreateRoom.setOnClickListener(this)
        binding.tvHeader.text = getString(R.string.user_list)
        madapter = Adapter(arrayList)
        binding.rvUserList.adapter = madapter
        setViewEventListeners()
        observeViewModel()
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

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                onBackPressed()
            }

            binding.btnCreateRoom -> {
                if (madapter.selectUserIdSets.isEmpty()) {
                    showToast(R.string.select_user_msg)
                    return
                } else {
                    viewModel.createAndEnterRoom()

                }
            }

        }
    }

    inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<Adapter.MyViewHolder?>() {
        val selectUserIdSets: MutableSet<String> = mutableSetOf()
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_item_select_user, viewGroup, false)
            return MyViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemData = data[position]
            holder.textviewName.text = itemData["name"]
            holder.ivProfile.setImageResource(R.drawable.ic_user_default)
            AppUtils.loadPicassoImage(itemData["profileImage"], holder.ivProfile)

//            holder.itemView.setOnClickListener {
//               // hitAddUserInGroup(itemData["userId"].toString())
//                viewModel.createAndEnterRoom()
//            }
            holder.itemView.setOnClickListener {
                if (!selectUserIdSets.contains(itemData["userId"])) {
                    // If not in the set, add it.
                    itemData["userId"]?.let { it1 -> selectUserIdSets.add("$it1") }
                    holder.checkBox.isChecked = true
                    holder.checkBox.isEnabled = false
                } else {
                    // If already in the set, remove it (to toggle selection).
                    selectUserIdSets.remove("${itemData["userId"]}")
                    holder.checkBox.isChecked = false
                    holder.checkBox.isEnabled = true
                }
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivProfile: ImageView
            var textviewName: TextView
            var checkBox: CheckBox

            init {
                ivProfile = itemView.findViewById(R.id.imageview_profile)
                textviewName = itemView.findViewById(R.id.textview_name)
                checkBox = itemView.findViewById(R.id.checkbox_user_select)
            }
        }
    }


    private fun setViewEventListeners() {
        binding.lnLayout.setOnClickListener {
            mActivity?.hideKeyboard()
        }
    }

    private fun observeViewModel() {
        viewModel.createdRoomId.observe(mActivity) { resource ->
            Log.d("DashboardFragment", "observe() resource: $resource")
            when (resource.status) {
                Status.LOADING -> {
                    // TODO : show loading view
                }

                Status.SUCCESS -> resource.data?.let {
                    hitCreateRoomApi(it)
                }

                Status.ERROR -> {
                    val message = if (resource?.errorCode == SendBirdError.ERR_INVALID_PARAMS) {
                        getString(R.string.dashboard_invalid_room_params)
                    } else {
                        resource?.message
                    }
                    mActivity?.showAlertDialog(
                        getString(R.string.dashboard_can_not_create_room),
                        message ?: UNKNOWN_SENDBIRD_ERROR
                    )
                }
            }
        }

        viewModel.fetchedRoomId.observe(mActivity) { resource ->
            Log.d("DashboardFragment", "observe() resource: $resource")
            when (resource.status) {
                Status.LOADING -> {
                    // TODO : show loading view
                }

                Status.SUCCESS -> resource.data?.let { goToPreviewActivity(it) }
                Status.ERROR -> {
                    mActivity?.showAlertDialog(
                        getString(R.string.dashboard_incorrect_room_id),
                        if (resource?.errorCode == 400200) {
                            getString(R.string.dashboard_incorrect_room_id_body)
                        } else {
                            resource?.message ?: UNKNOWN_SENDBIRD_ERROR
                        }
                    )
                }
            }
        }
    }

    private fun goToPreviewActivity(roomId: String) {
        val intent = Intent(mActivity, PreviewActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, roomId)
        }
        startActivityForResult(intent, REQUEST_CODE_PREVIEW)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PREVIEW && resultCode == RESULT_ENTER_FAIL) {
            val errorCode = data?.getIntExtra(EXTRA_ENTER_ERROR_CODE, -1)
            val errorMessage =
                if (errorCode == SendBirdError.ERR_PARTICIPANTS_LIMIT_EXCEEDED_IN_ROOM) {
                    getString(R.string.dashboard_can_not_enter_room_max_participants_count_exceeded)
                } else {
                    data?.getStringExtra(EXTRA_ENTER_ERROR_MESSAGE)
                } ?: UNKNOWN_SENDBIRD_ERROR

            mActivity?.showAlertDialog(
                getString(R.string.dashboard_can_not_enter_room),
                errorMessage
            )
        }
    }

    private fun hitCreateRoomApi(roomId: String) {
        val jsonObject = JSONObject()
        val json = JSONObject()
        // Convert the MutableSet to a JSONArray
        val jsonArray = JSONArray(madapter.selectUserIdSets)
        try {
            jsonObject.put("groupId", roomId)
            jsonObject.put("members", jsonArray)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity,
            AppUrls.createGroupVoiceCall,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJsonObject(response, roomId)
                }

                override fun OnFail(response: String) {
                    AppUtils.showToastSort(mActivity, response)
                }
            })
    }

    private fun parseJsonObject(response: JSONObject, roomId: String) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val data = jsonObject.getJSONObject("data")
                val intent = Intent(mActivity, RoomActivity::class.java).apply {
                    putExtra(EXTRA_ROOM_ID, roomId)
                    putExtra(EXTRA_IS_NEWLY_CREATED, true)
                }
                startActivity(intent)
               finish()
            } else AppUtils.showMessageDialog(
                mActivity,
                getString(R.string.create_group),
                jsonObject.getString(AppConstants.resMsg),
                2
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        hitGetFriendListApi()
        super.onResume()
    }
}