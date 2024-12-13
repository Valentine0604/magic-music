package org.valentine.utils;

public class VigenereCipher {
    // Klucz szyfrowania (warto go później zabezpieczyć bardziej zaawansowaną metodą)
    private static final String DEFAULT_KEY = "VALENTINE";

    /**
     * Szyfrowanie wiadomości szyfrem Vigenère'a
     * @param message wiadomość do zaszyfrowania
     * @return zaszyfrowana wiadomość
     */
    public static String encrypt(String message) {
        return encrypt(message, DEFAULT_KEY);
    }

    /**
     * Szyfrowanie wiadomości szyfrem Vigenère'a
     * @param message wiadomość do zaszyfrowania
     * @param key klucz szyfrujący
     * @return zaszyfrowana wiadomość
     */
    public static String encrypt(String message, String key) {
        StringBuilder encrypted = new StringBuilder();
        key = key.toUpperCase();
        message = message.toUpperCase();

        int keyLength = key.length();

        for (int i = 0, j = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            // Szyfruj tylko litery
            if (Character.isLetter(c)) {
                // Przesuń literę zgodnie z kluczem
                int shift = key.charAt(j % keyLength) - 'A';
                char encryptedChar = (char)(((c - 'A' + shift) % 26) + 'A');
                encrypted.append(encryptedChar);

                // Przesuń indeks klucza
                j++;
            } else {
                // Zostaw znaki specjalne bez zmian
                encrypted.append(c);
            }
        }

        return encrypted.toString();
    }

    /**
     * Deszyfrowanie wiadomości szyfrem Vigenère'a
     * @param encryptedMessage zaszyfrowana wiadomość
     * @return odszyfrowana wiadomość
     */
    public static String decrypt(String encryptedMessage) {
        return decrypt(encryptedMessage, DEFAULT_KEY);
    }

    /**
     * Deszyfrowanie wiadomości szyfrem Vigenère'a
     * @param encryptedMessage zaszyfrowana wiadomość
     * @param key klucz szyfrujący
     * @return odszyfrowana wiadomość
     */
    public static String decrypt(String encryptedMessage, String key) {
        StringBuilder decrypted = new StringBuilder();
        key = key.toUpperCase();
        encryptedMessage = encryptedMessage.toUpperCase();

        int keyLength = key.length();

        for (int i = 0, j = 0; i < encryptedMessage.length(); i++) {
            char c = encryptedMessage.charAt(i);

            // Deszyfruj tylko litery
            if (Character.isLetter(c)) {
                // Przesuń literę zgodnie z kluczem wstecz
                int shift = key.charAt(j % keyLength) - 'A';
                char decryptedChar = (char)(((c - 'A' - shift + 26) % 26) + 'A');
                decrypted.append(decryptedChar);

                // Przesuń indeks klucza
                j++;
            } else {
                // Zostaw znaki specjalne bez zmian
                decrypted.append(c);
            }
        }

        return decrypted.toString();
    }
}