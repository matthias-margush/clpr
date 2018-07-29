package clpr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class Client {
    public static void unwrap() {
    }

    public static void wrap(BufferedReader in, BufferedWriter writer) throws IOException {
        PrintWriter out = new PrintWriter(writer);
        String marker = UUID.randomUUID().toString();

        String line;
        out.println(marker);
        while ((line = in.readLine()) != null) {
            out.println(line);
        }
        out.println(marker);
        out.flush();
    }

    public static void wrap(InputStream in, OutputStream out) throws IOException {
        wrap(new BufferedReader(new InputStreamReader(in)),
             new BufferedWriter(new OutputStreamWriter(out)));
    }

    public static void unwrap(BufferedReader in, BufferedWriter writer) throws IOException {
        PrintWriter out = new PrintWriter(writer);
        String marker = in.readLine();
        String line;
        while ((line = in.readLine()) != null) {
            if (marker.equals(line)) {
                break;
            }
            out.println(line);
        }
        out.flush();
    }

    public static void unwrap(InputStream in, OutputStream out) throws IOException {
        unwrap(new BufferedReader(new InputStreamReader(in)),
               new BufferedWriter(new OutputStreamWriter(out)));
    }

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        InputStream commandInput = System.in;

        if (args.length > 2) {
            System.out.printf("using command line input: %s\n", args[2]);
            commandInput = new ByteArrayInputStream(args[2].getBytes());
        }

        try (Socket socket = new Socket(host, port);
             InputStream fromRepl = socket.getInputStream();
             OutputStream toRepl = socket.getOutputStream()) {

            wrap(System.in, toRepl);
            unwrap(fromRepl, System.out);

         } catch (IOException e) {
             e.printStackTrace();
         }
    }
}
