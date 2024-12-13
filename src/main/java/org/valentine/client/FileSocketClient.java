package org.valentine.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static org.valentine.utils.ServerConfig.SERVER_HOST;
import static org.valentine.utils.ServerConfig.SERVER_PORT;

public class FileSocketClient {

    public static void main(String[] args) {
        try {
            // Uruchomienie interfejsu użytkownika
            displayMenu();
        } catch (Exception e) {
            System.err.println("Błąd w aplikacji klienckiej: " + e.getMessage());
        }
    }

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

    private static void listFiles() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            output.println("LIST");
            String response = input.readLine();

            if (response.startsWith("SUCCESS")) {
                String[] files = response.split("\\|");
                System.out.println("Dostępne pliki:");
                for (int i = 1; i < files.length; i++) {
                    System.out.println("- " + files[i]);
                }
            } else {
                System.out.println("Błąd podczas pobierania listy plików: " + response);
            }
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    private static void readFile(Scanner scanner) {
        System.out.print("Podaj nazwę pliku do odczytu: ");
        String filename = scanner.nextLine();

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            output.println("READ|" + filename);
            String response = input.readLine();

            if (response.startsWith("SUCCESS")) {
                String content = response.substring("SUCCESS|".length());
                System.out.println("Zawartość pliku " + filename + ":");
                System.out.println(content);
            } else {
                System.out.println("Błąd podczas odczytu pliku: " + response);
            }
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

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            output.println("UPLOAD|" + filename + "|" + contentBuilder.toString());
            String response = input.readLine();

            System.out.println(response);
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    private static void deleteFile(Scanner scanner) {
        System.out.print("Podaj nazwę pliku do usunięcia: ");
        String filename = scanner.nextLine();

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            output.println("DELETE|" + filename);
            String response = input.readLine();

            System.out.println(response);
        } catch (IOException e) {
            System.err.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }
}