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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        params.put("categoria_id", Integer.parseInt(comboBoxCCategoria.getPromptText()));
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

}
