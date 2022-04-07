package com.github.horizontal.timeview.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.github.horizontal.timeview.Schedule
import com.github.horizontal.timeview.TimeTableViews
import com.github.horizontal.timeview.TimeTableViews.OnStickerSelectedListener

class MainActivity : Activity(), View.OnClickListener {
    private var context: Context? = null
    private var addBtn: Button? = null
    private var clearBtn: Button? = null
    private var saveBtn: Button? = null
    private var loadBtn: Button? = null
    private var timetable: TimeTableViews? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        context = this
        addBtn = findViewById(R.id.add_btn)
        clearBtn = findViewById(R.id.clear_btn)
        saveBtn = findViewById(R.id.save_btn)
        loadBtn = findViewById(R.id.load_btn)
        timetable = findViewById(R.id.timetable)
        initView()
    }

    private fun initView() {
        addBtn!!.setOnClickListener(this)
        clearBtn!!.setOnClickListener(this)
        saveBtn!!.setOnClickListener(this)
        loadBtn!!.setOnClickListener(this)
        timetable!!.setOnStickerSelectEventListener(object : OnStickerSelectedListener {
            override fun OnStickerSelected(idx: Int, schedules: java.util.ArrayList<Schedule>?) {
                val i = Intent(context, EditActivity::class.java)
                i.putExtra("mode", REQUEST_EDIT)
                i.putExtra("idx", idx)
                i.putExtra("schedules", schedules)
                startActivityForResult(i, REQUEST_EDIT)
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_btn -> {
                val i = Intent(this, EditActivity::class.java)
                i.putExtra("mode", REQUEST_ADD)
                startActivityForResult(i, REQUEST_ADD)
            }
            R.id.clear_btn -> timetable!!.removeAll()
            R.id.save_btn -> saveByPreference(timetable!!.createSaveData())
            R.id.load_btn -> loadSavedData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ADD -> if (resultCode == EditActivity.RESULT_OK_ADD) {
                val item = data!!.getSerializableExtra("schedules") as ArrayList<Schedule>?
                timetable!!.add(item!!)
            }
            REQUEST_EDIT ->
                /** Edit -> Submit  */
                if (resultCode == EditActivity.RESULT_OK_EDIT) {
                    val idx = data!!.getIntExtra("idx", -1)
                    val item = data.getSerializableExtra("schedules") as ArrayList<Schedule>?
                    timetable!!.edit(idx, item!!)
                } else if (resultCode == EditActivity.RESULT_OK_DELETE) {
                    val idx = data!!.getIntExtra("idx", -1)
                    timetable!!.remove(idx)
                }
        }
    }

    /** save timetableView's data to SharedPreferences in json format  */
    private fun saveByPreference(data: String) {
        val mPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = mPref.edit()
        editor.putString("timetable_demo", data)
        editor.commit()
        Toast.makeText(this, "saved!", Toast.LENGTH_SHORT).show()
    }

    /** get json data from SharedPreferences and then restore the timetable  */
    private fun loadSavedData() {
        timetable!!.removeAll()
        val mPref = PreferenceManager.getDefaultSharedPreferences(this)
        val savedData = mPref.getString("timetable_demo", "")
        if (savedData == null && savedData == "") return
        timetable!!.load(savedData)
        Toast.makeText(this, "loaded!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_ADD = 1
        const val REQUEST_EDIT = 2
    }
}