package org.example.client.util;

import org.example.client.network.ClientNetwork;
import org.example.common.command.Command;
import org.example.common.enums.MusicGenre;
import org.example.common.model.Album;
import org.example.common.model.Coordinates;
import org.example.common.model.MusicBand;

import java.time.LocalDateTime;
import java.util.Random;

public class RandomBandGenerator {
    public static final Random RANDOM = new Random();

    public static void generateAndSend (int count, ClientNetwork network) {
        System.out.println(" Generating " + count + " random band(s)...\n");

        for (int i = 1; i <= count; i++) {
            String key = "rand_" + i + "_" + System.currentTimeMillis();
            String name = "Band_" + RANDOM.nextInt(10000);
            String desc = "Auto-generated band #" + i;

            Coordinates coordinates = new Coordinates(RANDOM.nextDouble() * 1000, (long) RANDOM.nextInt(10000));
            MusicGenre genre = MusicGenre.values()[RANDOM.nextInt(MusicGenre.values().length)];
            int members = RANDOM.nextInt(49) + 1;
            int year = RANDOM.nextInt(75) + 1950;
            String albumName = "Album_" + RANDOM.nextInt(500);
            long albumLength = RANDOM.nextInt(190) + 10;

            MusicBand band = new MusicBand();
            band.setName(name);
            band.setDescription(desc);
            band.setCoordinates(coordinates);
            band.setGenre(genre);
            band.setNumberOfParticipants(members);
            band.setYear(year);
            band.setBestAlbum(new Album(albumName,albumLength));
            band.setCreationDate(LocalDateTime.now());

            Command command = new Command("insert", key, band, null, null);
            network.sendCommand(command);

            try {Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();


            }
            System.out.println(" [" + i + "/" + count + "] Added: " + name);
        }
        System.out.println(" Success: Generation complete.");
    }
}