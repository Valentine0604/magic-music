package org.valentine.server;

import org.valentine.utils.FileUtils;
import org.valentine.utils.ServerConfig;
import org.valentine.utils.VigenereCipher;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;
import static org.valentine.utils.FileUtils.isAllowedFile;
import static org.valentine.utils.ServerConfig.*;

public class ClientHandler implements Runnable{

    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            String encryptedInputLine = input.readLine();
            String inputLine = VigenereCipher.decrypt(encryptedInputLine);
            processCommand(inputLine);
        } catch (IOException e){
            e.printStackTrace();
        } finally{
            try{
                if(input != null) input.close();
                if(output != null) output.close();

                clientSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void processCommand(String command) throws IOException{
        if(command == null) return;

        String[] parts = command.split("\\|");
        String action = parts[0].toUpperCase();

        switch (action) {
            case "LIST" -> listFiles();
            case "READ" -> {
                if (parts.length > 1) {
                    readFile(parts[1]);
                } else {
                    sendEncryptedResponse(ERROR_UNKNOWN_COMMAND);
                }
            }
            case "UPLOAD" -> {
                if (parts.length > 2) {
                    uploadFile(parts[1], parts[2]);
                } else {
                    sendEncryptedResponse(ERROR_UNKNOWN_COMMAND);
                }
            }
            case "DELETE" -> {
                if (parts.length > 1) {
                    deleteFile(parts[1]);
                } else {
                    sendEncryptedResponse(ERROR_UNKNOWN_COMMAND);
                }
            }
            default -> sendEncryptedResponse(ERROR_UNKNOWN_COMMAND);
        }
    }

    private void sendEncryptedResponse(String response) {
        String encryptedResponse = VigenereCipher.encrypt(response);
        output.println(encryptedResponse);
    }

    // Pozostałe metody analogicznie, dodaj wywołanie sendEncryptedResponse() zamiast output.println()
    private void deleteFile(String filename) throws IOException {
        if(!FileUtils.isAllowedFile(filename)){
            sendEncryptedResponse(ERROR_INVALID_FILE_TYPE);
            return;
        }

        Path filePath = Paths.get(DATA_DIRECTORY, filename);

        if(!Files.exists(filePath)){
            sendEncryptedResponse(ERROR_FILE_NOT_FOUND);
            return;
        }

        Files.delete(filePath);
        sendEncryptedResponse(SUCCESS + "File has been deleted successfully.");
    }

    private void uploadFile(String filename, String fileContent) throws IOException {
        if(!FileUtils.isAllowedFile(filename)){
            sendEncryptedResponse(ERROR_INVALID_FILE_TYPE);
            return;
        }

        Path filePath = Paths.get(DATA_DIRECTORY, filename);

        try{
            Files.write(filePath, fileContent.getBytes());
            sendEncryptedResponse(SUCCESS + "File has been uploaded successfully.");
        } catch (IOException e){
            sendEncryptedResponse(ERROR_UPLOAD_FAILED);
        }
    }

    private void readFile(String filename) throws IOException{
        if(!isAllowedFile(filename)){
            sendEncryptedResponse(ERROR_INVALID_FILE_TYPE);
            return;
        }

        Path filePath = Paths.get(DATA_DIRECTORY, filename);

        if(!Files.exists(filePath)){
            sendEncryptedResponse(ERROR_FILE_NOT_FOUND);
            return;
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileContent = new String(fileBytes);

        sendEncryptedResponse(SUCCESS + fileContent);
    }

    private void listFiles() throws IOException {
        List<String> files = list(Paths.get(DATA_DIRECTORY))
                .filter(path -> FileUtils.isAllowedFile(path.getFileName().toString()))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

        sendEncryptedResponse(SUCCESS + String.join("|", files));
    }
}