package org.example.client.ui;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager  {

    private  static LocalizationManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private LocalizationManager() {
        setLocale(Locale.UK);
    }

    public static LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("messages", locale);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}
