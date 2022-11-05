package principal;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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

    public ClientSocket start() {

        /*
            Método start cria um objeto ClienteSocket
         */
        try {
            clientSocket = new ClientSocket(
                    new Socket(SERVER_ADDRESS, 8099));
            //System.out.println(clientSocket.getMessage());

            return clientSocket;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Servidor Encontra-se Offline!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
        return null;
    }

    public void messageLoop(String msg, String ra) throws IOException {

        /*
            Nome aqui ficou messaLoop porque antes estava na linha de comando
            dentro do loop, porém como ele ficou dentro de um botão, agora só chama
            a função sendMsg, que manda a mensagem pelo socket
        
         */
        clientSocket.sendMsg("{\"operacao\":\"mensagem\",\"mensagem\":\"" + msg + "\",\"privado\":\"" + ra + "\"}");

    }

    public void LogarDeslogar(String msg) throws Exception {

        clientSocket.sendMsg(msg);

    }

    public void EnviarCadastro(String msg) throws IOException {

        clientSocket.sendMsg(msg);

    }

    public void carregaUsuarios(Integer categoria_id) throws IOException {

        /*
        
            manda um pedido de lista pro servidor
         */
        JSONObject params = new JSONObject();
        params.put("categoria_id", categoria_id);
        

        JSONObject obj = new JSONObject();
        obj.put("operacao", "obter_usuarios");
        obj.put("parametros", params);
        //clientSocket.sendMsg("{\"operacao\":\"obter_usuarios\"}");
        clientSocket.sendMsg(obj.toJSONString());

    }
}
