package ar.com.qamwan.crypto.keystore.mozilla;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ar.com.qamwan.crypto.keystore.IKeyStore;
import ar.com.qamwan.crypto.keystore.KeyStoreInicializationException;
import ar.com.qamwan.crypto.keystore.SupportedKeystore;
import ar.com.qamwan.crypto.system.Platform;
import ar.com.qamwan.crypto.util.CertHelper;
import ar.com.qamwan.crypto.util.RegQuery;

public class MozillaKeyStore implements IKeyStore
{
    private MozillaHelper mozillaHelper;
    private String pkcs11file;
    private String currentprofile;
    private String mozillaDirectory;
    
    private String _configName;
    private Provider _pk11provider;
    private KeyStore _mozillaKeyStore;
    private RegQuery rq = new RegQuery();
    boolean _initialized;
    
	private Log log = LogFactory.getLog(MozillaKeyStore.class.getName());
    
    public MozillaKeyStore() throws IOException
    {
    	
        mozillaHelper = new MozillaHelper();
        pkcs11file = mozillaHelper.getPkcs11FilePath();
        currentprofile = mozillaHelper.getCurrentProfiledir();
        mozillaDirectory = mozillaHelper.getAbsoluteApplicationPath();
        
        /*
         * We create that file on a temporary way because we need it to initialize SunPKCS11
         * provider.
         */

        if (Platform.isCurrentWindows())
        {
        	
            _configName = rq.getCurrentUserPersonalFolderPath() + File.separator + "p11.cfg";
                        
            System.load(mozillaDirectory + "/mozcrt19.dll");
            System.load(mozillaDirectory + "/sqlite3.dll");
            System.load(mozillaDirectory+ "/nspr4.dll");
            System.load(mozillaDirectory+ "/plc4.dll");
            System.load(mozillaDirectory + "/plds4.dll");
            System.load(mozillaDirectory + "/nssutil3.dll");
            
        }
        else
        {
            _configName = "./.p11.cfg.1166440118";
            
            System.load(mozillaDirectory + "/mozcrt19.so");
            System.load(mozillaDirectory + "/sqlite3.so");
            System.load(mozillaDirectory+ "/nspr4.so");
            System.load(mozillaDirectory+ "/plc4.so");
            System.load(mozillaDirectory + "/plds4.so");
            System.load(mozillaDirectory + "/nssutil3.so");
        }

        File f = new File(_configName);

        /*
         * while (f.exists()) { _configName = _configName + System.currentTimeMillis(); f = new
         * File(_configName); }
         */

        FileOutputStream fos = new FileOutputStream(f);

        /*
         * Replaces are for windows, NSS will read the path with / file separator instead of
         * Microsoft standar \ and spaces must be scaped too.
         */

        if (Platform.isCurrentWindows())
        {
        	
        	String config ="name = NSS\r" + "library = " + pkcs11file + "\r"
                    + "attributes= compatibility" + "\r" + "slot=2\r" + "nssArgs=\""
                    + "configdir='"
                    + currentprofile.replace("\\", "/").replace(" ", "\\ ") + "' "
                    + "certPrefix='' " + "keyPrefix='' " + "secmod='secmod.db' " + "flags=readOnly\"\r";
                    
        	
            fos.write(config.getBytes());
        }
        else if (Platform.isCurrentLinux())
        {
            /*
             * TODO:With Linux is pending to test what's up with the white spaces in the path.
             */

           fos.write(("name = NSS-" + new Random().nextInt() + "\n" + "library = " + pkcs11file
                    + "\n" + "attributes= compatibility" + "\n" + "slot=2\n" + "nssArgs=\""
                    + "configdir='" + currentprofile + "' " + "certPrefix='' " + "keyPrefix='' "
                    + "secmod='secmod.db' " + "flags=readOnly\"\n").getBytes());
        }
        fos.close();

        
        if(currentprofile.endsWith(File.separator)){
    		currentprofile = currentprofile.substring(0, currentprofile.length()-1);
    	}
    	
    	String mozillaConfig = mozillaDirectory.replace(File.separator, "/") + "/";
    	String currentProfileConfig = "\"" + currentprofile.replace(File.separator, "/");
    	
    	
    	/*asi funciona
    	mozillaConfig = "C:/Program Files/Mozilla Firefox/";
    	currentProfileConfig = "\"C:/Documents and Settings/paguilera/Application Data/Mozilla/Firefox/Profiles/zw9mmzf9.default\"";*/
    	
    	
    	StringBuffer configuracion = new StringBuffer("name = NSS");
		configuracion.append("\n");
		
		configuracion.append("nssLibraryDirectory = " + mozillaConfig );
		configuracion.append("\n");
		
		configuracion.append("nssSecmodDirectory=" + currentProfileConfig );
		configuracion.append("\n");
		
		configuracion.append("nssDbMode = readOnly");
		configuracion.append("\n");
		
		configuracion.append("nssModule = keystore");
		configuracion.append("\n");
		
		configuracion.append("attributes = compatibility");
		 
		String confFile = configuracion.toString();
		//confFile.replace(File.separator, "/");
		
		System.out.println("----Configuration----");
		System.out.println(confFile);
		System.out.println("----End Configuration----");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(confFile.getBytes());
        
        _pk11provider = new sun.security.pkcs11.SunPKCS11(bais);
        Security.addProvider(_pk11provider); 
    }

