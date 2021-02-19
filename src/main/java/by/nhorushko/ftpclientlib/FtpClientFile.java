package by.nhorushko.ftpclientlib;

import org.apache.commons.net.ftp.FTPFile;

import java.time.Instant;
import java.util.Objects;

public class FtpClientFile {
    private final String  name;
    private final long size;
    private final boolean isFile;
    private final boolean isDirectory;
    private final Instant lastModified;

    public FtpClientFile(String name, long size, boolean isFile, boolean isDirectory, Instant lastModified) {
        this.name = name;
        this.size = size;
        this.isFile = isFile;
        this.lastModified = lastModified;
        this.isDirectory = isDirectory;
    }

    public FtpClientFile(FTPFile ftpFile) {
        this.isFile = ftpFile.isFile();
        this.isDirectory = ftpFile.isDirectory();
        this.name = ftpFile.getName();
        this.size = ftpFile.getSize();
        this.lastModified = ftpFile.getTimestamp().toInstant();
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isFile() {
        return isFile;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "FtpClientFile{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", isFile=" + isFile +
                ", isDirectory=" + isDirectory +
                ", lastModified=" + lastModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FtpClientFile)) return false;
        FtpClientFile that = (FtpClientFile) o;
        return getSize() == that.getSize() &&
                isFile() == that.isFile() &&
                isDirectory() == that.isDirectory() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getLastModified(), that.getLastModified());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSize(), isFile(), isDirectory(), getLastModified());
    }
}
