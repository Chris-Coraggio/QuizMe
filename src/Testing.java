import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Chris on 12/6/2016.
 */
public class Testing {

    static final int PORT = 50000;

    static Socket socket;
    static ObjectOutputStream out;
    static ObjectInputStream in;

    static Thread waitForGameLaunch;

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
        writeToOutstream(new String[]{"JOINGAME", gameKey});
        waitForGameLaunch = new Thread(){
            public void run(){
                try{
                    in.readObject();
                }catch(Exception e){}
            }
        };
        boolean validResponse = validateResponse("JOINGAMESUCCESS");
        waitForGameLaunch.start();
        return validResponse;
    }

    public static boolean launchGame(String gameKey){
        writeToOutstream(new String[]{"LAUNCHGAME", gameKey});
        return validateResponse("LAUNCHGAMESUCCESS");
    }

    public static Question getNextQuestion(){
        writeToOutstream(new String[]{"GETNEXTQUESTION"});
        try {
            Object[] response = (Object [])in.readObject();
            System.out.println(response);
            if (response[1] instanceof Question) {
                return (Question)response[1];
            }else return null;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void sendScore(int score){
        writeToOutstream(new String[]{"SCORE", Integer.toString(score)});
    }

    public static String[] getResults(){
        try {
            Object[] obj = (Object[])in.readObject();
            return Arrays.copyOf(obj, obj.length, String[].class); //for some reason casting didn't work here
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
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
        Thread.sleep(2000);
        Testing.register(username, "chris");
        Thread.sleep(2000);
        Testing.login(username, "chris");
        Scanner scan = new Scanner(System.in);
        String key = "";
        if(username.equals("chris")) Testing.createNewGame();
        else{
            key = scan.nextLine();
            Testing.joinGame(key);
        }
        scan.nextLine();
        if(username.equals("chris")) {
            key = scan.nextLine();
            Testing.launchGame(key);
        }
        Thread.sleep(2000);
        if(!username.equals("chris")){
            waitForGameLaunch.join();
        }
        for(int i=0; i < 5; i++) {
            System.out.println(Testing.getNextQuestion());
            //scan.nextLine();
        }
        if(username.equals("chris")) Testing.sendScore(200);
        else Testing.sendScore(100);
        System.out.println(Arrays.toString(Testing.getResults()));
        scan.nextLine();
    }
}
