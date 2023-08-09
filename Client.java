import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    static class ConnectionHandler implements Runnable{

        private final BufferedReader in;
        private final PrintWriter out;

        public ConnectionHandler(Socket s) throws IOException{
            this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.out = new PrintWriter(s.getOutputStream(), true);
        }

        public void run(){
            Console userInput = System.console();
            String username = null;
            while(username == null){
                username = userInput.readLine("Enter you username:");
            }
            this.out.println(username);

            while(true){
                String userMessage = userInput.readLine("");
                if(userMessage != null){
                    this.out.println(userMessage);
                }
            }
        }

        public String getMessage() throws IOException{
            return in.readLine();
        }
    }

    public static void main(String args[]){
        ExecutorService executor = null; // TODO: fix to always shutdown properly
        try{
            executor = Executors.newCachedThreadPool();
            try(Socket s = new Socket("localhost", 5050)){
                System.out.println("Connected");
                var handler = new ConnectionHandler(s);
                executor.execute(handler);
                
                while(true){
                    String message = handler.getMessage();
                    if(message != null){
                        System.out.println(message);
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } finally {
            executor.shutdown();
        }
    }
}
