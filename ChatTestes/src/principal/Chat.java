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
import javafx.application.Platform;
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

            FXMLLoader floader = new FXMLLoader(getClass().getResource("/principal/FXML.fxml")); 
            Parent root =(Parent) floader.load();
            
            FXMLController controle = floader.<FXMLController>getController();
            

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("CHAT");
            stage.show();
            setStage(stage);

            stage.setOnCloseRequest(w -> {

                switch (JOptionPane.showConfirmDialog(null, "Deseja Fechar o Programa? ", "Confirmação", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE)) {

                    case 0:
                        

                        try {
                        /*
                            Aqui ele pega o evento onClose da janela, pode notar que ele fecha 
                            o socket da janela do controller caso o usuário escolha o sim na janela
                            de confirmação. Antes de fechar ele mandava um null para matar a Thread
                            no servidor, removi por enquanto, pois estava bugando o servidor dos outros
                         */
                        //FXMLController.clientSocket.sendMsg(null);
                        //FXMLController.clientSocket.closeInOut();
                        controle.logout();
                        Platform.exit();
                        System.exit(0);

                    } catch (NullPointerException e) {

                        System.out.println("Cliente nem tentou conexão!");
                        Platform.exit();
                        System.exit(0);

                    } catch (Exception ex) {
                        Platform.exit();
                        System.exit(0);
                    }

                    break;

                    case 1:
                        /*
                            Se escolher não na janela ele cai aqui, essa função consume 
                            não deixa a janela fechar
                         */
                        w.consume();
                        break;
                }
            }
            );
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
