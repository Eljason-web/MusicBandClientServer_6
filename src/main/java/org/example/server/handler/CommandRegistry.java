package org.example.server.handler;

import org.example.common.command.Command;
import org.example.common.model.MusicBand;
import org.example.common.enums.MusicGenre;
import org.example.common.command.Response;
import org.example.common.model.Coordinates;
import org.example.common.model.Album;
import org.example.server.service.CollectionManager;

import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

public class CommandRegistry {
    public record CommandEntry(CommandHandler handler, String description) {}

    public interface CommandHandler {
        Response execute(CollectionManager manager, Command command);
    }

    private final Map<String, CommandEntry> registry = new LinkedHashMap<>();

    public CommandRegistry(){

        registry.put("help", new CommandEntry(
                (manager, command) -> new Response(true, getHelpMessage()),
                "Display help on available commands"));

        registry.put("info", new CommandEntry(
                (manager, command) -> new Response(true, manager.getInfo()),
                "Print collection information"));

        registry.put("show", new CommandEntry(
                (manager, command) -> {
                    var bands = manager.getSortedCollection();
                    return new Response(true, formatTable(bands));
                },
                "Print all elements of the collection"
        ));
        registry.put("clear", new CommandEntry(
                (manager, command) -> {
                    String ownerLogin = command.getLogin();
                    String result = manager.clear(ownerLogin);

                    return new Response(true, result);
                },
                "Clear collection (only your own bands)"));

        registry.put("insert", new CommandEntry(
                (manager, command) -> {
                    String ownerLogin = command.getLogin();
                    MusicBand band = command.getMusicBand();

                    if (band != null) {
                        band.setOwner(ownerLogin);
                    }

                    String result = manager.insert(command.getKey(), band, ownerLogin);
                    return new Response(true, result);
                },
                "Add a new element with the given key"));

        registry.put("add_random", new CommandEntry(
            (manager, command) -> {
                try {
                    int count = Integer.parseInt(command.getArgument());
                    if (count <= 0) {
                        return new Response(false, "Error: Number must be > 0");
                    }

                    String ownerLogin = command.getLogin();
                    int successCount = 0;

                    for (int i = 0; i < count; i++) {
                        String key = "rand_" + i + "_" + System.currentTimeMillis() + "_" +
                                    UUID.randomUUID().toString().substring(0, 4);
                        MusicBand band = generateRandomBand();

                        band.setOwner(ownerLogin);
                        String result = manager.insert(key, band, ownerLogin);

                        if (result.contains("✅")) {
                            successCount++;
                        }
                    }

                    return new Response(true, " Successfully added " + successCount + " band(s) to database.");

                } catch (NumberFormatException e) {
                    return new Response(false, " Error: Invalid number format");
                } catch (Exception e) {
                    return new Response(false, " Error:❌ Failed to generate bands");
                }
            },
                "Add n random music bands to the collection"));

        registry.put("update", new CommandEntry(
                (manager,command) -> {
                    String ownerLogin = command.getLogin();
                    MusicBand band = command.getMusicBand();

                    if (band != null) {
                        band.setOwner(ownerLogin);
                    }

                    String result = manager.update(command.getId(), band, ownerLogin);
                    return new Response(true, result);
                },
                "Update element by ID (only if you own it)"));

        registry.put("remove_key", new CommandEntry(
                (manager, command) -> {
                    String ownerLogin = command.getLogin();
                    String result = manager.removeKey(command.getKey(), ownerLogin);
                    return new Response(true, result);
                },
                "Remove element by key (only if you own it)"));

        registry.put("replace_if_lower", new CommandEntry(
                (manager, command) -> {
                    String ownerLogin = command.getLogin();
                    MusicBand band = command.getMusicBand();

                    if (band != null) {
                        band.setOwner(ownerLogin);
                    }

                    String result = manager.replaceIfLower(command.getKey(), band, ownerLogin);
                    return new Response(true, result);
                },
                "Replace if new value is less (only if you own it)"));

        registry.put("remove_greater", new CommandEntry(
                (manager, command) -> {
                    var removed = manager.removeGreater(command.getMusicBand());
                    return new Response(true, "Removed " + removed.size() + " band(s) greater than specified", removed);
                }
                ,"Remove elements greater than specified"
        ));


        registry.put("count_greater_than_description", new CommandEntry(
                (manager, command) -> {
                    String desc = command.getArgument();
                    long count = manager.countGreaterThanDescription(desc);
                    return new Response(true,"Number of bands with description greater than '" + desc + "': " + count);
                },
                "Count elements with description greater"));

        registry.put("filter_less_than_genre", new CommandEntry(
                (manager, command) -> {
                    try {
                        MusicGenre genre = MusicGenre.valueOf(command.getArgument().toUpperCase());
                        var bands = manager.filterLessThanGenre(genre);
                        return  new Response(true, "Bands with genre less than " + genre, bands);
                    } catch (IllegalArgumentException e) {
                        return new Response(false, " Invalid genre: '" + command.getArgument() + "'\nValid genres: " +
                               Arrays.toString(MusicGenre.values()));
                    }
                },
                "Display elements with genre less than specified"
        ));

        registry.put("print_field_descending_number_of_participants", new CommandEntry(
                (manager, command) -> {
                    var bands = manager.getBandsByParticipantsDescending();
                    return new Response(true, "Number of Participants(Descending)", bands);
                },
                "Output participants in descending order"
        ));
    }

