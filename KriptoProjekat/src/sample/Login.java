package sample;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Login
{

    public static String bytesToHex(byte[] bytes)
    {
        BigInteger number = new BigInteger(1, bytes);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while(hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    private static ArrayList<String> loadCerts()
    {
        ArrayList<String> accounts=new ArrayList<>();
        BufferedReader br=null;
        try {
            br = new BufferedReader(new FileReader(new File("certHashes.txt")));
            String st;
            while ((st = br.readLine()) != null)
            {
                accounts.add(new String(st));
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return accounts;
    }

    private static ArrayList<String> loadAccounts()
    {
        ArrayList<String> accounts=new ArrayList<>();
        BufferedReader br=null;
        try {
            br = new BufferedReader(new FileReader(new File("hashedAccounts.txt")));
            String st;
            while ((st = br.readLine()) != null)
            {
                accounts.add(new String(st));
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return accounts;
    }

    private static String certHashes(StringBuilder username) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(username + "/" + username + ".cer");
        byte[] hash = digest.digest(fis.readAllBytes());
        fis.close();
        String finalHash=bytesToHex(hash);
        return finalHash;
    }


    private static String calculateHashes(StringBuilder username, String password) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String stringToHash=new String(username.reverse().append(password));
        byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        String finalHash=bytesToHex(hash);
        return finalHash;
    }

    public static boolean verifyLogin(String username, String password) throws Exception {
        HashMap<String, String> accountsMap = new HashMap<>();
        HashMap<String, String> certMap = new HashMap<>();
        ArrayList<String> accounts = new ArrayList<>(loadAccounts());
        ArrayList<String> certs = new ArrayList<>(loadCerts());
        for (String account : accounts) {
            String[] temp = account.split("-");
            accountsMap.put(temp[0], temp[1]);
        }
        for (String cert : certs) {
            String[] temp = cert.split("-");
            certMap.put(temp[0], temp[1]);
        }
        if (!accountsMap.containsKey(username) || !certMap.containsKey(username))
            return false;
        if(!accountsMap.get(username).equals(calculateHashes(new StringBuilder(username), password)) || !certMap.get(username).equals(certHashes(new StringBuilder(username))))
            return false;
        if(!Certificate.verifyCertificate(username,username))
            return false;
        return true;
    }
}
