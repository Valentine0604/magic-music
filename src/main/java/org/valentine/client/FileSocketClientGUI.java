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
        // Główna konfiguracja okna
        setTitle("Klient Plików");
        setSize(800, 600); // Rozmiar okna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Utworzenie i dodanie paneli do GUI
        JPanel topPanel = createTopPanel();           // Panel z przyciskami
        add(topPanel, BorderLayout.NORTH);            // Położenie: góra
        JPanel centerPanel = createCenterPanel();     // Panel z tabelą
        add(centerPanel, BorderLayout.CENTER);        // Położenie: centrum
        JPanel bottomPanel = createBottomPanel();     // Panel z obszarem do edycji treści
        add(bottomPanel, BorderLayout.SOUTH);         // Położenie: dół
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Przyciski operacji
        JButton listButton = new JButton("Lista plików");
        JButton readButton = new JButton("Odczytaj plik");
        JButton uploadButton = new JButton("Wyślij plik");
        JButton deleteButton = new JButton("Usuń plik");
        JButton deleteSelectedButton = new JButton("Usuń wybrany plik"); // NOWY GUZIK do usuwania pliku z tabeli

        // Pole nazwy pliku
        fileNameField = new JTextField(20);
        panel.add(new JLabel("Nazwa pliku:"));
        panel.add(fileNameField);

        // Dodanie akcji do przycisków
        listButton.addActionListener(e -> listFiles());
        readButton.addActionListener(e -> readFile());
        uploadButton.addActionListener(e -> uploadFile());
        deleteButton.addActionListener(e -> deleteFile());
        deleteSelectedButton.addActionListener(e -> deleteSelectedFile()); // Akcja usuwania wybranego pliku

        // Dodanie przycisków do panelu
        panel.add(listButton);
        panel.add(readButton);
        panel.add(uploadButton);
        panel.add(deleteButton);
        panel.add(deleteSelectedButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Nagłówki kolumn tabeli
        String[] columnNames = {"Nazwa pliku"};
        tableModel = new DefaultTableModel(columnNames, 0); // Model tabeli
        filesTable = new JTable(tableModel); // Tworzenie tabeli

        // Dodanie scrollowania do tabeli
        JScrollPane scrollPane = new JScrollPane(filesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Słuchacz wybierania wierszy w tabeli
        filesTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = filesTable.getSelectedRow();
            if (selectedRow >= 0) {
                String selectedFile = (String) tableModel.getValueAt(selectedRow, 0); // Pobierz nazwę pliku
                fileNameField.setText(selectedFile); // Wyświetl ją w polu tekstowym
            }
        });

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Obszar tekstowy do wyświetlania/edycji treści pliku
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

            // Wyczyść tabelę z dotychczasowych wartości
            tableModel.setRowCount(0);

            // Rozdziel pliki i dodaj je do tabeli
            String[] files = response.substring("SUCCESS|".length()).split("\\|");

            if (files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
                showError("Brak plików na serwerze.");
                return;
            }

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
            showError("Podaj nazwę pliku lub wybierz go z listy!");
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

            // Odśwież listę plików po uploadzie
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
                if (response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Plik został usunięty: " + filename, "Operacja zakończona", JOptionPane.INFORMATION_MESSAGE);
                    listFiles();
                } else {
                    showError("Nie udało się usunąć pliku: " + response);
                }
            } catch (Exception e) {
                showError("Błąd usuwania pliku: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedFile() {
        int selectedRow = filesTable.getSelectedRow();

        if (selectedRow < 0) {
            showError("Wybierz plik do usunięcia z listy!");
            return;
        }

        String filename = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Czy na pewno chcesz usunąć plik: " + filename + "?",
                "Potwierdzenie usunięcia",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String response = sendEncryptedCommand("DELETE|" + filename);

                if (response.startsWith("SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Plik został usunięty: " + filename, "Operacja zakończona", JOptionPane.INFORMATION_MESSAGE);
                    tableModel.removeRow(selectedRow); // Usuń wiersz z tabeli po sukcesie
                } else {
                    showError("Nie udało się usunąć pliku: " + response);
                }
            } catch (Exception e) {
                showError("Błąd usuwania pliku: " + e.getMessage());
            }
        }
    }

    private String sendEncryptedCommand(String command) throws IOException {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String encryptedCommand = VigenereCipher.encrypt(command);
            output.println(encryptedCommand);

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
        SwingUtilities.invokeLater(() -> {
            FileSocketClientGUI gui = new FileSocketClientGUI();
            gui.setVisible(true);
        });
    }
}