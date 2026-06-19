package org.example.server.database;

import org.example.common.enums.MusicGenre;
import org.example.common.model.Album;
import org.example.common.model.Coordinates;
import org.example.common.model.MusicBand;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class BandDAO {

    public static boolean insertBand(MusicBand band, String ownerLogin) {
        String sql = "INSERT INTO bands (name, coordinates_x, coordinates_y, number_of_participants, " +
                    "description, genre, album_name, album_length, owner) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, band.getName());
            statement.setDouble(2, band.getCoordinates().getX());
            statement.setLong(3, band.getCoordinates().getY());
            statement.setInt(4, band.getNumberOfParticipants());
            statement.setString(5, band.getDescription());
            statement.setString(6, band.getGenre() != null ? band.getGenre().name() : null);
            statement.setString(7, band.getBestAlbum() != null ? band.getBestAlbum().getAlbumName() : null);
            statement.setLong(8, band.getBestAlbum() != null ? band.getBestAlbum().getLength() : 0);
            statement.setString(9, ownerLogin);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    band.setId((int) id);
                    return true;
                }
            }
            return false;

        } catch (SQLException e) {
            System.err.println(" Insert band failed: " + e.getMessage());
            return false;
        }
    }


    public static List<MusicBand> getAllBands() {
        List<MusicBand> bands = new ArrayList<>();
        String sql = "SELECT * FROM bands";

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                MusicBand band = mapResultSetToBand(resultSet);
                bands.add(band);
            }
        } catch (SQLException e) {
            System.err.println(" Get all bands failed: " + e.getMessage());
        }

        return bands;
    }

    public static boolean updatedBand(MusicBand band, String ownerLogin) {
        String sql = "UPDATE bands SET name = ?, coordinates_x = ?, coordinates_y = ?, " +
                "number_of_participants = ?, description = ?, genre = ?, " +
                "album_name = ?, album_length = ? " +
                "WHERE id = ? AND owner = ?";

        try (Connection connection = DatabaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, band.getName());
            statement.setDouble(2, band.getCoordinates().getX());
            statement.setLong(3, band.getCoordinates().getY());
            statement.setInt(4, band.getNumberOfParticipants());
            statement.setString(5, band.getDescription());
            statement.setString(6, band.getGenre() != null ? band.getGenre().name() : null);
            statement.setString(7, band.getBestAlbum() != null ? band.getBestAlbum().getAlbumName() : null);
            statement.setLong(8, band.getBestAlbum() != null ? band.getBestAlbum().getLength() : 0);
            statement.setLong(9, band.getId());
            statement.setString(10, ownerLogin);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(" Update band failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteBand(long bandId, String ownerLogin) {
        String sql = " DELETE FROM bands WHERE id = ? AND owner =?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, bandId);
            statement.setString(2, ownerLogin);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(" Delete band failed: " + e.getMessage());
            return false;
        }
    }

    private static MusicBand mapResultSetToBand(ResultSet resultSet) throws SQLException {
        MusicBand band = new MusicBand();
        band.setId((int) resultSet.getLong("id"));
        band.setName(resultSet.getString("name"));

        Coordinates coordinates = new Coordinates(
                resultSet.getDouble("coordinates_x"),
                resultSet.getLong("coordinates_y")
        );
        band.setCoordinates(coordinates);

        band.setCreationDate(resultSet.getTimestamp("creation_date").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());

        band.setNumberOfParticipants(resultSet.getInt("Number_of_participants"));
        band.setDescription(resultSet.getString("description"));

        String genreStr = resultSet.getString("genre");
        if (genreStr != null && !genreStr.isEmpty()) {
            band.setGenre(MusicGenre.valueOf(genreStr));
        }

        String albumName = resultSet.getString("album_name");
        long albumLength = resultSet.getLong("album_length");

        if (albumName != null) {
            Album album = new Album(albumName, albumLength);
            band.setBestAlbum(album);
        }
        return band;
    }
}


