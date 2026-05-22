package org.example.server;

import com.google.gson.*;
import org.example.common.MusicBand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class JsonFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileHandler.class);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src,Type typeOfSrc,JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>(){
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return LocalDateTime
                            .parse(json.getAsString(), DateTimeFormatter
                                    .ISO_DATE_TIME);
                }
            })
            .create();

    private static final String DATE_FIELD = "initializationDate";
    private static final String BANDS_FIELD = "bands";

    public static LoadResult loadCollection(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info(" No existing collection file found. Starting fresh.");
            return new LoadResult(LocalDateTime.now(), new LinkedHashMap<>());
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            LocalDateTime initData = root.has("initializationDate")
                ? GSON.fromJson(root.get("initializationDate"), LocalDateTime.class)
                : LocalDateTime.now();

            LinkedHashMap<String, MusicBand> collection = new LinkedHashMap<>();
            if (root.has("bands")) {
                JsonObject bandsJson = root.getAsJsonObject("bands");
                for (var entry : bandsJson.entrySet()) {
                    MusicBand band = GSON.fromJson(entry.getValue(), MusicBand.class);
                    collection.put(entry.getKey(), band);
                }
            }

            logger.info(" Loaded {} bands from: {}", collection.size(), filePath);
            return new LoadResult(initData, collection);

        } catch (Exception e) {
            logger.error(" Filed to load collection: {}", e.getMessage(), e);
            return new LoadResult(LocalDateTime.now(), new LinkedHashMap<>());
        }
    }

    public static void save(String filePath, LocalDateTime initDate, LinkedHashMap<String, MusicBand> collection) {
        JsonObject root = new JsonObject();
        root.add(DATE_FIELD, GSON.toJsonTree(initDate));

        JsonObject bandsJson = new JsonObject();
        for (var entry : collection.entrySet()) {
            bandsJson.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
        }
        root.add("bands", bandsJson);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(GSON.toJson(root));
            logger.info(" Collection saved to: {}", filePath);
        } catch (IOException e) {
            logger.error(" Filed to save collection: {}", e.getMessage(),e);
        }
    }

    public record LoadResult(LocalDateTime initDate, LinkedHashMap<String, MusicBand> collection) {

    }
}
