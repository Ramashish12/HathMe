package code.groupchatting.groupchannel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.utils.AppConstants
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityInviteMembersListBinding
import com.sendbird.chat.module.utils.Constants
import com.sendbird.chat.module.utils.showToast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class InviteMembersListActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityInviteMembersListBinding
    private var arrayList = ArrayList<HashMap<String, String>>()
    private lateinit var adapter: Adapter
    private var channelUrl: String? = ""
    private var channelTitle: String? = ""
    private var groupId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteMembersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inite()
    }

    private fun inite() {
        binding.header.tvHeader.text = getString(R.string.invite_members)
        binding.header.tvTitle.setOnClickListener(this)
        binding.header.tvTitle.text = getString(R.string.add)
        binding.header.ivBack.setOnClickListener(this)

        val intent = intent
        groupId = intent.getStringExtra("groupId") ?: ""
        channelUrl = intent.getStringExtra("channelURL") ?: ""
        channelTitle = intent.getStringExtra("channelName") ?: ""

        adapter = Adapter(arrayList)
        binding.recyclerviewMember.adapter = adapter
    }

    private fun hitChatInviteMemberListApi() {

        val jsonObject = JSONObject()
        val json = JSONObject()

        try {
            jsonObject.put("groupId", groupId)

            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        WebServices.postApi(
            mActivity, AppUrls.getInviteMemberList, json, true, true,
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
            // val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (response!!.getString("resCode") == "1") {

                val jsonArray = response.getJSONArray("data")

                for (i in 0 until jsonArray.length()) {
                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val hashMap = HashMap<String, String>()
                    if (jsonObject1.getBoolean("isInGroup").toString() == "false") {
                        hashMap["_id"] = jsonObject1.getString("userId")
                        hashMap["name"] = jsonObject1.getString("name")
                        hashMap["isInGroup"] = jsonObject1.getBoolean("isInGroup").toString()
                        hashMap["profileImage"] = jsonObject1.getString("profileImage")
                        arrayList.add(hashMap)
                        binding.header.tvTitle.visibility = View.VISIBLE
                    }
                    else
                    {
                        AppUtils.showMessageDialog(
                            mActivity, getString(R.string.invite_members),
                            getString(R.string.members_not_found), 1
                        )
                        binding.header.tvTitle.visibility = View.GONE
                    }

                }


            } else {
                //AppUtils.showToastSort(mActivity, response.getString("resMsg") ?: "")
                binding.header.tvTitle.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        adapter.notifyDataSetChanged()

    }

    private inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<Adapter.MyViewHolder?>() {
        val selectUserIdSet: MutableSet<String> = mutableSetOf()
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_item_select_user, viewGroup, false)
            return MyViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            holder.tvName.text = data[position]["name"]
            if (data[position]["profileImage"] == "" || data[position]["profileImage"] == null || data[position]["profileImage"] == "null") {

            } else {
                AppUtils.loadPicassoImage(data[position]["profileImage"], holder.ivImage)
            }
            holder.itemView.setOnClickListener {
                if (!selectUserIdSet.contains(data[position]["_id"])) {
                    // If not in the set, add it.
                    data[position]["_id"]?.let { it1 -> selectUserIdSet.add("$it1") }
                    holder.checkBox.isChecked = true
                    holder.checkBox.isEnabled = false
                } else {
                    // If already in the set, remove it (to toggle selection).
                    selectUserIdSet.remove("${data[position]["_id"]}")
                    holder.checkBox.isChecked = false
                    holder.checkBox.isEnabled = true
                }
            }


        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivImage: ImageView
            var tvName: TextView
            var checkBox: CheckBox

            init {
                ivImage = itemView.findViewById(R.id.imageview_profile)
                tvName = itemView.findViewById(R.id.textview_name)
                checkBox = itemView.findViewById(R.id.checkbox_user_select)
            }
        }
    }

    //add member in group
    private fun hitAddMemberInGroupListApi() {

        val jsonObject = JSONObject()
        val json = JSONObject()
        val jsonArray = JSONArray(adapter.selectUserIdSet)
        try {
            jsonObject.put("groupId", groupId)
            jsonObject.put("members", jsonArray)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.putApi(
            mActivity, AppUrls.addNewGroupMember, json, true, true,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseJsonAddMember(response)

                }

                override fun OnFail(response: String?) {

                }
            });
    }

    private fun parseJsonAddMember(response: JSONObject?) {

        try {
            val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (jsonObject!!.getString(AppConstants.resCode) == "1") {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg))
                selectUser()

            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg) ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        hitChatInviteMemberListApi()

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.ivBack -> {
                onBackPressed()
            }

            binding.header.tvTitle -> {
                if (adapter.selectUserIdSet.isEmpty()) {
                    showToast(R.string.select_user_msg)
                    return
                } else {
                    hitAddMemberInGroupListApi()
                }
            }
        }
    }

    private fun selectUser() {
        val intent = intent
        val arrayList = arrayListOf<String>()
        arrayList.addAll(adapter.selectUserIdSet)
        intent.putExtra(Constants.INTENT_KEY_SELECT_USER, arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }
}