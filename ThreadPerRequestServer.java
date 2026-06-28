import java.net.ServerSocket;
import java.net.Socket;

/** Versao 2 - Multi-threaded: uma thread nova por conexao. */
public class ThreadPerRequestServer {
    public static void main(String[] a) throws Exception {
        int port = a.length > 0 ? Integer.parseInt(a[0]) : 5000;
        Service svc = new Service(a.length > 1 ? a[1] : "data.txt");

        ServerSocket ss = new ServerSocket(port, 1024);
        System.out.println("[per-request] listening on " + port);
        while (true) {
            Socket s = ss.accept();
            new Thread(() -> svc.handle(s)).start(); // uma thread por conexao
        }
    }
}
