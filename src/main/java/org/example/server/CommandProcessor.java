package org.example.server;

import org.example.common.Command;
import org.example.common.MusicBand;
import org.example.common.MusicGenre;
import org.example.common.Response;

import java.util.Arrays;
import java.util.List;

public class CommandProcessor {

    private final CollectionManager collectionManager;
    private final String dataFile;

    public CommandProcessor(CollectionManager collectionManager, String dataFile) {
        this.collectionManager = collectionManager;
        this.dataFile = dataFile;
    }

    public Response processCommand(Command command) {
        if (command == null) {
            return new Response(false, " Error: Null command received");
        }

        String commandType = command.getCommandType();

        try {
            return switch (commandType) {
                case "help" -> processHelp();
                case "info" -> processInfo();
                case "show" -> processShow();
                case "clear" -> processClear();
                case "save" -> processSave();
                case "remove_key" -> processRemoveKey(command.getKey());
                case "remover_greater_key" -> processRemoveGreaterKey(command.getKey());
                case "update" -> processUpdate(command.getId(), command.getMusicBand());
                case "insert" -> processInsert(command.getKey(), command.getMusicBand());
                case "replace_if_lower" -> processReplaceIfLower(command.getKey(), command.getMusicBand());
                case "remove_greater" -> processRemoveGreater(command.getMusicBand());
                case "count_greater_than_description" -> processCountGreaterThanDescription(command.getArgument());
                case "filter_less_than_genre" -> processFilterLessThanGenre(command.getArgument());
                case "print_field_descending_number_of_participants" -> processPrintDescendingParticipants();
                default -> new Response(false, " Unknown command: '" + commandType + "'");
            };
        } catch (Exception e) {
            return new Response(false, " Error processing command: " + e.getMessage());
        }
    }

    private Response processHelp() {
        String helpText = """
                help: display help on available commands
                info: print collection information
                show: print all elements of the collection
                insert <key> {element}: add a new element with the given key
                update <id> {element}: update element by ID
                remove_key <key>: remove element by key
                clear: clear collection
                save: save collection to file (server-only)
                remove_greater {element}: remove elements greater than specified
                replace_if_lower <key> {element}: replace if new value is less
                remove_greater_key <key>: remove elements with key greater than given
                count_greater_than_description <description>: count elements with description greater
                filter_less_than_genre <genre>: display elements with genre less than specified
                print_field_descending_number_of_participants: output participants in descending order
                """;
        return new Response(true, helpText);
    }

    private Response processInfo() {
        String info = collectionManager.getInfo();
        return new Response(true, info);
    }

    private Response processShow() {
        List<MusicBand> bands = collectionManager.getSortedCollection();
        return new Response(true, "Collection Contents", bands);
    }

    private Response processClear() {
        String result = collectionManager.clear();
        return new Response(true, result);
    }

    // Server only command
    private Response processSave() {
        collectionManager.saveCollection(dataFile);
        return new Response(true, "Collection to save: " + dataFile);
    }

    // Commands

    private Response processRemoveKey(String key) {
        if (key == null || key.isEmpty()) {
            return new Response(false, "❌ Error: Key cannot be empty");
        }
        String result = collectionManager.removeKey(key);
        boolean success = !result.startsWith("❌");
        return new Response(success, result);

    }
    private Response processRemoveGreaterKey(String key) {
        if (key == null || key.isEmpty()){
            return new Response(false, "❌ Error: Key cannot be empty");
        }
        List<String> removed = collectionManager.removeGreaterKey(key);
        return new Response(true, "✅ Removed " + removed.size()
                + " band(s) with key greater than '" + key + "'");
    }

    //ID + ELEMENT COMMANDS
    private Response processUpdate(int id, MusicBand updatedBand) {
        if (updatedBand == null) {
            return new Response(false, "❌ Error: Element data cannot null");
        }
        updatedBand.setId(id);

        String result = collectionManager.update(id, updatedBand);
        boolean success = !result.startsWith("❌");
        return new Response(success, result);
    }


    private Response processInsert(String key, MusicBand band){
        if (key == null || key.isEmpty()) {
            return new Response(false, "❌ Error: Key cannot be empty");
        }
        if (band == null) {
            return new Response(false, "❌ Error: Element data cannot be null");
        }

        if (collectionManager.getCollection().getMusicBandLinkedHashMap().containsKey(key)) {
            return new Response(false, " Error: Band with key '" + key + "' already exists");
        }

        int nextId = collectionManager.getCollection().getMusicBandLinkedHashMap().values()
                .stream().mapToInt(b -> b.getId() != null ? b.getId() : 0)
                .max()
                .orElse(0) + 1;
        band.setId(nextId);

        band.setCreationDate(java.time.LocalDateTime.now());

        String result = collectionManager.insert(key, band);
        //System.out.println("DEBUG: After insert, band>id=" + band.getId());
        boolean success = !result.startsWith("❌");
        return new Response(success, result);
    }

    private Response processReplaceIfLower(String key, MusicBand newBand) {
        if (key == null || key.isEmpty()) {
            return new Response(false, "❌ Error: Element data cannot be empty");
        }
        if (newBand == null) {
            return new Response(false, "❌ Error: Element data cannot be null");
        }

        if (!collectionManager.getCollection().getMusicBandLinkedHashMap().containsKey(key)) {
            return new Response(false, " Error: No band found with key '" + key + "'");
        }

        MusicBand oldBand = collectionManager.getCollection().getMusicBandLinkedHashMap().get(key);
        int comparison = newBand.compareTo(oldBand);

        if (comparison < 0) {

            newBand.setId(oldBand.getId());
            newBand.setCreationDate(oldBand.getCreationDate());

            collectionManager.getCollection().getMusicBandLinkedHashMap().put(key, newBand);
            return new Response(true, "Band '" + key + "' replaced successfully");

        } else {
            return new Response(false, "Band '" + key + "' NOT replaced (new band is not lower)");
        }
    }

   // ELEMENT-BASED COMMANDS

    private Response processRemoveGreater(MusicBand inputBand){
            if (inputBand == null) {
                return new Response(false, "❌ Error: Element data cannot be null");
            }
            List<String> removed = collectionManager.removeGreater(inputBand);
            return new Response(true, "Removed " + removed.size() + " band(s) greater than specified");
    }

        //  STRING-ARGUMENT COMMANDS (with Stream API)
    private Response processCountGreaterThanDescription(String description) {
        if(description == null || description.isEmpty()) {
            return new Response(false, "❌ Error: Description cannot be empty");
        }
        long count = collectionManager.countGreaterThanDescription(description);
        return new Response(true, "Number of bands with description greater than '"
                + description + "': " + count);
    }

    private Response processFilterLessThanGenre(String genreStr) {
        if (genreStr == null || genreStr.isEmpty()) {
            return new Response(false, "❌ Error: Genre cannot be empty");
        }
        try{
            MusicGenre inputGenre = MusicGenre.valueOf(genreStr.toUpperCase());
            //  Stream API: Filter bands by genre comparison
            List<MusicBand> filtered = collectionManager.filterLessThanGenre(inputGenre);
            return new Response(true, "Bands with genre less than " + inputGenre, filtered);
        } catch (IllegalArgumentException e) {
            return new Response(false, "❌ Invalid genre: '" + genreStr + "'\nValid genres: " +
                Arrays.toString(MusicGenre.values()));
        }
    }

    private Response processPrintDescendingParticipants() {
        List<MusicBand> sorted = collectionManager.getBandsByParticipantsDescending();
        return new Response(true, "Number of participants (Descending)", sorted);
    }
}
