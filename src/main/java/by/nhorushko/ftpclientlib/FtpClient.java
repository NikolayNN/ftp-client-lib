package by.nhorushko.ftpclientlib;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class FtpClient {

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private FTPClient ftp;

    public FtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void open() {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        try {
            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            ftp.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            ftp.login(user, password);
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
    }

    public Collection<String> listDirectories(String path) {
        FTPFile[] files;
        try {
            files = ftp.listDirectories(path);
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    public Collection<FtpClientFile> listFiles(String path) {
        FTPFile[] files;
        try {
            files = ftp.listFiles(path);
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
        return Arrays.stream(files)
                .map(FtpClientFile::new)
                .filter(f -> f.isFile())
                .collect(Collectors.toList());
    }

    public void downloadFile(String source, String destination) throws IOException {
        FileOutputStream out = new FileOutputStream(destination);
        ftp.retrieveFile(source, out);
    }

    public void deleteFile(String path) {
        try {
            if (!ftp.deleteFile(path)) {
                throw new RuntimeException(String.format("File: %s was not deleted"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void putFileToPath(File file, String path) throws IOException {
        ftp.storeFile(path, new FileInputStream(file));
    }

    /**
     * skip first {@param offset} bytes
     * @return
     */
    public InputStream getInputStream(String path, long offset) {
        ftp.setRestartOffset(offset);
        return getInputStream(path);
    }

    public InputStream getInputStream(String path) {
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            return ftp.retrieveFileStream(path);
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
    }

    public long getFileSize(String path) {
        try {
            ftp.sendCommand("SIZE", path);
            if (ftp.getReplyCode() / 100 == 2) {
                String reply = ftp.getReplyString();
                reply = reply.replaceAll("(\\r\\n|\\n|\\r)", "");
                return Long.parseLong(reply.split(" ")[1]);
            }
            return 0;
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
    }

    public void close() {
        try {
            ftp.logout();
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        } catch (IOException e) {
            throw new FtpClientException(e);
        }
    }
}
