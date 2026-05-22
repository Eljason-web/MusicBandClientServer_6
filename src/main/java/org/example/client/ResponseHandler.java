package org.example.client;

import org.example.common.MusicBand;
import org.example.common.Response;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

public class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public void handleResponse(Response response) {
        if (response != null) {
        }

        if (response == null) {
            logger.debug(" Error: No response from server");
            System.out.println(" Error: No response from server");
            return;
        }

        System.out.println(response.getMessage());

        if (response.getBands() != null && !response.getBands().isEmpty()) {
            displayBands(response.getBands());
        }
    }

    private void displayBands(List<MusicBand> bands) {
        if (bands == null || bands.isEmpty()) {
            System.out.println("No bands to display");
            return;
        }

        System.out.println("_______________________________________________________________");
        System.out.printf(" %-10s %-25s %-15s %-10s%n", "ID", "Name", "Genre", "Members");
        System.out.println("_______________________________________________________________");

        for (MusicBand band : bands){

                    String id = band.getId() != null ? band.getId().toString() : "N/A";
                    String name = band.getName() != null ? band.getName(): "Unknown";
                    String genre = band.getGenre() != null ? band.getGenre().toString() : "N/A";
                    String members = band.getNumberOfParticipants() != null ? band
                            .getNumberOfParticipants().toString() : "N/A";

                    System.out.printf(" %-10s %-25s %-15s %-10s%n", id, name, genre, members);
        }
        System.out.println("_______________________________________________________________");
        System.out.println("Total: " + bands.size() + " band(s)");
    }
}
