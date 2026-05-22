package org.example.common;

import java.io.Serial;
import java.io.Serializable;

public class Album implements Serializable {

    @Serial
    private static  final long serialVersionUID = 1L;
    private String name;
    private long length;

    public Album(String name, long length) {
        this.name = name;
        this.length = length;
    }

    public void SetName (String name) {
        this.name = name;
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
                "name='" + name + '\'' +
                ", length=" + length +
                '}';
    }
}
