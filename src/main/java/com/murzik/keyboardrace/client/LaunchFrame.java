package com.murzik.keyboardrace.client;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Random;

public class LaunchFrame extends Application {
    private final String IP = "localhost";
    private final String port = "5619";
    private Stage stage;
    private final String[] names = {
            "Miku", "Meiko", "Meiko",
            "Kaito", "Rin", "Len",
            "Luka", "Gumi", "Lily",
            "Icy", "Darcy", "Stormy",
            "Bloom", "Stella", "Flora",
            "Musa", "Tecna", "Aisha",
            "Idk", "Lol", "Valtor",
            "Privalov", "Nevstruev", "Kamnoedov",
            "Demin", "Kivrin", "Hunta",
            "Los'", "Gusev", "Aelita", "Alex"
    };

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Klavogonki");
        GridPane pane = createLaunchMenuPane();

        Scene scene = new Scene(pane, 800, 600);
        stage.setScene(scene);

        stage.show();
    }

    private GridPane createLaunchMenuPane() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(40, 40, 40, 40));
        pane.setVgap(10);

        Label header = new Label("Клавогонки");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pane.add(header, 0,0,2,1);
        GridPane.setHalignment(header, HPos.CENTER);
        GridPane.setMargin(header, new Insets(20, 0,20,0));

        //IP-address
        Label IPLabel = new Label("Адрес сервера");
        IPLabel.setFont(Font.font("Arial",16));
        pane.add(IPLabel, 0,1);
        TextField IPField = new TextField(IP);
        IPField.setFont(Font.font("Arial",14));
        IPField.setStyle("-fx-text-fill: gray;");
        IPField.setPrefHeight(30);
        pane.add(IPField, 0,2);


        //Port
        Label portLabel = new Label("Порт");
        portLabel.setFont(Font.font("Arial",16));
        pane.add(portLabel, 0,3);
        TextField portField = new TextField(port);
        portField.setFont(Font.font("Arial",14));
        portField.setStyle("-fx-text-fill: gray;");
        portField.setPrefHeight(30);
        pane.add(portField, 0,4);

        //Name
        Label nameLabel = new Label("Ваше имя");
        nameLabel.setFont(Font.font("Arial",16));
        pane.add(nameLabel, 0,5);
        TextField nameField = new TextField(
                names[new Random().nextInt(names.length)]
        );
        nameField.setFont(Font.font("Arial",14));
        nameField.setStyle("-fx-text-fill: gray;");
        nameField.setPrefHeight(30);
        pane.add(nameField, 0,6);

        //Errors
        Text er = new Text("");
        er.setFont(Font.font("Arial",14));
        er.setFill(Color.RED);
        GridPane.setHalignment(er, HPos.CENTER);
        GridPane.setMargin(er, new Insets(10, 0, 0,0));
        pane.add(er, 0, 7);

        //Button Start
        Button startButton = new Button("Начать");
        startButton.setPrefHeight(50);
        startButton.setDefaultButton(true);
        startButton.setPrefWidth(160);
        startButton.setFont(Font.font("Arial",16));
        pane.add(startButton, 0, 8, 2, 1);
        GridPane.setHalignment(startButton, HPos.CENTER);

        //Info Button
        Button infoButton = new Button("Об игре");
        infoButton.setPrefHeight(50);
        infoButton.setDefaultButton(true);
        infoButton.setPrefWidth(160);
        infoButton.setFont(Font.font("Arial",16));
        pane.add(infoButton, 0, 9);
        GridPane.setHalignment(infoButton, HPos.CENTER);
        GridPane.setMargin(infoButton, new Insets(0, 0,0,0));

        IPField.setOnMouseClicked(event -> IPField.selectAll());
        portField.setOnMouseClicked(event -> portField.selectAll());
        nameField.setOnMouseClicked(event -> nameField.selectAll());

        IPField.setOnKeyPressed(event -> changeColor(IPField));
        portField.setOnKeyPressed(event -> changeColor(portField));
        nameField.setOnKeyPressed(event -> changeColor(nameField));

        infoButton.setOnAction(event -> {

            Stage newStage = new Stage();
            newStage.initOwner(stage);
            newStage.initModality(Modality.WINDOW_MODAL);

            new InfoFrame().start(newStage);
        });

        startButton.setOnAction(event -> {
            er.setText("");
            if (
                    !portField.getText().isEmpty()
                            && !IPField.getText().isEmpty()
                            && !nameField.getText().isEmpty()
            ) {
                try {
                    String serverHost = IPField.getText();
                    int serverPort = Integer.parseInt(portField.getText());

                    Stage newStage = new Stage();
                    newStage.initOwner(stage);
                    newStage.initModality(Modality.WINDOW_MODAL);

                    new GameFrame(nameField.getText(), serverPort, serverHost).start(newStage);

                } catch (NumberFormatException ex){
                    ex.printStackTrace();
                }


            }
            else {
                er.setText("Заполните все поля.");
            }
        });

        return pane;
    }



    public static void changeColor (TextField field) {
        field.setStyle("-fx-text-fill: black;");
    }


    public static void main(String[] args) {
        launch();
    }
}
