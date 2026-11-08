package com.Clinivora.ui;

import com.Clinivora.App;
import com.Clinivora.config.AppContext;
import com.Clinivora.model.CaseReportRow;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.service.ReportService;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.util.CsvReportExporter;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.PdfReportExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportsController {
    @FXML
    private DatePicker startPicker;
    @FXML
    private DatePicker endPicker;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private PieChart statusPie;
    @FXML
    private BarChart<String, Number> arrivalsBar;
    @FXML
    private TextArea summaryArea;
    @FXML
    private TableView<CaseReportRow> detailTable;
    @FXML
    private TableColumn<CaseReportRow, String> caseCol;
    @FXML
    private TableColumn<CaseReportRow, String> patientCodeCol;
    @FXML
    private TableColumn<CaseReportRow, String> patientNameCol;
    @FXML
    private TableColumn<CaseReportRow, String> statusCol;
    @FXML
    private TableColumn<CaseReportRow, String> arrivalCol;
    @FXML
    private TableColumn<CaseReportRow, String> complaintCol;
    @FXML
    private TableColumn<CaseReportRow, String> priorityCol;

    private List<CaseReportRow> currentRows = List.of();
    private Map<String, Long> currentStatusCounts = Map.of();
    private String currentSummary = "";

    @FXML
    private void initialize() {
        LocalDate today = LocalDate.now();
        startPicker.setValue(today.minusDays(7));
        endPicker.setValue(today);

        List<String> statuses = new ArrayList<>();
        statuses.add("ALL");
        for (CaseStatus status : CaseStatus.values()) {
            statuses.add(status.name());
        }
        statusFilter.setItems(FXCollections.observableArrayList(statuses));
        statusFilter.getSelectionModel().select("ALL");

        caseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCaseCode()));
        patientCodeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientCode()));
        patientNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientName()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() == null ? "" : c.getValue().getStatus().name()));
        arrivalCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().getArrivalTime())));
        complaintCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getChiefComplaint() == null ? "" : c.getValue().getChiefComplaint()));
        priorityCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPriority() == null ? "" : c.getValue().getPriority().name()));

        onRefresh();
    }

    @FXML
    private void onRefresh() {
        try {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            CaseStatus status = resolveStatus();
            ReportService reports = AppContext.getInstance().getReportService();

            currentRows = reports.listDetailedCases(start, end, status);
            currentStatusCounts = reports.caseStatusReport();
            Map<String, Long> daily = reports.dailyArrivalCounts(start, end);
            Map<String, Long> doctors = reports.doctorAssignmentCounts(start, end);
            currentSummary = reports.buildSummaryText(
                    start, end, status, currentRows, currentStatusCounts, daily, doctors);

            detailTable.setItems(FXCollections.observableArrayList(currentRows));
            summaryArea.setText(currentSummary);
            fillPie(currentStatusCounts);
            fillBar(daily);
        } catch (Exception ex) {
            AlertHelper.showError("Report refresh failed",
                    ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    private CaseStatus resolveStatus() {
        String selected = statusFilter.getValue();
        if (selected == null || "ALL".equals(selected)) {
            return null;
        }
        return CaseStatus.fromString(selected);
    }

    private void fillPie(Map<String, Long> statusCounts) {
        statusPie.getData().clear();
        statusCounts.forEach((key, value) -> {
            if (value != null && value > 0) {
                statusPie.getData().add(new PieChart.Data(key + " (" + value + ")", value));
            }
        });
        if (statusPie.getData().isEmpty()) {
            statusPie.getData().add(new PieChart.Data("No data", 1));
        }
    }

    private void fillBar(Map<String, Long> daily) {
        arrivalsBar.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Arrivals");
        daily.forEach((day, count) -> series.getData().add(new XYChart.Data<>(day, count)));
        arrivalsBar.getData().add(series);
    }

    @FXML
    private void onExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export PDF report");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("clinivora-report.pdf");
        File file = chooser.showSaveDialog(App.getPrimaryStage());
        if (file == null) {
            return;
        }
        try {
            PdfReportExporter.export(file.toPath(), currentSummary, currentStatusCounts, currentRows);
            AlertHelper.showSuccess("PDF exported", file.getAbsolutePath());
        } catch (Exception ex) {
            AlertHelper.showError("PDF export failed", ex.getMessage());
        }
    }

    @FXML
    private void onExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV report");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("clinivora-report.csv");
        File file = chooser.showSaveDialog(App.getPrimaryStage());
        if (file == null) {
            return;
        }
        try {
            CsvReportExporter.export(file.toPath(), currentRows);
            AlertHelper.showSuccess("CSV exported", file.getAbsolutePath());
        } catch (Exception ex) {
            AlertHelper.showError("CSV export failed", ex.getMessage());
        }
    }
}
