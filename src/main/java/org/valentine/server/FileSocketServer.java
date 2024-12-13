package org.valentine.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.valentine.utils.ServerConfig.*;

public class FileSocketServer {

    public static void main(String[] args) {
        try{
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is running on port " + SERVER_PORT);

            ExecutorService executorService = Executors.newCachedThreadPool();

            while(true){
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
