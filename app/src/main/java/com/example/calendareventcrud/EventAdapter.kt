package com.example.calendareventcrud

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calendareventcrud.databinding.ItemEventLayoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private var list: List<CalendarEvent>):
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    private final val TAG = BASE_TAG + EventAdapter::class.java.simpleName

    init { for (i in list){ //Log.e(TAG, "init $i" )
    }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<CalendarEvent>) {
        Log.d(TAG, "updateData: ")
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        binding.root.apply {
            layoutParams = lp
        }
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position = position)
    }

    inner class ViewHolder(private val binding: ItemEventLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        private val layout = binding.root

        @SuppressLint("SetTextI18n")
        fun bind(event: CalendarEvent, position: Int){
            Log.e(TAG, "bind: $event", )
            binding.apply {
                eventDate.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(event.startTime))
                eventTitle.text = event.title
                eventTimePeriod.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(event.endTime))

                this.itemEventLayout.setOnClickListener { configureCalendarEventCallback?.invoke(event) }
            }
        }
    }

    private var configureCalendarEventCallback: ((CalendarEvent) -> Unit)? = null
    fun configureCustomView(callback: (CalendarEvent) -> Unit) {
        this.configureCalendarEventCallback = callback
    }
}