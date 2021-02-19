package by.nhorushko.ftpclientlib;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.Instant;
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
        FileEntry f1 =new FileEntry("/data/foobar.txt", "abcdef 1234567890");
        f1.setLastModified(Date.from(Instant.parse("2019-12-31T22:00:00Z")));
        fileSystem.add(f1);
        fileSystem.add(new DirectoryEntry("/data/dir1"));
        fileSystem.add(new DirectoryEntry("/data/dir2"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.setCommandHandler("SIZE", (command, session) -> session.sendReply(250, "2000"));
        fakeFtpServer.start();

        ftpClient = new FtpClient("localhost", fakeFtpServer.getServerControlPort(), "user", "password");
        ftpClient.open();
    }

    @Test
    public void listDirectories(){
        Collection<String> actual = ftpClient.listDirectories("/data");
        assertEquals(2, actual.size());
        assertThat(actual).contains("dir1");
        assertThat(actual).contains("dir2");
    }

    @Test
    public void listDirectories_unexistsDir(){
        Collection<String> actual = ftpClient.listDirectories("/unexists");
        assertEquals(0, actual.size());
    }

    @Test
    public void givenRemoteFile_whenListingRemoteFiles_thenItIsContainedInList() throws IOException {
        Collection<FtpClientFile> actual = ftpClient.listFiles("");
        System.out.println(actual);
        assertEquals(1, actual.size());
        assertThat(actual).contains(new FtpClientFile("foobar.txt", 17l, true, false, Instant.parse("2019-12-31T21:00:00Z")));
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

        ftpClient.deleteFile("/buz.txt");
        assertThat(fakeFtpServer.getFileSystem().exists("/buz.txt")).isFalse();
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
