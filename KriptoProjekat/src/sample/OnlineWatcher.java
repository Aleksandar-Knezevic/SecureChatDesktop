package sample;

import java.io.IOException;
import java.nio.file.*;

public class OnlineWatcher extends Thread
{
    private static int test=0;
    @Override
    public void run()
    {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path directory = Paths.get("C:\\Users\\a\\Desktop\\Java\\PotencijalniProjektni\\activeUsers");
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while(true)
            {
                for (WatchEvent<?> event :  watchKey.pollEvents()) {
                    if(test>0)
                        Controller.updateOnline();
                    test++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
