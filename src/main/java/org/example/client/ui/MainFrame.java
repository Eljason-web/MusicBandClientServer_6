package org.example.client.ui;

import org.example.client.network.ClientNetwork;
import org.example.common.command.Command;
import org.example.common.command.Response;
import org.example.common.model.MusicBand;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {

    private final String currentUser;
    private final String password;
    private final ClientNetwork network;
    private List<MusicBand> bands = new ArrayList<>();

    private JComboBox<String> sortCombo;
    private JComboBox<String> genreFilter;
    private JTextField ownerFilter;

    private JTable bandTable;


    private BandTableModel tableModel;
    private JLabel statusLabel;
    private  VisualizationPanel canvasPanel;

    private static final String[] COLUMN_NAMES = {
            "ID", "Name", "X", "Y", "Date", "Ppl", "Description", "Genre", "Album", "Length", "Owner"
    };

    public MainFrame(String currentUser, String password, ClientNetwork network) {
        this.currentUser = currentUser;
        this.password = password;
        this.network = network;
        initializeUI();
        loadDataFromServer();
    }

    private void applyFilters() {
        statusLabel.setText("Apply filters...");

        String selectedGenre = (String) genreFilter.getSelectedItem();
        String ownerText = ownerFilter.getText().trim().toLowerCase();
        String sortBy = (String) sortCombo.getSelectedItem();

        List<MusicBand> filtered = bands.stream()
                .filter(band -> {
                    assert selectedGenre != null;
                    if (selectedGenre.equals("All")) return true;
                    return band.getGenre() != null && band.getGenre().name().equals(selectedGenre);
                })
                .filter(band -> {
                    if (ownerText.isEmpty()) return true;
                    return band.getOwner() != null && band.getOwner().toLowerCase().contains(ownerText);
                })
                .sorted((bands1, bands2) -> {
                    switch (Objects.requireNonNull(sortBy)) {
                        case "Name":
                            return bands1.getName().compareTo(bands2.getName());
                        case "Date":
                            return bands1.getCreationDate().compareTo(bands2.getCreationDate());
                        case "Participants":
                            return Integer.compare(bands1.getNumberOfParticipants(), bands2.getNumberOfParticipants());
                        case "Album Length":
                            long length1 = bands1.getBestAlbum() != null ? bands1.getBestAlbum().getLength(): 0;
                            long length2 = bands2.getBestAlbum() != null ? bands2.getBestAlbum().getLength(): 0;
                            return Long.compare(length1, length2);
                        default:
                            return 0;
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        tableModel.setBands(filtered);
        canvasPanel.setBands(filtered);

        statusLabel.setText("Filtered: " + filtered.size() + " objects");
    }

    private void clearFilters() {
        genreFilter.setSelectedIndex(0);
        ownerFilter.setText("");
        sortCombo.setSelectedIndex(0);

        tableModel.setBands(bands);
        canvasPanel.setBands(bands);

        statusLabel.setText("Filters cleared | Objects: " + bands.size());
    }

    private void initializeUI() {
        setTitle("Music Band Manager - " + currentUser);
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("User: " + currentUser);
        JComboBox<String> languageCombo = new JComboBox<>(new String[]{"English (UK)", "Русский", "Deutsch", "Magyar"});
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());

        topBar.add(userLabel);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(new JLabel("Language: "));
        topBar.add(languageCombo);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(logoutButton);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter & Sort"));

        JLabel genreLabel = new JLabel("Genre:");
        genreFilter = new JComboBox<>(new String[]{"All", "ROCK", "JAZZ", "POP", "RAP",
                "PSYCHEDELIC_CLOUD_RAP", "SOUL", "POST_PUNK", "RAGGAE", "HIP_POP", "GOSPEL"});

        JLabel ownerLabel = new JLabel("Owner:");
        ownerFilter = new JTextField(10);

        JLabel sortLabel = new JLabel("Sort by:");
        sortCombo = new JComboBox<>(new String[]{"None", "Name", "Date", "Participants", "Album Length"});

        JButton applyBtn = new JButton("Apply");
        JButton clearBtn = new JButton("Clear");

        filterPanel.add(genreLabel);
        filterPanel.add(genreFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(ownerLabel);
        filterPanel.add(ownerFilter);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(sortLabel);
        filterPanel.add(sortCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(applyBtn);
        filterPanel.add(clearBtn);

        applyBtn.addActionListener(e -> applyFilters());
        clearBtn.addActionListener(e -> clearFilters());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        tableModel = new BandTableModel();

        bandTable = new JTable(tableModel);
        bandTable.setAutoCreateRowSorter(true);

        JScrollPane tableScrollPane = new JScrollPane(bandTable);

        bandTable.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int row = bandTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = bandTable.convertRowIndexToModel(row);
                        MusicBand band = tableModel.getBandAt(modelRow);
                        openEditDialog(band);
                    }
                }
            }
        });

        canvasPanel = new VisualizationPanel(currentUser, this, this::openEditDialog);
        JScrollPane canvasScrollPane = new JScrollPane(canvasPanel);

        splitPane.setLeftComponent(tableScrollPane);
        splitPane.setRightComponent(canvasScrollPane);
        splitPane.setDividerLocation(600);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Ready");

        JButton addRandomBtn = new JButton("Add Random");
        clearBtn = new JButton("Clear");
        JButton deleteBtn = new JButton("Delete Selected");


        addRandomBtn.addActionListener(e -> handleAddRandom());
        clearBtn.addActionListener(e -> handleClear());
        deleteBtn.addActionListener(e -> handleDeleteSelected());


        bottomBar.add(addRandomBtn);
        bottomBar.add(clearBtn);
        bottomBar.add(deleteBtn);
        bottomBar.add(Box.createHorizontalStrut(20));
        bottomBar.add(statusLabel);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.BEFORE_FIRST_LINE);
        add(splitPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void openEditDialog(MusicBand band) {

        if (!band.getOwner().equals(currentUser)) {
            JOptionPane.showMessageDialog(this, "You can only edit your own objects!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        EditBandDialog dialog = new EditBandDialog(this, band);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            statusLabel.setText("Updating object...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        Command command = new Command("update");
                        command.setLogin(currentUser);
                        command.setPassword(password);
                        command.setMusicBand(dialog.getUpdatedBand());
                        network.sendCommand(command);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    loadDataFromServer();
                }
            }.execute();
        }
    }

    private void loadDataFromServer() {
        statusLabel.setText("Loading data from server...");

        new SwingWorker<List<MusicBand>, Void>() {
            @Override
            protected List<MusicBand> doInBackground() {
                try {
                    Command command = new Command("show");
                    command.setLogin(currentUser);
                    command.setPassword(password);
                    Response response = network.sendCommand(command);


                    if (response.isSuccess() && response.getBands() != null) {
                        return response.getBands();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    bands = get();
                    tableModel.setBands(bands);
                    canvasPanel.setBands(bands);
                    statusLabel.setText("Connected | Objects: " + bands.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading data: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void handleAddRandom() {
        String input = JOptionPane.showInputDialog(this,
                "How many random bands would you like to add?",
                "Add Random Bands", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        statusLabel.setText("Adding random band...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    Command command = new Command("add_random");
                    command.setLogin(currentUser);
                    command.setPassword(password);

                    command.setArgument(input.trim());

                    Response response = network.sendCommand(command);

                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                response.getMessage(),
                                "Success",JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                response.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                loadDataFromServer();
            }
        }.execute();
    }

    private void handleClear() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure want to clear ALL your bands from the collection?",
                "Confirm Clear", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_NO_OPTION) {
            statusLabel.setText("Clearing collection...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        Command command = new Command("clear");
                        command.setLogin(currentUser);
                        command.setPassword(password);
                        network.sendCommand(command);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected  void done() {
                    loadDataFromServer();
                }
            }.execute();
        }
    }

    private void handleDeleteSelected() {

        int[] selectedRows = bandTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select a row in the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bandsToDeleteCount = 0;
        List<Integer> idsToDelete = new ArrayList<>();

        for (int selectedRow : selectedRows) {
            int modelRow = bandTable.convertRowIndexToModel(selectedRow);
            MusicBand band = tableModel.getBandAt(modelRow);

            if (band.getOwner() != null && band.getOwner().equals(currentUser)) {
                bandsToDeleteCount++;
                idsToDelete.add(band.getId());
            }
        }

        if (bandsToDeleteCount == 0) {
            JOptionPane.showMessageDialog(this, "You can only delete your own objects!"
                    , "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + bandsToDeleteCount + " selected band(s)?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            statusLabel.setText("Deleting objects...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        for(Integer id : idsToDelete) {
                            Command command = new Command("remove_by_id");
                            command.setLogin(currentUser);
                            command.setPassword(password);
                            command.setArgument(String.valueOf(id));
                            network.sendCommand(command);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    loadDataFromServer();
                }
            }.execute();
        }
    }

    private void handleLogout() {
        dispose();
        try {
            network.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    private static class BandTableModel extends AbstractTableModel {
        private List<MusicBand> data = new ArrayList<>();

        public MusicBand getBandAt (int rowIndex) {
            return data.get(rowIndex);
        }

        private void setBands(List<MusicBand> bands) {
            this.data = bands;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MusicBand band = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> band.getId();
                case 1 -> band.getName();
                case 2 -> band.getCoordinates() != null ? band.getCoordinates().getX() : 0;
                case 3 -> band.getCoordinates() != null ? band.getCoordinates().getY() : 0;
                case 4 -> band.getCreationDate() != null ?
                        band.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
                case 5 -> band.getNumberOfParticipants();
                case 6 -> band.getDescription();
                case 7 -> band.getGenre() != null ? band.getGenre().name() : "";
                case 8 -> band.getBestAlbum() != null ? band.getBestAlbum().getAlbumName() : "";
                case 9 -> band.getBestAlbum() != null ? band.getBestAlbum().getLength() : 0;
                case 10 -> band.getOwner();
                default -> null;
            };
        }
    }
}