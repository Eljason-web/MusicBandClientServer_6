package org.example.client.ui;

import org.example.common.model.MusicBand;

import javax.swing.*;
import java.awt.*;

public class InfoPopup extends JDialog {

    public InfoPopup(Frame owner, MusicBand band) {
        super(owner, "object Information: " + band.getName(), true);
        setSize(350, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0, 2, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        addField(contentPanel, "ID: ", band.getId() != null ? band.getId().toString() : "N/A");
        addField(contentPanel, "Name:", band.getName());
        addField(contentPanel, "Coordinates:", band.getCoordinates() != null ?
                "(" + band.getCoordinates().getX() + ", " + band.getCoordinates().toString() : "N/A");
        addField(contentPanel, "Date:", band.getCreationDate() != null ? band.getCreationDate().toString() : "N/A");
        addField(contentPanel, "Participants:", band.getNumberOfParticipants() != null ? band.getNumberOfParticipants().toString() : "N/A");
        addField(contentPanel, "Description:", band.getDescription());
        addField(contentPanel, "Genre:", band.getGenre() != null ? band.getGenre().name() : "N/A");
        addField(contentPanel, "Album:", band.getBestAlbum() != null ? band.getBestAlbum().getAlbumName() : "N/A");
        addField(contentPanel, "Length:", band.getBestAlbum() != null ? String.valueOf(band.getBestAlbum().getLength()) : "N/A");
        addField(contentPanel, "Owner:", band.getOwner());

        add(contentPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private void addField(JPanel panel, String label,String value) {
        panel.add(new JLabel(label));
        panel.add(new JLabel(value != null ? value : ""));
    }
}
