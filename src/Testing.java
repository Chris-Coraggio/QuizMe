import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Chris on 12/6/2016.
 */
public class Testing {

    static final int PORT = 50000;

    static Socket socket;
    static ObjectOutputStream out;
    static ObjectInputStream in;

    public static boolean connectToServer(String ipAddress){ //returns whether or not connection was successful
        try {
            socket = new Socket(ipAddress, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        }catch(IOException e){
            System.out.println(String.format(
                    "Failed to connect to server. Ensure it is running on %s:%s and try again",
                    ipAddress, PORT));
            return false;
        }
    }

    public static boolean login(String username, String password){ //returns true if successful
        writeToOutstream(new String[]{"LOGIN", username, password});
        return validateResponse("LOGINSUCCESS");
    }

    public static boolean register(String username, String password){ //returns true if successful
        writeToOutstream(new String[]{"REGISTER", username, password});
        return validateResponse("REGISTERSUCCESS");
    }

    public static boolean createNewGame(){
        writeToOutstream(new String[]{"STARTNEWGAME"});
        return validateResponse("NEWGAMESUCCESS");
    }

    public static boolean joinGame(String gameKey){
        writeToOutstream(new String[]{"JOINGAME"});
        return validateResponse("JOINGAMESUCCESS");
    }

    public static boolean validateResponse(String successMessage){
        try {
            String[] response = (String[]) in.readObject();
            if (response[0].equals(successMessage)) {
                return true;
            } else return false;
        }catch(Exception e){
            return false;
        }
    }

    public static void writeToOutstream(Object[] message){
        try {
            if(message instanceof String[]){
                System.out.println(String.format("Sent to server: %s", String.join(" ", (String [])message)));
            }
            out.writeObject(message);
            out.flush();
        }catch(IOException e){
            System.out.println("Error writing to outstream");
        }
    }




    public static void main(String [] args) throws Exception{
        String username = "chri";
        Testing.connectToServer("127.0.0.1");
        Thread.sleep(1000);
        Testing.register(username, "chris");
        Thread.sleep(1000);
        Testing.login(username, "chris");
        Scanner scan = new Scanner(System.in);
        if(username.equals("chris")) Testing.createNewGame();
        else Testing.joinGame(Server.games.get(0).getGameKey());
        scan.nextLine();
    }
}