    private String formatTable(List<MusicBand> bands) {
        if (bands.isEmpty()) {
            return "Collection Contents\nEmpty collection";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Collection Contents\n");
        sb.append("---------------------------------------------------------------------------\n");
        sb.append(String.format("  %-10s %-25s %-15s %-10s\n", "ID", "Name", "Genre", "Members"));
        sb.append("---------------------------------------------------------------------------\n");

        for (MusicBand band : bands) {
            String idStr = band.getId() != null ? String.valueOf(band.getId()) : "N/A";
            String nameStr = band.getName() != null ? band.getName() : "Unknown";
            String genreStr = band.getGenre() != null ? band.getGenre().toString() : "N/A";
            int membersCount = band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : 0;

            sb.append(String.format(" %-10s %-25s %-15s %-10d\n",
                    idStr,
                    nameStr,
                    genreStr,
                    membersCount
            ));
        }

        sb.append("-----------------------------------------------------------------------------\n");
        sb.append("Total: ").append(bands.size()).append(" band(s)");
        return sb.toString();
    }

    public String getHelpMessage() {
        return "Available Commands:\n" + registry.entrySet().stream()
                .map(entry -> entry.getKey() + " : " + entry.getValue().description())
                        .collect(Collectors.joining("\n"));
    }

    public CommandHandler getHandler(String commandName) {
        CommandEntry entry = registry.get(commandName);
        return entry != null ? entry.handler() : null;
    }

    private MusicBand generateRandomBand() {
        Random random = new Random();

        String name = "Band_" + random.nextInt(10000);
        String desc = "Auto-generated band";

        var coordinates = new Coordinates(
                random.nextDouble() * 1000,
                random.nextInt(10000)
        );

        var genre = MusicGenre.values()[
                random.nextInt(MusicGenre.values().length)
        ];

        int members = random.nextInt(49) + 1;
        int year = random.nextInt(75) + 1950;

        var album = new Album(
                "Album_" + random.nextInt(500),
                (random.nextInt(190) + 10)
        );

        var band = new MusicBand();
        band.setName(name);
        band.setDescription(desc);
        band.setCoordinates(coordinates);
        band.setGenre(genre);
        band.setNumberOfParticipants(members);
        band.setYear(year);
        band.setBestAlbum(album);
        band.setCreationDate(LocalDateTime.now());

        return band;
    }
}
