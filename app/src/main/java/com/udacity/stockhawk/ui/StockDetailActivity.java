package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.StockFormatUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sakydpozrux on 21/05/2017.
 */

public class StockDetailActivity extends AppCompatActivity {
    @BindView(R.id.line_chart)
    LineChart mLineChart;

    @BindView(R.id.symbol)
    TextView mTextSymbol;

    @BindView(R.id.price)
    TextView mTextPrice;

    @BindView(R.id.change)
    TextView mTextChange;

    private static final String HISTORY_LINE_SEPARATOR = "\n";
    private static final String HISTORY_PAIR_SEPARATOR = ", ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        Uri uri = getIntent().getData();
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, R.string.error_stock_not_found, Toast.LENGTH_LONG).show();
                return;
            }

            setSymbol(cursor);
            setPrice(cursor);
            setPercentageChange(cursor);
            setHistoryChart(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHistoryChart(Cursor cursor) {
        final String history = cursor.getString(Contract.Quote.POSITION_HISTORY);

        List<Entry> entries = new ArrayList<>();
        final ArrayList<String> labelsAxisX = new ArrayList<>();

        setEntriesAndLabels(history, entries, labelsAxisX);

        LineDataSet dataSet = new LineDataSet(entries, null);
        LineData lineData = new LineData(dataSet);

        setChartAppearance();
        setLabelsFormat(labelsAxisX);

        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    private void setEntriesAndLabels(String history, List<Entry> entries, ArrayList<String> labelsAxisX) {
        final List<String> historyPairs = Arrays.asList(history.split(HISTORY_LINE_SEPARATOR));

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
            Collections.reverse(historyPairs);

        for (String historyPair: historyPairs) {
            final String[] xy = historyPair.split(HISTORY_PAIR_SEPARATOR);

            final float value = Float.parseFloat(xy[1]);
            final int positionAxisX = entries.size();
            entries.add(new Entry(positionAxisX, value));

            final long longDate = Long.parseLong(xy[0]);
            DateFormat sdf = SimpleDateFormat.getDateInstance();
            final String formattedDate = sdf.format(new Date(longDate));
            labelsAxisX.add(formattedDate);
        }
    }

    private void setLabelsFormat(final ArrayList<String> labelsAxisX) {
        mLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labelsAxisX.get((int)value);
            }
        });
    }

    private void setChartAppearance() {
        mLineChart.getLegend().setEnabled(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.getXAxis().setLabelRotationAngle(-90);

        int resolvedLightColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
            resolvedLightColor = getColor(R.color.colorPrimaryLight);
        else
            resolvedLightColor = getResources().getColor(R.color.colorPrimaryLight);

        mLineChart.setDrawingCacheBackgroundColor(resolvedLightColor);

        mLineChart.getXAxis().setTextColor(resolvedLightColor);
        mLineChart.getAxisLeft().setTextColor(resolvedLightColor);
        mLineChart.getAxisRight().setTextColor(resolvedLightColor);

        mLineChart.setNoDataText(getString(R.string.error_stock_not_found));
        mLineChart.setNoDataTextColor(resolvedLightColor);
    }

    private void setPercentageChange(Cursor cursor) {
        final float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        mTextChange.setText(StockFormatUtils.getPercentageFormat(percentageChange / 100));

        int backgroundResource =
                (percentageChange >= 0)
                        ? R.drawable.percent_change_pill_green
                        : R.drawable.percent_change_pill_red;

        mTextChange.setBackgroundResource(backgroundResource);
    }

    private void setPrice(Cursor cursor) {
        final float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
        mTextPrice.setText(StockFormatUtils.getDollarFormat(price, true));
    }

    private void setSymbol(Cursor cursor) {
        final String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        mTextSymbol.setText(symbol);
    }
}
