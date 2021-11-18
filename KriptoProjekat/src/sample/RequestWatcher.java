package sample;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;

public class RequestWatcher extends Thread {
    private String user = null;

    public RequestWatcher(String username) {
        user = username;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path directory = Paths.get("C:\\Users\\a\\Desktop\\Java\\PotencijalniProjektni\\" + user);
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.context().toString().contains("request")) {
                        File file = new File(user + "/" + event.context().toString());
                        if(User.available && Certificate.verifyCertificate(user, event.context().toString().replace("request.bmp", "")))
                             User.displayRequest(event.context().toString().replace("request.bmp", ""), Stego.decode(file));
                        Thread.sleep(1000);
                        file.delete();

                    }
                    if (event.context().toString().contains("close")) {
                        File file = new File(user + "/" + event.context().toString());
                        file.delete();
                        File file2=new File(user + "/" + User.whomRequest + ".bin");
                        file2.delete();
                        User.available=true;
                        Platform.runLater(() ->
                        {
                            Thread t=Thread.currentThread();
                            t.setPriority(9);
                            User.chatWindow.close();
                        });

                    }
                    if (event.context().toString().contains("accept")) {
                        File file = new File("C:\\Users\\a\\Desktop\\Java\\PotencijalniProjektni" + "\\" + user + "\\" + event.context().toString());
                        file.delete();
                        User.displayChatWindow(event.context().toString().replace("accepted.txt", ""));
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (CRLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
