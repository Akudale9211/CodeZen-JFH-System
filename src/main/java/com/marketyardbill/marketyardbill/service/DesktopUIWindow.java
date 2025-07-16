package com.marketyardbill.marketyardbill.service;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class DesktopUIWindow extends Application {

    private static String url;

    public static void openURL(String urlToOpen) {
        url = urlToOpen;
        Application.launch();
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().load(url);

        stage.setTitle("My App UI");
        stage.setScene(new Scene(webView, 1024, 768));
        stage.setOnCloseRequest(e -> {
            System.out.println("Window closed. Shutting down...");
            System.exit(0); // This kills the Spring Boot server too
        });

        stage.show();
    }
}