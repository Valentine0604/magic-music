package org.valentine.client;

import org.valentine.utils.VigenereCipher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class FileSocketClientGUI extends JFrame {
    private JTextField fileNameField;
    private JTextArea contentArea;
    private JTable filesTable;
    private DefaultTableModel tableModel;

    public FileSocketClientGUI() {
        // Konfiguracja głównego okna
        setTitle("Klient Plików");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel górny - operacje
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Panel środkowy - lista plików
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel dolny - obszar treści
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Przyciski operacji
        JButton listButton = new JButton("Lista plików");
        JButton readButton = new JButton("Odczytaj plik");
        JButton uploadButton = new JButton("Wyślij plik");
        JButton deleteButton = new JButton("Usuń plik");

        // Pole nazwy pliku
        fileNameField = new JTextField(20);
        panel.add(new JLabel("Nazwa pliku:"));
        panel.add(fileNameField);

        // Dodanie akcji do przycisków
        listButton.addActionListener(e -> listFiles());
        readButton.addActionListener(e -> readFile());
        uploadButton.addActionListener(e -> uploadFile());
        deleteButton.addActionListener(e -> deleteFile());

        // Dodanie przycisków do panelu
        panel.add(listButton);
        panel.add(readButton);
        panel.add(uploadButton);
        panel.add(deleteButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Nagłówki kolumn
        String[] columnNames = {"Nazwa pliku"};
        tableModel = new DefaultTableModel(columnNames, 0);
        filesTable = new JTable(tableModel);

        // Przewijanie dla tabeli
        JScrollPane scrollPane = new JScrollPane(filesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Obszar treści pliku
        contentArea = new JTextArea(10, 50);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void listFiles() {
        try {
            String response = sendEncryptedCommand("LIST");

            // Czyszczenie poprzedniej zawartości tabeli
            tableModel.setRowCount(0);

            // Dodanie plików do tabeli
            String[] files = response.substring("SUCCESS|".length()).split("\\|");
            for (String file : files) {
                tableModel.addRow(new Object[]{file});
            }
        } catch (Exception e) {
            showError("Błąd listowania plików: " + e.getMessage());
        }
    }

    private void readFile() {
        String filename = fileNameField.getText();
        if (filename.isEmpty()) {
            showError("Podaj nazwę pliku!");
            return;
        }

        try {
            String response = sendEncryptedCommand("READ|" + filename);

            if (response.startsWith("SUCCESS")) {
                String content = response.substring("SUCCESS|".length());
                contentArea.setText(content);
            } else {
                showError(response);
            }
        } catch (Exception e) {
            showError("Błąd odczytu pliku: " + e.getMessage());
        }
    }

    private void uploadFile() {
        String filename = fileNameField.getText();
        String content = contentArea.getText();

        if (filename.isEmpty() || content.isEmpty()) {
            showError("Podaj nazwę pliku i zawartość!");
            return;
        }

        try {
            String response = sendEncryptedCommand("UPLOAD|" + filename + "|" + content);
            JOptionPane.showMessageDialog(this, response, "Operacja", JOptionPane.INFORMATION_MESSAGE);

            // Odśwież listę plików po uploadzeniu
            listFiles();
        } catch (Exception e) {
            showError("Błąd wysyłania pliku: " + e.getMessage());
        }
    }

    private void deleteFile() {
        String filename = fileNameField.getText();
        if (filename.isEmpty()) {
            showError("Podaj nazwę pliku!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Czy na pewno chcesz usunąć plik: " + filename + "?",
                "Potwierdzenie usunięcia",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String response = sendEncryptedCommand("DELETE|" + filename);
                JOptionPane.showMessageDialog(this, response, "Operacja", JOptionPane.INFORMATION_MESSAGE);

                // Odśwież listę plików po usunięciu
                listFiles();
            } catch (Exception e) {
                showError("Błąd usuwania pliku: " + e.getMessage());
            }
        }
    }

    private String sendEncryptedCommand(String command) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            // Zaszyfruj komendę
            String encryptedCommand = VigenereCipher.encrypt(command);
            output.println(encryptedCommand);

            // Odczytaj zaszyfrowaną odpowiedź
            String encryptedResponse = input.readLine();
            return VigenereCipher.decrypt(encryptedResponse);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Błąd",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        // Uruchomienie GUI w wątku EDT
        SwingUtilities.invokeLater(() -> {
            FileSocketClientGUI gui = new FileSocketClientGUI();
            gui.setVisible(true);
        });
    }
}