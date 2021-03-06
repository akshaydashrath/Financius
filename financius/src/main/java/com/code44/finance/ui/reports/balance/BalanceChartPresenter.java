package com.code44.finance.ui.reports.balance;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import com.code44.finance.R;
import com.code44.finance.data.model.Account;
import com.code44.finance.money.AmountGrouper;
import com.code44.finance.ui.common.presenters.Presenter;
import com.code44.finance.utils.interval.BaseInterval;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;

public abstract class BalanceChartPresenter extends Presenter {
    private final BalanceChartView balanceChartView;
    private final Formatter formatter;

    public BalanceChartPresenter(BalanceChartView balanceChartView) {
        this.balanceChartView = balanceChartView;
        this.formatter = new Formatter();
    }

    public void setData(Account account, Cursor cursor, BaseInterval baseInterval) {
        final AmountGrouper.AmountCalculator amountCalculator = getTransactionValidator();
//        final AmountGrouper amountGrouper = new AmountGrouper(baseInterval);
//        final Map<AmountGrouper.AmountCalculator, List<Long>> groups = amountGrouper.getGroups(cursor, amountCalculator);

//        final List<Line> lines = new ArrayList<>();
//        final Line line = getLine(account, groups.get(amountCalculator))
//                .setColor(ThemeUtils.getColor(balanceChartView.getContext(), R.attr.textColorNeutral))
//                .setHasLabels(true)
//                .setHasLabelsOnlyForSelected(true);
//        onLineCreated(amountCalculator, line);
//        lines.add(line);
//
//        final LineChartData lineChartData = new LineChartData(lines);
//        lineChartData.setAxisXBottom(getAxis(baseInterval));
//
//        balanceChartView.setLineGraphData(lineChartData);
    }

    protected abstract AmountGrouper.AmountCalculator getTransactionValidator();

    protected abstract void onLineCreated(AmountGrouper.AmountCalculator amountCalculator, Line line);

    protected Context getContext() {
        return balanceChartView.getContext();
    }

    private Line getLine(Account account, List<Long> amounts) {
        final List<PointValue> points = new ArrayList<>();
        int index = amounts.size() - 1;
        long totalAmount = account.getBalance();
        Collections.reverse(amounts);
        for (Long amount : amounts) {
            points.add(new PointValue(index, totalAmount));
            totalAmount -= amount;
            index--;
        }

        final int lineWidthDp = (int) (balanceChartView.getResources().getDimension(R.dimen.report_line_graph_width) / Resources.getSystem().getDisplayMetrics().density);
        return new Line(points)
                .setCubic(true)
                .setStrokeWidth(lineWidthDp)
                .setPointRadius(lineWidthDp)
                .setFormatter(formatter)
                .setHasPoints(false);
    }

    private Axis getAxis(BaseInterval baseInterval) {
        final List<AxisValue> values = new ArrayList<>();
        final Period period = BaseInterval.getSubPeriod(baseInterval.getType(), baseInterval.getLength());

        Interval interval = new Interval(baseInterval.getInterval().getStart(), period);
        int index = 0;
        while (interval.overlaps(baseInterval.getInterval())) {
            values.add(new AxisValue(index++, BaseInterval.getSubTypeShortestTitle(interval, baseInterval.getType()).toCharArray()));
            interval = new Interval(interval.getEnd(), period);
        }

        return new Axis(values).setHasLines(true).setHasSeparationLine(true);
    }

    private static class Formatter implements LineChartValueFormatter {
        public Formatter() {
        }

        @Override public int formatChartValue(char[] chars, PointValue pointValue) {
//            final char[] fullText = MoneyFormatter.format(mainCurrencyFormat, (long) pointValue.getY()).toCharArray();
//            final int size = Math.min(chars.length, fullText.length);
//            System.arraycopy(fullText, 0, chars, chars.length - size, size);
//            return size;
            return 0;
        }
    }
}
