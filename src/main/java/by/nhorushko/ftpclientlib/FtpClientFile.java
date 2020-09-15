package by.nhorushko.ftpclientlib;

import org.apache.commons.net.ftp.FTPFile;

import java.time.Instant;
import java.util.Objects;

public class FtpClientFile {
    private final String  name;
    private final long size;
    private final boolean isFile;
    private final Instant lastModified;

    public FtpClientFile(String name, long size, boolean isFile, Instant lastModified) {
        this.name = name;
        this.size = size;
        this.isFile = isFile;
        this.lastModified = lastModified;
    }

    public FtpClientFile(FTPFile ftpFile) {
        this.isFile = ftpFile.isFile();
        this.name = ftpFile.getName();
        this.size = ftpFile.getSize();
        this.lastModified = ftpFile.getTimestamp().toInstant();
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

    @Override
    public String toString() {
        return "FtpClientFile{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", isFile=" + isFile +
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
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSize(), isFile(), lastModified);
    }
}
