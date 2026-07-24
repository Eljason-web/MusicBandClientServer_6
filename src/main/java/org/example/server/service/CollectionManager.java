package org.example.server.service;

import org.example.common.model.MusicBand;
import org.example.common.model.MusicBandCollection;
import org.example.common.enums.MusicGenre;
import org.example.server.database.BandDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    private final MusicBandCollection collection;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager(MusicBandCollection collection) {
        this.collection = collection;
    }

    public CollectionManager(){
        this.collection = new MusicBandCollection();
    }

    public void loadFromDatabase() {
        lock.writeLock().lock();
        try {
            collection.getMusicBandLinkedHashMap().clear();
            List<MusicBand> bands = BandDAO.getAllBands();

            for (MusicBand band : bands) {
                String key = String.valueOf(band.getId());
                collection.getMusicBandLinkedHashMap().put(key, band);
            }

            logger.info(" Collection initialized with {} elements", collection.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void saveToDatabase() {
        lock.readLock().lock();
        try {

            logger.info(" Collection state is synchronized with database");
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<MusicBand> filterLessThanGenre(MusicGenre inputGenre) {
        lock.readLock().lock();
        try {
            return collection.getMusicBandLinkedHashMap().values().stream()
                    .filter(band -> band.getGenre().compareTo(inputGenre) > 0)
                    .sorted(Comparator.comparing(MusicBand::getNumberOfParticipants))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public long countGreaterThanDescription(String description) {
        lock.readLock().lock();
        try {
            return collection.getMusicBandLinkedHashMap().values()
                    .stream().filter(musicBand -> musicBand
                            .getDescription().compareTo(description) > 0)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<MusicBand> getBandsByParticipantsDescending() {
        lock.readLock().lock();
        try {
            return collection.getMusicBandLinkedHashMap().values().stream()
                    .sorted(Comparator.comparingInt(MusicBand::getNumberOfParticipants)
                            .reversed()).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> removeGreater(MusicBand inputBand) {
        lock.writeLock().lock();
        try {
            List<String> keyToRemove = collection.getMusicBandLinkedHashMap().entrySet().stream()
                    .filter(entry -> entry.getValue().compareTo(inputBand) > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            for (String key : keyToRemove) {
                MusicBand band = collection.getMusicBandLinkedHashMap().get(key);
                if (BandDAO.deleteBand(band.getId())) {
                    collection.getMusicBandLinkedHashMap().remove(key);
                }
            }
            return keyToRemove;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<MusicBand> getSortedCollection(){
        lock.readLock().lock();
        try {
            return collection.getMusicBandLinkedHashMap().values().stream()
                    .sorted(Comparator.comparing(band ->
                            band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : 0))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public String insert(String key, MusicBand band) {
        lock.writeLock().lock();
        try {
            if (collection.getMusicBandLinkedHashMap().containsKey(key)) {
                return "❌ Error: Band with key '" + key + "' already exists";
            }
            if (band == null) return " Error: Element data cannot be null";

            collection.getMusicBandLinkedHashMap().put(key, band);
            return "✅ Music band has been added with ID " + band.getId();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String update(int id, MusicBand updatedBand, String currentUser) {
        lock.writeLock().lock();
        try {

            String key = findKeyById(id);
            if (key == null) {
                return "❌ Error: Music band with id " + id + " does not exist";
            }

            MusicBand original = collection.getMusicBandLinkedHashMap().get(key);

            if (!original.getOwner().equals(currentUser)) {
                return " Access Denied: You can only modify your own bands. This band belongs to " + original.getOwner();
            }

            updatedBand.setId(original.getId());
            updatedBand.setCreationDate(original.getCreationDate());
            updatedBand.setOwner(original.getOwner());

            boolean dbSuccess = BandDAO.updatedBand(updatedBand, currentUser);

            if (dbSuccess) {
                collection.getMusicBandLinkedHashMap().put(key, updatedBand);
                return "✅ Update music band with id " + id + " successfully";
            } else {
                return "❌ Failed to update band (you may not own this band)";
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String removeKey(String key, String ownerLogin) {
        lock.writeLock().lock();
        try {

            if (!collection.getMusicBandLinkedHashMap().containsKey(key)) {
                return " Error: No band found with key '" + key + "'";
            }

            MusicBand band = collection.getMusicBandLinkedHashMap().get(key);

            boolean dbSuccess = BandDAO.deleteBand(band.getId(), ownerLogin);

            if (dbSuccess) {
                collection.getMusicBandLinkedHashMap().remove(key);
                return "✅ Music band with key " + key + " has been removed successfully ";
            } else {
                return " Failed to delete band (you may not own this band)";
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String replaceIfLower(String key, MusicBand newBand, String ownerLogin) {
        lock.writeLock().lock();
        try {

            if (!collection.getMusicBandLinkedHashMap().containsKey(key)) {
                return "❌ Error: No band found with key '" + key + "'";
            }

            MusicBand oldBand = collection.getMusicBandLinkedHashMap().get(key);
            int comparison = newBand.compareTo(oldBand);

            if (comparison < 0) {
                newBand.setId(oldBand.getId());
                newBand.setCreationDate(oldBand.getCreationDate());

                boolean dbSuccess = BandDAO.updatedBand(newBand, ownerLogin);

                if (dbSuccess) {
                    collection.getMusicBandLinkedHashMap().put(key, newBand);
                    return "✅ Band '" + key + "' replaced successfully";
                } else {
                    return " Failed to replace band (you may not own this band)";

                }
            } else {
                return " Band '" + key + "' NOT replaced (new band is not lower)";
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String clear(String ownerLogin) {
        lock.writeLock().lock();
        try {
            if (ownerLogin == null || ownerLogin.isEmpty()) {
                return " Invalid user.";
            }

            List<String> keyToRemove = new ArrayList<>();

            for (Map.Entry<String, MusicBand> entry : collection.getMusicBandLinkedHashMap().entrySet()) {
                if (ownerLogin.equals(entry.getValue().getOwner())) {
                    keyToRemove.add(entry.getKey());
                }
            }

            if (keyToRemove.isEmpty()) {
                return "❌ You have no bands in the collection to modify.";
            }

            for (String key : keyToRemove) {
                collection.getMusicBandLinkedHashMap().remove(key);
            }

            boolean dbSuccess = BandDAO.deleteBandsByOwner(ownerLogin);

            if (dbSuccess) {
                return "✅ Successfully cleared " + keyToRemove.size() + " bands from memory and database.";
            } else {
                return "⚠️ Cleared from memory, but failed to clear from database(check table name in BandDAO).";
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getInfo() {
        lock.readLock().lock();
        try {

            return "Initialization date: " + collection.getInitializationDate() + "\n" +
                    "Number of elements: " + collection.getMusicBandLinkedHashMap().size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public String removeById(int id, String ownerLogin) {
        lock.writeLock().lock();
        try {
            String key = findKeyById(id);

            if (key == null) {
                return " Error: Music band with id " + id + " does not exist";
            }

            MusicBand band = collection.getMusicBandLinkedHashMap().get(key);
            if (!band.getOwner().equals(ownerLogin)) {
                return " Access Denied: You can only delete your own bands.";
            }

            boolean dbSuccess = BandDAO.deleteBand(band.getId(), ownerLogin);

            if (dbSuccess) {
                collection.getMusicBandLinkedHashMap().remove(key);
                return " Music band with id " + id + " has been removed successfully";
            } else {
                return " Failed to delete band from database.";
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    public String findKeyById(int id) {
        return collection.getMusicBandLinkedHashMap().entrySet().stream()
                .filter(entry -> {
                    Integer bandID = entry.getValue().getId();
                    return bandID != null && bandID == id;
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

}
