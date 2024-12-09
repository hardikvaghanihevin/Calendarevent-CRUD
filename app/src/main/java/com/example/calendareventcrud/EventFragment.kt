package com.example.calendareventcrud

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calendareventcrud.CalendarUtils.getAllCalendarEvents
import com.example.calendareventcrud.CalendarUtils.getEvents
import com.example.calendareventcrud.CalendarUtils.getPersonalAndGlobalEvents
import com.example.calendareventcrud.MyNavigation.navOptions
import com.example.calendareventcrud.databinding.FragmentEventBinding

class EventFragment : Fragment(R.layout.fragment_event) {
    private val TAG = BASE_TAG + EventFragment::class.java.simpleName

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventBinding.bind(view)

        binding.fab.setOnClickListener { view ->
            Log.i(TAG, "onViewCreated: clicked fab:")
            findNavController().navigate(R.id.newEventFragment, null, navOptions)
        }
        val event = getAllCalendarEvents(requireContext())
//        val event = getPersonalAndGlobalEvents(requireContext())
        val eventJson = GsonUtil.toJson(event)
        LogUtil.logLongMessage(TAG, "onViewCreated: $eventJson")

        binding.apply {
            //region Event handlers
            //endregion
            rvEvent.layoutManager = LinearLayoutManager(requireContext())
            rvEvent.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = 0
                    outRect.bottom = 0
                }
            })
            rvEvent.setHasFixedSize(true)
            eventAdapter = EventAdapter(emptyList<CalendarEvent>())
            binding.rvEvent.adapter = eventAdapter
            eventAdapter.configureCustomView {event:CalendarEvent->
                // got event update
                Log.e(TAG, "onViewCreated: $event" )
            }
        }
        if (event.isNotEmpty())
        {
            binding.rvEvent.visibility = View.VISIBLE
            binding.includedProgressLayout.progressBar.visibility = View.GONE
            binding.tvNotify.visibility = View.GONE
        } else {
            binding.rvEvent.visibility = View.GONE
            binding.includedProgressLayout.progressBar.visibility = View.GONE
            binding.tvNotify.visibility = View.VISIBLE
        }


        val data: List<CalendarEvent> = event.filter { it.calendarId != 1L }
        eventAdapter.updateData(data)

        eventAdapter.configureCustomView { val i = deleteEvent(requireContext(),it.id!!)
            Log.d(TAG, "onViewCreated: $i")
            eventAdapter.updateData(data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}