    public void load(char[] pin) throws KeyStoreException, NoSuchAlgorithmException, IOException,
            CertificateException
    {
    	try
        {
	        _mozillaKeyStore = KeyStore.getInstance("PKCS11", _pk11provider);
	        _mozillaKeyStore.load(null, pin);
	        _initialized = true;
        }
        catch (Exception e)
        {
        	 throw new KeyStoreInicializationException(
     				"Cannot initializace MsCapiKeyStore, wrong Password?", e);
        }
       
    }

    public Enumeration<String> aliases() throws KeyStoreException, Exception
    {
        if (!_initialized)
        {
            throw (new Exception("UninitializedKeyStore"));
        }

        Enumeration<String> e = _mozillaKeyStore.aliases();
        
        while (e.hasMoreElements())
        {
            System.out.println("Alias: " + e.nextElement());
        }
        return _mozillaKeyStore.aliases();
    }

    public String getAliasFromCertificate(Certificate cer) throws Exception
    {
        if (!_initialized)
        {
            throw (new Exception("UninitializedKeyStore"));
        }

        X509Certificate xcer = (X509Certificate) cer, auxCer = null;
        String auxAlias = null;

        Enumeration<String> e = _mozillaKeyStore.aliases();
        
        while (e.hasMoreElements())
        {
            auxAlias = e.nextElement();
            auxCer = (X509Certificate) _mozillaKeyStore.getCertificate(auxAlias);
            if ((auxCer.getIssuerDN().equals(xcer.getIssuerDN()))
                    && (auxCer.getSerialNumber().equals(xcer.getSerialNumber())))
            {
                return auxAlias;
            }
        }
        return null;
    }

    public Certificate getCertificate(String alias) throws KeyStoreException, Exception
    {
        if (!_initialized)
        {
            throw (new Exception("UninitializedKeyStore"));
        }

        return _mozillaKeyStore.getCertificate(alias);
    }

    public List<Certificate> getUserCertificates() throws KeyStoreException, Exception
    {
        return null;
    }

    public Key getKey(String alias, char[] pin) throws KeyStoreException, Exception {
        if (!_initialized)
        {
            throw (new Exception("UninitializedKeyStore"));
        }

        return _mozillaKeyStore.getKey(alias, pin);
    }

    /*public byte[] signMessage(byte[] toSign, String alias) throws NoSuchAlgorithmException,
            Exception
    {
        byte[] res = null;
        PrivateKey privKey = (PrivateKey) _mozillaKeyStore.getKey(alias, null);

        if (privKey == null)
        {
            return null;
        }

        Signature rsa = Signature.getInstance("SHA1withRSA", getProvider());
        rsa.initSign(privKey);
        rsa.update(toSign);
        res = rsa.sign();

        return res;
    }*/

    public SupportedKeystore getName()
    {
        return SupportedKeystore.MOZILLA;
    }

    public String getTokenName()
    {
        return "Firefox";
    }

    public Provider getProvider()
    {
        return _mozillaKeyStore.getProvider(); // _pk11provider;
    }
    
    public void setProvider(Provider provider) throws Exception
    {
        //Does nothing, seems non sense by this time.
    	throw new Exception("Method not implemented");
    }

    public void cleanUp()
    {
        File f = new File(_configName);
        f.delete();
        Security.removeProvider(_pk11provider.getName());
    }

	public Certificate getCertificate(String serialNumber, String issuerDN)
			throws KeyStoreException, Exception {
		Certificate cert = null;
		for (Enumeration<String> e = this.aliases(); e.hasMoreElements();) {
			
			X509Certificate certTmp = (X509Certificate) this.getCertificate(e.nextElement());
			String serialTmp = CertHelper.getSerialNumberAsString(certTmp.getSerialNumber());
			String issuerDnTmp = certTmp.getIssuerDN().getName();
			if (serialTmp.equals(serialNumber.toUpperCase())
					&& CertHelper.areSameDN(issuerDnTmp,issuerDN)) {
				cert = certTmp;
			}
		}
		return cert;
	}

   
}