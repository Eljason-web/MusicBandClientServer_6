package org.example.client.ui;

import org.example.client.network.ClientNetwork;
import org.example.common.command.Command;
import org.example.common.command.Response;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginFrame extends JFrame {

    private JComboBox<String> languageCombo;
    private JLabel userLabel;
    private JTextField usernameField;
    private JLabel passLabel;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private final Map<String, Locale> languages = new HashMap<>();
    private ClientNetwork network;

    public LoginFrame() {
        initializeLanguages();
        initializeUI();
        updateTexts();
    }


    private void initializeLanguages () {
        languages.put("English (UK)", Locale.UK);
        languages.put("Русский", new Locale("ru"));
        languages.put("Deutsch", Locale.GERMAN);
        languages.put("Magyar", new Locale("hu"));
    }

    private void initializeUI() {
        setTitle("Music Band Manager");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        languageCombo = new JComboBox<>(languages.keySet().toArray(new String [0]));

        languageCombo.addActionListener(e -> {
            String selected = (String) languageCombo.getSelectedItem();
            LocalizationManager.getInstance().setLocale(languages.get(selected));
            updateTexts();
        });
        mainPanel.add(languageCombo, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        userLabel = new JLabel();
        mainPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        passLabel = new JLabel();
        mainPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton();
        registerButton = new JButton();

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> JOptionPane.
                showMessageDialog(this, "Registration logic coming next!"));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, gbc);

        gbc.gridy = 4;
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);
    }

    private void updateTexts(){
        LocalizationManager localizationManager = LocalizationManager.getInstance();

        setTitle(localizationManager.getString("app.title") + " - Login");
        userLabel.setText(localizationManager.getString("login.username"));
        passLabel.setText(localizationManager.getString("login.password"));

        loginButton.setText(localizationManager.getString("btn.login"));
        registerButton.setText(localizationManager.getString("btn.register"));
        statusLabel.setText(localizationManager.getString("status.ready"));
    }

    private void handleLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating...");

        new LoginWorker(user, pass).execute();

    }

    private  class LoginWorker extends SwingWorker<Response, Void> {
        private final String username;
        private final String password;

        private LoginWorker(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Response doInBackground() {
            try {
                network = new ClientNetwork("localhost", 12345);

                Command loginCommand = new Command("login");
                loginCommand.setLogin(username);
                loginCommand.setPassword(password);

                return network.sendCommand(loginCommand);

            } catch (Exception e) {
                return new Response(false, "Connection error: " + e.getMessage());
            }
        }

        protected void done() {
            try {
                Response response = get();

                if (response.isSuccess()) {
                    dispose();//close login window

                    MainFrame mainFrame = new MainFrame(username, password, network);
                    mainFrame.setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        response.getMessage(),
                            "Login Failed",
                        JOptionPane.ERROR_MESSAGE);

                    loginButton.setEnabled(true);
                    statusLabel.setText(LocalizationManager.getInstance().getString("status.ready"));
                }
            } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Error: " + e.getMessage()
                        ,"Error",
                        JOptionPane.ERROR_MESSAGE);

                    loginButton.setEnabled(true);
                    statusLabel.setText(LocalizationManager.getInstance().getString("status.ready"));
            }
        }
    }


}
