import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Servidor {

    private static ServerSocket server;
    private static int porta = 8086;
    private final static Path path = Paths.get("src\\fortune-br.txt");
    private int NUM_FORTUNES = 0;
    private final HashMap<Integer, String> hm = new HashMap<>();

    public void iniciar() {
        System.out.println("Servidor iniciado na porta: " + porta);
        try {
        	HashMap<Integer, String> hm = new HashMap<>();
            server = new ServerSocket(porta);
            NUM_FORTUNES = countFortunes();
            fileToMap(hm);

            while (true) {
                Socket socket = server.accept();
                processarCliente(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encerrarServidor();
        }
    }

	public int countFortunes() throws FileNotFoundException {

		int lineCount = 0;

		InputStream is = new BufferedInputStream(new FileInputStream(path.toString()));
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			String line = "";
			while (!(line == null)) {

				if (line.equals("%"))
					lineCount++;

				line = br.readLine();

			} // fim while

			System.out.println(lineCount);
		} catch (IOException e) {
			System.out.println("SHOW: Excecao na leitura do arquivo.");
		}
		return lineCount;
	}

    public void fileToMap(HashMap<Integer, String> hm) throws FileNotFoundException {

		InputStream is = new BufferedInputStream(new FileInputStream(path.toString()));
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

			int lineCount = 0;

			String line = "";
			while (!(line == null)) {

				if (line.equals("%"))
					lineCount++;

				line = br.readLine();
				StringBuffer fortune = new StringBuffer();
				while (!(line == null) && !line.equals("%")) {
					fortune.append(line + "\n");
					line = br.readLine();
					// System.out.print(lineCount + ".");
				}

				hm.put(lineCount, fortune.toString());
//				System.out.println(fortune.toString());
//
//				System.out.println(lineCount);
			} // fim while

		} catch (IOException e) {
			System.out.println("SHOW: Excecao na leitura do arquivo.");
		}
    }

    private void processarCliente(Socket socket) {
        try (DataInputStream entrada = new DataInputStream(socket.getInputStream());
             DataOutputStream saida = new DataOutputStream(socket.getOutputStream())) {

            String mensagem = entrada.readUTF();
            String resposta = parser(mensagem);

            saida.writeUTF(resposta);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String parser(String mensagem) {
        try {
            // Parse da mensagem JSON
        	System.out.println(mensagem);
            String[] partes = mensagem.split("\"method\":");
            if (partes.length != 2) {
                return "{\"result\":\"false\"}\n";
            }

            String[] metodoArgs = partes[1].split("\"args\":");
            String metodo = metodoArgs[0].trim().replace("\"", "");
            String args = metodoArgs[1].trim().replace("\"", "").replace("[", "").replace("]", "");

            if (metodo.equals("read")) {
                // Ler uma fortuna aleatória
                Random random = new Random();
                int randomIndex = random.nextInt(NUM_FORTUNES);

                String randomFortune = hm.get(randomIndex);

                return "{\"result\":\"" + randomFortune + "\"}\n";
            } else if (metodo.equals("write")) {
                // Escrever uma nova fortuna
                if (args.isEmpty()) {
                    return "{\"result\":\"false\"}\n";
                }

                // Inserir no mapa
                int newIndex = NUM_FORTUNES;
                hm.put(newIndex, args);
                NUM_FORTUNES++;

                // Inserir no arquivo
                try {
                    Files.write(path, (args + "\n").getBytes(), StandardOpenOption.APPEND);
                } catch (Exception e) {
                    System.out.println("SHOW: Exceção na escrita do arquivo.");
                }

                return "{\"result\":\"" + args + "\"}\n";
            } else {
                return "{\"result\":\"false\"}\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"result\":\"false\"}\n";
        }
    }

    private void encerrarServidor() {
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Servidor().iniciar();
    }
}
