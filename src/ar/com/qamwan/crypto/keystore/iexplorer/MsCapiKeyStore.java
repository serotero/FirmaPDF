package ar.com.qamwan.crypto.keystore.iexplorer;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ar.com.qamwan.crypto.keystore.IKeyStore;
import ar.com.qamwan.crypto.keystore.KeyStoreInicializationException;
import ar.com.qamwan.crypto.keystore.SupportedKeystore;
import ar.com.qamwan.crypto.ui.dialog.PasswordDialog;
import ar.com.qamwan.crypto.util.CertHelper;
import ar.com.qamwan.crypto.ui.dialog.PasswordDialog;

@SuppressWarnings("unused")
public class MsCapiKeyStore implements IKeyStore {

	char[] keyStorePassword = null;

	private Log log = LogFactory.getLog(MsCapiKeyStore.class.getName());

	private KeyStore mscapi;

	public MsCapiKeyStore() {
		try {
			log.debug("Inicializating MsCapiKeyStore...");
			mscapi = KeyStore.getInstance("Windows-MY",
				"SunMSCAPI");
			log.debug("MsCapiKeyStore initialized.");
		} catch (KeyStoreException e) {
			log.error("MsCapiKeyStore cannot be initialized.",e);
		} catch (NoSuchProviderException e) {
			log.error("MsCapiKeyStore cannot be initialized.",e);
		}
	}
	
	public String getAliasFromCertificate(Certificate cer)
			throws KeyStoreException {
		X509Certificate xcer = (X509Certificate) cer, auxCer = null;
		String auxAlias = null;
		Enumeration<String> e = mscapi.aliases();

		while (e.hasMoreElements()) {
			auxAlias = e.nextElement();
			auxCer = (X509Certificate) mscapi.getCertificate(auxAlias);
			log.info("Token Issuer : " + auxCer.getIssuerDN());
			log.info("Registered Issuer : " + xcer.getIssuerDN());
			if ((auxCer.getIssuerDN().equals(xcer.getIssuerDN()))
					&& (auxCer.getSerialNumber().equals(xcer.getSerialNumber()))) {
				return auxAlias;
			}
		}

		return null;
	}

	public void load(char[] pin) throws KeyStoreException,
			NoSuchAlgorithmException, IOException, CertificateException {
		try {
			mscapi.load(null, pin);
		} catch (Exception e) {
			throw new KeyStoreInicializationException(
					"Cannot initializace MsCapiKeyStore, wrong Password?", e);
		}
	}

	public Enumeration<String> aliases() throws KeyStoreException, Exception {
		return mscapi.aliases();
	}

	public Certificate getCertificate(String alias) throws KeyStoreException,
			Exception {
		return mscapi.getCertificate(alias);
	}

	public List<Certificate> getUserCertificates() throws KeyStoreException,
			Exception {
		List<Certificate> certs = new ArrayList<Certificate>();

		for (Enumeration<String> e = this.aliases(); e.hasMoreElements();) {
			Certificate cert = this.getCertificate(e.nextElement());
			certs.add(cert);
		}
		return certs;
	}

	public Key getKey(String alias, char[] pin) throws KeyStoreException,
			Exception {
		return mscapi.getKey(alias, pin);
	}

	public Provider getProvider() {
		return mscapi.getProvider();
	}

	public void setProvider(Provider provider) throws Exception {
		// Does nothing, seems non sense by this time.
		throw new Exception("Method not implemented");
	}

	public SupportedKeystore getName() {
		return SupportedKeystore.MSCAPI;
	}

	public String getTokenName() {
		return "Sun Windows Capi";
	}

	public void cleanUp() {
		mscapi = null;
		Runtime.getRuntime().gc();
	}

	public Certificate getCertificate(String serialNumber, String issuerDN)
			throws KeyStoreException, Exception {
		mscapi = KeyStore.getInstance("Windows-MY",
				"SunMSCAPI");
		mscapi.load(null, null);
		Certificate cert = null;
		log.info("looking in MSCapi Cert Store for Serial Number : " + serialNumber + " , issuerDN : " + issuerDN);
		Enumeration<String> aliasesEnum = mscapi.aliases();
		
		if (!aliasesEnum.hasMoreElements()) {
			log.info("No Certificates!!!");
			throw new Exception("No certificates!");
		}

		// list through windows personal store
		while (aliasesEnum.hasMoreElements()) {
			String alias = aliasesEnum.nextElement();
			log.info("Alias : " + alias);
			X509Certificate certTmp = (X509Certificate) mscapi.getCertificate(alias);
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
