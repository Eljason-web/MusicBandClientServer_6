package org.example.common.command;

import org.example.common.model.MusicBand;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;
    private List<MusicBand> bands;

    private int chunkIndex = 1;
    private int totalChunks = 1;
    private List<Response> additionalChunks;

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

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public List<Response> getAdditionalChunks() {
        return additionalChunks;
    }

    public void setAdditionalChunks(List<Response> additionalChunks) {
        this.additionalChunks = additionalChunks;
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
