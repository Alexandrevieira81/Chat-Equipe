
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONObject;

public class ClientSocket {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private Usuario usuario;

    public ClientSocket(Socket socket) throws IOException {

        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    //Lê a mensagem pelo objeto in usando o método readLine() do BufferedReader
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMessage() {

        try {
            return in.readLine();

        } catch (IOException e) {
            return null;
        }
    }

    //Envia a mensagem pelo objeto out usando o método prinln do PrintWriter
    public boolean sendMsg(String msg) {

        out.println(msg);

        return !out.checkError();
    }

    //Fecha totalmente o socket
    public void closeInOut() throws IOException {

        in.close();
        out.close();
        socket.close();
    }

    @Override
    public String toString() {

        String MyString = socket.getRemoteSocketAddress().toString();
        MyString = MyString.replace("/", "");//Removendo a barra que vem no método getRemoteSocketAddress()
        JSONObject obj = new JSONObject();
        obj.put("disponivel", usuario.getDisponibilidade());
        obj.put("ra", usuario.getRa());
        obj.put("conexao", MyString);
        obj.put("nome", usuario.getNome());
        obj.put("categoria_id", usuario.getCategoria());

        return obj.toString();
    }

    public SocketAddress getRemoteSocketAddress() {

        return socket.getRemoteSocketAddress();

    }

    /*
        Essa função recebe a lista de todos os clientes online e compara com
        o cliente que solicitou o login.
     */
    public boolean verificarUsuarioLogado(List<ClientSocket> clients, String ra) {

        Iterator<ClientSocket> iterator = clients.iterator();
        while (iterator.hasNext()) { //percorres a list clients
            ClientSocket clientSocket = iterator.next();

            if (ra.equals(clientSocket.getUsuario().getRa())) {

                return true;
            }
        }
        return false;

    }

}
