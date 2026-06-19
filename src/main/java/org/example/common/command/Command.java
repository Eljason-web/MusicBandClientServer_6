package org.example.common.command;

import org.example.common.model.MusicBand;

import java.io.Serial;
import java.io.Serializable;

public class Command implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String commandType;
    private String key;
    private MusicBand musicBand;
    private String argument;
    private Integer id;

    private String login;
    private String password;

    public Command(String commandType, String key, MusicBand musicBand, String argument, Integer id) {
        this.commandType = commandType;
        this.key = key;
        this.musicBand = musicBand;
        this.argument = argument;
        this.id = id;
    }

    public Command (String commandType){
        this(commandType, null, null, null, 0);
    }

    public Command(String commandType, MusicBand musicBand) {
        this(commandType, null, musicBand,null,0);
    }

    public String getCommandType() {
        return commandType;
    }

    @SuppressWarnings("unused")
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MusicBand getMusicBand() {
        return musicBand;
    }

    public void setMusicBand(MusicBand musicBand) {
        this.musicBand = musicBand;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Command{" +
                "commandType='" + commandType + '\'' +
                ", key='" + key + '\'' +
                ", musicBand=" + musicBand +
                ", argument='" + argument + '\'' +
                ", id=" + id +
                '}';
    }
}
