package org.example.client.ui;

import org.example.common.enums.MusicGenre;
import org.example.common.model.Coordinates;
import org.example.common.model.MusicBand;
import org.example.common.model.Album;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;

public class BandInsertInteractive {
    private final Scanner scanner;

    public BandInsertInteractive(Scanner scanner) {
        this.scanner = scanner;
    }

    public MusicBand buildBandInteractively() {
        System.out.println("Enter name: ");
        String name = readNonEmpty("Name cannot be empty");

        System.out.println("Enter description: ");
        String description = readNonEmpty("Description cannot be empty");

        System.out.println("Enter Coordinates (x y): ");
        Coordinates coordinates = readCoordinates();

        System.out.println("Enter genre (" + Arrays.toString(MusicGenre.values()) + "): ");
        MusicGenre genre = readGenre();

        System.out.println("Enter number of participants: ");
        Integer participants = readPositiveInt("Must be > 0");

        System.out.println("Enter year of establishment: ");
        Integer year = readPositiveInt("Year must be > 0");

        System.out.print("Enter album name: ");
        String albumName = readNonEmpty("Album name cannot be empty");

        System.out.println("Enter album length: ");
        Long albumLength = readPositiveLong();

        MusicBand band = new MusicBand();
        band.setName(name);
        band.setDescription(description);
        band.setCoordinates(coordinates);
        band.setCreationDate(LocalDateTime.now());
        band.setNumberOfParticipants(participants);
        band.setAlbumsCount(0);
        band.setGenre(genre);
        band.setBestAlbum(new Album(albumName, albumLength));
        band.setYear(year);
        return band;
    }

    private String readNonEmpty(String errorMsg) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty())
                return input;
            System.out.println(" " + errorMsg + ". Try again: ");
        }
    }

    private Coordinates readCoordinates() {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) return null;
            if (input.isEmpty()) {
                System.out.println(" Input cannot be empty. Try again.");
                continue;
            }

            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                System.out.println(" Invalid format. Expected: <x> <y> (Enter BOTH on one line)");
                continue;
            }
            try {
                double x = Double.parseDouble(parts[0]);
                long y = Long.parseLong(parts[1]);
                return new Coordinates(x,y);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid numbers. Use: <double> <long> (e.g., 12.5 20)");
            }
        }
    }

    private MusicGenre readGenre() {
        while (true) {
            String input = scanner.nextLine().trim().toUpperCase();
            try{
                return MusicGenre.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println(" Invalid genre. Valid: " + Arrays.toString(MusicGenre.values()));
            }
        }
    }

    private Integer readPositiveInt (String errorMsg) {
        while (true) {
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val > 0) return val;
                System.out.println(" " + errorMsg);
            } catch (NumberFormatException e) {
                System.out.println(" Must be a valid integer");
            }
        }
    }

    private Long readPositiveLong() {
        while (true) {
            try {
                long val = Long.parseLong(scanner.nextLine().trim());
                if (val > 0) return val;
                System.out.println(" " + "Must be > 0");
            } catch (NumberFormatException e) {
                System.out.println(" Must be a valid number");
            }
        }
    }
}
