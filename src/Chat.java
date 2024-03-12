import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Chat {
    private static final int CHUNK_SIZE = 1024;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private static int getPort() {
        Random random = new Random();
        return random.nextInt(9999 - 1025) + 1025;
    }

    public static void main(String[] args) throws IOException {
        int port = getPort();
        System.out.println(ANSI_CYAN + "This program is listening on Port " + port + ANSI_RESET);

        new Writer().start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            listen(serverSocket);
        } catch (BindException e) {
            System.out.println("Port " + port + " already in use.");
            port = getPort();
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                listen(serverSocket);
            }
        }
        catch (IOException e) {
            throw new IOException(e);
        }
    }

    private static void listen(ServerSocket listener) {
        while(true) {
            try (
                    Socket clientSocket = listener.accept();
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
            ) {
                String msg;
                while(!(msg = in.readUTF()).equalsIgnoreCase("BYE")) {
                    if (msg.toLowerCase().contains("transfer")) {
                        String[] details = msg.split(" ");
                        String filename = details[1];
                        int fileSize = Integer.parseInt(details[2]);
                        try {
                            System.out.println("Receiving file " + filename);
                            receiveFile(in, fileSize, filename);
                            System.out.println("File downloaded.");
                        } catch (IOException e) {
                            System.out.println("Error downloading file: " + e.getMessage());
                        }
                    } else {
                        System.out.println(ANSI_BLUE + "Received message: " + msg + ANSI_RESET);
                    }
                }
            } catch (IOException e) {
                System.out.println("Disconnected");
            }
        }
    }

    private static class Writer extends Thread {
        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("Enter the port number of the program you wish to connect to: ");
                String inp = input.readLine();

                handleCtrlC(inp);

                if (!inp.matches("[0-9]+")) {
                    System.out.print("Invalid input. Enter a correct port number: ");
                    inp = input.readLine();
                    handleCtrlC(inp);
                }

                int port = Integer.parseInt(inp);

                if (port > 65535) {
                    System.out.println("Invalid port. Enter a valid port number");
                    port = Integer.parseInt(input.readLine());
                }

                try(
                        Socket socket = new Socket("localhost", port);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
                ) {
                    System.out.println("Connected to program on port " + port);
                    System.out.println("Send any message you like or say \"transfer <filename>\" to transfer a file.");

                    ArrayList<String> exitWords = new ArrayList<>(List.of("exit", "bye", "end"));
                    String cmd;

                    while (true) {
                        cmd = input.readLine();
                        if (exitWords.contains(cmd)) {
                            System.out.println("Exiting...");
                            System.exit(1);
                        }
                        if (cmd.toLowerCase().contains("transfer")) {
                            String filename = cmd.split(" ")[1];
                            try {
                                sendFile(filename, out);
                                System.out.println("File sent.");
                            }
                            catch (FileNotFoundException e) {
                                System.out.println("File not found " + e.getMessage());
                            }
                        } else {
                            sendMsg(cmd, out);
                        }
                    }
                }
                catch (ConnectException e) {
                    System.out.println("Connection refused. Check if port number is correct");
                    run();
                }
                catch (IOException e) {
                    System.out.println("Connection failed / disconnected.");
                    throw new IOException(e);
                }

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void sendFile(String filename, ObjectOutputStream out)
                throws IOException {
            try (
                    FileInputStream stream = new FileInputStream(filename)) {

                File file = new File(filename);
                long fileSize = file.length();
                out.writeUTF("TRANSFER " + filename + " " + fileSize);
                out.flush();

                int bytesRead;
                byte[] buffer = new byte[CHUNK_SIZE];

                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
//                System.out.print(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                }
            }

            catch (FileNotFoundException e) {
                throw new FileNotFoundException(e.getMessage());
            }
        }

        private void sendMsg(String msg, ObjectOutputStream out) throws IOException {
            try {
                out.writeUTF(msg);
                out.flush();
            } catch (IOException e) {
                throw new IOException(e);
            }
        }

        private void handleCtrlC(String inp) {
            if (inp == null) {
                System.exit(1);
            }
        }
    }

    private static void receiveFile(ObjectInputStream inputStream, int fileSize, String filename) throws IOException {
        try (OutputStream fs = new FileOutputStream(
                "new" + filename.substring(0, 1).toUpperCase() + filename.substring(1))) {

            int length = 0;
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while (length < fileSize) {
                bytesRead = inputStream.read(buffer);
                // System.out.print(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                fs.write(buffer, 0, bytesRead);
                fs.flush();
                length += bytesRead;
            }

        } catch (IOException e) {
            throw new IOException(e);
        }

    }
}
