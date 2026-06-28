import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Serviço compartilhado pelas tres versoes do servidor.
 * Le dados de um arquivo e atende uma conexao de cliente.
 *
 * Protocolo (texto):  "GET <id>" -> "OK <linha>" | "ERROR ..."   ("QUIT" encerra)
 */
public class Service {

    private final String file;

    public Service(String file) {
        this.file = file;
    }

    /** Operacao de leitura: devolve a linha 'id' do arquivo (I/O real por requisicao). */
    public String read(int id) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                if (i++ == id) return line;
            }
        }
        return null;
    }

    /** Atende uma conexao: le requisicoes "GET <id>" e responde a linha lida. */
    public void handle(Socket socket) {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            String req;
            while ((req = in.readLine()) != null) {
                if (req.equals("QUIT")) break;
                String[] p = req.split(" ");
                if (p.length == 2 && p[0].equals("GET")) {
                    String v = read(Integer.parseInt(p[1]));
                    out.println(v == null ? "ERROR not found" : "OK " + v);
                } else {
                    out.println("ERROR use: GET <id>");
                }
            }
        } catch (IOException | NumberFormatException e) {
            // conexao encerrada ou requisicao invalida: ignora
        }
    }
}
