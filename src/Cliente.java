import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Cliente {

    private static Socket socket;
    private static DataInputStream entrada;
    private static DataOutputStream saida;

    private int porta = 8086;

    public void iniciar() {
        System.out.println("Cliente iniciado na porta: " + porta);

        try {
            socket = new Socket("127.0.0.1", porta);

            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            // Solicitar leitura ou escrita ao servidor
            while (true) {
                System.out.println("Escolha uma ação (read/write/quit):\n");
                String acao = br.readLine();

                if (acao.equalsIgnoreCase("read")) {
                    solicitarLeituraFortuna();
                } else if (acao.equalsIgnoreCase("write")) {
                    System.out.println("Digite a fortuna a ser escrita:\n");
                    String fortuna = br.readLine();
                    solicitarEscritaFortuna(fortuna);
                } else if (acao.equalsIgnoreCase("quit")) {
                    encerrarConexao();
                    break;
                } else {
                    System.out.println("Ação inválida.\n");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encerrarConexao();
        }
    }

    private void solicitarLeituraFortuna() {
        try {
            // Construir mensagem JSON para leitura
            String mensagem = "{\"method\":\"read\",\"args\":[\"\"]}\n";

            // Enviar mensagem ao servidor
            saida.writeUTF(mensagem);

            // Receber a resposta do servidor
            String resposta = entrada.readUTF();

            // Mostrar a fortuna lida
            System.out.println("Fortuna lida do servidor:\n" + resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void solicitarEscritaFortuna(String fortuna) {
        try {
            // Construir mensagem JSON para escrita
            String mensagem = "{\"method\":\"write\",\"args\":[\"" + fortuna + "\"]}\n";

            // Enviar mensagem ao servidor
            saida.writeUTF(mensagem);

            // Receber a confirmação do servidor
            String resposta = entrada.readUTF();

            // Mostrar a confirmação
            System.out.println("Confirmação do servidor:\n" + resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void encerrarConexao() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Cliente().iniciar();
    }
}
