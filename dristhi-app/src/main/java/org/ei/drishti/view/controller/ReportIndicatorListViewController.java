package org.ei.drishti.view.controller;

import android.content.Context;
import android.content.Intent;
import com.google.gson.Gson;
import org.ei.drishti.domain.Report;
import org.ei.drishti.domain.ReportIndicator;
import org.ei.drishti.domain.ReportsCategory;
import org.ei.drishti.dto.MonthSummaryDatum;
import org.ei.drishti.repository.AllReports;
import org.ei.drishti.util.DateUtil;
import org.ei.drishti.view.activity.ReportIndicatorDetailActivity;
import org.ei.drishti.view.contract.CategoryReports;
import org.ei.drishti.view.contract.IndicatorReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.ei.drishti.AllConstants.CATEGORY_DESCRIPTION;
import static org.ei.drishti.AllConstants.INDICATOR_DETAIL;

public class ReportIndicatorListViewController {
    private final Context context;
    private final AllReports allReports;
    private final String category;
    private List<Report> reports;
    private ReportsCategory reportsCategory;

    public ReportIndicatorListViewController(Context context, AllReports allReports, String category) {
        this.context = context;
        this.allReports = allReports;
        this.category = category;
    }

    public String get() {
        reportsCategory = ReportsCategory.valueOf(category);

        reports = allReports.allFor(reportsCategory.indicators());

        List<IndicatorReport> indicatorReports = new ArrayList<IndicatorReport>();
        for (Report report : reports) {
            ReportIndicator indicator = report.reportIndicator();
            List<MonthSummaryDatum> monthSummaryData = report.monthlySummaries();
            sortMonthlySummaries(monthSummaryData);
            MonthSummaryDatum currentMonthSummary = monthSummaryData.get(0);

            String currentMonth = DateUtil.today().monthOfYear().getAsString();
            String currentProgress = currentMonthSummary.month().equals(currentMonth) ? currentMonthSummary.currentProgress() : "0";
            String annualTarget = (isBlank(report.annualTarget())) ? "NA" : report.annualTarget();

            indicatorReports.add(new IndicatorReport(indicator.name(), indicator.description(), annualTarget,
                    currentProgress, currentMonth, currentMonthSummary.year(), currentMonthSummary.aggregatedProgress()
            ));
        }

        return new Gson().toJson(new CategoryReports(reportsCategory.description(), indicatorReports));
    }

    public void sortMonthlySummaries(List<MonthSummaryDatum> monthSummaryData) {
        Collections.sort(monthSummaryData, new Comparator<MonthSummaryDatum>() {
            @Override
            public int compare(MonthSummaryDatum monthSummaryDatum, MonthSummaryDatum anotherMonthSummaryDatum) {
                return parseInt(anotherMonthSummaryDatum.month()) - parseInt(monthSummaryDatum.month());
            }
        });
    }

    public void startReportIndicatorDetail(String indicator) {
        for (Report report : reports) {
            if (report.reportIndicator().name().equals(indicator)) {
                Intent intent = new Intent(context.getApplicationContext(), ReportIndicatorDetailActivity.class);
                intent.putExtra(INDICATOR_DETAIL, report);
                intent.putExtra(CATEGORY_DESCRIPTION, reportsCategory.description());
                context.startActivity(intent);
                return;
            }
        }
    }
}
