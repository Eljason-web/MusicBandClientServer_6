package org.example.client.ui;

import org.example.common.enums.MusicGenre;
import org.example.common.model.MusicBand;

import javax.swing.*;
import java.awt.*;

public class EditBandDialog extends JDialog {

    private final MusicBand originalBand;
    private MusicBand updatedBand;
    private boolean saved = false;

    private final JTextField nameField;
    private final JTextField xField;
    private final JTextField yField;
    private final JTextField participantsField;
    private final JTextField descriptionField;
    private final JTextField albumNameField;
    private final JTextField albumLengthField;
    private final JComboBox<MusicGenre> genreCombo;

    public EditBandDialog(Frame owner, MusicBand band) {
        super(owner, "Edit Band: " + band.getName(), true);
        this.originalBand = band;

        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(new JLabel("ID:"));
        mainPanel.add(new JLabel(String.valueOf(band.getId())));

        mainPanel.add(new JLabel("Name:"));
        nameField = new JTextField(band.getName());
        mainPanel.add(nameField);

        mainPanel.add(new JLabel("X:"));
        xField = new JTextField(String.valueOf(band.getCoordinates() != null ? band.getCoordinates().getX() : 0));
        mainPanel.add(xField);

        mainPanel.add(new JLabel("Y:"));
        yField = new JTextField(String.valueOf(band.getCoordinates() != null ? band.getCoordinates().getY() : 0));
        mainPanel.add(yField);

        mainPanel.add(new JLabel("Participants:"));
        participantsField = new JTextField(String.valueOf(band.getNumberOfParticipants()));
        mainPanel.add(participantsField);

        mainPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField(band.getDescription() != null ? band.getDescription() : "");
        mainPanel.add(descriptionField);

        mainPanel.add(new JLabel("Genre:"));
        genreCombo = new JComboBox<>(MusicGenre.values());
        if (band.getGenre() != null) genreCombo.setSelectedItem(band.getGenre());
        mainPanel.add(genreCombo);

        mainPanel.add(new JLabel("Album Name:"));
        albumNameField = new JTextField(band.getBestAlbum() != null ? band.getBestAlbum().getAlbumName() : "");
        mainPanel.add(albumNameField);

        mainPanel.add(new JLabel("Album Length:"));
        albumLengthField = new JTextField(band.getBestAlbum() != null ? String.valueOf(band.getBestAlbum().getLength()) : "0");
        mainPanel.add(albumLengthField);


        JPanel buttons = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> saveAndClose());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void saveAndClose() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty!");
                return;
            }

            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            int participants = Integer.parseInt(participantsField.getText().trim());
            long albumLength = Long.parseLong(albumLengthField.getText().trim());

            if (participants <= 0) {
                JOptionPane.showMessageDialog(this, "Participants must be > 0");
                return;
            }

            originalBand.setName(nameField.getText().trim());
            originalBand.setNumberOfParticipants(participants);
            originalBand.setDescription(descriptionField.getText().trim());
            originalBand.setGenre((MusicGenre) genreCombo.getSelectedItem());

            if (originalBand.getCoordinates() != null) {
                originalBand.getCoordinates().setX(x);
                originalBand.getCoordinates().setY((float) y);
            }

            if (originalBand.getBestAlbum() != null) {
                originalBand.getBestAlbum().setAlbumName(albumNameField.getText().trim());
                originalBand.getBestAlbum().setLength(albumLength);
            }

            updatedBand = originalBand;
            saved = true;
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format!");
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public MusicBand getUpdatedBand() {
        return updatedBand;
    }
}