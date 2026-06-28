import java.net.ServerSocket;

/** Versao 1 - Single-threaded: uma thread atende um cliente por vez. */
public class SingleThreadedServer {
    public static void main(String[] a) throws Exception {
        int port = a.length > 0 ? Integer.parseInt(a[0]) : 5000;
        Service svc = new Service(a.length > 1 ? a[1] : "data.txt");

        ServerSocket ss = new ServerSocket(port, 1024);
        System.out.println("[single] listening on " + port);
        while (true) {
            svc.handle(ss.accept()); // trata a conexao na propria thread do accept
        }
    }
}
