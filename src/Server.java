import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Chris on 11/14/2016.
 */
public class Server {

    static final int PORT = 50000;
    static ServerSocket socket = null;

    public static void main(String [] args){
        String ipAddress = "";
        try {
            ipAddress = InetAddress.getLocalHost().toString();
        }catch(UnknownHostException e){
            System.out.println("Failed to discover own IP address");
        }
        System.out.println(ipAddress);
        try {
            socket = new ServerSocket(PORT);
        }catch(IOException err){
            System.out.println("Failed to create server socket");
        }
        while(true){
            try {
                new ServerWorker(socket.accept()).start();
            }catch(IOException e){
                System.out.println("Failed to connect to client");
            }
        }
    }

    static class ServerWorker extends Thread{

        private Socket client;
        private PrintWriter out;
        private BufferedReader in;

        public ServerWorker(Socket client) {
            this.client = client;
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException e) {
                System.out.println("Failed to set up client I/O");
            }
        }

        public void run(){
            while(true){

            }
        }
        
        public void close() throws Exception{
            out.close();
            in.close();
        }


    }
}
