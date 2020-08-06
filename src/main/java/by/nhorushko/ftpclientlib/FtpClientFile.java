package by.nhorushko.ftpclientlib;

import org.apache.commons.net.ftp.FTPFile;

import java.util.Objects;

public class FtpClientFile {
    private final String  name;
    private final long size;
    private final boolean isFile;

    public FtpClientFile(String name, long size, boolean isFile) {
        this.name = name;
        this.size = size;
        this.isFile = isFile;
    }

    public FtpClientFile(FTPFile ftpFile) {
        this.isFile = ftpFile.isFile();
        this.name = ftpFile.getName();
        this.size = ftpFile.getSize();
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
                "isFile=" + isFile +
                ", name='" + name + '\'' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FtpClientFile)) return false;
        FtpClientFile that = (FtpClientFile) o;
        return isFile == that.isFile &&
                size == that.size &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFile, name, size);
    }
}
