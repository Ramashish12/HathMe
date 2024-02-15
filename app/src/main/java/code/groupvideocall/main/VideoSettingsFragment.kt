package code.groupvideocall.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import code.activity.MainActivity
import code.groupvideocall.util.Status
import code.groupvideocall.util.dpToPixel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hathme.android.R
import com.hathme.android.databinding.FragmentVideoSettingsBinding
import com.sendbird.calls.SendBirdCall

class VideoSettingsFragment : Fragment() {
    private val viewModel: VideoSettingsViewModel = VideoSettingsViewModel()
    lateinit var binding: FragmentVideoSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_settings, container, false)
        //setViewEventListener()
        setUserInfo()
        observeViewModel()
        return binding.root
    }

    private fun setUserInfo() {
        val user = SendBirdCall.currentUser
        binding.settingsTextViewUserId.text = if (user?.userId.isNullOrEmpty()) {
            getString(R.string.no_nickname)
        } else {
            String.format(getString(R.string.user_id_template), user?.userId)
        }

        binding.settingsTextViewUserName.text = if (user?.nickname.isNullOrEmpty()) {
            getString(R.string.no_nickname)
        } else {
            user?.nickname
        }

        val radius = activity?.dpToPixel(32) ?: 0
        Glide.with(this)
            .load(user?.profileUrl)
            .apply(
                RequestOptions()
                    .transform(RoundedCorners(radius))
                    .error(R.drawable.ic_user_default)
            )
            .into(binding.settingsImageViewProfile)
    }



    private fun observeViewModel() {
        viewModel.deauthenticateLiveData.observe(requireActivity()) {
            when (it.status) {
                Status.SUCCESS -> {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }

                else -> {}
            }
        }
    }
}
