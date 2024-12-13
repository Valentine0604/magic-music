package org.valentine.utils;

import java.util.Arrays;
import java.util.List;

public class FileUtils {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "txt", "csv", "json", "xlsx", "log"
    );

    public static boolean isAllowedFile(String fileName) {
        String extension = getFileExtension(fileName);

        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');

        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}
