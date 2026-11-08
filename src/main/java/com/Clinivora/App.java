package com.Clinivora;

import com.Clinivora.config.AppContext;
import com.Clinivora.config.UserSettingsStore;
import com.Clinivora.ui.helper.ViewLoader;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.ui.theme.ThemeManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;
    private static UserSettingsStore settingsStore;

    @Override
    public void start(Stage stage) {
        loadFonts();
        primaryStage = stage;

        settingsStore = new UserSettingsStore();
        settingsStore.load();
        ThemeManager.initialize(settingsStore);
        I18n.initialize(settingsStore);

        AppContext.getInstance().bootstrap();

        Parent root = ViewLoader.load("/com/Clinivora/view/LoginView.fxml");
        Scene scene = new Scene(root, 1200, 780);
        ThemeManager.getInstance().applyToScene(scene);
        I18n.getInstance().applyOrientation(scene);

        stage.setTitle(I18n.getInstance().t("app.title"));
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/images/clinivora-icon.png")));
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/images/clinivora-logo.png")));
        stage.setScene(scene);
        stage.show();
    }

    private static void loadFonts() {
        Font.loadFont(App.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"), 13);
        Font.loadFont(App.class.getResourceAsStream("/fonts/Roboto-Medium.ttf"), 13);
        Font.loadFont(App.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"), 13);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static UserSettingsStore getSettingsStore() {
        return settingsStore;
    }

    public static void showLogin() {
        Parent root = ViewLoader.load("/com/Clinivora/view/LoginView.fxml");
        replaceRoot(root);
    }

    public static void showMainShell() {
        Parent root = ViewLoader.load("/com/Clinivora/view/MainShellView.fxml");
        replaceRoot(root);
    }

    private static void replaceRoot(Parent root) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1200, 780);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        ThemeManager.getInstance().applyToScene(scene);
        I18n.getInstance().applyOrientation(scene);
        primaryStage.setTitle(I18n.getInstance().t("app.title"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
