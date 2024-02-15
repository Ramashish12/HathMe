package code.groupvideocall.room

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import code.groupvoicecall.room.ParticipantListFragmentArgs
import com.hathme.android.R
import com.hathme.android.databinding.FragmentVideoParticipantListBinding
import com.sendbird.calls.Room
import com.sendbird.calls.SendBirdCall
import com.sendbird.calls.quickstart.groupcall.room.VideoParticipantListAdapter

class VideoParticipantListFragment : Fragment() {

    lateinit var binding: FragmentVideoParticipantListBinding
    val args: ParticipantListFragmentArgs by navArgs()
    lateinit var room: Room

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_participant_list, container, false)
        room = SendBirdCall.getCachedRoomById(args.roomId) ?: throw IllegalStateException("Fragment $this should have Room instance.")
        initView()
        return binding.root
    }

    private fun initView() {
        binding.participantListTextViewTitle.text = String.format(
            getString(R.string.participant_list_title),
            room.participants.size
        )

        binding.participantListImageViewClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.participantListRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = VideoParticipantListAdapter(room.roomId).apply {
                participants = room.participants
            }
        }
    }
}
