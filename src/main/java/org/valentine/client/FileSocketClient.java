package org.valentine.client;

import org.valentine.utils.VigenereCipher;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileSocketClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try {
            displayMenu();
        } catch (Exception e) {
            System.err.println("Błąd w aplikacji klienckiej: " + e.getMessage());
        }
    }

    // Pozostałe metody jak poprzednio, ale dodaj szyfrowanie wysyłanych wiadomości
    private static void sendEncryptedCommand(String command) throws IOException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            // Zaszyfruj komendę przed wysłaniem
            String encryptedCommand = VigenereCipher.encrypt(command);
            output.println(encryptedCommand);

            // Odszyfruj odpowiedź
            String encryptedResponse = input.readLine();
            String response = VigenereCipher.decrypt(encryptedResponse);

            // Wyświetl lub przetwórz odpowiedź jak poprzednio
            processServerResponse(response);
        }
    }

    private static void processServerResponse(String response) {
        if (response.startsWith("SUCCESS")) {
            String content = response.substring("SUCCESS|".length());
            System.out.println("Operacja zakończona sukcesem. Szczegóły:");
            System.out.println(content);
        } else if (response.startsWith("ERROR")) {
            System.out.println("Błąd: " + response.substring("ERROR|".length()));
        } else {
            System.out.println("Nieznana odpowiedź serwera: " + response);
        }
    }

    private static void listFiles() {
        try {
            sendEncryptedCommand("LIST");
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    private static void readFile(Scanner scanner) {
        System.out.print("Podaj nazwę pliku do odczytu: ");
        String filename = scanner.nextLine();

        try {
            sendEncryptedCommand("READ|" + filename);
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    private static void uploadFile(Scanner scanner) {
        System.out.print("Podaj nazwę pliku do przesłania: ");
        String filename = scanner.nextLine();

        System.out.println("Wprowadź zawartość pliku (zakończ wpisaniem 'KONIEC'):");
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("KONIEC")) {
            contentBuilder.append(line).append("\n");
        }

        try {
            sendEncryptedCommand("UPLOAD|" + filename + "|" + contentBuilder.toString());
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    private static void deleteFile(Scanner scanner) {
        System.out.print("Podaj nazwę pliku do usunięcia: ");
        String filename = scanner.nextLine();

        try {
            sendEncryptedCommand("DELETE|" + filename);
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    // Reszta kodu pozostaje taka sama jak w poprzedniej wersji
    private static void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- MENU KLIENTA PLIKÓW ---");
            System.out.println("1. Lista plików");
            System.out.println("2. Odczyt pliku");
            System.out.println("3. Przesłanie pliku");
            System.out.println("4. Usunięcie pliku");
            System.out.println("5. Wyjście");
            System.out.print("Wybierz opcję: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> listFiles();
                case 2 -> readFile(scanner);
                case 3 -> uploadFile(scanner);
                case 4 -> deleteFile(scanner);
                case 5 -> {
                    System.out.println("Zamykanie aplikacji...");
                    return;
                }
                default -> System.out.println("Nieprawidłowy wybór. Spróbuj ponownie.");
            }
        }
    }
}