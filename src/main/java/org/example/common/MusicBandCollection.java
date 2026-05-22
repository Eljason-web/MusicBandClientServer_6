package org.example.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MusicBandCollection implements Serializable {
    public MusicBandCollection(LinkedHashMap<String, MusicBand> musicBandLinkedHashMap, LocalDateTime initializationDate) {
        this.musicBandLinkedHashMap = musicBandLinkedHashMap;
        this.initializationDate = initializationDate;
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, MusicBand> musicBandLinkedHashMap;
    private final LocalDateTime initializationDate;

    public MusicBandCollection() {
        this.musicBandLinkedHashMap = new LinkedHashMap<>();
        this.initializationDate = LocalDateTime.now();
    }

    public MusicBandCollection(LocalDateTime initializationDate, LinkedHashMap<String, MusicBand> musicBandLinkedHashMap) {
        this.musicBandLinkedHashMap = musicBandLinkedHashMap;
        this.initializationDate = LocalDateTime.now();
    }

    public LocalDateTime getInitializationDate(){
        return initializationDate;
    }

    public LinkedHashMap<String, MusicBand> getMusicBandLinkedHashMap() {
        return musicBandLinkedHashMap;
    }

    public int size(){
        return musicBandLinkedHashMap.size();
    }

    public String findKeyById(int id) {
        for (Map.Entry<String, MusicBand> entry : musicBandLinkedHashMap.entrySet()) {
            String key = entry.getKey();
            MusicBand band = entry.getValue();

            if (entry.getValue() != null && entry.getValue().getId().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public MusicBand getBandById(int id){
        String key = findKeyById(id);
        return (key != null) ? musicBandLinkedHashMap.get(key) : null;
    }
    public MusicBand getMusicBandByKey(String key){
        return musicBandLinkedHashMap.get(key);
    }

    public void addToCollection(String key, MusicBand musicBand){
        musicBandLinkedHashMap.put(key, musicBand);
        sortCollection();
    }

    public void removeMusicBand(String key){
        musicBandLinkedHashMap.remove(key);
    }

    public void clearCollection(){
        musicBandLinkedHashMap.clear();
    }

    public boolean updateByKey(String key, MusicBand updatedBand){
        if(!musicBandLinkedHashMap.containsKey(key)) {
            return false;
        }
        MusicBand existing = musicBandLinkedHashMap.get(key);
        if (existing != null) {
            updatedBand.setId(existing.getId());
        }
        musicBandLinkedHashMap.put(key, updatedBand);
        return true;
    }

    public void sortCollection(){
        List<Map.Entry<String, MusicBand>> sortedEntries = musicBandLinkedHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(MusicBand::getId)))
                .toList();

        clearCollection();

        for(Map.Entry<String, MusicBand> entry : sortedEntries) {
            musicBandLinkedHashMap.put(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public String toString() {
        return "MusicBandCollection{" +
                "musicBandLinkedHashMap=" + musicBandLinkedHashMap +
                '}';
    }
}

