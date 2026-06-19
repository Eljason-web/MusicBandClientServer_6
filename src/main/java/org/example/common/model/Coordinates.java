package org.example.common.model;

import java.io.Serial;
import java.io.Serializable;
@SuppressWarnings("unused")
public class Coordinates implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Double x;
    private float y;

    public Coordinates(Double x, float y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public long getY() {
        return (long) y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
