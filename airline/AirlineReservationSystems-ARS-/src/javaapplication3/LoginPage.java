package javaapplication3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Login and register form. After successful login opens FlightSelector.
 */
public class LoginPage extends JFrame implements ActionListener {
    private final MultiFlightServer oneServer;

    private final File usersDB;

    private JLabel userNameLabel, passLabel;
    private JTextField userNameInput;
    private JPasswordField passInput;
    private JButton loginButton, registerButton;

    private JFrame registerFrame;
    private JTextField userNameInputRegister;
    private JPasswordField passInputRegister;
    private JButton registerEnsureButton, exitRegisterBtn;

    public LoginPage() {
        oneServer = new MultiFlightServer();

        usersDB = new File(System.getProperty("user.home"), "java_app_users.txt");

        setTitle("LOGIN");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        userNameLabel = new JLabel("UserName");
        passLabel = new JLabel("Password");
        userNameInput = new JTextField();
        passInput = new JPasswordField();
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        userNameLabel.setBounds(50, 20, 200, 100);
        userNameInput.setBounds(150, 55, 150, 30);
        passLabel.setBounds(50, 90, 200, 100);
        passInput.setBounds(150, 125, 150, 30);
        loginButton.setBounds(90, 200, 100, 30);
        registerButton.setBounds(210, 200, 100, 30);

        add(userNameLabel);
        add(userNameInput);
        add(passLabel);
        add(passInput);
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        // ensure users file exists with a default user
        try {
            if (!usersDB.exists()) {
                usersDB.createNewFile();
                try (BufferedWriter bw = Files.newBufferedWriter(usersDB.toPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    bw.write("username: admin password: admin123");
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not create users DB file: " + ex.getMessage());
        }

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String userName = userNameInput.getText().trim();
            String password = new String(passInput.getPassword()).trim();
            try {
                ArrayList<String> userNameFromFile = getUsersNameFromFile();
                ArrayList<String> passwordFromFile = getPasswordFromFile();

                if (userNameFromFile.contains(userName) && passwordFromFile.contains(password)) {
                    // open flight selector
                    SwingUtilities.invokeLater(() -> new FlightSelector(oneServer));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid login", "Message", JOptionPane.ERROR_MESSAGE);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Users DB not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
            userNameInput.setText("");
            passInput.setText("");
        } else if (e.getSource() == registerButton) {
            registerForm();
        } else if (e.getSource() == registerEnsureButton) {
            String userNameRegister = userNameInputRegister.getText().trim();
            String passRegister = new String(passInputRegister.getPassword()).trim();

            if (userNameRegister.equals("") || passRegister.equals("")) {
                JOptionPane.showMessageDialog(registerFrame, "Invalid register", "Message", JOptionPane.ERROR_MESSAGE);
            } else {
                // append to file
                try (BufferedWriter bw = Files.newBufferedWriter(usersDB.toPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    bw.write("username: " + userNameRegister + " password: " + passRegister);
                    bw.newLine();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(registerFrame, "Failed to write user file", "Error", JOptionPane.ERROR_MESSAGE);
                }

                // open flight selector for the new user
                SwingUtilities.invokeLater(() -> new FlightSelector(oneServer));
                registerFrame.dispose();
            }
            userNameInputRegister.setText("");
            passInputRegister.setText("");
        } else if (e.getSource() == exitRegisterBtn) {
            if (registerFrame != null) registerFrame.dispose();
        }
    }

    private void registerForm() {
        registerFrame = new JFrame("REGISTER");
        registerFrame.setSize(400, 300);
        registerFrame.setLocationRelativeTo(this);
        registerFrame.setResizable(false);
        registerFrame.setLayout(null);

        JLabel userNameLabelRegister = new JLabel("Enter UserName");
        JLabel passLabelRegister = new JLabel("Enter Password");
        userNameInputRegister = new JTextField();
        passInputRegister = new JPasswordField();
        registerEnsureButton = new JButton("Confirm");
        exitRegisterBtn = new JButton("Exit");

        userNameLabelRegister.setBounds(50, 20, 200, 100);
        userNameInputRegister.setBounds(190, 55, 150, 30);
        passLabelRegister.setBounds(50, 90, 200, 100);
        passInputRegister.setBounds(190, 125, 150, 30);
        registerEnsureButton.setBounds(90, 200, 100, 30);
        exitRegisterBtn.setBounds(210, 200, 100, 30);

        registerFrame.add(userNameLabelRegister);
        registerFrame.add(userNameInputRegister);
        registerFrame.add(passLabelRegister);
        registerFrame.add(passInputRegister);
        registerFrame.add(registerEnsureButton);
        registerFrame.add(exitRegisterBtn);

        registerEnsureButton.addActionListener(this);
        exitRegisterBtn.addActionListener(this);

        registerFrame.setVisible(true);
    }

    public ArrayList<String> getUsersNameFromFile() throws FileNotFoundException {
        ArrayList<String> users = new ArrayList<>();
        try (Scanner values = new Scanner(usersDB)) {
            while (values.hasNext()) {
                String token = values.next();
                if (token.contains("username")) {
                    if (values.hasNext()) {
                        String detail = values.next();
                        users.add(detail);
                    }
                }
            }
        }
        return users;
    }

    public ArrayList<String> getPasswordFromFile() throws FileNotFoundException {
        ArrayList<String> passwords = new ArrayList<>();
        try (Scanner values = new Scanner(usersDB)) {
            while (values.hasNext()) {
                String token = values.next();
                if (token.contains("password")) {
                    if (values.hasNext()) {
                        String detail = values.next();
                        passwords.add(detail);
                    }
                }
            }
        }
        return passwords;
    }
}
