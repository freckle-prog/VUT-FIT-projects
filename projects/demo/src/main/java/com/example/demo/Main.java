package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.ToolBarSkin;
import javafx.scene.control.skin.TooltipSkin;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.spi.ToolProvider;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader root = new FXMLLoader(Main.class.getResource("main-scene.fxml"));
        Scene scene = new Scene(root.load(), 600, 413);
        stage.setTitle("UML Editor");
        stage.getIcons().add(new Image("C:\\Users\\lebed\\projects\\demo\\lib\\IMG_20200306_172134.jpg"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}