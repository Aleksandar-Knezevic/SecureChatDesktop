package sample;


import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;


public class User
{
    private static String username;
    private String password;
    public static Stage chatWindow;
    public static boolean available=true;
    public static String whomRequest=null;
    public static Label receivedMessage=null;
    public static Stage request=null;

    public static void createUser(String name, String pass) throws Exception {
        User user=new User(name, pass);
        Protection.decryptPrivateKey(name);
        RequestWatcher rW = new RequestWatcher(name);
        InboxWatcher iW = new InboxWatcher(name);
        iW.start();
        rW.start();
        user.addToActive();
    }
    public static void closeSomething(Stage something)
    {
        Platform.runLater(() -> something.close());
    }
    public User(String name, String pass)
    {
        username=name;
        password=pass;
    }
    public void addToActive() throws IOException {
        PrintWriter pw=null;
        try {

            pw = new PrintWriter(new FileWriter(new File("activeUsers/activeUsers.txt"), true));
            pw.append(username + '\n');

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            pw.close();
        }
    }


    public static void sendMessage(String destination, String message) throws Exception {
        byte[] encrypted=Protection.symmetricEncrypt(message, username, destination);
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(destination + "/" + username + ".bin");
            fos.write(encrypted);

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            fos.close();
        }
    }

    public static void requestCommunication(String whom,String file) throws CertificateException, FileNotFoundException, CRLException {
        whomRequest= whom;

        if(Certificate.verifyCertificate(username,whom)) {
            try {
                Stego.embed(file, whom, username, "request");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            Platform.runLater(() ->
            {
                Stage theWindow=new Stage();
                Label label = new Label(whom + "-sertifikat nije validan. Komunikacija nije omogucena.");
                label.setWrapText(true);
                Button button = new Button("OK");
                button.setOnAction(e ->
                {
                    theWindow.close();
                });
                VBox vBox=new VBox(10);
                vBox.getChildren().addAll(label,button);
                theWindow.setScene(new Scene(vBox, 300, 100));
                theWindow.setTitle("Error");
                theWindow.show();
            });
        }
    }

    public static void displayRequest(String fromWhom, String message) throws Exception {
        Label label=new Label(message);
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        HBox hBox=new HBox(10);
        hBox.getChildren().addAll(yesButton, noButton);
        VBox vBox=new VBox(10);
        vBox.getChildren().addAll(label, hBox);
        yesButton.setOnAction(e->
        {
            available=false;
            File file=new File(fromWhom + "/" + username + "accepted.txt");
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            closeSomething(request);
            displayChatWindow(fromWhom);

        });
        noButton.setOnAction(e->
        {
            closeSomething(request);
            available=true;
        });

        Platform.runLater(() ->
        {
            request=new Stage();
            request.setTitle("Request");
            request.setScene(new Scene(vBox, 300, 100));
            request.show();
        });
    }

    public static void displayChatWindow(String fromWhom)
    {
        available=false;
        VBox vBox = new VBox(10);
        receivedMessage=new Label();
        receivedMessage.setWrapText(true);
        TextField messageSpace = new TextField();
        Button sendButton = new Button("Send");
        Button finish=new Button("Finish");
        sendButton.setOnAction(e ->
        {
            try {
                sendMessage(fromWhom, messageSpace.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageSpace.clear();
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(messageSpace, sendButton, finish);
        vBox.getChildren().addAll(hBox, receivedMessage);
        finish.setOnAction(e->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("picture", "*.bmp", "*.png"));
            File file = fileChooser.showOpenDialog(chatWindow);
            if(file!=null) {
                try {
                    Stego.embed(file.getAbsolutePath(), fromWhom, username, "close");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            File file2=new File(username + "/" + fromWhom + ".bin");
            file2.delete();
            available=true;
            closeSomething(chatWindow);
        });

        Platform.runLater(() ->
        {
            Thread t=Thread.currentThread();
            t.setPriority(8);
            chatWindow = new Stage();
            chatWindow.initModality(Modality.APPLICATION_MODAL);
            chatWindow.setTitle("Chat with " + fromWhom);
            chatWindow.setScene(new Scene(vBox, 300, 100));
            chatWindow.show();
        });

    }

    public static void receivedMessage(byte[] message, String fromWhom) throws Exception {
        byte[] result=Protection.symmetricDecrypt(message, fromWhom, username);
        Platform.runLater(() ->
        {
            receivedMessage.setText(new String(result));

        });
    }

}
