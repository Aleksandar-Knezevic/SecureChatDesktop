package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


public class Controller extends Parent  {
    @FXML public TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button OKButton;
    public static Stage activeWindow;
    public static String loggedName=null;



    public void loginButton() throws Exception {

        String username=usernameField.getText();
        String password=passwordField.getText();
        boolean rez=Login.verifyLogin(username, password);
        if(!rez)
        {
            Stage window=new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            Parent newroot = null;
            try {
                newroot = FXMLLoader.load(getClass().getResource("AlertBox.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            window.setTitle("Error");
            window.setScene(new Scene(newroot, 280, 100));
            window.showAndWait();
        }
        else
        {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
            User.createUser(username, password);
            loadCurrentlyActive(username);

        }
    }
    public void OKButtonClicked()
    {
        Stage stage = (Stage) OKButton.getScene().getWindow();
        stage.close();
    }


    public static void loadCurrentlyActive(String username)
    {
        loggedName= username;
        BufferedReader br=null;
        List<String> activeUsers=new ArrayList();
        try {
            br=new BufferedReader(new FileReader(new File("activeUsers/activeUsers.txt")));
            String st;
            while ((st = br.readLine()) != null)
            {
                st=st.replace("\n", "").replace("\r", "");
                if(!(st.equals(username)))
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
        VBox vBox = new VBox(10);
        Label label=new Label("Active users");
        vBox.getChildren().add(label);
        for(String active : activeUsers) {
            Label label1 = new Label();
            label1.setText(active);
            Button button = new Button("Request communication");
            button.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("picture", "*.bmp", "*.png"));
                    File file = fileChooser.showOpenDialog(activeWindow);
                    if(file!=null)
                        User.requestCommunication(active, file.getAbsolutePath());
                } catch (CertificateException ex) {
                    ex.printStackTrace();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (CRLException ex) {
                    ex.printStackTrace();
                }
            });
            HBox hBox = new HBox(10, label1, button);
            vBox.getChildren().add(hBox);
        }

        Platform.runLater(() ->
        {
            Thread t=Thread.currentThread();
            t.setPriority(8);
            activeWindow=new Stage();
            activeWindow.setTitle(loggedName);
            activeWindow.setScene(new Scene(vBox, 300, 400));
            activeWindow.show();
        });

    }
    public static void updateOnline()
    {
        BufferedReader br=null;
        List<String> activeUsers=new ArrayList();
        try {
            br=new BufferedReader(new FileReader(new File("activeUsers/activeUsers.txt")));
            String st;
            while ((st = br.readLine()) != null)
            {
                st=st.replace("\n", "").replace("\r", "");
                if(!(st.equals(loggedName)))
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
        VBox vBox = new VBox(10);
        Label label=new Label("Active users");
        vBox.getChildren().add(label);
        for(String active : activeUsers) {
            Label label1 = new Label();
            label1.setText(active);
            Button button = new Button("Request communication");
            button.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("picture", "*.bmp", "*.png"));
                    File file = fileChooser.showOpenDialog(activeWindow);
                    if (file != null)
                        User.requestCommunication(active, file.getAbsolutePath());
                } catch (CertificateException ex) {
                    ex.printStackTrace();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (CRLException ex) {
                    ex.printStackTrace();
                }
            });
            HBox hBox = new HBox(10, label1, button);
            vBox.getChildren().add(hBox);
        }

        Platform.runLater(() ->
        {
            activeWindow.setScene(new Scene(vBox, 300, 400));
        });

    }

}
