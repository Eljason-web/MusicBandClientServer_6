package org.example.common.model;

import org.example.common.enums.MusicGenre;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class MusicBand implements Comparable<MusicBand>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer id;
        private String name;
        private Coordinates coordinates;
        private java.time.LocalDateTime creationDate;
        private Integer numberOfParticipants;
        private Integer albumsCount;
        private String description;
        private MusicGenre genre;
        private Album bestAlbum;
        private int year;

        public MusicBand(Integer id, String name, Coordinates coordinates,
                         LocalDateTime creationDate, Integer numberOfParticipants,
                         Integer albumsCount, String description,
                         MusicGenre genre, Album bestAlbum) {

            this.id = id;
            this.name = name;
            this.coordinates = coordinates;
            this.creationDate = creationDate;
            this.numberOfParticipants = numberOfParticipants;
            this.albumsCount = albumsCount;
            this.description = description;
            this.genre = genre;
            this.bestAlbum = bestAlbum;
        }

        public MusicBand() {}

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
        }

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
        }

        public Integer getNumberOfParticipants() {
            return numberOfParticipants;
        }

        public void setNumberOfParticipants(Integer numberOfParticipants) {
            this.numberOfParticipants = numberOfParticipants;
        }

        public Integer getAlbumsCount() {
            return albumsCount;
        }

        public void setAlbumsCount(Integer albumsCount) {
            this.albumsCount = albumsCount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public MusicGenre getGenre() {
            return genre;
        }

        public void setGenre(MusicGenre genre) {
            this.genre = genre;
        }

        public Album getBestAlbum() {
            return bestAlbum;
        }

        public void setBestAlbum(Album bestAlbum) {
            this.bestAlbum = bestAlbum;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }


    @Override
    public int compareTo(MusicBand other) {
            if (null == other) {
                return 1;
            }

            int thisParticipants = this.numberOfParticipants != null ? this.numberOfParticipants : 0;
            int otherParticipants = other.numberOfParticipants != null ? other.numberOfParticipants : 0;

            return Integer.compare(thisParticipants, otherParticipants);
    }
}

