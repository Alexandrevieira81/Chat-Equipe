
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author MASTER
 */
public class ChatServer {

    private ServerSocket serverSocket;

    /*
        Lista que conterá todos os usuários conectados, ela varia de acordo
        com a entrada e saídas desses usuários no servidor.Essa lista vai auxiliar 
        o envio de mensagens privadas e envio da lista dos usuários online, além
        de controlar se um usauário já está logado no servidor.
     */
    public ChatServer() {

    }

    public ServerSocket start(int porta) throws IOException {

        //cria o um socket servidor
        serverSocket = new ServerSocket(porta);
        System.out.println("Servidor Inicializado na Porta! " + porta);
        System.out.println("Aguardando Conexao ......");
        return serverSocket;

    }
    

   
}
