/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package principal;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static principal.Chat.setStage;

/**
 * FXML Controller class
 *
 * @author MASTER
 */
public class FXMLCadastroController implements Initializable {

    @FXML
    private TextField textCSenha;
    @FXML
    private TextField textCRa;
    @FXML
    private TextField textCNome;
    @FXML
    private TextArea textAreaCDescricao;
    @FXML
    private ComboBox<String> comboBoxCCategoria;
    @FXML
    private Button btnCadastrar;

    ChatClient cliente;
    ClientSocket clientSocket;
    @FXML
    private Button btnVoltar;
    private Integer categoria = -1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        /*
            Cria um objeto ChatClient para que possamos usar
            todas as funções dele como o métodos start que cria o socket
         */
        cliente = new ChatClient();
        

        ObservableList<String> categorias = FXCollections.observableArrayList("programador", "eletricista","mecanico","cientista","professor","analista","gamer","stremer");//colocar o resto das categorias
        comboBoxCCategoria.setItems(categorias);
        // TODO
    }

    @FXML
    private void Cadastrar(ActionEvent event) throws IOException {

        /*
            Aqui o objeto cliente do tipo ChatClient retorna um clientSocket
            com ele realizamos todas as operações necessárias
         */
        clientSocket = cliente.start();

        /*
            para não extender classe  e implementar o médoto Run
            utilizamos uma função anônima seguida de uma expressão lambda,
            dessa forma é possível passar a função clientMessageReturnLoop()
            para a Thread como parâmetro.
            
         */
        new Thread(() -> clientMessageReturnLoop()).start();

        /* {
          {
            "operacao": "cadastrar",
                "parametros": {
                                "nome": "Jacúncio José",
                                "ra": "2098270",
                                "senha": "010203",
                                "categoria_id": 3,
                                "descricao": "Estudante de Engenharia de Software"
                                }
             }
            }
         */
 /*
            Aqui é meio chato de trabalhar, o macete é criar os mais internos
            e depois de criado da o put deles nos mais externos
         */
        JSONObject params = new JSONObject();
        params.put("nome", textCNome.getText());
        params.put("ra", textCRa.getText());
        params.put("senha", textCSenha.getText());
        params.put("categoria_id", (comboBoxCCategoria.getSelectionModel().getSelectedIndex()));
        params.put("descricao", textAreaCDescricao.getText());

        JSONObject obj = new JSONObject();
        obj.put("operacao", "cadastrar");
        obj.put("parametros", params);

        try {
            /*
                note que aqui ocorre uma conversão para string, isso para enviar no buffer
             */
            System.out.println("Protocolo de Cadastro: " + obj.toJSONString());
            cliente.EnviarCadastro(obj.toJSONString());
            // cliente.messageLoop("{\"ra\":\"" + textUsuario.getText() + "\",\"senha\":\"" + textSenha.getText() + "\"}");
            // Envia uma mensagem no momento da conexão para identificar o Cliente
        } catch (IOException ex) {
            Logger.getLogger(FXMLCadastroController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void clientMessageReturnLoop() {

        String msg;
        Integer status = 0;
        JSONObject json;
        JSONParser parser = new JSONParser();
        String operacao = " ";
        String mensagem = " ";

        while ((msg = clientSocket.getMessage()) != null) {//pega um interrupção de conexão do servidor, Ex: Queda

            operacao = " ";//realimentar a variável a cada entrada do loop previve um problema 
            //caso o json venha com formato inconsistente ou de pegar informações de um cadastro anterior
            System.out.println("Retorno do Servidor: " + msg);

            status = null;
            try {

                /*
                    Foi craido um objeto JSONObject "json" que vai receber o conteúdo
                    "fatiado" pelo JSONParser "parser"
                 */
                json = (JSONObject) parser.parse(msg);//aqui ele realimenta as variáveis

                /*
                    Aqui tem que verificar o protocolo, mas acho que não retorna a operação
                    no cadastro. De qualquer forma os testes como: if ((json.get("status")) != null)
                    evitam que o programa trave caso alguma dessa chaves venha nula
                 */
                if ((json.get("status")) != null) {

                    status = Integer.parseInt(json.get("status").toString());

                } else {
                    status = 0;
                }

                if (json.get("operacao") != null) {  //Não colocar to string de jeito nenhum da problema
                    operacao = json.get("operacao").toString();
                } else {
                    operacao = " ";
                }

                if (json.get("mensagem") != null) {  //Não colocar to string de jeito nenhum da problema
                    mensagem = json.get("mensagem").toString();
                } else {
                    mensagem = " ";
                }

            } catch (ParseException ex) {
                System.out.println("Formato de Protocolo Incoreto!");
            } catch (NullPointerException ev) {
                System.out.println("Servidor enviou parâmentro nulo!");
            }

            if (status == 201) {
                System.out.println("Cadastrado com Sucesso!");
                break;

            } else if (status == 202) {
                System.out.println(mensagem);
                break;

            } else if (status == 400) {
                System.out.println("Dados não Correspondem com a Operação!");
                break;

            } else if (status == 500) {
                System.out.println("Formato de protocolo Inválido!");
                break;
            }
        }

        try {
            clientSocket.closeInOut();//função fecha o Socket
            System.out.println("Desconectado do Servidor");

        } catch (IOException ex) {
            System.out.println("Problemas ao encerrar conexão");
        }

    }

    public void start(Stage stage) throws IOException {

        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("principal/FXMLCadastro.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Cadastro");
            stage.show();
            setStage(stage);

        } catch (Exception e) {
        }

    }

    private void selecionarCategoria(ActionEvent event) {

        /*
            semelhante o que ocorre em usuário online só que no combobox
         */
        if (comboBoxCCategoria.getSelectionModel().getSelectedIndex() > -1) {
            consultar(event);
        }
    }

    private void consultar(ActionEvent event) {

        /*
            Aqui vai chamar a função pra obter a lista de usuários online
            dai vai passa o parâmetro da categoria para filtrar, por enquanto ele
            só pritn a seleção
         */
        //comboBoxCCategoria.getSelectionModel().getSelectedItem();
        this.categoria = comboBoxCCategoria.getSelectionModel().getSelectedIndex();

    }

    @FXML
    private void voltarLogin(ActionEvent event) {
        FXMLController l = new FXMLController();
        fecha();
        try {
            l.start(new Stage());
        } catch (Exception e) {
        }
    }

    public void fecha() {
        Chat.getStage().close();
    }

}
