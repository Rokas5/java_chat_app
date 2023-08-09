import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;

public class Server {
    static List<ConnectionHandler> connectedUsers = new ArrayList<>();
    
    static class ConnectionHandler implements Runnable{

        private final Socket s;
        private final BufferedReader in;
        private final PrintWriter out;

        String username;

        public ConnectionHandler(Socket s) throws IOException{
            this.s = s;
            this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.out = new PrintWriter(s.getOutputStream(), true);
        }

        public void run(){
            try{
                while(true){
                    String name = this.in.readLine();
                    if(name != null){
                        this.username = name;
                        String formattedMessage = "User " + username + " has joined the chat!";
                        System.out.println(formattedMessage);
                        broadcastMessage(formattedMessage, this);
                        break;
                    }
                }

                while(true){
                    String message = this.in.readLine();
                    if(message != null){
                        String formattedMessage = username+": " + message;
                        System.out.println(formattedMessage);
                        broadcastMessage(formattedMessage, this);
                    }
                }
            } catch(Exception e){
                String formattedMessage = username + " has disconnected";
                System.out.println(formattedMessage);
                broadcastMessage(formattedMessage, null);
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }
    }

    static void broadcastMessage(String message, ConnectionHandler sender){
        for(var user : connectedUsers){
            if(!user.equals(sender)){
                user.sendMessage(message);
            }
        }
    }

    public static void main(String args[]){
        ExecutorService executor = null; // TODO: fix to always shutdown properly
        try{
            executor = Executors.newCachedThreadPool();
            try(ServerSocket ss = new ServerSocket(5050)){
                while(true){
                        Socket s = ss.accept();
                        if(s != null){
                            var handler = new ConnectionHandler(s);
                            connectedUsers.add(handler);
                            executor.execute(handler);
                        }
                    }
            } catch(Exception e){
                e.printStackTrace();
            }
        } finally{
            executor.shutdown();
        }
    }
}