package sample;


import java.io.*;
import java.nio.file.*;

public class InboxWatcher extends Thread
{
    private String user;
    public InboxWatcher(String username)
    {
        user=username;
    }
    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path directory = Paths.get("C:\\Users\\a\\Desktop\\Java\\PotencijalniProjektni\\" + user);
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String res=event.context().toString();
                    if (!res.contains("request") && !res.contains("accept") && !res.contains("close") && !res.contains("Private.der")) {
                        FileInputStream fis;
                        try {
                            File testfile=new File(user + "/" + event.context());
                            if(testfile.getAbsoluteFile().exists()) {
                                fis = new FileInputStream(user + "/" + event.context());
                                //byte[] receivedMessage = new byte[fis.available()];
                                byte[] receivedMessage = fis.readAllBytes();
                                fis.close();
                                User.receivedMessage(receivedMessage, event.context().toString().replace(".bin", ""));
                            }
                        } catch (Exception ex) {
                            //ex.printStackTrace();
                        }
                    }
                    }
                        }
                    }
                catch (IOException e) {
                        //e.printStackTrace();
        }
    }
        }
