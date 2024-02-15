package code.groupvoicecall.main

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import code.groupvoicecall.util.BaseActivity
import com.google.android.material.tabs.TabLayout
import com.hathme.android.R
import com.hathme.android.databinding.ActivityGroupCallBinding
import code.groupvoicecall.util.requestPermissions
import code.utils.AppSettings

class GroupVoiceCallActivity : BaseActivity() {
    private lateinit var binding: ActivityGroupCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_call)

        val tabLayout = binding.tabLayoutMain
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)

        val viewPager2 = binding.viewPagerMain
        viewPager2.adapter = ViewPagerAdapter(this)

        requestPermissions()
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> tab.setIcon(R.drawable.icon_rooms)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> tab.setIcon(R.drawable.icon_rooms_grey)
            }
        }
        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }

    inner class ViewPagerAdapter(
        fragmentActivity: FragmentActivity
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 1
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                else -> throw IndexOutOfBoundsException()
            }
        }
    }

}
