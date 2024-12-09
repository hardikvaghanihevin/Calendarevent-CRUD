package com.example.calendareventcrud

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.calendareventcrud.CalendarUtils.addEventToCalendar
import com.example.calendareventcrud.DateUtil.DATE_FORMAT_dd_MMM_yyyy
import com.example.calendareventcrud.DateUtil.TIME_FORMAT_hh_mm_a
import com.example.calendareventcrud.DateUtil.mergeDateAndTime
import com.example.calendareventcrud.DateUtil.splitTimeString
import com.example.calendareventcrud.DateUtil.stringToLong
import com.example.calendareventcrud.databinding.DialogItemDatePickerBinding
import com.example.calendareventcrud.databinding.DialogItemTimePickerBinding
import com.example.calendareventcrud.databinding.FragmentNewEventBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class NewEventFragment : Fragment(R.layout.fragment_new_event) {
    private val TAG = BASE_TAG + NewEventFragment::class.java.simpleName

    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    private var selectedStartDateTime: Long? = null
    private var selectedEndDateTime: Long? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewEventBinding.bind(view)

        checkAndRequestCalendarPermissions()

        binding.tvStartDatePicker.setOnClickListener {
            showDatePickerDialog(isStartDate = true)
        }
        binding.tvEndDatePicker.setOnClickListener {
            showDatePickerDialog(isStartDate = false)
        }
        binding.tvStartTimePicker.setOnClickListener {
            showTimePickerDialog(isStartTime = true)
        }
        binding.tvEndTimePicker.setOnClickListener {
            showTimePickerDialog(isStartTime = false)
        }
        //binding.tInEdtEventName.addTextChangedListener { }
        //binding.tInEdtEventNote.addTextChangedListener {  }
        binding.switchAllDay.setOnCheckedChangeListener { buttonView, isChecked -> }

        binding.mBtnSave.setOnClickListener {

            val dateStartLong = stringToLong(binding.tvStartDatePicker.text.toString() , pattern = DATE_FORMAT_dd_MMM_yyyy )
            val timeStartLong = stringToLong(binding.tvStartTimePicker.text.toString() , pattern = TIME_FORMAT_hh_mm_a )
            selectedStartDateTime = mergeDateAndTime(dateEpoch = dateStartLong, timeEpoch = timeStartLong)

            val dateEndLong = stringToLong(binding.tvEndDatePicker.text.toString() , pattern = DATE_FORMAT_dd_MMM_yyyy )
            val timeEndLong = stringToLong(binding.tvEndTimePicker.text.toString() , pattern = TIME_FORMAT_hh_mm_a )
            selectedEndDateTime = mergeDateAndTime(dateEpoch = dateEndLong, timeEpoch = timeEndLong)

            val title = binding.tInEdtEventName.text.toString()
            val description = binding.tInEdtEventNote.text.toString()
            val location = "Your Location" // Replace with actual location
            val startTime = selectedStartDateTime // Replace with your selected start time in epoch
            val endTime = selectedEndDateTime // Replace with your selected end time in epoch
            val isAllDay = binding.switchAllDay.isChecked

            if (startTime != null && endTime != null) {

                val calendarId = getPrimaryCalendarId(requireContext())
                if (calendarId != null) {
                    val event = CalendarEvent(
                        title = title,
                        calendarId = calendarId,
                        description = description,
                        location = location,
                        startTime = startTime,
                        endTime = endTime,
                        isAllDay = isAllDay
                    )

                    Log.e(TAG, "onViewCreated: $event", )
                    val eventId = addEventToCalendar(requireContext(), event)
                    if (eventId != null) {
                        Snackbar.make(binding.root, "Event added with ID: $eventId", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(binding.root, "Failed to add event", Snackbar.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private var bindingDatePicker: DialogItemDatePickerBinding? = null
    @SuppressLint("InflateParams")
    fun showDatePickerDialog(isStartDate: Boolean) {
        var selectedEpochTime: Long = Calendar.getInstance().timeInMillis // Default to current date

        val dialogView = layoutInflater.inflate(R.layout.dialog_item_date_picker, null)
        bindingDatePicker = DialogItemDatePickerBinding.bind(dialogView)

        val datePicker = bindingDatePicker?.datePicker
        val btnOkay = bindingDatePicker?.mBtnOky
        val btnCancel = bindingDatePicker?.mBtnCancel

        // Get the current date
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-based
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Initialize DatePicker with the current date
        datePicker?.init(currentYear, currentMonth, currentDay) { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year" // Month is 0-based
            Log.d(TAG, "Selected Date: $selectedDate")
        }

        // Create and display the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set background to transparent if needed
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent) // Set your background drawable here

        // Ensure the dialog's size wraps the content
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
            )
        }

        dialog.setCancelable(true)

        // Handle "OK" button click
        btnOkay?.setOnClickListener {
            // Fetch selected date
            val selectedYear = datePicker?.year ?: currentYear
            val selectedMonth = datePicker?.month ?: currentMonth // 0-based
            val selectedDay = datePicker?.dayOfMonth ?: currentDay

            // Convert to epoch time
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                set(Calendar.MILLISECOND, 0) // Reset milliseconds
            }.timeInMillis
            selectedEpochTime = selectedCalendar

            val selectedDate = "$selectedDay/$selectedMonth/$selectedYear"
            Log.d(TAG, "Final Selected Date (Epoch): $selectedEpochTime, Date: $selectedDate ")

            if (isStartDate) {
                binding.tvStartDatePicker.text = DateUtil.longToString(timestamp = DateUtil.getStartAndEndOfDay(selectedEpochTime).first, pattern = DateUtil.DATE_FORMAT_dd_MMM_yyyy)
            } else {
                binding.tvEndDatePicker.text = DateUtil.longToString(timestamp = DateUtil.getStartAndEndOfDay(selectedEpochTime).second, pattern = DateUtil.DATE_FORMAT_dd_MMM_yyyy)
            }

            dialog.dismiss()
        }

        // Handle "Cancel" button click
        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private var bindingTimePicker: DialogItemTimePickerBinding? = null
    @SuppressLint("InflateParams")
    fun showTimePickerDialog(isStartTime: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_item_time_picker,null)
        bindingTimePicker = DialogItemTimePickerBinding.bind(dialogView)

        val timePicker = bindingTimePicker?.timePicker
        val btnOky = bindingTimePicker?.mBtnOky
        val btnCancel = bindingTimePicker?.mBtnCancel

        // Configure TimePicker
        timePicker?.apply {
            setIs24HourView(false) // Use 12-hour format
            //hour = 0 // Set the hour (0 for 12 AM)
            //minute = 23 // Set the minute
            // Programmatically set a time (e.g., 0:12)
            /*if (arguments?.containsKey(KEY_EVENT) == true){
                val data = DateUtil.longToString(timestamp = argEvent.startTime.takeIf { isStartTime } ?: argEvent.endTime,pattern = DateUtil.TIME_FORMAT_hh_mm_a)

                // Split the time string into hour, minute, and AM/PM
                val time = splitTimeString(data)
                val hour = time.first.toInt()
                val minute = time.second.toInt()
                val amPm = time.third

                // Set the hour and minute
                this.hour = if (amPm == "PM" && hour != 12) {
                    hour + 12 // Convert PM hours (except 12 PM) to 24-hour format
                } else if (amPm == "AM" && hour == 12) {
                    0 // Convert 12 AM to 0 hours (midnight)
                } else {
                    hour
                }
                this.minute = minute
            }*/
        }

        // Create and display the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set background to transparent if needed
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent) // Set your background drawable here

        // Ensure the dialog's size wraps the content
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
            )
        }

        dialog.setCancelable(true)

        // Handle "OK" button click
        btnOky?.setOnClickListener {

            val hour = timePicker?.hour ?: 0
            val minute = timePicker?.minute ?: 0

            // Combine selected hour and minute with the current date
            val selectedTimeInMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            Log.d(TAG,"TimePicker: Selected Time in Millis: $selectedTimeInMillis")
            val selectedTime = String.format("%02d:%02d", hour, minute)
            Log.d(TAG,"TimePicker: Selected Time: $selectedTime")

            if (isStartTime) {
                binding.tvStartTimePicker.text = DateUtil.longToString(timestamp = selectedTimeInMillis, pattern = DateUtil.TIME_FORMAT_hh_mm_a)
            } else {
                binding.tvEndTimePicker.text = DateUtil.longToString(timestamp = selectedTimeInMillis, pattern = DateUtil.TIME_FORMAT_hh_mm_a)
            }


            dialog.dismiss()
        }

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    // region Call this function to request permissions as needed
    private fun checkAndRequestCalendarPermissions() {
        // Check READ_CALENDAR permission
        requestCalendarPermission(Manifest.permission.READ_CALENDAR, REQUEST_CODE_READ_CALENDAR)
        // Check WRITE_CALENDAR permission
        requestCalendarPermission(Manifest.permission.WRITE_CALENDAR, REQUEST_CODE_WRITE_CALENDAR)
    }

    private fun requestCalendarPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
        } else {
            // Permission already granted
            handlePermissionGranted(permission)
        }
    }

    private fun handlePermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.READ_CALENDAR -> {
                // Logic for when READ_CALENDAR is granted
                Toast.makeText(requireContext(), "Read calendar permission granted.", Toast.LENGTH_SHORT).show()
            }
            Manifest.permission.WRITE_CALENDAR -> {
                // Logic for when WRITE_CALENDAR is granted
                Toast.makeText(requireContext(), "Write calendar permission granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CODE_READ_CALENDAR -> {
                    // READ_CALENDAR granted
                    handlePermissionGranted(Manifest.permission.READ_CALENDAR)
                }
                REQUEST_CODE_WRITE_CALENDAR -> {
                    // WRITE_CALENDAR granted
                    handlePermissionGranted(Manifest.permission.WRITE_CALENDAR)
                }
            }
        } else {
            when (requestCode) {
                REQUEST_CODE_READ_CALENDAR -> {
                    // READ_CALENDAR denied
                    Toast.makeText(requireContext(), "Read calendar permission denied.", Toast.LENGTH_SHORT).show()
                }
                REQUEST_CODE_WRITE_CALENDAR -> {
                    // WRITE_CALENDAR denied
                    Toast.makeText(requireContext(), "Write calendar permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_READ_CALENDAR = 1
        private const val REQUEST_CODE_WRITE_CALENDAR = 2
    }
    //endregion

}




