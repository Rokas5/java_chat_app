import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.*;
import java.util.*;
import java.nio.file.*;

public class Server {
    private static List<ConnectionHandler> connectedUsers = new ArrayList<>();
    private static List<String> messages = new ArrayList<>();

    private static Lock lock = new ReentrantLock();
    private static BufferedWriter logOuputStream;
    
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
                        broadcastMessage("User " + username + " has joined the chat!", this);
                        break;
                    }
                }

                // send all sent messages in chat
                for(String message : messages){
                    this.out.println(message);
                }

                while(true){
                    String message = this.in.readLine();
                    if(message != null){
                        broadcastMessage(username+": " + message, this);
                    }
                }
            } catch(Exception e){
                try{
                    broadcastMessage(username + " has disconnected", null);
                } catch(IOException IOe){
                    System.out.println("Failed to send disconnect message");
                    IOe.printStackTrace();
                }
            } finally {
                try{
                    this.in.close();
                } catch(IOException e){
                    System.out.println("Failed to close input stream");
                    e.printStackTrace();
                }
                this.out.close();
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }
    }

    static void broadcastMessage(String message, ConnectionHandler sender) throws IOException{
        lock.lock();
        messages.add(message);
        logOuputStream.write(message+'\n');
        logOuputStream.flush();
        System.out.println(message);
        try{
            for(var user : connectedUsers){
                if(user!= null && !user.equals(sender)){
                    user.sendMessage(message);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void main(String args[]){
        ExecutorService executor = null; // TODO: fix to always shutdown properly
        try{
            Path chatLogFile = Paths.get(".").resolve("chatLog.txt");
            Files.deleteIfExists(chatLogFile);

            logOuputStream = Files.newBufferedWriter(chatLogFile);

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
        } catch(IOException e){

        } finally{
            executor.shutdown();
        }
    }
}