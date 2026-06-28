import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cliente de carga: abre N conexoes em paralelo, cada uma enviando "GET"
 * em laco pelo tempo dado, e imprime a vazao (requisicoes por segundo).
 *
 * Uso: java Client [host] [porta] [concorrencia] [duracaoSeg] [maxId]
 */
public class Client {
    public static void main(String[] a) throws Exception {
        String host = a.length > 0 ? a[0] : "localhost";
        int port = a.length > 1 ? Integer.parseInt(a[1]) : 5000;
        int conc = a.length > 2 ? Integer.parseInt(a[2]) : 16;
        int secs = a.length > 3 ? Integer.parseInt(a[3]) : 6;
        int maxId = a.length > 4 ? Integer.parseInt(a[4]) : 1000;

        AtomicLong ok = new AtomicLong();
        long end = System.nanoTime() + secs * 1_000_000_000L;
        Thread[] ts = new Thread[conc];

        for (int i = 0; i < conc; i++) {
            final int seed = i;
            ts[i] = new Thread(() -> {
                try (Socket s = new Socket(host, port)) {
                    s.setSoTimeout(3000); // se nao for atendido (single ocupado) -> timeout
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    int id = seed;
                    while (System.nanoTime() < end) {
                        out.println("GET " + (id++ % maxId));
                        if (in.readLine() == null) break;
                        ok.incrementAndGet();
                    }
                } catch (IOException e) {
                    // conexao recusada/timeout: cliente nao atendido
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        System.out.printf("concurrency=%d throughput=%.0f req/s%n", conc, ok.get() / (double) secs);
    }
}
