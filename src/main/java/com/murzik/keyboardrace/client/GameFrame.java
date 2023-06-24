package com.murzik.keyboardrace.client;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class GameFrame extends Application {
    private final String name;
    private final Socket socket;
    private final BufferedWriter bufferedWriter;
    private final BufferedReader bufferedReader;
    private final long startingTime;

    private final Timer timer =  new Timer();

    GameFrame(String name, int serverPort, String serverHost) {
        this.name = name;
        try {
            socket = new Socket(serverHost, serverPort);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 20240);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 20240);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            bufferedWriter.write(name + "\n");
            bufferedWriter.flush();

            startingTime = Long.parseLong(bufferedReader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //client.write(name);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Klavogonki");
        GridPane pane = createGamePanel();

        Scene scene = new Scene(pane, 800, 600);
        stage.setScene(scene);

        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);

        stage.show();
    }

    private GridPane createGamePanel() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(40, 40, 40, 40));
        pane.setVgap(10);

        Label header = new Label("Клавогонки");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pane.add(header, 0,0,3,1);
        GridPane.setHalignment(header, HPos.CENTER);
        GridPane.setMargin(header, new Insets(20, 0,20,0));

        //Players table
        Label playersLabel = new Label("Игроки");
        playersLabel.setFont(Font.font("Arial",16));
        GridPane.setHalignment(playersLabel, HPos.CENTER);
        GridPane.setMargin(playersLabel, new Insets(0, 0,20,0));
        pane.add(playersLabel, 0,1, 2, 1);

        TextArea playersField = new TextArea("\n\n");
        playersField.setFont(Font.font("Arial",14));
        playersField.setPrefHeight(65);
        playersField.setPrefWidth(600);
        playersField.setEditable(false);
        pane.add(playersField, 0,2);
        GridPane.setMargin(playersField, new Insets(0, 10,0,0));

        //Timer
        TextArea timerField = new TextArea("До начала игры:\n        30 сек");
        timerField.setFont(Font.font("Arial",14));
        timerField.setPrefHeight(60);
        timerField.setPrefWidth(140);
        timerField.setEditable(false);
        pane.add(timerField, 1,2);

        //Text
        Label textLabel = new Label("Текст для ввода");
        textLabel.setFont(Font.font("Arial",16));
        GridPane.setHalignment(textLabel, HPos.CENTER);
        GridPane.setMargin(textLabel, new Insets(0, 0,20,0));
        pane.add(textLabel, 0,3, 2, 1);


        TextArea textField = new TextArea("Текст для ввода появится здесь за 5 сек до начала игры...");
        textField.setFont(Font.font("Arial",14));
        textField.setPrefHeight(120);
        textField.setPrefWidth(600);
        textField.setEditable(false);
        textField.setWrapText(true);
        pane.add(textField, 0,4, 2, 1);

        //Input
        Label inputLabel = new Label("Вводить текст здесь");
        inputLabel.setFont(Font.font("Arial",16));
        GridPane.setHalignment(inputLabel, HPos.CENTER);
        pane.add(inputLabel, 0,5, 2, 1);

        Label errorLabel = new Label("Исправьте ошибку!!!");
        errorLabel.setFont(Font.font("Arial",12));
        GridPane.setHalignment(errorLabel, HPos.LEFT);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);
        pane.add(errorLabel, 0,6, 1, 1);

        TextArea inputField = new TextArea();
        inputField.setFont(Font.font("Arial",14));
        inputField.setPrefHeight(90);
        inputField.setPrefWidth(600);
        inputField.setEditable(false);
        textField.setWrapText(true);
        pane.add(inputField, 0,7, 2, 1);

        timer.scheduleAtFixedRate(new UpdateInfoBar(playersField, timerField, textField, inputField, errorLabel), 1000, 500);

        return pane;
    }

    private void closeWindowEvent(WindowEvent event) {
        System.out.println("Window close request ...");
        timer.cancel();
        stop();
    }

    @Override
    public void stop() {
        try {

            bufferedWriter.write("exit\n");
            bufferedWriter.flush();

            bufferedWriter.close();
            bufferedReader.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Stage is closing");
    }

    class UpdateInfoBar extends TimerTask {
        private final TextArea infoField;
        private final TextArea timerField;
        private final Label errorLabel;
        private final TextArea textField;
        private final TextArea inputField;

        private boolean needText = false;

        UpdateInfoBar(TextArea infoField, TextArea timerField, TextArea textField, TextArea inputField, Label errorLabel) {
            this.infoField = infoField;
            this.timerField = timerField;
            this.textField = textField;
            this.inputField = inputField;
            this.errorLabel = errorLabel;
        }

        public void run() {
            String text;
            try {

                if (needText) {
                    bufferedWriter.write("needText\n");
                    bufferedWriter.flush();


                    text = bufferedReader.readLine();
                    while (text.equals("")) {
                        text = bufferedReader.readLine();
                    }
                    textField.setText(text);
                    needText = false;
                }


                bufferedWriter.write("ok\n");
                bufferedWriter.flush();

                text = bufferedReader.readLine();


                long gameTime = 0;
                while (gameTime == 0) {
                    try {
                        gameTime = (Long.parseLong(text) - startingTime) / 1000;
                    } catch (NumberFormatException e) {
                        text = bufferedReader.readLine();
                    }
                }
                System.out.println(name + " " + text);

                if (gameTime <= 30) {
                    timerField.setText("До начала игры:\n        "
                            + (30 - gameTime)
                            + " сек");

                    updateInfo(false);

                    if (gameTime == 25) {
                        needText = true;
                    }
                    if (gameTime == 30) {
                        inputField.setEditable(true);
                    }
                }
                //else if (gameTime <= 40) {
                else if (gameTime <= 210) {
                    timerField.setText("До конца игры:\n        "
                            + (180 - gameTime + 30)
                            + " сек");

                    updateInfo(false);

                    bufferedWriter.write("sendText\n");
                    inputField.setWrapText(true);
                    bufferedWriter.write(inputField.getText() + "\n");
                    bufferedWriter.flush();

                    text = bufferedReader.readLine();
                    while (text.equals("")) {
                        text = bufferedReader.readLine();
                    }
                    errorLabel.setVisible(text.equals("incorrect"));

                }
                else {
                    bufferedWriter.flush();
                    bufferedWriter.write("end\n");
                    bufferedWriter.flush();

                    updateInfo(true);

                    inputField.setEditable(false);
                    inputField.setText("Игра завершена.");

                    timer.cancel();
//                    bufferedWriter.write("exit\n");
//                    bufferedWriter.flush();
                }

            } catch (IOException e) {
                throw new RuntimeException();
            }

        }
        private void updateInfo(boolean end) {

            try {
                String text = bufferedReader.readLine();

                while (text.equals("")) {
                    text = bufferedReader.readLine();
                }
                if (end)
                    text = bufferedReader.readLine();
                text = text.replaceAll(";", "\n");
                infoField.setText(text);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
