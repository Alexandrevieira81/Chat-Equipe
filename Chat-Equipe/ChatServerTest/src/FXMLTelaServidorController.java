/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.zone.ZoneRulesProvider.refresh;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 *
 * @author MASTER
 */
public class FXMLTelaServidorController implements Initializable {

    @FXML
    private Button buttonIniciar;
    @FXML
    private TextField textFieldPorta;
    @FXML
    private ListView<String> listViewLogs;

    ChatServer server;

    private final List<ClientSocket> clients = new LinkedList();
    private CarregaUsuarios listarUsuarios = new CarregaUsuarios();
    JSONObject retorno = null;
    JSONObject dados = null;
    ServerSocket serverSocket;
    ObservableList<String> logs;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        server = new ChatServer();
        logs = FXCollections.observableArrayList();

    }

    @FXML
    private void InciarServidor(ActionEvent event) {

        try {

            serverSocket = server.start(Integer.parseInt(textFieldPorta.getText()));
            new Thread(() -> {
                try {
                    ClientConectionLoop();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLTelaServidorController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro! Verifque a porta, ou verifique se o servidor já está ativo!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    // Loop "Infinito" que fica aguardando conexões dos clientes
    private void ClientConectionLoop() throws IOException {

        while (true) {

            /*
                A cada solicitação de conexão ele cria um socket do tipo cliente,
                sendo exclusivo para a conexão no momento.
             */
            ClientSocket clientSocket = new ClientSocket(serverSocket.accept());
            System.out.println("Cliente Conectado " + clientSocket.getRemoteSocketAddress());

            //InetAddress host = serverSocket.getInetAddress(); Apenas pega o momento de conexão do usuário
            String msg = "Conectado com Sucesso";

            /*
                Para a atulização da Threads se faz uso do método Run, porém ele
                não aceita argumentos, sendo que a forma mais simples de entragar
                o controle da função clientMessageLoop para uma thread é a utilização
                de uma função anônima. O try catch está dentro da thread para tratar
                o clientSocket e não uma exceção da própria thread, pois está
                caso acontece será capturada pelo throws IOException da função
                ClientConectionLoop()
                
             */
            new Thread(() -> {
                try {
                    clientMessageLoop(clientSocket);
                } catch (IOException ex) {
                    System.out.println("Problemas ao encerrar a Conexao");
                }
            }).start();
            //clientSocket.sendMsg(msg);

        }

    }

    public void clientMessageLoop(ClientSocket clientSocket) throws IOException {

        //String temp = (clientSocket.getMessage());
        JSONObject json;
        JSONObject params = null;
        JSONParser parserMessage = new JSONParser();

        Usuario user = new Usuario();
        String msg;
        String operacao = " ";
        String conteudoMsg = " ";
        String msgPrivada = " ";

        while ((msg = clientSocket.getMessage()) != null) {

            System.out.println("Protocolo vindo do Cliente: " + msg);

            SincronizarLogs("Recebido de: ", clientSocket, msg);

            try {
                /*
             Transforma a mensagem recebida para o formato json do pacote simpleJson
                 */
                operacao = " ";//evita aplicação responder caso a mensagem venha null do cliente, pois e vai ficar com a operação passada se não limpar a variável
                json = (JSONObject) parserMessage.parse(msg);
                operacao = (String) json.get("operacao");// Extraí o valor da chave operação

                if (operacao.equals("login")) {

                    params = (JSONObject) json.get("parametros");
                    String senha = (String) params.get("senha");
                    String ra = (String) params.get("ra");
                    user = listarUsuarios.localizarUsuario(ra, senha);//localiza o usuário cadastrado pelo ra e senha

                    if (clients.size() > 0) {//Evita a ocorrência do erro de nullpointer da thread caso a lista clients esteja vazia

                        if (clientSocket.verificarUsuarioLogado(clients, ra)) {
                            /*
                            retorno = new JSONObject() cria um novo objeto dentro da
                            varável retorno, evitando assim que informações permancenaçam 
                            na variável retorno. Evita lixo residual.Note que esse if verifica 
                            se o usuário já está logado, caso não esteja ele retorna uma mensagem
                            para o usuário, envia um null para ter certeza do encerramento da conexão
                            e depois fecha o socket no lado do servidor.
                             */
                            retorno = new JSONObject();
                            dados = new JSONObject();
                            retorno.put("status", 403);
                            retorno.put("mensagem", "Cliente já está Logado!");
                            retorno.put("dados", dados);
                            clientSocket.sendMsg(retorno.toJSONString());
                            SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                            //clientSocket.sendMsg(null);
                            //clientSocket.closeInOut();
                            // System.out.println("Socket fechado para o cliente:Logado " + clientSocket.getRemoteSocketAddress());

                        } else {
                            /*
                        ************Métodos PUT SE REPETEM DEVIDO A INSERÇÃO DE CARACTERES INDEJEDADOS
                                    EM POSIÇÕES ALEATÓRIAS, DEPOIS APLICA O REPLACE FICA MUITO COMPLICADO.
                                    POR ISSO DESSAS REPETIÇÕES, POIS SE CRIAR UMA CLASSE MENSAGENS DE RESPOSTA,
                                    TADA A MENSAGEM TERÁ QUE TER UM REPLACE COMPLEXO.
                             */
                            user.setDisponibilidade(1);//seta que o usuário está disponível
                            dados = new JSONObject();
                            retorno = new JSONObject();
                            dados.put("nome", user.getNome());
                            System.out.println("Cliente Logado: " + user.getNome());//Exibe o nome do usuário logado
                            clientSocket.setUsuario(user);
                            clients.add(clientSocket);//Como o login ocorreu adiciona o usuário a lista de usuários online
                            retorno.put("status", 200);
                            retorno.put("mensagem", "Login efetuado com Sucesso");
                            retorno.put("dados", dados);
                            clientSocket.sendMsg(retorno.toJSONString());
                            SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());

                            this.BroadcastingLogar(clientSocket);//chama o função que envia a lista de usuários online atualizada
                        }

                    } else {

                        /*
                            Esse parte faz o conexão caso não exista ninguém online, ou seja, a lista de clients
                            estaja vazia, porém a linha "user = listarUsuarios.localizarUsuario(ra, senha);" já
                            retornou um usário válido.
                           
                         */
                        user.setDisponibilidade(1);
                        dados = new JSONObject();
                        retorno = new JSONObject();
                        dados.put("nome", user.getNome());
                        System.out.println("Cliente Logado: " + user.getNome());//Exibe o nome do usuário logado
                        clientSocket.setUsuario(user);
                        clients.add(clientSocket);
                        retorno.put("status", 200);
                        retorno.put("mensagem", "Login efetuado com Sucesso");
                        retorno.put("dados", dados);
                        clientSocket.sendMsg(retorno.toJSONString());
                        SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                        this.BroadcastingLogar(clientSocket);
                    }

                } else if (operacao.equals("cadastrar")) {

                    params = (JSONObject) json.get("parametros");
                    String nome = (String) params.get("nome");
                    String senha = (String) params.get("senha");
                    String ra = (String) params.get("ra");
                    Integer categoria = Integer.parseInt(params.get("categoria_id").toString());
                    String descricao = (String) params.get("descricao");

                    dados = new JSONObject();
                    retorno = new JSONObject();

                    if (listarUsuarios.localizarUsuarioCadastrado(ra)) {

                        retorno.put("status", 202);
                        retorno.put("mensagem", "Usuário já encontra-se cadastrado");
                        retorno.put("dados", dados);
                        clientSocket.sendMsg(retorno.toJSONString());
                        SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                        clientSocket.sendMsg(null);
                        clientSocket.closeInOut();
                        System.out.println("Socket fechado para o cliente:Cadastro " + clientSocket.getRemoteSocketAddress());

                    } else {

                        listarUsuarios.gravarUsuario(nome, ra, senha, categoria, descricao);
                        retorno.put("status", 201);
                        retorno.put("mensagem", "Cadastro efetuado com Sucesso");
                        retorno.put("dados", dados);
                        clientSocket.sendMsg(retorno.toJSONString());
                        SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                        clientSocket.sendMsg(null);
                        clientSocket.closeInOut();
                        System.out.println("Socket fechado para o cliente:Cadastro " + clientSocket.getRemoteSocketAddress());

                    }

                }

                SincronizarLogs("Recebido de: ", clientSocket, msg);

                json = (JSONObject) parserMessage.parse(msg);

                /*
                    Esse Ifs evitam que um protocolo incorreto trave o sistema
                 */
                if ((operacao = (String) json.get("operacao")) == null) {
                    operacao = "";
                }
                if ((conteudoMsg = (String) json.get("mensagem")) == null) {
                    conteudoMsg = "";
                }
                if ((msgPrivada = (String) json.get("privado")) == null) {
                    msgPrivada = "";
                }

                if (operacao.equals("obter_usuarios")) {

                    retorno = new JSONObject();
                    dados = new JSONObject();
                    dados.put("usuarios", clients.toString());
                    retorno.put("status", 200);
                    retorno.put("mensagem", "lista de Usuários");
                    retorno.put("dados", dados);

                    clientSocket.sendMsg(retorno.toString().replace("\"" + "[", "[").replace("]" + "\"", "]").replace("\\", ""));
                    SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());

                    //clientSocket.sendMsg("{\"status\":\"300\",\"mensagem\":\"Lista de Usuários\",\"dados\":{\"usuarios\":[" + clients.toString().replace("[", "").replace("]", "").replace(" ", "") + "]}}");
                }
                if (operacao.equals("logout")) {

                    retorno = new JSONObject();
                    dados = new JSONObject();

                    retorno.put("status", 600);
                    retorno.put("mensagem", "Cliente Desconectado!");
                    retorno.put("dados", dados);
                    clientSocket.sendMsg(retorno.toJSONString());
                    SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                    clientSocket.getUsuario().setDisponibilidade(0);
                    clients.remove(clientSocket);
                    this.BroadcastingLogar(clientSocket);//manda a lista de usuários atualizada excluíndo o usuário que está deslogando
                    clientSocket.closeInOut();
                    System.out.println("Socket fechado para o cliente LOGOUT " + clientSocket.getRemoteSocketAddress());
                    break;
                }
                //clientSocket.sendMsg("[ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " ]" + " Servidor: " + msg);
                if (operacao.equals("mensagem")) {

                    this.mensagemPrivada(clientSocket, conteudoMsg, msgPrivada);
                }


                /*
                Verificar, esse if estava voltando a lista quando cliente tentava cadstrar algúem já cadstrado
            
              if (msg == null && (!retorno.get("status").equals(201))) {
                
                    Após cadastrar ou o cliente desconectar inexperadamente
                    o conexão também é encerrada, esse If faz essa distinção
                
                retorno = new JSONObject();
                dados = new JSONObject();
                retorno.put("status", 600);
                retorno.put("mensagem", "Cliente Desconectado!");
                retorno.put("dados", dados);
                clientSocket.sendMsg(retorno.toJSONString());
                clients.remove(clientSocket);
                this.BroadcastingLogar(clientSocket);
                clientSocket.closeInOut();
                System.out.println("Socket fechado para o cliente NULL " + clientSocket.getRemoteSocketAddress());

            }
                 */
            } catch (ParseException ex) {

                //Exceção para formato de protocolo fora do padraõ Json
                retorno = new JSONObject();
                dados = new JSONObject();
                retorno.put("status", 400);
                retorno.put("mensagem", "Formato Incompatível com Json");
                retorno.put("dados", dados);
                clientSocket.sendMsg(retorno.toJSONString());
                SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                //clientSocket.closeInOut();
                //System.out.println("Socket fechado para o cliente:PARSEEXCEPTION " + clientSocket.getRemoteSocketAddress());
            } catch (NullPointerException e) {

                if ((operacao.equals("login") && (!(params.containsKey("senha")) || (!(params.containsKey("ra")))))) {
                    System.out.println("Protocolo de Login Incorreto!");
                    retorno = new JSONObject();
                    dados = new JSONObject();
                    retorno.put("status", 404);
                    retorno.put("mensagem", "Protocolo de Login Incorreto!");
                    retorno.put("dados", dados);
                    System.out.println("Indo pro cliente: " + retorno.toJSONString());
                    clientSocket.sendMsg(retorno.toJSONString());
                    SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                    //clientSocket.closeInOut();
                    //System.out.println("Socket fechado para o cliente: NULLPOINTER" + clientSocket.getRemoteSocketAddress());

                } else if (operacao.equals("login")) {
                    System.out.println("Cliente Nao Encontrado!");
                    retorno = new JSONObject();
                    dados = new JSONObject();
                    retorno.put("status", 404);
                    retorno.put("mensagem", "Cliente Nao Encontado!");
                    retorno.put("dados", dados);
                    System.out.println("Indo para o cliente: " + retorno.toJSONString());
                    clientSocket.sendMsg(retorno.toJSONString());
                    SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                    //clientSocket.closeInOut();
                    //System.out.println("Socket fechado para o cliente: NULLPOINTER" + clientSocket.getRemoteSocketAddress());

                } else if (operacao.equals("cadastrar")) {

                    System.out.println("Erro no Servidor!");
                    retorno = new JSONObject();
                    dados = new JSONObject();
                    retorno.put("status", 500);
                    retorno.put("mensagem", "Erro no Protocolo de Cadastro!");
                    retorno.put("dados", dados);
                    System.out.println("Indo pro cliente: " + retorno.toJSONString());
                    clientSocket.sendMsg(retorno.toJSONString());
                    SincronizarLogs("Enviado para: ", clientSocket, retorno.toJSONString());
                    clientSocket.closeInOut();
                    System.out.println("Socket fechado para o cliente: NULLPOINTER" + clientSocket.getRemoteSocketAddress());

                } else if (operacao.equals(" ")) {
                    clientSocket.getUsuario().setDisponibilidade(0);
                    clients.remove(clientSocket);
                    this.BroadcastingLogar(clientSocket);
                    clientSocket.closeInOut();
                    System.out.println("Socket fechado para o cliente: Saída" + clientSocket.getRemoteSocketAddress());
                }

            }
        }
        /*Movido para esse método,pois no método ClientConectionLoop
          ele trava o cliente. Tanta atribuição do nome quanto a adição
          na lista de clientes tem que ficar fora do while.
         */

    }

    private void BroadcastingLogar(ClientSocket sender) {

        JSONObject retorno = null;
        JSONObject dados = null;
        retorno = new JSONObject();
        dados = new JSONObject();


        /*
        função necessária para controlar os fluxos de atualização das listas enviadas para os usuários.
        As listas auxiliares servem para que não tenha interferência na lista de usuários online.
        Os laços auxiliares evitam que um usuário receba seus próprios dados
        
         */
        List<ClientSocket> clientsAux = new LinkedList();

        clientsAux.addAll(clients);

        clientsAux.remove(sender);

        Iterator<ClientSocket> iterator = clients.iterator();

        while (iterator.hasNext()) {
            /*
            laço mais externo, compara as listas auxiliares com a lista de usuários conectados,
            esse lista só é modificada em caso de desconexão do usuário, seja por meio de requisão
            ou forçada por falha de rede ou do sistema
             */
            ClientSocket clientSocket = iterator.next();

            if (sender.equals(clientSocket)) {
                /*
                    Váriável sender é o cliente que fez a requisição sendo que esse if controla
                    a condição mais simples, como é fácil identificar que fez o pedido, na lista
                    clientsAux o próprio requisitante já foi excluído, sendo assim apenas outros 
                    usuários são enviados para ele.
                 */

                dados.put("usuarios", clientsAux.toString());

                retorno.put("status", 200);
                retorno.put("mensagem", "lista de usuarios");
                retorno.put("dados", dados);

                clientSocket.sendMsg(retorno.toString().replace("\"" + "[", "[").replace("]" + "\"", "]").replace("\\", ""));
                SincronizarLogs("Enviado para: ", clientSocket, retorno.toString().replace("\"" + "[", "[").replace("]" + "\"", "]").replace("\\", ""));

                // clientSocket.sendMsg("{\"operacao\":\"lista\",\"mensagem\":\"Lista de Usuários\",\"dados\":{\"usuarios\":[" + clientsAux.toString().replace("[", "").replace("]", "").replace(" ", "") + "]}}");
            } else {
                List<ClientSocket> clientsP = new LinkedList();
                clientsP.addAll(clients);
                /*
                clientsP recebera todos os dados sempre que entrar no else, pois ela terá que encontrar
                    o cliente que não é o requisitante, porém não poderá enviar o dados dele para ele mesmo,
                    caso ele não receba cara vez que entra no laço não será possível comparar com todos os clientes
                    online existentes
                 */
                Iterator<ClientSocket> iteratorP = clientsP.iterator();

                while (iteratorP.hasNext()) {
                    ClientSocket clientSocketP = iteratorP.next();
                    /*
                        Segundo while, note que a variável clientesocket veio do primeiro while, sendo assim
                        ela trabalha com o cliente que não está fazendo a requisição, esse variável será comparada 
                        com uma lista completa que recebeu os dados dos clients online, isso é necessário porque é
                        preciso excluir os dados desse cliente quando o envio é para ele mesmo, porém para clientes
                        diferentes dele é necessa´rio enviar seus dados de contato para possibilitar uma futura conexão
                     */
                    if (clientSocket.equals(clientSocketP)) {
                        /*
                        
                        Esse função funciona da seguinte maneira: a varaiável clientsockt é comparada
                        com todos os usuários online, quando ela encontra o usuário correspondente ela
                        usa a função remove e tira esse usuário da lista auxiliar clientsP, posteriormente
                        fazendo dessa lista para ele. Como esse laço é o mais interno damos um break, já 
                        que nosso objetivo foi alcançado. Lembrando que o laço exrtno passará por todos os
                        usuários repetindo esse processo, ou seja, enviando para todos os contatos uma lista
                        já filtrada. Lembrando que o requisitante da conexão ou desconexão já recebe sua lista no primeiro
                        if.
                         */
                        clientsP.remove(clientSocket);

                        dados = new JSONObject();

                        dados.put("usuarios", clientsP.toString());

                        retorno.put("status", 200);
                        retorno.put("mensagem", "lista de usuarios");
                        retorno.put("dados", dados);

                        clientSocket.sendMsg(retorno.toString().replace("\"" + "[", "[").replace("]" + "\"", "]").replace("\\", ""));
                        SincronizarLogs("Enviado para: ", clientSocket, retorno.toString().replace("\"" + "[", "[").replace("]" + "\"", "]").replace("\\", ""));
                        //clientSocket.sendMsg("{\"operacao\":\"lista\",\"mensagem\":\"Lista de Usuários\",\"dados\":{\"usuarios\":[" + clientsP.toString().replace("[", "").replace("]", "").replace(" ", "") + "]}}");
                        break;

                    }

                }

            }

        }
    }

    private void mensagemPrivada(ClientSocket sender, String msg, String ra) {
        FXMLTelaServidorController tela = new FXMLTelaServidorController();
        Iterator<ClientSocket> iterator = clients.iterator();
        while (iterator.hasNext()) { //percorres a list clients
            ClientSocket clientSocket = iterator.next();

            if (ra.equals(clientSocket.getUsuario().getRa())) {
                /*
                manda a mensagem pelo identificar único ra, que está dentro de um 
                objeto tipo Usuario que por sua fez está dentro de um clientSocket,
                clientSocket este com um thread exclusiva.
                
                 */
                if (!clientSocket.sendMsg("{\"operacao\":\"mensagem\",\"mensagem\":\"[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "] " + sender.getUsuario().getNome() + " : " + msg + "\"}")) {
                    SincronizarLogs("Enviado para: ", clientSocket, "{\"operacao\":\"mensagem\",\"mensagem\":\"[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "] " + sender.getUsuario().getNome() + " : " + msg + "\"}");

                    //caso servidor tente mandar a mensagem e o cliente não respoder ele remove o cliente da lista
                    iterator.remove();
                }
            }
        }
    }

    public void SincronizarLogs(String direcao, ClientSocket clientSocket, String msg) {

        Platform.runLater(() -> {
            AtualizarLogs(direcao, clientSocket, msg);

            refresh();
        });
    }

    public void AtualizarLogs(String direcao, ClientSocket clientSocket, String msg) {
        logs.add("________________________________________");
        logs.add(direcao + " " + clientSocket.getRemoteSocketAddress().toString());
        logs.add(msg);
        listViewLogs.setItems(logs);

    }
}
