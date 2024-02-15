package code.groupvoicecall.room

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.groupvoicecall.main.DashboardViewModel
import code.groupvoicecall.preview.PreviewActivity
import code.groupvoicecall.util.EXTRA_IS_NEWLY_CREATED
import code.groupvoicecall.util.EXTRA_ROOM_ID
import code.groupvoicecall.util.REQUEST_CODE_PREVIEW
import code.groupvoicecall.util.Status
import code.groupvoicecall.util.UNKNOWN_SENDBIRD_ERROR
import code.groupvoicecall.util.showAlertDialog
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityInvitationListBinding
import com.hathme.android.databinding.ActivitySelectUserListBinding
import com.sendbird.calls.SendBirdError
import org.json.JSONException
import org.json.JSONObject

class InvitationListActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityInvitationListBinding
    private lateinit var madapter: Adapter
    private val viewModel: DashboardViewModel = DashboardViewModel()
    var arrayList = ArrayList<HashMap<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitationListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()
        inite()
    }

    private fun inite() {
        binding.header.ivBack.setOnClickListener(this)
        binding.header.tvHeader.text = getString(R.string.invitations)

        madapter = Adapter(arrayList)
        binding.rvList.adapter = madapter


    }
    private fun observeViewModel() {
        viewModel.createdRoomId.observe(mActivity) { resource ->
            Log.d("DashboardFragment", "observe() resource: $resource")
            when (resource.status) {
                Status.LOADING -> {
                    // TODO : show loading view
                }
                Status.SUCCESS -> resource.data?.let { goToRoomActivity(it) }
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
    private fun goToRoomActivity(roomId: String) {
        val intent = Intent(mActivity, RoomActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_IS_NEWLY_CREATED, true)
        }

        startActivity(intent)
    }
    private fun goToPreviewActivity(roomId: String) {
        val intent = Intent(mActivity, PreviewActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, roomId)
        }

        startActivityForResult(intent, REQUEST_CODE_PREVIEW)
    }
    private fun hitGetInvitationListApi() {

        WebServices.getApi(mActivity, AppUrls.getGroupVoiceCallList,
            true,
            true, object : WebServicesCallback {
            override fun OnJsonSuccess(response: JSONObject) {
                parseJson(response)
            }

            override fun OnFail(response: String?) {

            }
        })
    }

    private fun parseJson(response: JSONObject) {
        arrayList.clear()
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                val jsonArray = jsonObject.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val hashMap = java.util.HashMap<String, String>()
                    hashMap["_id"] = jsonObject1.getString("_id")
                    hashMap["groupId"] = jsonObject1.getString("groupId")
                    hashMap["status"] = jsonObject1.getString("status")
                    hashMap["creator"] = jsonObject1.getString("creator")

                    arrayList.add(hashMap)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        madapter.notifyDataSetChanged()
    }

    inner class Adapter(var data: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<Adapter.MyViewHolder?>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.row_invitation_list, viewGroup, false)
            return MyViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemData = data[position]
            if (data[position]["creator"].equals(AppSettings.getString(AppSettings.userId)))
            {
              holder.tvName.text = AppSettings.getString(AppSettings.name)+" Invite you for voice calling"
              AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage),holder.ivProfile)
            }
            holder.btnAccept.setOnClickListener(View.OnClickListener {

                itemData["groupId"]?.let { it1 -> viewModel.fetchRoomById(it1) }
            })
            
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivProfile: ImageView
            var tvName: TextView
            var btnAccept: Button
            var btnReject: Button

            init {
                ivProfile = itemView.findViewById(R.id.ivProfile)
                tvName = itemView.findViewById(R.id.tvName)
                btnAccept = itemView.findViewById(R.id.btnAccept)
                btnReject = itemView.findViewById(R.id.btnReject)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v){
            binding.header.ivBack->{
                onBackPressed()
            }
        }
    }

    override fun onResume() {
         hitGetInvitationListApi()
        super.onResume()
    }
}