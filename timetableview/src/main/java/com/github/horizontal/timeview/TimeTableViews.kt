package com.github.horizontal.timeview

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.github.tlaabs.timetableview.R
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class TimeTableViews : LinearLayout {
    private var rowCount = 0
    private var columnCount = 0
    private var cellHeight = 0
    private var cellWidth = 0
    private lateinit var headerTitle: Array<String>
    private lateinit var stickerColors: Array<String>
    private var startTime: LocalDateTime = LocalDateTime.now()
    private var stickerBox: RelativeLayout? = null
    var tableHeader: TableLayout? = null
    var tableBox: TableLayout? = null
    //private  var context: Context? = null
    var stickers = HashMap<Int, Sticker>()
    private var stickerCount = -1
    private var stickerSelectedListener: OnStickerSelectedListener? = null

    constructor(context: Context) : super(context, null) {
        //this.context = context
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        //this.context = context
        getAttrs(attrs)
        init()
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TimetableView)
        rowCount = a.getInt(R.styleable.TimetableView_row_count, DEFAULT_ROW_COUNT)
        //columnCount = a.getInt(R.styleable.TimetableView_column_count, DEFAULT_COLUMN_COUNT);
        cellHeight = a.getDimensionPixelSize(
            R.styleable.TimetableView_cell_height, dp2Px(
                DEFAULT_CELL_HEIGHT_DP
            )
        )
        cellWidth = a.getDimensionPixelSize(
            R.styleable.TimetableView_side_cell_width, dp2Px(
                DEFAULT_SIDE_CELL_WIDTH_DP
            )
        )
        val titlesId =
            a.getResourceId(R.styleable.TimetableView_header_title, R.array.default_header_title)
        headerTitle = a.resources.getStringArray(titlesId)
        val colorsId =
            a.getResourceId(R.styleable.TimetableView_sticker_colors, R.array.default_sticker_color)
        stickerColors = a.resources.getStringArray(colorsId)
        val endTime: LocalDateTime = startTime.plusHours(6)
        val d = Duration.between(startTime, endTime)
        val totalMinutes = Math.toIntExact(d.toMinutes())
        columnCount = totalMinutes / TIME_SCALE + 1
        //int min =  (totalMins/5);
        //startTime = a.getInt(R.styleable.TimetableView_start_time, DEFAULT_START_TIME);
        a.recycle()
    }

    private fun init() {
        val layoutInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.view_timetable, this, false)
        addView(view)
        stickerBox = view.findViewById(R.id.sticker_box)
        tableHeader = view.findViewById(R.id.table_header)
        tableBox = view.findViewById(R.id.table_box)
        createTable()
    }

    fun setOnStickerSelectEventListener(listener: OnStickerSelectedListener?) {
        stickerSelectedListener = listener
    }

    private fun getHeaderTime(i: Int, localDateTime: LocalDateTime?): String {
        var localDateTime = localDateTime
        if (i != 0) {
            localDateTime = localDateTime!!.plusMinutes(i.toLong() * TIME_SCALE)
        }
        return String.format(
            Locale.ENGLISH,
            "%02d:%02d",
            localDateTime!!.hour,
            localDateTime.minute
        )
        //return localDateTime.getHour() + ":" + localDateTime.getMinute();

        /*this.startTime.getHour() +":" + this.startTime.getMinute()
        int p = (this.startTime.getHour() + i) % 24;
        int res = p <= 12 ? p : p - 12;
        return res + "";*/
    }

    fun add(schedules: ArrayList<Schedule>) {
        add(schedules, -1)
    }

    private fun add(schedules: ArrayList<Schedule>?, specIdx: Int) {
        val count = if (specIdx < 0) ++stickerCount else specIdx
        val sticker = Sticker()
        if (schedules != null) {
            for (schedule in schedules) {
                val tv = TextView(context)
                val param = createStickerParam(schedule)
                tv.layoutParams = param
                tv.setPadding(10, 0, 10, 0)
                tv.text = """
                    ${schedule.classTitle}
                    ${schedule.classPlace}
                    """.trimIndent()
                tv.setTextColor(Color.parseColor("#FFFFFF"))
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STICKER_FONT_SIZE_DP.toFloat())
                tv.setTypeface(null, Typeface.BOLD)
                tv.setOnClickListener { v: View? ->
                    if (stickerSelectedListener != null) stickerSelectedListener!!.OnStickerSelected(
                        count,
                        schedules
                    )
                }
                sticker.addTextView(tv)
                sticker.addSchedule(schedule)
                stickers[count] = sticker
                stickerBox!!.addView(tv)
            }
        }
        setStickerColor()
    }

    fun createSaveData(): String {
        return SaveManager.saveSticker(stickers)
    }

    fun load(data: String?) {
        removeAll()
        stickers = SaveManager.loadSticker(data)
        var maxKey = 0
        for (key in stickers.keys) {
            val schedules = stickers[key]?.schedules
            add(schedules, key)
            if (maxKey < key) maxKey = key
        }
        stickerCount = maxKey + 1
        setStickerColor()
    }

    fun removeAll() {
        for (key in stickers.keys) {
            val sticker = stickers[key]
            sticker?.view?.forEach { tv ->
                stickerBox!!.removeView(tv)
            }
        }
        stickers.clear()
    }

    fun edit(idx: Int, schedules: ArrayList<Schedule>) {
        remove(idx)
        add(schedules, idx)
    }

    private fun createTable() {
        createTableHeader()
        for (i in 1 until rowCount) {
            val tableRow = TableRow(context)
            tableRow.layoutParams = createTableLayoutParam()
            for (k in 0 until columnCount) {
                val tv = TextView(context)
                tv.layoutParams = createTableRowParam(cellHeight)
                if (k == 0) {
                    tv.text = headerTitle[i]
                    //tv.setText(getHeaderTime(i));
                    tv.setTextColor(ContextCompat.getColor(context, R.color.colorHeaderText))
                    tv.setTextSize(
                        TypedValue.COMPLEX_UNIT_DIP,
                        DEFAULT_SIDE_HEADER_FONT_SIZE_DP.toFloat()
                    )
                    tv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHeader))
                    tv.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    tv.layoutParams = createTableRowParam(cellWidth, cellHeight)
                } else {
                    tv.text = ""
                    tv.background = ContextCompat.getDrawable(context, R.drawable.item_border)
                    tv.gravity = Gravity.RIGHT
                }
                tableRow.addView(tv)
            }
            tableBox!!.addView(tableRow)
        }
    }

    private fun createTableHeader() {
        val tableRow = TableRow(context)
        tableRow.layoutParams = createTableLayoutParam()
        for (i in 0 until columnCount) {
            val tv = TextView(context)
            if (i == 0) {
                tv.layoutParams = createTableRowParam(cellWidth, cellHeight)
            } else {
                tv.layoutParams = createTableRowParam(cellHeight)
            }
            tv.setTextColor(ContextCompat.getColor(context, R.color.colorHeaderText))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HEADER_FONT_SIZE_DP.toFloat())
            tv.text = getHeaderTime(i, startTime)
            tv.gravity = Gravity.RIGHT
            tableRow.addView(tv)
        }
        tableHeader?.addView(tableRow)
    }

    fun remove(idx: Int) {
        val sticker = stickers[idx]
        sticker?.view?.forEach { tv ->
            stickerBox!!.removeView(tv)
        }
        stickers.remove(idx)
        setStickerColor()
    }

    private fun setStickerColor() {
        val size = stickers.size
        val orders = IntArray(size)
        var i = 0
        for (key in stickers.keys) {
            orders[i++] = key
        }
        Arrays.sort(orders)
        val colorSize = stickerColors.size
        i = 0
        while (i < size) {
            stickers[orders[i]]?.view?.forEach { v ->
                v.setBackgroundColor(Color.parseColor(stickerColors[i % colorSize]))
            }
            i++
        }
    }

    private fun createStickerParam(schedule: Schedule): RelativeLayout.LayoutParams {
        val param = RelativeLayout.LayoutParams(calStickerWidthPx(schedule), cellHeight)
        param.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        param.addRule(RelativeLayout.ALIGN_PARENT_START)
        param.setMargins(calStickerStartTime(schedule.startTime), cellHeight * schedule.day, 0, 0)
        //param.setMargins(calStickerStartPxByTime(schedule.getStartTime())+calCellWidth()-cellWidth,cellHeight*schedule.getDay(),0,0);
        return param
    }

    private fun calStickerWidthPx(schedule: Schedule): Int {
        val duration = Duration.between(schedule.startTime, schedule.endTime)
        val totalMinutes = Math.toIntExact(duration.toMinutes())
        return (totalMinutes / 60.0f * calCellWidth() * DIVISION_SCALE).toInt()
    }

    private fun calStickerStartPxByTime(time: LocalDateTime): Int {
        return (time.hour - startTime!!.hour) * calCellWidth() + (time.minute / 60.0f * calCellWidth()).toInt()
    }

    private fun calStickerStartTime(time: LocalDateTime): Int {
        val duration = Duration.between(startTime, time)
        var totalMinutes = Math.toIntExact(duration.toMinutes())
        return if (startTime!!.hour == time.hour) {
            totalMinutes += if (totalMinutes == 0) {
                TIME_SCALE - 1
            } else {
                TIME_SCALE
            }
            (totalMinutes / 60.0f * calCellWidth() * DIVISION_SCALE).toInt()
        } else {
            val totalHours = duration.toHours().toInt()
            var calculatedTime = totalMinutes
            if (totalHours == 0) {
                calculatedTime += TIME_SCALE
            } else if (totalHours >= 2) {
                calculatedTime -= if (totalHours % 2 == 0) {
                    totalHours / 2 * TIME_SCALE
                } else {
                    (totalHours - 1) * TIME_SCALE
                }
            }
            totalHours * calCellWidth() + (calculatedTime / 60.0f * calCellWidth() * DIVISION_SCALE).toInt()
        }
    }

    private fun calCellWidth(): Int {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return (size.x - paddingStart - paddingEnd - cellWidth) / (rowCount - 1)
    }

    private fun createTableLayoutParam(): TableLayout.LayoutParams {
        return TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )
    }

    private fun createTableRowParam(h_px: Int): TableRow.LayoutParams {
        return TableRow.LayoutParams(calCellWidth(), h_px)
    }

    private fun createTableRowParam(w_px: Int, h_px: Int): TableRow.LayoutParams {
        return TableRow.LayoutParams(w_px, h_px)
    }

    interface OnStickerSelectedListener {
        fun OnStickerSelected(idx: Int, schedules: ArrayList<Schedule>?)
    }

    companion object {
        private const val DEFAULT_ROW_COUNT = 13

        //private static final int DEFAULT_COLUMN_COUNT = 25;
        private const val DEFAULT_CELL_HEIGHT_DP = 50
        private const val DEFAULT_SIDE_CELL_WIDTH_DP = 80

        //private static final int DEFAULT_START_TIME = LocalDateTime.now();
        private const val DEFAULT_SIDE_HEADER_FONT_SIZE_DP = 13
        private const val DEFAULT_HEADER_FONT_SIZE_DP = 15
        private const val DEFAULT_STICKER_FONT_SIZE_DP = 13
        private const val TIME_SCALE = 5
        private const val ONE_HOUR = 60
        private const val DIVISION_SCALE = ONE_HOUR / TIME_SCALE
        private fun dp2Px(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}