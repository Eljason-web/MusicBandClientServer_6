package org.example.common.model;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public class Album implements Serializable {

    @Serial
    private static  final long serialVersionUID = 1L;
    private String albumName;
    private long length;

    public Album(String albumName, long length) {
        this.albumName = albumName;
        this.length = length;
    }

    public void setAlbumName(String name) {
        this.albumName = name;
    }
    public long getLength() {
        return length;
    }

    public void setLength (long length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + albumName + '\'' +
                ", length=" + length +
                '}';
    }
}
