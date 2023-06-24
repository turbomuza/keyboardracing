package com.murzik.keyboardrace.client;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InfoFrame extends Application  {
    @Override
    public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        Label info = new Label("Автор: MUZYA\n");
        info.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pane.add(info, 0,0,1,1);
        GridPane.setHalignment(info, HPos.CENTER);
        GridPane.setMargin(info, new Insets(20, 0,20,0));

        Scene scene = new Scene(pane, 300, 300);
        stage.setScene(scene);

        stage.show();
    }

}
