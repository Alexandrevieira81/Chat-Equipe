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
        ObservableList<String> categorias = FXCollections.observableArrayList("1-Pedreiro", "2-Eletricista");//colocar o resto das categorias
        comboBoxCategoria.setItems(categorias);

    }

    // TODO
    @FXML
    private void conectar(ActionEvent event) throws Exception {

        try {

            /*
                cria um novo socket os parâmentros porta e servidor estão fixos na classe
                ChatClient
                Esse if verifica se já existe uma conexão aberta, caso já exista ele reutiliza
                o socket já aberto
             */
            if (clientSocket.getSocket().getRemoteSocketAddress() != null) {
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

            if (clientSocket != null) {
                /*
                    Caso o clientSocket esteja nulo ele cria um novo socket
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

    }

    @FXML
    private void desconectarChat(ActionEvent event) throws Exception {

        try {

            /*
                Semelhante o função logar,porém com diferença na operação
                e nos controles de interface, o tratamento é diferente no servidor
                devido a operacaologout
             */
            JSONObject params = new JSONObject();
            params.put("ra", textUsuario.getText());
            params.put("senha", textSenha.getText());

            JSONObject obj = new JSONObject();
            obj.put("operacao", "logout");
            obj.put("parametros", params);
            cliente.LogarDeslogar(obj.toJSONString());

        } catch (IOException ex) {
            System.out.println("Problemas ao Encerrar Conexão");
        }
    }

    @FXML
    private void enviarMensagem(ActionEvent event) throws IOException {

        if (textFieldMensagem.getText().equals("")) { //Evitar que o usuário mande vazio
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
                dados = (JSONObject) json.get("dados"); //método pega o valor da chave dados
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
                    /*
                        sleep evita bagunçar o chat
                    */
                            
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (((status == 200)) && (dados.containsKey("usuarios"))) {
                
                /*
                    Por enquanto é a forma de diferenciar quando é login e lista é pela 
                    key usuarios, porém acho que vão mudar isso
                */
                atualizarUsuariosOnline(dados);
                System.out.println("Lista de Usuários Online");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (status == 200 && (!dados.containsKey("usuarios"))) {

                System.out.println("Logado com Sucesso!");
                btnEnviar.setDisable(false);
                btnDesconectar.setDisable(false);
                btnConectar.setDisable(true);

            } else if (status == 202) { //Desconecta pela solicitação do usuário
                
                System.out.println("Usuário já encontra-se Desconectado!");

            } else if (status == 403) {

                System.out.println("Cliente já Encontra-se Conectado!");
                btnEnviar.setDisable(true);
                btnDesconectar.setDisable(true);
                btnConectar.setDisable(false);
                break;

            } else if (status == 400) {
                System.out.println("Parâmetros enviados não correspondem à operação!");
                break;

            } else if (status == 404) {

                System.out.println("Usuário não encontrado ou Usuário ou senha inválido!");
                //break;

            } else if (status == 500) {

                System.out.println("Formato de protocolo Inválido!");

                break;
            } else if (status == 600) { //Desconecta pela solicitação do usuário
                btnEnviar.setDisable(true);
                btnDesconectar.setDisable(true);
                btnConectar.setDisable(false);
                
                /*
                    Platform.runLater importante, pois entrega o controle para Thread
                    do javafx, unica forma de solicitar atualização de tela pela nosso Thread
                */
                Platform.runLater(() -> {
                    listViewUsersOnline.getItems().clear();

                    refresh();
                });

                try {
                    /*
                        como aqui é logout é chamada a função que fecha o socket  clientSocket.closeInOut()
                    */
                    clientSocket.closeInOut();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                clientSocket = null;
                System.out.println("Desconectado com Sucesso!");
                break;// Não sei se o mais certo seria um return, pore´m o break está fazendo a mesma função
            }

        }
        System.out.println("Thread finalizada, retorno ao botão logar");

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

        /*
            Chama a função que preeche o ListView de usuários online
        */
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

            JSONArray usuarios = (JSONArray) dados.get("usuarios");//pega o array de usuários

            for (int i = 0; i < usuarios.size(); i++) { //navega pelo array de usuários

                aux = (JSONObject) usuarios.get(i);
                user = new Usuario();
                user.setNome(aux.get("nome").toString());
                /*  quando eu comento essa linha de baixo apresenta um erro
                    de conversão, só encapsulei com um parênteses a mais e sumiu
                    caso ocorra em outra parte do código é só envolver no parênteses
                
                */
                user.setDisponibilidade((Integer.parseInt(aux.get("disponivel").toString())));

                user.setRa(aux.get("ra").toString());
                /*
                    Depois de pegar os valor via get do json ele monta um user
                    e coloca na lista que será enviada para preencher o listView.
                    Vamos ter que mudar aqui a forma de apresentar, o model usuário
                    do cliente poderá ter a disponibilidade string, daí a gente recebe o 
                    número e trata em um switch case.
                */
                onLinesAux.add(user);

            }

            /*
                Threads que atualizam interface devem ser executadas dentro da Thread 
                JavaFX, por isso o controle da "carregarListaUsuario(onLinesAux);"
                é entregua para o método Platform.runLater(() ->, pois assim evita
                uma exceção.
             */
            Platform.runLater(() -> {
                carregarListaUsuario(onLinesAux); //chama a função que seta os items na tela

                refresh();
            });

        } else {
            System.out.println("Lista Vazia");
        }

    }

    //Atualiza a Interface
    public void carregarListaUsuario(ObservableList<Usuario> on) {

        listViewUsersOnline.getItems().clear();//limpa tudo
        onLines = FXCollections.observableArrayList(on);//prenche a lista atualizada
        listViewUsersOnline.setItems(onLines);//seta o componente de tela

    }

    //Captura a seleção do mouse no ListView do usuários online
    @FXML
    private void selecionarUsuarioOnline(MouseEvent event) {
        
        /*
            escuta mudanças no listView
        */
        if (listViewUsersOnline.getSelectionModel().getSelectedIndex() > -1) {
            
            /*
                Caso ocorra a mudança ele chama o consultarUsuarioOnline
            */
            consultarUsuarioOnline(event);

        }

    }

    //Seta a variável que controla o fluxo do chat privado a cada clique do mouse 
    //no listView de usuários online
    private void consultarUsuarioOnline(MouseEvent event) {

        /*
            Pega o item selecionado no listview, e seleciona o Ra, preenche a variável global
            chat privado, a qual controla com quem o cliente quer falar
        */
        Usuario registroSel = listViewUsersOnline.getSelectionModel().getSelectedItem();
        chatPrivado = registroSel.getRa();
    }

    @FXML
    private void selecionarCategoria(ActionEvent event) {

        /*
            semelhante o que ocorre em usuário online só que no combobox
        */
        if (comboBoxCategoria.getSelectionModel().getSelectedIndex() > -1) {
            consultar(event);
        }
    }

    private void consultar(ActionEvent event) {

        /*
            Aqui vai chamar a função pra obter a lista de usuários online
            dai vai passa o parâmetro da categoria para filtrar, por enquanto ele
            só pritn a seleção
        */
        comboBoxCategoria.getSelectionModel().getSelectedItem();
        System.out.println(comboBoxCategoria.getSelectionModel().getSelectedItem());

    }

}
