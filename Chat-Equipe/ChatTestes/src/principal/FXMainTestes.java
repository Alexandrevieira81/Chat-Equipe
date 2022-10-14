/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package principal;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexandre de Souza Vieira Ra 1488880 
 */
public class FXMainTestes extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent parent= FXMLLoader.load(getClass().getClassLoader().getResource("principal/FXMLCadastro.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CADASTRO CHAT SISTEMAS DISTRIBUÍDOS!");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

