package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.service.DashboardService;
import com.Clinivora.service.NavigationService;
import com.Clinivora.service.NavigationService.View;
import com.Clinivora.service.dashboard.DashboardSnapshot;
import com.Clinivora.service.dashboard.PipelineRow;
import com.Clinivora.service.dashboard.WaitItem;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.util.DateTimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class DashboardController {
    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("EEE d", Locale.ENGLISH);

    @FXML
    private Label greetingLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label floorChip;
    @FXML
    private Label refreshedLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private HBox kpiRow;
    @FXML
    private Label esiTitleLabel;
    @FXML
    private Label esiHintLabel;
    @FXML
    private PieChart esiPie;
    @FXML
    private Label arrivalsTitleLabel;
    @FXML
    private Label arrivalsHintLabel;
    @FXML
    private BarChart<String, Number> arrivalsBar;
    @FXML
    private CategoryAxis arrivalsXAxis;
    @FXML
    private NumberAxis arrivalsYAxis;
    @FXML
    private Label waitsTitleLabel;
    @FXML
    private VBox waitsList;
    @FXML
    private Label pipelineTitleLabel;
    @FXML
    private Label pipelineCountLabel;
    @FXML
    private TableView<PipelineRow> pipelineTable;
    @FXML
    private TableColumn<PipelineRow, String> colCode;
    @FXML
    private TableColumn<PipelineRow, String> colPatient;
    @FXML
    private TableColumn<PipelineRow, String> colStatus;
    @FXML
    private TableColumn<PipelineRow, String> colEsi;
    @FXML
    private TableColumn<PipelineRow, String> colWait;
    @FXML
    private Label actionsTitleLabel;
    @FXML
    private HBox actionsRow;

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().caseCode()));
        colPatient.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().patientName()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().status() == null ? "—" : c.getValue().status().name()));
        colEsi.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().priority() == null ? "—" : c.getValue().priority().name()));
        colWait.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().waitMinutes() + "m"));
        pipelineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        arrivalsYAxis.setForceZeroInRange(true);
        arrivalsYAxis.setMinorTickVisible(false);
        arrivalsXAxis.setTickLabelRotation(-25);

        I18n.getInstance().addListener(lang -> {
            localizeStatic();
            refresh();
        });
        localizeStatic();
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void localizeStatic() {
        I18n i18n = I18n.getInstance();
        refreshButton.setText(i18n.t("common.refresh"));
        esiTitleLabel.setText(i18n.t("dashboard.esi.title"));
        esiHintLabel.setText(i18n.t("dashboard.esi.hint"));
        arrivalsTitleLabel.setText(i18n.t("dashboard.arrivals.title"));
        arrivalsHintLabel.setText(i18n.t("dashboard.arrivals.hint"));
        waitsTitleLabel.setText(i18n.t("dashboard.waits.title"));
        pipelineTitleLabel.setText(i18n.t("dashboard.pipeline.title"));
        actionsTitleLabel.setText(i18n.t("dashboard.actions.title"));
        colCode.setText(i18n.t("dashboard.col.code"));
        colPatient.setText(i18n.t("dashboard.col.patient"));
        colStatus.setText(i18n.t("dashboard.col.status"));
        colEsi.setText(i18n.t("dashboard.col.esi"));
        colWait.setText(i18n.t("dashboard.col.wait"));
    }

    private void refresh() {
        AppContext ctx = AppContext.getInstance();
        DashboardService dashboard = ctx.getDashboardService();
        I18n i18n = I18n.getInstance();

        var user = ctx.getUserSession().getCurrentUser();
        String name = user == null ? i18n.t("dashboard.guest") : user.getDisplayName();
        UserRole role = ctx.getUserSession().getRole();
        greetingLabel.setText(i18n.t("dashboard.greeting", name));
        welcomeLabel.setText(role == null
                ? i18n.t("dashboard.signedIn")
                : i18n.t("dashboard.roleLine", role.name()));
        refreshedLabel.setText(i18n.t("dashboard.refreshed", DateTimeUtil.formatDisplay(DateTimeUtil.now())));

        DashboardSnapshot snap = dashboard.loadSnapshot();
        applyFloorChip(snap);
        buildKpis(snap);
        fillPie(dashboard.esiBreakdown());
        fillBar(dashboard.arrivalsLast7Days());
        buildWaits(dashboard.longestWaits(6));
        var pipeline = dashboard.openPipeline(25);
        pipelineTable.setItems(FXCollections.observableArrayList(pipeline));
        pipelineCountLabel.setText(i18n.t("dashboard.pipeline.count", pipeline.size()));
        buildActions(ctx.getNavigationService());
    }

    private void applyFloorChip(DashboardSnapshot snap) {
        I18n i18n = I18n.getInstance();
        String key = AppContext.getInstance().getDashboardService().floorStatusKey(snap);
        floorChip.setText(i18n.t(key));
        floorChip.getStyleClass().setAll("floor-chip");
        if (snap.isQuietFloor()) {
            floorChip.getStyleClass().add("floor-quiet");
        } else if (snap.isElevated()) {
            floorChip.getStyleClass().add("floor-elevated");
        } else {
            floorChip.getStyleClass().add("floor-steady");
        }
    }

    private void buildKpis(DashboardSnapshot snap) {
        I18n i18n = I18n.getInstance();
        kpiRow.getChildren().clear();
        kpiRow.getChildren().addAll(
                kpiTile(String.valueOf(snap.openErCases()), i18n.t("dashboard.kpi.open"),
                        i18n.t("dashboard.kpi.open.cap"), "kpi-accent-blue"),
                kpiTile(String.valueOf(snap.awaitingTriage()), i18n.t("dashboard.kpi.triage"),
                        i18n.t("dashboard.kpi.triage.cap"), "kpi-accent-warn"),
                kpiTile(String.valueOf(snap.inTreatment()), i18n.t("dashboard.kpi.treatment"),
                        i18n.t("dashboard.kpi.treatment.cap"), "kpi-accent-teal"),
                kpiTile(String.valueOf(snap.waitingTickets()), i18n.t("dashboard.kpi.queue"),
                        i18n.t("dashboard.kpi.queue.cap"), "kpi-accent-violet"),
                kpiTile(String.valueOf(snap.availableDoctors()), i18n.t("dashboard.kpi.doctors"),
                        i18n.t("dashboard.kpi.doctors.cap"), "kpi-accent-green")
        );
    }

    private VBox kpiTile(String value, String title, String caption, String accentClass) {
        Region accent = new Region();
        accent.getStyleClass().addAll("kpi-accent", accentClass);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("kpi-caption");
        captionLabel.setWrapText(true);

        VBox text = new VBox(2, valueLabel, titleLabel, captionLabel);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox body = new HBox(12, accent, text);
        body.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(body);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void fillPie(Map<String, Long> esi) {
        esiPie.getData().clear();
        long total = 0;
        I18n i18n = I18n.getInstance();
        for (Map.Entry<String, Long> entry : esi.entrySet()) {
            long value = entry.getValue() == null ? 0 : entry.getValue();
            if (value <= 0) {
                continue;
            }
            total += value;
            String label = DashboardService.ESI_UNTAGGED.equals(entry.getKey())
                    ? i18n.t("dashboard.esi.untagged")
                    : entry.getKey();
            esiPie.getData().add(new PieChart.Data(label + " (" + value + ")", value));
        }
        if (total == 0) {
            esiPie.getData().add(new PieChart.Data(i18n.t("dashboard.chart.empty"), 1));
        }
    }

    private void fillBar(Map<String, Long> daily) {
        arrivalsBar.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(I18n.getInstance().t("dashboard.arrivals.series"));
        for (Map.Entry<String, Long> entry : daily.entrySet()) {
            String label;
            try {
                label = LocalDate.parse(entry.getKey()).format(DAY_LABEL);
            } catch (Exception ex) {
                label = entry.getKey();
            }
            series.getData().add(new XYChart.Data<>(label, entry.getValue()));
        }
        arrivalsBar.getData().add(series);
    }

    private void buildWaits(java.util.List<WaitItem> waits) {
        waitsList.getChildren().clear();
        if (waits.isEmpty()) {
            Label empty = new Label(I18n.getInstance().t("dashboard.waits.empty"));
            empty.getStyleClass().add("empty-state");
            waitsList.getChildren().add(empty);
            return;
        }
        for (WaitItem item : waits) {
            waitsList.getChildren().add(waitRow(item));
        }
    }

    private HBox waitRow(WaitItem item) {
        Label code = new Label(item.caseCode());
        code.getStyleClass().add("wait-code");
        Label patient = new Label(item.patientName());
        patient.getStyleClass().add("wait-patient");
        Label minutes = new Label(item.waitMinutes() + "m");
        minutes.getStyleClass().add("wait-minutes");

        Label esi = new Label(item.priority() == null ? "—" : item.priority().name());
        esi.getStyleClass().add("status-chip");
        if (item.priority() != null) {
            esi.getStyleClass().add(esiStyle(item.priority()));
        } else {
            esi.getStyleClass().add("chip-neutral");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox left = new VBox(2, code, patient);
        HBox row = new HBox(10, left, spacer, esi, minutes);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("wait-row");
        return row;
    }

    private static String esiStyle(TriagePriority priority) {
        return switch (priority) {
            case P1 -> "esi-p1";
            case P2 -> "esi-p2";
            case P3 -> "esi-p3";
            case P4 -> "esi-p4";
            case P5 -> "esi-p5";
        };
    }

    private void buildActions(NavigationService navigation) {
        I18n i18n = I18n.getInstance();
        actionsRow.getChildren().clear();
        addAction(navigation, View.EMERGENCY_ROOM, i18n.t("nav.emergency"), true);
        addAction(navigation, View.PATIENTS, i18n.t("nav.patients"), false);
        addAction(navigation, View.ROUTINE_QUEUE, i18n.t("nav.queue"), false);
        addAction(navigation, View.REPORTS, i18n.t("nav.reports"), false);
        addAction(navigation, View.DOCTORS, i18n.t("nav.doctors"), false);
        if (actionsRow.getChildren().isEmpty()) {
            Label none = new Label(i18n.t("dashboard.actions.none"));
            none.getStyleClass().add("muted");
            actionsRow.getChildren().add(none);
        }
    }

    private void addAction(NavigationService navigation, View view, String label, boolean primary) {
        if (!navigation.isVisibleForCurrentRole(view)) {
            return;
        }
        Button button = new Button(label);
        button.getStyleClass().add(primary ? "primary-button" : "secondary-button");
        button.setOnAction(e -> navigation.goTo(view));
        actionsRow.getChildren().add(button);
    }
}
