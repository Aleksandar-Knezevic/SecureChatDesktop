package sample;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class Protection {

    public static PrivateKey getPrivateKey(String username) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(username + "/" + username + "Private.der"));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey getPublicKey(String username) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(username+ "/" + username + "Public.der"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static byte[] digitallySign(String message, String source) throws Exception {
        PrivateKey priv = getPrivateKey(source);
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(priv);
        sig.update(message.getBytes());
        byte[] realSig = sig.sign();
        return realSig;
    }

    public static boolean digitallyVerify(byte[] input, byte[] signature, String source) throws Exception {
        PublicKey pubKey = getPublicKey(source);
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pubKey);
        sig.update(input);
        return sig.verify(signature);

    }
    private static SecretKey getSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        return keyGen.generateKey();
    }

    public static byte[] symmetricEncrypt(String message, String source, String destination) throws Exception {
        SecretKey symmetricKey=getSymmetricKey();
        byte[] digitalSignature = digitallySign(message, source);
        byte[] messageBytes=message.getBytes();
        byte[] output = new byte[messageBytes.length + digitalSignature.length];
        System.arraycopy(messageBytes, 0, output, 0, messageBytes.length);
        System.arraycopy(digitalSignature, 0, output, messageBytes.length, digitalSignature.length);
        Cipher cipher= Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        byte[] cipherText=cipher.doFinal(output);
        byte[] encryptedKey=RSAEncrypt(symmetricKey, destination);
        byte[] finalOutput=new byte[cipherText.length + encryptedKey.length];
        System.arraycopy(cipherText, 0, finalOutput, 0, cipherText.length);
        System.arraycopy(encryptedKey, 0, finalOutput, cipherText.length, encryptedKey.length);
        return finalOutput;
    }

    public static byte[] symmetricDecrypt(byte[] input, String source, String destination) throws Exception {
        byte[] output=null;
        byte[] symmetricKeyBytes = new byte[256];
        System.arraycopy(input, input.length-256, symmetricKeyBytes, 0, 256);
        byte[] symmetricKey = RSADecrypt(symmetricKeyBytes, destination);
        //byte[] encodedKey = symmetricKey.getBytes(StandardCharsets.UTF_8);
        SecretKey originalKey = new SecretKeySpec(symmetricKey, "AES");
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] encryptedMessage=new byte[input.length-256];
        System.arraycopy(input, 0, encryptedMessage, 0, input.length-256);
        output=cipher.doFinal(encryptedMessage);
        byte[] originalMessage=new byte[output.length-256];
        byte[] digitalSignature=new byte[256];
        System.arraycopy(output, 0, originalMessage, 0, output.length-256);
        System.arraycopy(output, output.length-256, digitalSignature, 0, 256);
        if(digitallyVerify(originalMessage, digitalSignature, source))
            return originalMessage;
        else {
            //System.out.println("Invalid signature");
            return "Narusena sigurnost".getBytes();
        }
    }

    public static byte[] RSAEncrypt(SecretKey data, String destination) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(destination));
        return cipher.doFinal(data.getEncoded());
    }

    public static byte[] RSADecrypt(byte[] data, String destination) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(destination));
        return cipher.doFinal(data);
    }

    public static void DecryptAccounts() throws Exception {
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] rezultat = md.digest("sigurnost".getBytes());
        byte[] key = Arrays.copyOf(rezultat, 24);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("accounts.bin")));
        byte[] plaintext = bis.readAllBytes();
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
        byte[] cipherText = cipher.doFinal(plaintext);
        bis.close();
        String result=new String(cipherText);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("hashedAccounts.txt")));
        bw.write(result);
        bw.close();
    }

    public static void decryptPrivateKey(String username) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] rezultat = md.digest(username.getBytes());
        byte[] key = Arrays.copyOf(rezultat, 8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DES");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(username + "/" + username + "Private.bin")));
        byte[] plaintext = bis.readAllBytes();
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] cipherText = cipher.doFinal(plaintext);
        bis.close();
        FileOutputStream fos = new FileOutputStream(username + "/" + username + "Private.der");
        fos.write(cipherText);
        fos.close();
    }

}
