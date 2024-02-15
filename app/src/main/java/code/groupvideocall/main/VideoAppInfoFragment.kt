package code.groupvideocall.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.hathme.android.R
import com.hathme.android.databinding.FragmentAppInfoVideoBinding
import com.sendbird.calls.SendBirdCall

class VideoAppInfoFragment : Fragment() {
    lateinit var binding: FragmentAppInfoVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_info_video, container, false)
        binding.appInfoTextViewId.text = SendBirdCall.applicationId
        binding.appInfoImageViewLeftArrow.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}