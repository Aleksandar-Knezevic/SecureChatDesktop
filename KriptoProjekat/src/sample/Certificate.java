package sample;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.*;

public class Certificate
{
    public static X509Certificate getUserCert(String user) throws CertificateException, FileNotFoundException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new FileInputStream(user+"/" + user+".cer"));
    }

    public static X509Certificate getCACert(String username) throws CertificateException, FileNotFoundException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new FileInputStream(username + "/cacert.cer"));
    }

    public static boolean checkCRL(String whose) throws CertificateException, FileNotFoundException, CRLException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRLEntry revokedCertificate=null;
        X509CRL crl= (X509CRL) cf.generateCRL(new FileInputStream("crList.crl"));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(whose + "/" + whose + ".cer"));
        revokedCertificate = crl.getRevokedCertificate(cert.getSerialNumber());
        if(revokedCertificate!=null)
            return true;
        return false;
    }


    public static boolean verifyCertificate(String username, String whose) throws CertificateException, FileNotFoundException, CRLException {
        X509Certificate caCert = getCACert(username);
        X509Certificate userCert = getUserCert(whose);
        if(checkCRL(whose))
            return false;
        try
        {
            userCert.verify(caCert.getPublicKey());
            userCert.checkValidity();
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
    /*public static boolean selfVerify(String username) throws CertificateException, FileNotFoundException, CRLException {
        X509Certificate userCert = getUserCert(username);
        if(checkCRL(username))
            return false;
        try
        {
            userCert.checkValidity();
        }
        catch(Exception e)
        {
            return false;
        }
        return true;

    } */
}
