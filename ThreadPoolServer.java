import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Versao 3 - Pool de threads (Slide 35): conexoes sao submetidas ao executor. */
public class ThreadPoolServer {
    public static void main(String[] a) throws Exception {
        int port = a.length > 0 ? Integer.parseInt(a[0]) : 5000;
        Service svc = new Service(a.length > 1 ? a[1] : "data.txt");
        int poolSize = a.length > 2 ? Integer.parseInt(a[2]) : 50;

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        ServerSocket ss = new ServerSocket(port, 1024);
        System.out.println("[pool] listening on " + port + " (poolSize=" + poolSize + ")");
        while (true) {
            Socket s = ss.accept();
            pool.execute(() -> svc.handle(s)); // pool reaproveita as threads
        }
    }
}
