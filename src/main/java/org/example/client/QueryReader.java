package org.example.client;
import org.example.common.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class QueryReader {
    private final Scanner scanner;

    public QueryReader() {
        this.scanner = new Scanner(System.in);
    }

    public Command readCommand() {
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }
        return parseCommand(input);
    }

    public Command parseCommand(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String commandType = parts[0].toLowerCase();
        Command command = new Command(commandType);
        String argument = (parts.length > 1) ? parts[1].trim() : null;

        try{
            switch (commandType){
                case "help":
                case "info":
                case "show":
                case "clear":
                case "exit":
                case "save":
                case "print_field_descending_number_of_participants":
                    return command;

                case "remove_key":
                    String key = parts.length > 1 ? parts[1] : null;
                    return new Command("remove_key", key, null, null,0);
                case "remove_greater_key":
                    if (argument == null || argument.isEmpty()) {
                        System.out.println(" Error: Key required. Usage: " + commandType + " <key>");
                        return null;
                    }
                    command.setKey(argument);
                    return command;

                case "update":
                    return parseUpdateCommand(argument, command);
                case "insert":
                    return parseInsertCommand(argument, command);
                case "replace_if_lower":
                    return parseReplaceIfLowerCommand(argument, command);

                case "remove_greater":
                    return parseElementCommand(argument, command);
                case "count_greater_than_description":
                case "filter_less_than_genre":
                    if (argument == null || argument.isEmpty()) {
                        System.out.println(" Error: Argument required. Usage: " + commandType + " <value>");
                        return null;
                    }
                    command.setArgument(argument);
                    return command;
//                    System.out.println(" Error: 'save' command: '" + commandType + "'");
//                    System.out.println("Type 'help' to see available commands");
//                    return null;

                default:
                    System.out.println(" Unknown command: '" + commandType + "'");
                    System.out.println(" Type 'help' to see available commands");
                    return null;
            }
        }catch (Exception e){
            System.out.println(" Error: parse command: " + e.getMessage());
            return null;
        }
    }
    private Command parseUpdateCommand(String argument, Command command) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: update <id> {name:...,genre:...,member:...}");
            return null;
        }

        int braceIndex = argument.indexOf('{');
        if (braceIndex == -1){
            System.out.println(" Error: Invalid format. Expected: update <id> {element}");
            return null;
        }

        String idStr = argument.substring(0, braceIndex).trim();
        try {
            command.setId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            System.out.println(" Error: ID must be a valid integer");
            return null;
        }
        String elementStr = argument.substring(braceIndex);
        MusicBand band = parseMusicBand(elementStr);
        if (band == null) return null;

        command.setMusicBand(band);
        return command;
    }

    private Command parseInsertCommand(String argument, Command command) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: insert <key> {name:...,genre:...,members:...}");
            return null;
        }

        int braceIndex = argument.indexOf('{');
        if (braceIndex == -1){
            System.out.println(" Error: Invalid format. Expected: replace_if_lower <key> {element}");
            return null;
        }

        String key = argument.substring(0, braceIndex).trim();
        command.setKey(key);

        String elementStr = argument.substring(braceIndex);
        MusicBand band = parseMusicBand(elementStr);
        if (band == null) return null;

        command.setMusicBand(band);
        return command;
    }

    private Command parseReplaceIfLowerCommand(String argument, Command command) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: replace_if_lower <key> {name:...,genre:...,members:...}");
            return null;
        }

        int braceIndex = argument.indexOf("{");
        if (braceIndex == -1) {
            System.out.println(" Error: Invalid format. Expected: replace_if_lower <key> {element}");
            return null;
        }

        String key = argument.substring(0, braceIndex).trim();
        command.setKey(key);

        String elementStr = argument.substring(braceIndex);
        MusicBand band = parseMusicBand(elementStr);
        if (band == null) return null;

        command.setMusicBand(band);
        return command;
    }

    private Command parseElementCommand(String argument, Command command) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: remove_greater {name:...,genre:...,members:...}");
            return null;
        }

        MusicBand band = parseMusicBand(argument);
        if (band == null) return null;

        command.setMusicBand(band);
        return command;
    }

    private MusicBand parseMusicBand(String input) {

        String content = input.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);
        content = content.trim();

        if (content.isEmpty()) {
            System.out.println(" Error: Empty element data");
            return null;
        }

        String name = "Unknown";
        MusicGenre genre = MusicGenre.ROCK;
        int member = 1;
        int year = 0;

        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim().toLowerCase();
            String value = kv[1].trim();

            switch (key) {
                case "name":
                    name = value;
                    break;
                case "genre":
                    try {
                        genre = MusicGenre.valueOf(value.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println(" Invalid genre: '" + value + "'");
                        System.out.println(" Valid genres: " + java.util.Arrays.toString(MusicGenre.values()));
                        return null;
                    }
                    break;
                case "members":
                case "member":
                    try {
                        member = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        System.out.println(" Error: members must be a number");
                        return null;
                    } break;
                case "year":
                    try {
                        year = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        System.out.println(" Error: year must be a number");
                        return null;
                    }
                    break;
            }
        }

        return new MusicBand(
                null,
                name,
                new Coordinates(0.0, (float) 0.0f),
                LocalDateTime.now(),
                member,
                0,
                "Description",
                genre,
                new Album("Album", 1L)
        );
    }

    public void close() {
        scanner.close();
    }
}
