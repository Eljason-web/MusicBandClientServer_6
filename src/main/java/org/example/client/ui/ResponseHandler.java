package org.example.client.ui;

import org.example.common.model.MusicBand;
import org.example.common.command.Response;

import java.util.List;
import java.util.stream.Collectors;

public class ResponseHandler {

    public void handleResponse(Response response) {
        if (response.getMessage() != null && !response.getMessage().isEmpty()) {
            System.out.println(response.getMessage());
        }

        if (response.getData() != null) {
            if (response.getData() instanceof List<?> dataList) {
                if (!dataList.isEmpty()) {
                    System.out.println("Remove items: " + dataList.stream().
                            map(Object::toString).
                            collect(Collectors.joining(", ")));
                }
            }
        }

        if (response.getBands() != null && !response.getBands().isEmpty()) {
            if (!response.getMessage().contains("Collection Contents")) {
                displayBands(response.getBands());
            }
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

        for (MusicBand band : bands) {

            String id = band.getId() != null ? band.getId().toString() : "N/A";
            String name = band.getName() != null ? band.getName() : "Unknown";
            String genre = band.getGenre() != null ? band.getGenre().toString() : "N/A";
            String members = band.getNumberOfParticipants() != null ? band
                    .getNumberOfParticipants().toString() : "N/A";

            System.out.printf(" %-10s %-25s %-15s %-10s%n", id, name, genre, members);
        }
        System.out.println("_______________________________________________________________");
        System.out.println("Total: " + bands.size() + " band(s)");
    }
}
