package by.nhorushko.ftpclientlib;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandHandler;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class FtpClientIT {
    private FakeFtpServer fakeFtpServer;

    private FtpClient ftpClient;

    @Before
    public void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.setCommandHandler("SIZE", (command, session) -> session.sendReply(250, "2000"));
        fakeFtpServer.start();

        ftpClient = new FtpClient("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
        ftpClient.open();
    }

    @Test
    public void givenRemoteFile_whenListingRemoteFiles_thenItIsContainedInList() throws IOException {
        Collection<String> files = ftpClient.listFiles("");
        assertThat(files).contains("foobar.txt");
    }

    @Test
    public void givenRemoteFile_whenDownloading_thenItIsOnTheLocalFilesystem() throws IOException {
        ftpClient.downloadFile("/buz.txt", "downloaded_buz.txt");
        assertThat(new File("downloaded_buz.txt")).exists();
        new File("downloaded_buz.txt").delete(); // cleanup
    }

    @Test
    public void givenLocalFile_whenUploadingIt_thenItExistsOnRemoteLocation()
            throws URISyntaxException, IOException {

        File file = new File(getClass().getClassLoader().getResource("baz.txt").toURI());
        ftpClient.putFileToPath(file, "/buz.txt");
        assertThat(fakeFtpServer.getFileSystem().exists("/buz.txt")).isTrue();
    }

    @Test
    public void getFileSize_() {
        long fileSize = ftpClient.getFileSize("/data/foobar.txt");
        assertEquals(2000, fileSize);
    }

    @Test
    public void getInputStream_() throws IOException {
        InputStream inputStream = ftpClient.getInputStream("/data/foobar.txt");
        String actual = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
        assertEquals("abcdef 1234567890", actual);
    }

    @After
    public void teardown() throws IOException {
        ftpClient.close();
        fakeFtpServer.stop();
    }
}