package com.github.horizontal.timeview.demo

import android.app.Activity
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.github.horizontal.timeview.Schedule
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class EditActivity : Activity(), View.OnClickListener {
    private var context: Context? = null
    private var deleteBtn: Button? = null
    private var submitBtn: Button? = null
    private var subjectEdit: EditText? = null
    private var classroomEdit: EditText? = null
    private var professorEdit: EditText? = null
    private var daySpinner: Spinner? = null
    private var startTv: TextView? = null
    private var endTv: TextView? = null

    //request mode
    private var mode = 0
    private var schedule: Schedule? = null
    private var editIdx = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        init()
    }

    private fun init() {
        context = this
        deleteBtn = findViewById(R.id.delete_btn)
        submitBtn = findViewById(R.id.submit_btn)
        subjectEdit = findViewById(R.id.subject_edit)
        classroomEdit = findViewById(R.id.classroom_edit)
        professorEdit = findViewById(R.id.professor_edit)
        daySpinner = findViewById(R.id.day_spinner)
        startTv = findViewById(R.id.start_time)
        endTv = findViewById(R.id.end_time)

        //set the default time
        schedule = Schedule()
        schedule!!.startTime = LocalDateTime.now()
        schedule!!.endTime = LocalDateTime.now().plusHours(2)

        /*schedule.setStartTime(new Time(10,0));
        schedule.setEndTime(new Time(13,30));*/checkMode()
        initView()
    }

    /**
     * check whether the mode is ADD or EDIT
     */
    private fun checkMode() {
        val i = intent
        mode = i.getIntExtra("mode", MainActivity.REQUEST_ADD)
        if (mode == MainActivity.REQUEST_EDIT) {
            loadScheduleData()
            deleteBtn!!.visibility = View.VISIBLE
        }
    }

    private fun initView() {
        submitBtn!!.setOnClickListener(this)
        deleteBtn!!.setOnClickListener(this)
        daySpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                schedule!!.day = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        startTv!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val dialog = TimePickerDialog(
                    context,
                    listener,
                    schedule!!.startTime.hour,
                    schedule!!.startTime.minute,
                    false
                )
                dialog.show()
            }

            private val listener = OnTimeSetListener { view, hourOfDay, minute ->
                val time = String.format(Locale.ENGLISH, "%02d:%02d", hourOfDay, minute)
                startTv!!.text = time
                val hour = String.format(Locale.ENGLISH, "%02d", hourOfDay)
                val minutes = String.format(Locale.ENGLISH, "%02d", minute)
                schedule!!.startTime = LocalDateTime.of(
                    schedule!!.startTime.toLocalDate(),
                    LocalTime.of(hour.toInt(), minutes.toInt())
                )
                startTv!!.text = "$hourOfDay:$minute"
                schedule!!.startTime = LocalDateTime.of(
                    schedule!!.startTime.toLocalDate(),
                    LocalTime.of(hourOfDay, minute)
                )
                /*schedule.getStartTime().setHour(hourOfDay);
                            schedule.getStartTime().setMinute(minute);*/
            }
        })
        endTv!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val dialog = TimePickerDialog(
                    context,
                    listener,
                    schedule!!.endTime.hour,
                    schedule!!.endTime.minute,
                    false
                )
                dialog.show()
            }

            private val listener = OnTimeSetListener { view, hourOfDay, minute ->
                val time = String.format(Locale.ENGLISH, "%02d:%02d", hourOfDay, minute)
                endTv!!.text = time
                val hour = String.format(Locale.ENGLISH, "%02d", hourOfDay)
                val minutes = String.format(Locale.ENGLISH, "%02d", minute)
                schedule!!.endTime = LocalDateTime.of(
                    schedule!!.endTime.toLocalDate(),
                    LocalTime.of(hour.toInt(), minutes.toInt())
                )

                /*schedule.getEndTime().setHour(hourOfDay);
                            schedule.getEndTime().setMinute(minute);*/
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.submit_btn -> if (mode == MainActivity.REQUEST_ADD) {
                inputDataProcessing()
                val i = Intent()
                val schedules = ArrayList<Schedule?>()
                //you can add more schedules to ArrayList
                schedules.add(schedule)
                i.putExtra("schedules", schedules)
                setResult(RESULT_OK_ADD, i)
                finish()
            } else if (mode == MainActivity.REQUEST_EDIT) {
                inputDataProcessing()
                val i = Intent()
                val schedules = ArrayList<Schedule?>()
                schedules.add(schedule)
                i.putExtra("idx", editIdx)
                i.putExtra("schedules", schedules)
                setResult(RESULT_OK_EDIT, i)
                finish()
            }
            R.id.delete_btn -> {
                val i = Intent()
                i.putExtra("idx", editIdx)
                setResult(RESULT_OK_DELETE, i)
                finish()
            }
        }
    }

    private fun loadScheduleData() {
        val i = intent
        editIdx = i.getIntExtra("idx", -1)
        val schedules = i.getSerializableExtra("schedules") as ArrayList<Schedule>?
        schedule = schedules!![0]
        subjectEdit!!.setText(schedule!!.classTitle)
        classroomEdit!!.setText(schedule!!.classPlace)
        professorEdit!!.setText(schedule!!.professorName)
        daySpinner!!.setSelection(schedule!!.day)
    }

    private fun inputDataProcessing() {
        schedule!!.classTitle = subjectEdit!!.text.toString()
        schedule!!.classPlace = classroomEdit!!.text.toString()
        schedule!!.professorName = professorEdit!!.text.toString()
    }

    companion object {
        const val RESULT_OK_ADD = 1
        const val RESULT_OK_EDIT = 2
        const val RESULT_OK_DELETE = 3
    }
}