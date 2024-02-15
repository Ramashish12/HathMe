package code.groupchatting.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.utils.AppUtils
import com.hathme.android.R
import com.sendbird.android.user.User

class AdapterUsreList(private val dataList: ArrayList<HashMap<String,String>>,
                      private val listener: AdapterUsreList.OnItemClickListener?,
                      private val selectMode: Boolean) :
    RecyclerView.Adapter<AdapterUsreList.MyViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(user: User, position: Int)
    }
    val selectUserIdSet: MutableSet<String> = mutableSetOf()
    val arrayListIdSet = ArrayList<String>()
    val selectUserNameSet: MutableSet<String> = mutableSetOf()

    // Create ViewHolder instances and inflate the item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_select_user, parent, false)
        return MyViewHolder(itemView)
    }

    // Bind data to the views in each item
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemData = dataList[position]
        holder.tvName.text = itemData["name"]
        holder.ivImage.setImageResource(R.drawable.ic_user_default)
        AppUtils.loadPicassoImage(itemData["profileImage"],holder.ivImage)
        holder.itemView.setOnClickListener {
            if (!selectUserIdSet.contains(itemData["userId"])) {
                // If not in the set, add it.
                itemData["userId"]?.let { it1 -> selectUserIdSet.add("$it1") }
                holder.checkBox.isChecked = true
                holder.checkBox.isEnabled = false
            } else {
                // If already in the set, remove it (to toggle selection).
                selectUserIdSet.remove("${itemData["userId"]}")
                holder.checkBox.isChecked = false
                holder.checkBox.isEnabled = true
            }
        }

    }

    // Return the number of items in the list
    override fun getItemCount(): Int {
        return dataList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.imageview_profile)
        val tvName: TextView = itemView.findViewById(R.id.textview_name)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_user_select)
        }
}
