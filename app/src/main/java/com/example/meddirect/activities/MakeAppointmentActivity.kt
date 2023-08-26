package com.example.meddirect.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.example.meddirect.R
import com.example.meddirect.adapters.CalendarAdapter
import com.example.meddirect.databinding.ActivityMakeAppointmentBinding
import com.example.meddirect.model.CalendarDate
import com.example.meddirect.utils.HorizontalItemDecoration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MakeAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMakeAppointmentBinding
    private lateinit var bundle: Bundle
    private val sdf = SimpleDateFormat("MMMM yyyy",Locale.ENGLISH)
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val currentDate = Calendar.getInstance(Locale.ENGLISH)
    //private val dates = ArrayList<Date>()
    private lateinit var adapter: CalendarAdapter
    private val calendarList = ArrayList<CalendarDate>()
    private lateinit var selectedDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bundle = intent.extras!!

        setUpAdapter()
        setUpCalendar()
        setUpClickListener()
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
    }

    private fun setUpAdapter() {
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.single_calendar_margin)
        binding.recyclerView.addItemDecoration(HorizontalItemDecoration(spacingInPixels))
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        adapter = CalendarAdapter { _: CalendarDate, position: Int ->
            calendarList.forEachIndexed { index, calendarModel ->
                calendarModel.isSelected = index == position
                if(calendarModel.isSelected){
                    selectedDate = calendarModel.data
                }
            }
            adapter.setData(calendarList)
        }
        binding.recyclerView.adapter = adapter
    }


    private fun setUpCalendar() {
        handleNextAndPrevious()
//        val calendarList = ArrayList<CalendarDate>()
        calendarList.clear()
        binding.tvDateMonth.text = sdf.format(cal.time)
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        var dateItr = currentDate.get(Calendar.DATE)
        if(cal != currentDate){
            dateItr = 1
        }
        //dates.clear()
        monthCalendar.set(Calendar.DAY_OF_MONTH, dateItr)
        while (dateItr <= maxDaysInMonth) {
            //dates.add(monthCalendar.time)
            calendarList.add(CalendarDate(monthCalendar.time))
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
            dateItr += 1
        }
//        calendarList2.clear()
//        calendarList2.addAll(calendarList)
        adapter.setData(calendarList)
    }
}