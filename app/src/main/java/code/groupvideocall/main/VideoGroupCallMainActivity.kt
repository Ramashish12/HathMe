package code.groupvideocall.main

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import code.groupvideocall.util.BaseActivity
import code.groupvideocall.util.requestPermissions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hathme.android.R
import com.hathme.android.databinding.ActivityVideoGroupCallMainBinding

class VideoGroupCallMainActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoGroupCallMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_group_call_main)

        val tabLayout = binding.tabLayoutMain
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)

        val viewPager2 = binding.viewPagerMain
        viewPager2.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val iconResourceId = when (position) {
                0 -> R.drawable.icon_rooms
                else -> return@TabLayoutMediator
            }

            tab.setIcon(iconResourceId)
        }.attach()
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

        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    inner class ViewPagerAdapter(
        fragmentActivity: FragmentActivity
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 1
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> VideoDashboardFragment()
                else -> throw IndexOutOfBoundsException()
            }
        }
    }
}
