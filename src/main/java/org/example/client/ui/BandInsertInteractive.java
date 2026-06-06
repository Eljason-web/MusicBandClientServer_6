package org.example.client.ui;

import org.example.client.util.MusicBandValidator;
import org.example.common.enums.MusicGenre;
import org.example.common.model.Coordinates;
import org.example.common.model.MusicBand;
import org.example.common.model.Album;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.function.Function;

public class BandInsertInteractive {
    private final Scanner scanner;

    public BandInsertInteractive(Scanner scanner) {
        this.scanner = scanner;
    }

    public MusicBand buildBandInteractively() {
        try {
            System.out.println("Enter name: ");
            String name = readUntilValid(input -> MusicBandValidator.parseNonEmpty(input,"Name"));

            System.out.println("Enter description: ");
            String description = readUntilValid(input -> MusicBandValidator.parseNonEmpty(input,"Description"));

            System.out.println("Enter Coordinates (x y): ");
            Coordinates coordinates = readUntilValid(MusicBandValidator::parseCoordinates);

            System.out.println("Enter genre (" + Arrays.toString(MusicGenre.values()) + "): ");
            MusicGenre genre = readUntilValid(MusicBandValidator::parseGenre);

            System.out.println("Enter number of participants: ");
            Integer participants = readUntilValid(input -> MusicBandValidator.parsePositiveInt(input,"participants"));

            System.out.println("Enter year of establishment: ");
            Integer year = readUntilValid(input -> MusicBandValidator.parsePositiveInt(input,"Year" ));

            System.out.print("Enter album name: ");
            String albumName = readUntilValid(input -> MusicBandValidator.parseNonEmpty(input, "Album name"));

            System.out.println("Enter album length: ");
            Long albumLength = readUntilValid(input -> MusicBandValidator.parsePostiveLong(input, "Album length"));

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

        } catch (CancellationException e) {
            return null;
        }
    }

    private <T> T readUntilValid(Function<String, T> validator) {
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("cancel")) {
                throw new CancellationException();
            }
            try {
                return validator.apply(input);
            } catch (IllegalArgumentException e) {
                System.out.println(" " + e.getMessage() + ". Try again:");
            }
        }
    }
}
