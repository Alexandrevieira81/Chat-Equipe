/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package principal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author Alexandre de Souza Vieira Ra 1488880
 */
public class Chat extends Application {

    private static Stage stage;

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("principal/FXML.fxml"));

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CHAT");
            stage.show();
            setStage(stage);

            stage.setOnCloseRequest(w -> {

                switch (JOptionPane.showConfirmDialog(null, "Deseja Fechar o Programa? ", "Confirmação", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE)) {

                    case 0:
                        System.out.println("Fechou o socket");

                        try {
                            FXMLController.clientSocket.sendMsg(null);
                            FXMLController.clientSocket.closeInOut();
                            stage.close();
                        } catch (IOException ex) {
                            System.out.println("Cliente já desconectou !");
                        } catch (NullPointerException e) {
                            System.out.println("Cliente nem tentou conexão!");
                            stage.close();
                        }

                        break;

                    case 1:
                        w.consume();
                        break;
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        Chat.stage = stage;
    }

}
