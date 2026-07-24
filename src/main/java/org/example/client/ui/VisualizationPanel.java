package org.example.client.ui;

import org.example.common.model.MusicBand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.function.Consumer;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class VisualizationPanel extends JPanel {

    private List<MusicBand> bands = new ArrayList<>();
    private final String currentUser;
    private final Frame parentFrame;
    private final Consumer<MusicBand> onEditRequest;
    private static final int SHAPE_SIZE = 50;

    private final List<int[]> drawPositions = new ArrayList<>();

    public VisualizationPanel(String currentUser, Frame parentFrame, Consumer<MusicBand> onEditRequest) {
        this.currentUser = currentUser;
        this.parentFrame = parentFrame;
        this.onEditRequest = onEditRequest;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 800));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    checkClickForEdit(e.getX(), e.getY());
                }else {
                    checkClick(e.getX(), e.getY());
                }
            }
        });
    }

    public void setBands(List<MusicBand> bands) {
        this.bands = bands;
        this.drawPositions.clear();
        repaint();
    }

    private Color getColorForBand(MusicBand band) {

        if (band.getGenre() != null) {
            return switch (band.getGenre()) {
                case ROCK -> Color.RED;
                case JAZZ -> Color.BLUE;
                case POP -> Color.PINK;
                case RAP -> Color.ORANGE;
                case PSYCHEDELIC_CLOUD_RAP -> Color.MAGENTA;
                case SOUL -> Color.CYAN;
                case POST_PUNK -> new Color(128, 0, 128);
                case RAGGAE -> new Color(34, 139, 34);
                case HIP_POP -> new Color(255, 215, 0);
                case GOSPEL -> Color.WHITE;
            };
        }
        return Color.GRAY;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawPositions.clear();

        for (MusicBand band : bands) {
            int x = calculateX(band.getCoordinates().getX());
            int y = calculateY((double) band.getCoordinates().getY());

            Color bandColor = getColorForBand(band);
            graphics2D.setColor(bandColor);

            graphics2D.fillOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawOval(x, y, SHAPE_SIZE, SHAPE_SIZE);

            drawPositions.add(new int[]{x, y});

        }
    }

    private int calculateX(double x) {
        return (int) (x * 5 + 400);
    }

    private int calculateY(double y) {
        return (int) (y * 5 + 400);
    }

    private void checkClick(int clickX, int clickY) {
        for (int i = 0; i < bands.size() && i < drawPositions.size(); i++) {
            int [] position = drawPositions.get(i);
            if (position[0] == -1) continue;


            double centerX = position[0] + (SHAPE_SIZE / 2.0);
            double centerY = position[1] + (SHAPE_SIZE / 2.0);
            double distance = Math.sqrt(Math.pow(clickX - centerX, 2) + Math.pow(clickY - centerY, 2));

            if (distance <= SHAPE_SIZE / 2.0) {

                InfoPopup popup = new InfoPopup(parentFrame, bands.get(i));
                popup.setVisible(true);
                break;
            }
        }
    }

    private void checkClickForEdit(int clickX, int clickY) {
        for (int i = 0; i < bands.size() && i < drawPositions.size(); i++) {
            int[] position = drawPositions.get(i);
            if (position[0] == - 1) continue;

            double centreX = position[0] + (SHAPE_SIZE / 2.0);
            double centreY = position[1] + (SHAPE_SIZE / 2.0);
            double distance = Math.sqrt(Math.pow(clickX - centreX, 2) + Math.pow(clickY - centreY, 2));

            if (distance <= SHAPE_SIZE / 2.0) {
                MusicBand clickedBand = bands.get(i);
                if (clickedBand.getOwner() != null && clickedBand.getOwner().equals(currentUser)) {
                    if (onEditRequest != null) {
                        onEditRequest.accept(clickedBand);
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "You can only edit your own objects!",
                            "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }
    }
}
