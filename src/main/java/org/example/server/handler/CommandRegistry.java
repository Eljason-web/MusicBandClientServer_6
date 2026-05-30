package org.example.server.handler;

import org.example.common.command.Command;
import org.example.common.model.MusicBand;
import org.example.common.enums.MusicGenre;
import org.example.common.command.Response;
import org.example.server.service.CollectionManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandRegistry {

    public record CommandEntry(CommandHandler handler, String description) {}

    public interface CommandHandler {
        Response execute(CollectionManager manager, Command command);
    }

    private final Map<String, CommandEntry> registry = new LinkedHashMap<>();

    public CommandRegistry(){

        registry.put("help", new CommandEntry(
                (manager, cmd) -> new Response(true, getHelpMessage()),
                "Display help on available commands"));

        registry.put("info", new CommandEntry(
                (manager, cmd) -> new Response(true, manager.getInfo()),
                "Print collection information"));

        registry.put("show", new CommandEntry(
                (manager, cmd) -> {
                    var bands = manager.getSortedCollection();
                    return new Response(true, formatTable(bands));
                },
                "Print all elements of the collection"
        ));

        registry.put("clear", new CommandEntry(
                (manager, cmd) -> new Response(true, manager.clear()),
                "Clear collection"));

        registry.put("save", new CommandEntry((manager, cmd) -> {
            manager.saveCollection("bands.json");
            return new Response(true, "Collection to save: bands.json");
        },"Save collection to file (server-only)"));

        registry.put("insert", new CommandEntry(
                (manager, cmd) -> new Response(true, manager.insert(cmd.getKey(), cmd.getMusicBand())),
                "Add a new element with the given key"));

        registry.put("update", new CommandEntry(
                (manager,cmd) -> new Response(true, manager.update(cmd.getId(), cmd.getMusicBand())),
                "Update element by ID"));

        registry.put("remove_key", new CommandEntry(
                (manager, cmd) -> new Response(true, manager.removeKey(cmd.getKey())),
                "Remove element by key"));

        registry.put("replace_if_lower", new CommandEntry(
                (manager, cmd) -> new Response(true, manager.replaceIfLower(cmd.getKey(),cmd.getMusicBand())),
                "Replace if new value is less"));

        registry.put("remove_greater", new CommandEntry(
                (manager, cmd) -> {
                    var removed = manager.removeGreater(cmd.getMusicBand());
                    return new Response(true, "Removed " + removed.size() + " band(s) greater than specified", removed);
                }
                ,"Remove elements greater than specified"
        ));
        registry.put("remove_greater_key", new CommandEntry(
                (manager, cmd) -> {
                    var removed = manager.removeGreaterKey(cmd.getKey());
                    return new Response(true, "Removed " + removed.size() + " band(s) with key greater than '" + cmd.getKey() + "'", removed);
                },
                "Remove elements with key greater than given"));

        registry.put("count_greater_than_description", new CommandEntry(
                (manager, cmd) -> {
                    String desc = cmd.getArgument();
                    long count = manager.countGreaterThanDescription(desc);
                    return new Response(true,"Number of bands with description greater than '" + desc + "': " + count);
                },
                "Count elements with description greater"));

        registry.put("filter_less_than_genre", new CommandEntry(
                (manager, cmd) -> {
                    try {
                        MusicGenre genre = MusicGenre.valueOf(cmd.getArgument().toUpperCase());
                        var bands = manager.filterLessThanGenre(genre);
                        return  new Response(true, "Bands with genre less than " + genre, bands);
                    } catch (IllegalArgumentException e) {
                        return new Response(false, " Invalid genre: '" + cmd.getArgument() + "'\nValid genres: " +
                                java.util.Arrays.toString(MusicGenre.values()));
                    }
                },
                "Display elements with genre less than specified"
        ));

        registry.put("print_field_descending_number_of_participants", new CommandEntry(
                (manager, cmd) -> {
                    var bands = manager.getBandsByParticipantsDescending();
                    return new Response(true, "Number of Participants(Descending)", bands);
                },
                "Output participants in descending order"
        ));
    }

    private String formatTable(java.util.List<MusicBand> bands) {
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
}
