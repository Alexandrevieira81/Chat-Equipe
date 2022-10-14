package principal;

import java.io.IOException;
import java.net.Socket;
import org.json.simple.JSONObject;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/**
 *
 * @author MASTER
 */
public class ChatClient {

    /**
     * @param args the command line arguments
     */
    //ssh.chauchuty.cf
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private ClientSocket clientSocket;

    public ChatClient() {

    }

    public ClientSocket start() throws IOException {

        clientSocket = new ClientSocket(
                new Socket(SERVER_ADDRESS, 8089));
        //System.out.println(clientSocket.getMessage());

        return clientSocket;
    }

    public void messageLoop(String msg, String ra) throws IOException {

        clientSocket.sendMsg("{\"operacao\":\"mensagem\",\"mensagem\":\"" + msg + "\",\"privado\":\"" + ra + "\"}");

    }

    public void LogarDeslogar(String msg) throws Exception {

        clientSocket.sendMsg(msg);

    }

    public void EnviarCadastro(String msg) throws IOException {

        clientSocket.sendMsg(msg);

    }

    public void carregaUsuarios(String ra, String senha) throws IOException {

        JSONObject params = new JSONObject();
        params.put("ra", ra);
        params.put("senha", senha);

        JSONObject obj = new JSONObject();
        obj.put("operacao", "logout");
        obj.put("parametros", params);

        clientSocket.sendMsg("{\"operacao\":\"obter_usuarios\"}");

    }
}
