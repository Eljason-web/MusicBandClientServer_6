package org.example.server;

import org.example.common.MusicBand;
import org.example.common.MusicBandCollection;
import org.example.common.MusicGenre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private MusicBandCollection collection;

    public CollectionManager(MusicBandCollection collection) {
        this.collection = collection;
    }

    public CollectionManager(){
        this.collection = new MusicBandCollection();
    }

    public void loadCollection(String filePath) {
        JsonFileHandler.LoadResult result = JsonFileHandler.loadCollection(filePath);
        this.collection = new MusicBandCollection(result.initDate(), result.collection());
        logger.info(" Collection initialized with {} elements", collection.size());
    }

    public void saveCollection(String filePath) {
        JsonFileHandler.save(filePath, collection.getInitializationDate(), collection.getMusicBandLinkedHashMap());
    }

    public List<MusicBand> filterLessThanGenre(MusicGenre inputGenre) {
        return collection.getMusicBandLinkedHashMap().values().stream()
                .filter(band -> band.getGenre().compareTo(inputGenre) > 0)
                .sorted(Comparator.comparing(MusicBand::getNumberOfParticipants))
                .collect(Collectors.toList());
    }

    public long countGreaterThanDescription(String description) {
        return collection.getMusicBandLinkedHashMap().values()
                .stream().filter(musicBand -> musicBand
                .getDescription().compareTo(description) > 0 )
                .count();
    }

    public List<MusicBand> getBandsByParticipantsDescending() {
        return collection.getMusicBandLinkedHashMap().values().stream()
                .sorted(Comparator.comparingInt(MusicBand::getNumberOfParticipants)
                        .reversed()).collect(Collectors.toList());
    }

    public List<String> removeGreater(MusicBand inputBand) {
        List<String> keyToRemove = collection.getMusicBandLinkedHashMap().entrySet().stream().filter(entry -> entry.getValue()
                .compareTo(inputBand) > 0).map(Map.Entry::getKey).collect(Collectors.toList());
        keyToRemove.forEach(key -> collection.getMusicBandLinkedHashMap().remove(key));
        return keyToRemove;
    }

    public List<String> removeGreaterKey(String inputKey) {
        List<String> keysToRemove = collection.getMusicBandLinkedHashMap()
                .keySet().stream().filter(key -> key.compareTo(inputKey) > 0)
                .collect(Collectors.toList());
        keysToRemove.forEach(key -> collection.getMusicBandLinkedHashMap().remove(key));
        return keysToRemove;
    }

    public List<MusicBand> getSortedCollection(){
        return collection.getMusicBandLinkedHashMap().values().stream()
                .sorted(Comparator.comparing(band ->
                        band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : 0))
                .collect(Collectors.toList());
    }

    public String insert(String key, MusicBand band){
        if (collection.getMusicBandLinkedHashMap().containsKey(key)) {
            return "❌ Error: Band with key '" + key + "' already exists";
        }

        if (band == null) return " Error: Element data cannot be null";

        collection.getMusicBandLinkedHashMap().put(key, band);
        return " Music band has band added with the key " + key;
    }

    public String update(int id, MusicBand updatedBand) {
        String key = findKeyById(id);
        if (key == null){
            return "❌ Error: Music band with id " + id + " does not exist";
        }

        MusicBand original = collection.getMusicBandLinkedHashMap().get(key);
        updatedBand.setId(original.getId());
        updatedBand.setCreationDate(original.getCreationDate());

        collection.getMusicBandLinkedHashMap().put(key, updatedBand);
        return " Update music band with id " + id + " successfully";
    }

    public String removeKey(String key) {
        if (!collection.getMusicBandLinkedHashMap().containsKey(key)){
            return " Error: No band found with key '" + key + "'";
        }
        collection.getMusicBandLinkedHashMap().remove(key);
        return "✅ Music band with key" + key +  "has been removed successfully";
    }

    public String replaceIfLower(String key, MusicBand newBand) {
        if (!collection.getMusicBandLinkedHashMap().containsKey(key)) {
            return "❌ Error: No band found with key '" + key + "'";
        }

        MusicBand oldBand = collection.getMusicBandLinkedHashMap().get(key);
        int comparison = newBand.compareTo(oldBand);

        if(comparison < 0){
            newBand.setId(oldBand.getId());
            newBand.setCreationDate(oldBand.getCreationDate());

            collection.getMusicBandLinkedHashMap().put(key,newBand);
            return "✅ Band '" + key + "' replaced successfully";
        } else {
            return " Band '" + key + "' NOT replaced";
        }
    }

    public String clear() {
        collection.getMusicBandLinkedHashMap().clear();
        return " Music band Collection has been cleared";
    }


    public String getInfo() {
        return "Initialization date: " + collection.getInitializationDate() + "\n" +
                "Number of elements: " + collection.getMusicBandLinkedHashMap().size();
    }


    public String findKeyById(int id) {
        return collection.getMusicBandLinkedHashMap().entrySet().stream()
                .filter(entry -> entry.getValue().getId() == id)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public MusicBandCollection getCollection() {
        return collection;
    }
}
