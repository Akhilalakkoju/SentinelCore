package backend.collector.model;

import backend.enums.LogSource;

import java.nio.file.Path;

public class LogFile {

    private Path path;
    private LogSource source;
    private long lastPosition;

    public LogFile(Path path, LogSource source) {
        this.path = path;
        this.source = source;
        this.lastPosition = 0;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public LogSource getSource() {
        return source;
    }

    public void setSource(LogSource source) {
        this.source = source;
    }

    public long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(long lastPosition) {
        this.lastPosition = lastPosition;
    }
}