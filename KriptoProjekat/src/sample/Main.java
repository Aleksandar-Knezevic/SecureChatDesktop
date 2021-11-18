package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


// login window controller
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loginWindow.fxml"));
        primaryStage.setTitle("Secure chat");
        primaryStage.setScene(new Scene(root, 540, 300));
        primaryStage.show();
    }



    public static void main(String[] args) throws Exception {
        Protection.DecryptAccounts();
        OnlineWatcher ow=new OnlineWatcher();
        ow.start();
        launch(args);
        finalCleanup();
        System.exit(0);
    }

    public static void finalCleanup()
    {
        String name=Controller.loggedName;
        BufferedReader br=null;
        List<String> activeUsers=new ArrayList();
        try {
            br=new BufferedReader(new FileReader(new File("activeUsers/activeUsers.txt")));
            String st;
            while ((st = br.readLine()) != null)
            {
                st=st.replace("\n", "").replace("\r", "");
                if(!(st.equals(name)))
                    activeUsers.add(st);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter bw=null;
        try {
            String finalString=new String();
            bw=new BufferedWriter(new FileWriter(new File("activeUsers/activeUsers.txt")));
            for(String st : activeUsers)
                finalString=finalString.concat(st).concat("\n");
            bw.write(finalString);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally
        {
            try {
                bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File folder = new File(name);
        File[] listOfFiles = folder.listFiles();
        for(int i=0;i<listOfFiles.length;i++)
        {
            if(listOfFiles[i].getName().contains("Private.der"))
                listOfFiles[i].delete();
            if(!listOfFiles[i].getName().contains(name) && !listOfFiles[i].getName().contains("cacert"))
                listOfFiles[i].delete();
        }
        File accs=new File("hashedAccounts.txt");
        if(accs.exists())
            accs.delete();
        System.exit(0);
    }
}
