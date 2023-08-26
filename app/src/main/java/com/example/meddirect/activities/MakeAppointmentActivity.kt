package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.example.meddirect.R
import com.example.meddirect.adapters.CalendarAdapter
import com.example.meddirect.adapters.TimeAdapter
import com.example.meddirect.databinding.ActivityMakeAppointmentBinding
import com.example.meddirect.model.CalendarDate
import com.example.meddirect.model.TimeSlot
import com.example.meddirect.utils.HorizontalItemDecoration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MakeAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMakeAppointmentBinding
    private lateinit var bundle: Bundle
    private lateinit var adapterCalendar: CalendarAdapter
    private lateinit var adapterTimeSlot: TimeAdapter
    private lateinit var selectedDate: CalendarDate
    private lateinit var selectedTime: TimeSlot
    private val calendarList = ArrayList<CalendarDate>()
    private val timeSlotList = ArrayList<TimeSlot>()
    private val sdf = SimpleDateFormat("MMMM yyyy",Locale.ENGLISH)
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val currentDate = Calendar.getInstance(Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bundle = intent.extras!!

        setUpCalendarAdapter()
        setUpCalendar()
        setUpTimeAdapter()
        setUpClickListener()
        adapterTimeSlot.setTimeData(timeSlotList)
    }

    private fun handleNextAndPrevious() {
        if(cal == currentDate){
            binding.ivCalendarPrevious.visibility = ImageView.INVISIBLE
        }else{
            binding.ivCalendarPrevious.visibility = ImageView.VISIBLE
        }
        if((currentDate.get(Calendar.MONTH) + 1)%12 == cal.get(Calendar.MONTH)%12){
            binding.ivCalendarNext.visibility = ImageView.INVISIBLE
        }else{
            binding.ivCalendarNext.visibility = ImageView.VISIBLE
        }
    }

    private fun setUpClickListener() {
        binding.ivCalendarNext.setOnClickListener {
            cal.add(Calendar.MONTH, 1)
            setUpCalendar()
        }
        binding.ivCalendarPrevious.setOnClickListener {
            cal.add(Calendar.MONTH, -1)
            setUpCalendar()
        }
        binding.buttonConfirmAppointment.setOnClickListener {
            val intent = Intent(this,ShowAppointmentDetailsActivity::class.java)
            val sendBundle = Bundle()
            intent.putExtras(sendBundle)
            startActivity(intent)
        }
    }

    private fun setUpTimeAdapter() {
        timeSlotList.addAll(arrayListOf(
            TimeSlot("08"), TimeSlot("09"),TimeSlot("10"), TimeSlot("11"),
            TimeSlot("14"), TimeSlot("15"), TimeSlot("16"), TimeSlot("19"),
            TimeSlot("20"), TimeSlot("21"), TimeSlot("22")
        ))
        val rvTime = binding.rvTime
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.single_calendar_margin)
        rvTime.addItemDecoration(HorizontalItemDecoration(spacingInPixels))
        rvTime.layoutManager = GridLayoutManager(this,4)
        adapterTimeSlot = TimeAdapter{_:TimeSlot, position:Int ->
            timeSlotList.forEachIndexed { index, timeSlot ->
                timeSlot.isSelected = index == position
                if(timeSlot.isSelected){
                    selectedTime = timeSlot
                }
            }
            adapterTimeSlot.setTimeData(timeSlotList)
        }
        rvTime.adapter = adapterTimeSlot
    }

    private fun setUpCalendarAdapter() {
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.single_calendar_margin)
        binding.rvCalendar.addItemDecoration(HorizontalItemDecoration(spacingInPixels))
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvCalendar)
        adapterCalendar = CalendarAdapter { _: CalendarDate, position: Int ->
            calendarList.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
                if(calendarModel.isSelected){
                    selectedDate = calendarModel
                }
            }
            adapterCalendar.setCalendarData(calendarList)
        }
        binding.rvCalendar.adapter = adapterCalendar
    }


    private fun setUpCalendar() {
        handleNextAndPrevious()
        calendarList.clear()
        binding.tvDateMonth.text = sdf.format(cal.time)
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        var dateItr = currentDate.get(Calendar.DATE) + 1
        if(cal != currentDate){
            dateItr = 1
        }
        monthCalendar.set(Calendar.DAY_OF_MONTH, dateItr)
        while (dateItr <= maxDaysInMonth) {
            calendarList.add(CalendarDate(monthCalendar.time))
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
            dateItr += 1
        }
        adapterCalendar.setCalendarData(calendarList)
    }
}