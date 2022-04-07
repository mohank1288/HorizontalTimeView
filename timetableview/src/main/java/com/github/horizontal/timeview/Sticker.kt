package com.github.horizontal.timeview

import android.widget.TextView
import com.github.horizontal.timeview.Schedule
import java.io.Serializable
import java.util.ArrayList

class Sticker : Serializable {
    val view: ArrayList<TextView>
    val schedules: ArrayList<Schedule>
    fun addTextView(v: TextView) {
        view.add(v)
    }

    fun addSchedule(schedule: Schedule) {
        schedules.add(schedule)
    }

    init {
        view = ArrayList()
        schedules = ArrayList()
    }
}