package com.binge;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Button button = new Button("Hello, JavaFX");
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(button);
        Scene scene = new Scene(borderPane, 300, 300);
        stage.setScene(scene);

        stage.setTitle("Binge");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(Main.class, args);	//啟動JavaFX，自動呼叫start(Stage)
    }
}
