package com.github.horizontal.timeview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.tlaabs.timetableview.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class TimetableView extends LinearLayout {
    private static final int DEFAULT_ROW_COUNT = 13;
    private static final int DEFAULT_COLUMN_COUNT = 25;
    private static final int DEFAULT_CELL_HEIGHT_DP = 60;
    private static final int DEFAULT_SIDE_CELL_WIDTH_DP = 80;
    private static final int DEFAULT_START_TIME = 0;

    private static final int DEFAULT_SIDE_HEADER_FONT_SIZE_DP = 13;
    private static final int DEFAULT_HEADER_FONT_SIZE_DP = 15;
    private static final int DEFAULT_STICKER_FONT_SIZE_DP = 13;

    private int rowCount;
    private int columnCount;
    private int cellHeight;
    private int cellWidth;
    private String[] headerTitle;
    private String[] stickerColors;
    private int startTime;

    private RelativeLayout stickerBox;
    TableLayout tableHeader;
    TableLayout tableBox;

    private final Context context;

    HashMap<Integer, Sticker> stickers = new HashMap<>();
    private int stickerCount = -1;

    private OnStickerSelectedListener stickerSelectedListener = null;

    public TimetableView(Context context) {
        super(context, null);
        this.context = context;
    }

    public TimetableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimetableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttrs(attrs);
        init();
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimetableView);
        rowCount = a.getInt(R.styleable.TimetableView_row_count, DEFAULT_ROW_COUNT);
        columnCount = a.getInt(R.styleable.TimetableView_column_count, DEFAULT_COLUMN_COUNT);
        cellHeight = a.getDimensionPixelSize(R.styleable.TimetableView_cell_height, dp2Px(DEFAULT_CELL_HEIGHT_DP));
        cellWidth = a.getDimensionPixelSize(R.styleable.TimetableView_side_cell_width, dp2Px(DEFAULT_SIDE_CELL_WIDTH_DP));
        int titlesId = a.getResourceId(R.styleable.TimetableView_header_title, R.array.default_header_title);
        headerTitle = a.getResources().getStringArray(titlesId);
        int colorsId = a.getResourceId(R.styleable.TimetableView_sticker_colors, R.array.default_sticker_color);
        stickerColors = a.getResources().getStringArray(colorsId);
        startTime = a.getInt(R.styleable.TimetableView_start_time, DEFAULT_START_TIME);
        a.recycle();
    }

    private void init() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_timetable, this, false);
        addView(view);

        stickerBox = view.findViewById(R.id.sticker_box);
        tableHeader = view.findViewById(R.id.table_header);
        tableBox = view.findViewById(R.id.table_box);

        createTable();
    }

    public void setOnStickerSelectEventListener(OnStickerSelectedListener listener) {
        stickerSelectedListener = listener;
    }


    public void add(ArrayList<Schedule> schedules) {
        add(schedules, -1);
    }

    private void add(final ArrayList<Schedule> schedules, int specIdx) {
        final int count = specIdx < 0 ? ++stickerCount : specIdx;
        Sticker sticker = new Sticker();
        for (Schedule schedule : schedules) {
            TextView tv = new TextView(context);

            RelativeLayout.LayoutParams param = createStickerParam(schedule);
            tv.setLayoutParams(param);
            tv.setPadding(10, 0, 10, 0);
            tv.setText(schedule.getClassTitle() + "\n" + schedule.getClassPlace());
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STICKER_FONT_SIZE_DP);
            tv.setTypeface(null, Typeface.BOLD);

            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stickerSelectedListener != null)
                        stickerSelectedListener.OnStickerSelected(count, schedules);
                }
            });

            sticker.addTextView(tv);
            sticker.addSchedule(schedule);
            stickers.put(count, sticker);
            stickerBox.addView(tv);
        }
        setStickerColor();
    }

    public String createSaveData() {
        return SaveManager.saveSticker(stickers);
    }

    public void load(String data) {
        removeAll();
        stickers = SaveManager.loadSticker(data);
        int maxKey = 0;
        for (int key : stickers.keySet()) {
            ArrayList<Schedule> schedules = Objects.requireNonNull(stickers.get(key)).getSchedules();
            add(schedules, key);
            if (maxKey < key) maxKey = key;
        }
        stickerCount = maxKey + 1;
        setStickerColor();
    }

    public void removeAll() {
        for (int key : stickers.keySet()) {
            Sticker sticker = stickers.get(key);
            for (TextView tv : Objects.requireNonNull(sticker).getView()) {
                stickerBox.removeView(tv);
            }
        }
        stickers.clear();
    }

    public void edit(int idx, ArrayList<Schedule> schedules) {
        remove(idx);
        add(schedules, idx);
    }

    public void remove(int idx) {
        Sticker sticker = stickers.get(idx);
        for (TextView tv : Objects.requireNonNull(sticker).getView()) {
            stickerBox.removeView(tv);
        }
        stickers.remove(idx);
        setStickerColor();
    }

    private void setStickerColor() {
        int size = stickers.size();
        int[] orders = new int[size];
        int i = 0;
        for (int key : stickers.keySet()) {
            orders[i++] = key;
        }
        Arrays.sort(orders);

        int colorSize = stickerColors.length;

        for (i = 0; i < size; i++) {
            for (TextView v : Objects.requireNonNull(stickers.get(orders[i])).getView()) {
                v.setBackgroundColor(Color.parseColor(stickerColors[i % (colorSize)]));
            }
        }
    }

    private void createTable() {
        createTableHeader();
        for (int i = 1; i < rowCount; i++) {
            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(createTableLayoutParam());

            for (int k = 0; k < columnCount; k++) {
                TextView tv = new TextView(context);
                tv.setLayoutParams(createTableRowParam(cellHeight));
                if (k == 0) {
                    tv.setText(headerTitle[i]);
                    //tv.setText(getHeaderTime(i));
                    tv.setTextColor(getResources().getColor(R.color.colorHeaderText));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIDE_HEADER_FONT_SIZE_DP);
                    tv.setBackgroundColor(getResources().getColor(R.color.colorHeader));
                    tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    tv.setLayoutParams(createTableRowParam(cellWidth, cellHeight));
                } else {
                    tv.setText("");
                    tv.setBackground(getResources().getDrawable(R.drawable.item_border));
                    tv.setGravity(Gravity.RIGHT);
                }
                tableRow.addView(tv);
            }
            tableBox.addView(tableRow);
        }
    }

    private void createTableHeader() {
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(createTableLayoutParam());

        for (int i = 0; i < columnCount; i++) {
            TextView tv = new TextView(context);
            if (i == 0) {
                tv.setLayoutParams(createTableRowParam(cellWidth, cellHeight));
            } else {
                tv.setLayoutParams(createTableRowParam(cellHeight));
            }
            tv.setTextColor(getResources().getColor(R.color.colorHeaderText));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HEADER_FONT_SIZE_DP);
            tv.setText(getHeaderTime(i));
            //tv.setText(headerTitle[i]);
            tv.setGravity(Gravity.RIGHT);

            tableRow.addView(tv);
        }
        tableHeader.addView(tableRow);
    }

    private RelativeLayout.LayoutParams createStickerParam(Schedule schedule) {
        int cell_w = calCellWidth();

        //RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(cell_w, calStickerHeightPx(schedule));
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams( calStickerHeightPx(schedule),cell_w);

        param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        //param.setMargins(cellWidth + cell_w * schedule.getDay(), calStickerTopPxByTime(schedule.getStartTime()), 0, 0);

        param.setMargins(calStickerTopPxByTime(schedule.getStartTime()),cellWidth + cell_w * schedule.getDay(), 0, 0);

        return param;
    }

    private int calCellWidth() {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return (size.x - getPaddingLeft() - getPaddingRight() - cellWidth) / (rowCount -2);
    }

    private int calStickerHeightPx(Schedule schedule) {
        int startTopPx = calStickerTopPxByTime(schedule.getStartTime());
        int endTopPx = calStickerTopPxByTime(schedule.getEndTime());
        return endTopPx - startTopPx;
    }

    private int calStickerTopPxByTime(Time time) {
        return (time.getHour() - startTime) * cellHeight + (int) ((time.getMinute() / 60.0f) * cellHeight);
    }

    private TableLayout.LayoutParams createTableLayoutParam() {
        return new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
    }

    private TableRow.LayoutParams createTableRowParam(int h_px) {
        return new TableRow.LayoutParams(calCellWidth(), h_px);
    }

    private TableRow.LayoutParams createTableRowParam(int w_px, int h_px) {
        return new TableRow.LayoutParams(w_px, h_px);
    }

    private String getHeaderTime(int i) {
        int p = (startTime + i) % 24;
        int res = p <= 12 ? p : p - 12;
        return res + "";
    }

    static private int dp2Px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void onCreateByBuilder(Builder builder) {
        this.rowCount = builder.rowCount;
        this.columnCount = builder.columnCount;
        this.cellHeight = builder.cellHeight;
        this.cellWidth = builder.sideCellWidth;
        this.headerTitle = builder.headerTitle;
        this.stickerColors = builder.stickerColors;
        this.startTime = builder.startTime;

        init();
    }


    public interface OnStickerSelectedListener {
        void OnStickerSelected(int idx, ArrayList<Schedule> schedules);
    }

    static class Builder {
        private final Context context;
        private int rowCount;
        private int columnCount;
        private int cellHeight;
        private int sideCellWidth;
        private String[] headerTitle;
        private String[] stickerColors;
        private int startTime;
        private int headerHighlightColor;

        public Builder(Context context) {
            this.context = context;
            rowCount = DEFAULT_ROW_COUNT;
            columnCount = DEFAULT_COLUMN_COUNT;
            cellHeight = dp2Px(DEFAULT_CELL_HEIGHT_DP);
            sideCellWidth = dp2Px(DEFAULT_SIDE_CELL_WIDTH_DP);
            headerTitle = context.getResources().getStringArray(R.array.default_header_title);
            stickerColors = context.getResources().getStringArray(R.array.default_sticker_color);
            startTime = DEFAULT_START_TIME;
            headerHighlightColor = context.getResources().getColor(R.color.default_header_highlight_color);
        }

        public Builder setRowCount(int n) {
            this.rowCount = n;
            return this;
        }

        public Builder setColumnCount(int n) {
            this.columnCount = n;
            return this;
        }

        public Builder setCellHeight(int dp) {
            this.cellHeight = dp2Px(dp);
            return this;
        }

        public Builder setSideCellWidth(int dp) {
            this.sideCellWidth = dp2Px(dp);
            return this;
        }

        public Builder setHeaderTitle(String[] titles) {
            this.headerTitle = titles;
            return this;
        }

        public Builder setStickerColors(String[] colors) {
            this.stickerColors = colors;
            return this;
        }

        public Builder setStartTime(int t) {
            this.startTime = t;
            return this;
        }

        public Builder setHeaderHighlightColor(int c) {
            this.headerHighlightColor = c;
            return this;
        }

        public TimetableView build() {
            TimetableView timetableView = new TimetableView(context);
            timetableView.onCreateByBuilder(this);
            return timetableView;
        }
    }
}
