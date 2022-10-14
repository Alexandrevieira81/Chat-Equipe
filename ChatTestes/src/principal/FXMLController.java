/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package principal;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.zone.ZoneRulesProvider.refresh;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javax.swing.JOptionPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 *
 * @author MASTER
 */
public class FXMLController implements Initializable {

    //private TextField textPalavra;
    //private TextArea textAreaPalavra;
    @FXML
    private Button btnConectar;
    ChatClient cliente;
    @FXML
    private Button btnEnviar;
    @FXML
    private Button btnDesconectar;
    @FXML
    private TextArea textAreaChat;
    @FXML
    private TextField textFieldMensagem;

    @FXML
    private TextField textUsuario;
    @FXML
    private Label lbSenha;
    @FXML
    private TextField textSenha;
    @FXML
    private Button btnUsuarios;

    @FXML
    private ListView<Usuario> listViewUsersOnline;
    ObservableList<Usuario> onLines;
    private static String chatPrivado; //responsável por direcionar o chat
    public static ClientSocket clientSocket;
    @FXML
    private ComboBox<String> comboBoxCategoria;
    private ObservableList<String> categorias;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*
            Semelhante a classe ChatServer no servidor, controla as operações de entrada e saída
            e crição dos sockets
         */
        cliente = new ChatClient();
        chatPrivado = " ";
        ObservableList<String> categorias = FXCollections.observableArrayList("1-Pedreiro", "2-Eletricista");
        comboBoxCategoria.setItems(categorias);

    }

    // TODO
    @FXML
    private void conectar(ActionEvent event) throws Exception {

        try {

            /*
                cria um novo socket os parâmentros porta e servidor estão fixos na classe
                ChatClient
             */
            if (clientSocket.getSocket().getRemoteSocketAddress()!= null) {
                System.out.println(clientSocket.getSocket().getRemoteSocketAddress().toString());
                JSONObject params = new JSONObject();
                params.put("ra", textUsuario.getText());
                params.put("senha", textSenha.getText());

                JSONObject obj = new JSONObject();
                obj.put("operacao", "login");
                obj.put("parametros", params);

                cliente.LogarDeslogar(obj.toJSONString());
                new Thread(() -> clientMessageReturnLoop()).start();

            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Servidor Encontra-se Offline!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        } catch (NullPointerException e) {

            clientSocket = cliente.start();

            /*
                esse método apenas envia uma mensagem se adaptar nada, o servidor vai tratar 
                ela e ver se se tratá de login ou logout
             */
            JSONObject params = new JSONObject();
            params.put("ra", textUsuario.getText());
            params.put("senha", textSenha.getText());

            JSONObject obj = new JSONObject();
            obj.put("operacao", "login");
            obj.put("parametros", params);

            cliente.LogarDeslogar(obj.toJSONString());
            new Thread(() -> clientMessageReturnLoop()).start();

        }

    }

    @FXML
    private void desconectarChat(ActionEvent event) throws Exception {

        try {

            /*
                Semelhante o função logar,porém com diferença na operação
                e nos controles de interface
             */
            JSONObject params = new JSONObject();
            params.put("ra", textUsuario.getText());
            params.put("senha", textSenha.getText());

            JSONObject obj = new JSONObject();
            obj.put("operacao", "logout");
            obj.put("parametros", params);
            cliente.LogarDeslogar(obj.toJSONString());
            btnEnviar.setDisable(true);
            btnDesconectar.setDisable(true);
            btnConectar.setDisable(false);
            listViewUsersOnline.getItems().clear();
            clientSocket.closeInOut();
            clientSocket=null;
            

        } catch (IOException ex) {
            System.out.println("Problemas ao Encerrar Conexão");
        }
    }

    @FXML
    private void enviarMensagem(ActionEvent event) throws IOException {

        if (textFieldMensagem.getText().equals("")) {
            System.out.println("Campo está vazio: Digite algo");
        } else {

            textAreaChat.appendText("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "]" + " EU: " + textFieldMensagem.getText() + "\n");
            // Essa função envia uma mensage privada só precisa da mensagem e do valor do chatPrivado,sendo que o chatPrivado contém o ra do usuário
            cliente.messageLoop(textFieldMensagem.getText(), chatPrivado);
            textFieldMensagem.clear();
        }
    }

    public void clientMessageReturnLoop() {

        String msg;
        Integer status = 0;
        JSONObject json = null;
        JSONParser parser = new JSONParser();
        String operacao = " ";
        JSONObject dados = null;

        //Loop "Infinito"
        // Aqui já recebe a mensagem pelo método clientSocket.getMessage() e testa se seu valor não está nulo
        while ((msg = clientSocket.getMessage()) != null) {

            status = null;
            try {

                json = (JSONObject) parser.parse(msg);//trasforma a msg vinda em formato simpel json
                dados = (JSONObject) json.get("dados");
                System.out.println(msg);
                if ((json.get("status")) != null) {
                    /*
                        como vem valor inteiro do status precisa pegar a string pelo toString e depois converte-lo,
                        isso evita erros de tipo.
                     */
                    status = Integer.parseInt(json.get("status").toString());

                } else {
                    status = 0;
                }

                if (json.get("operacao") != null) {  //Não colocar to string de jeito nenhum na condição de comparação
                    operacao = json.get("operacao").toString();// Aqui pode toString
                } else {
                    operacao = " ";
                }

            } catch (ParseException ex) {
                System.out.println("Formato de Protocolo Incoreto!");
            } catch (NullPointerException ev) {
                System.out.println("Resposta não Enviada do Servidor");
            }

            if (operacao.equals("mensagem")) {

                textAreaChat.appendText(json.get("mensagem").toString() + "\n");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (((status == 200)) && (dados.containsKey("usuarios"))) {

                atualizarUsuariosOnline(dados);
                System.out.println("Lista de Usuários Online");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (status == 200 && (!dados.containsKey("usuarios"))) {

                System.out.println("Logado com Sucesso!");
                btnEnviar.setDisable(false);
                btnDesconectar.setDisable(false);
                btnConectar.setDisable(true);

            }
            if (status == 403) {

                System.out.println("Cliente já está Conectado!");
                btnEnviar.setDisable(true);
                btnDesconectar.setDisable(true);
                btnConectar.setDisable(false);
                break;

            }

            if (status == 404) {

                System.out.println("Login ou Senha Incorretos!");
                btnEnviar.setDisable(true);
                btnDesconectar.setDisable(true);
                btnConectar.setDisable(false);
                break;

            }

            if (status == 400) {
                System.out.println("Dados não Correspondem com a Operação!");
                break;

            }

            if (status == 600) { //Desconecta pela solicitação do usuário
                System.out.println("Desconectado com Sucesso!");
                break;
            }

        }
        System.out.println("Passou pelo null depois do fechamento");

        /*
        try {
            clientSocket.closeInOut();//função fecha o Socket
            System.out.println("Desconectado do Servidor");
            btnEnviar.setDisable(true);
            btnDesconectar.setDisable(true);
            btnConectar.setDisable(false);

        } catch (IOException ex) {
            System.out.println("Problemas ao encerrar conexão");
        }
         */
    }

    @FXML
    private void listarUsuarios(ActionEvent event) {

        try {
            cliente.carregaUsuarios(textSenha.getText(), textUsuario.getText());

        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void atualizarUsuariosOnline(JSONObject dados) {

        Usuario user;
        JSONObject aux;

        ObservableList<Usuario> onLinesAux = FXCollections.observableArrayList();

        /*
            Como o retorno da lista está com status 200 no protocolo, burrice 
            repetir já que nós que decidiríamos essa flags, mas paciência, é necessário
            diferenciar através da sub-chave usuarios. Lembrando que usuarios está dentro da 
            chave dados e essa função recebe dados como parâmetro
         */
        //Esse If testa se a chave usuarios existe
        if (dados.containsKey("usuarios")) {

            JSONArray usuarios = (JSONArray) dados.get("usuarios");

            for (int i = 0; i < usuarios.size(); i++) {

                aux = (JSONObject) usuarios.get(i);
                user = new Usuario();
                user.setNome(aux.get("nome").toString());

                user.setDisponibilidade(Integer.parseInt(aux.get("disponivel").toString()));

                user.setRa(aux.get("ra").toString());
                onLinesAux.add(user);

            }

            /*
                Threads que atualizam interface devem ser executadas dentro da Thread 
                JavaFX, por isso o controle da "carregarListaUsuario(onLinesAux);"
                é entregua para o método Platform.runLater(() ->, pois assim evita
                uma exceção.
             */
            Platform.runLater(() -> {
                carregarListaUsuario(onLinesAux);

                refresh();
            });

        } else {
            System.out.println("Lista Vazia");
        }

    }

    //Atualiza a Interface
    public void carregarListaUsuario(ObservableList<Usuario> on) {

        listViewUsersOnline.getItems().clear();
        onLines = FXCollections.observableArrayList(on);
        listViewUsersOnline.setItems(onLines);

    }

    //Captura a seleção do mouse no ListView do usuários online
    @FXML
    private void selecionarUsuarioOnline(MouseEvent event) {

        if (listViewUsersOnline.getSelectionModel().getSelectedIndex() > -1) {
            consultarUsuarioOnline(event);

        }

    }

    //Seta a variável que controla o fluxo do chat privado a cada clique do mouse 
    //no listView de usuários online
    private void consultarUsuarioOnline(MouseEvent event) {

        Usuario registroSel = listViewUsersOnline.getSelectionModel().getSelectedItem();
        chatPrivado = registroSel.getRa();
    }

    @FXML
    private void selecionarCategoria(ActionEvent event) {

        if (comboBoxCategoria.getSelectionModel().getSelectedIndex() > -1) {
            consultar(event);
        }
    }

    private void consultar(ActionEvent event) {

        comboBoxCategoria.getSelectionModel().getSelectedItem();
        System.out.println(comboBoxCategoria.getSelectionModel().getSelectedItem());

    }

}
