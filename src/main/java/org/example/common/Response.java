package org.example.common;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;
    private List<MusicBand> bands;

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, String message,Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Response(boolean success, String message, List<MusicBand> band) {
        this.success = success;
        this.message = message;
        this.bands = band;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<MusicBand> getBands() {
        return bands;
    }

    public void setBand(List<MusicBand> band) {
        this.bands = band;
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", band=" + bands +
                '}';
    }
}
