package org.example.client.util;

import org.example.common.enums.MusicGenre;
import org.example.common.model.Coordinates;

import java.util.Arrays;

public class MusicBandValidator {

    public static Coordinates parseCoordinates(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length != 2) throw new IllegalArgumentException("Expected format: <x> <y>");
        try {
            return new Coordinates(Double.parseDouble(parts[0]), Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numbers. Use format: <double> <long>");
        }
    }

    public static MusicGenre parseGenre(String input) {
        try {
            return MusicGenre.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid genre. Valid: " + Arrays.toString(MusicGenre.values()));
        }
    }

    public static Integer parsePositiveInt(String input, String fieldName) {
        try {
            int val = Integer.parseInt(input.trim());
            if (val <= 0) throw new IllegalArgumentException(fieldName + " must be > 0");
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer");
        }
    }

    public static Long parsePostiveLong(String input, String fieldName) {
        try {
            long val = Long.parseLong(input.trim());
            if (val <= 0) throw new IllegalArgumentException(fieldName + " must be > 0");
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    public static String parseNonEmpty(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be empty");
        return input.trim();
    }
}
