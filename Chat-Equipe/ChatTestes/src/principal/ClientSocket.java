package principal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public ClientSocket(Socket socket) throws IOException {

        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String getMessage() {

        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public boolean sendMsg(String msg) {

        out.println(msg);
        return !out.checkError();
    }

    public SocketAddress getRemoteSocketAddress() {

        return socket.getRemoteSocketAddress();

    }

    public void closeInOut() throws IOException {

        in.close();
        out.close();
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

}
