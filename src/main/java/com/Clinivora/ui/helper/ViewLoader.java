package com.Clinivora.ui.helper;

import com.Clinivora.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.function.Consumer;

public final class ViewLoader {
    private ViewLoader() {
    }

    public static Parent load(String fxmlClasspath) {
        return load(fxmlClasspath, null);
    }

    public static <T> Parent load(String fxmlClasspath, Consumer<T> controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlClasspath));
            try {
                loader.setResources(com.Clinivora.ui.i18n.I18n.getInstance().getBundle());
            } catch (IllegalStateException ignored) {
                // I18n may be uninitialized in isolated unit tests
            }
            Parent root = loader.load();
            if (controllerConfigurer != null) {
                @SuppressWarnings("unchecked")
                T controller = loader.getController();
                controllerConfigurer.accept(controller);
            }
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlClasspath, e);
        }
    }

    public static <T> LoadedView<T> loadWithController(String fxmlClasspath) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlClasspath));
            try {
                loader.setResources(com.Clinivora.ui.i18n.I18n.getInstance().getBundle());
            } catch (IllegalStateException ignored) {
                // I18n may be uninitialized in isolated unit tests
            }
            Parent root = loader.load();
            return new LoadedView<>(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlClasspath, e);
        }
    }

    public record LoadedView<T>(Parent root, T controller) {
    }
}
