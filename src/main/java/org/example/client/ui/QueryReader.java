package org.example.client.ui;
import org.example.common.command.Command;
import org.example.common.enums.MusicGenre;
import org.example.common.model.Album;
import org.example.common.model.Coordinates;
import org.example.common.model.MusicBand;

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

    String key = null;

    public Command parseCommand(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String commandType = parts[0].toLowerCase();
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
                    return new Command(commandType);

                case "remove_key":
                    key = parts.length > 1 ? parts[1] : null;
                    return new Command("remove_key", key, null, null,0);
                case "remove_greater_key":
                    if (argument == null || argument.isEmpty()) {
                        System.out.println(" Error: Key required.");
                        return null;
                    }
                    Command commandRGK = new Command(commandType);
                    commandRGK.setKey(argument);
                    return commandRGK;

                case "update":
                    return parseUpdateCommand(argument);
                case "insert":
                     key = parts.length > 1 ? parts[1].trim() : null;
                    if (key == null) {
                        System.out.println(" Usage: insert <key>");
                        return null;
                    }
                    BandInsertInteractive validator = new BandInsertInteractive(scanner);
                    MusicBand band = validator.buildBandInteractively();
                    if (band == null) return null;
                    return new Command("insert", key, band, null,0);

                case "add_random": {
                    if (argument == null || argument.isEmpty()) {
                        System.out.println(" Usage: add_random <number>");
                        return null;
                    }
                    Command command = new Command("add_random");
                    command.setArgument(argument);
                    return command;
                }

                case "replace_if_lower":
                    return parseReplaceIfLowerCommand(argument);

                case "remove_greater":
                    return parseElementCommand(argument);
                case "count_greater_than_description":
                case "filter_less_than_genre":
                    if (argument == null || argument.isEmpty()) {
                        System.out.println(" Error: Argument required.");
                        return null;
                    }
                    Command commandArg = new Command(commandType);
                    commandArg.setArgument(argument);
                    return commandArg;
                default:
                    System.out.println(" Unknown command: '" + commandType + "'");
                    return null;
            }
        }catch (Exception e){
            System.out.println(" Error: parse command: " + e.getMessage());
            return null;
        }
    }


    private Command parseUpdateCommand(String argument) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: update <id> {element}");
            return null;
        }

        int braceIndex = argument.indexOf('{');
        if (braceIndex == -1){
            System.out.println(" Error: Invalid format.");
            return null;
        }

        String idStr = argument.substring(0, braceIndex).trim();
        Command command = new Command("update");
        try {
            command.setId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            System.out.println(" Error: ID must be a valid integer");
            return null;
        }
        MusicBand band = parseMusicBand(argument.substring(braceIndex));
        if (band == null) return null;
        command.setMusicBand(band);
        return command;
    }

    private Command parseReplaceIfLowerCommand(String argument) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: replace_if_lower <key> {element}");
            return null;
        }

        int braceIndex = argument.indexOf("{");
        if (braceIndex == -1) {
            System.out.println(" Error: Invalid format.");
            return null;
        }

        Command command = new Command("replace_if_lower");
        command.setKey(argument.substring(0, braceIndex).trim());
        MusicBand band = parseMusicBand(argument.substring(braceIndex));
        if (band == null) return null;
        command.setMusicBand(band);
        return command;
    }

    private Command parseElementCommand(String argument) {
        if (argument == null || argument.isEmpty()) {
            System.out.println(" Error: Usage: remove_greater {element}");
            return null;
        }

        MusicBand band = parseMusicBand(argument);
        if (band == null) return null;
        Command command = new Command("remove_greater");
        command.setMusicBand(band);
        return command;
    }

    private MusicBand parseMusicBand(String input) {
        String content = input.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);
        content = content.trim();

        if (content.isEmpty()) return null;


        String name = "Unknown";
        MusicGenre genre = MusicGenre.ROCK;
        int member = 1;
        int year = 0;


        for (String pair : content.split(",")) {
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
                    } catch (Exception e) {
                        return null;}
                    break;
                case "members":
                case "member":
                    try {
                        member = Integer.parseInt(value);
                    } catch (Exception e) {
                        return null;
                    } break;
                case "year":
                    try {
                        year = Integer.parseInt(value);
                    } catch (Exception e) {
                        return null;
                    }
                    break;
            }
        }

        MusicBand band = new MusicBand(
                null,
                name,
                new Coordinates(0.0,  0L),
                LocalDateTime.now(),
                member, 0,
                "Auto-generated",
                genre,
                new Album("Album", 1L)
        );
        band.setYear(year);
        return band;
    }

    public void close() {
        scanner.close();
    }
}
