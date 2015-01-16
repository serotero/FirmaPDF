package ar.com.qamwan.crypto.sign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ar.com.qamwan.applet.PdfSignatureApplet;
import ar.com.qamwan.crypto.util.CertHelper;


public class PdfSignature implements SignatureInterface {

	private static Provider provider = new BouncyCastleProvider();
	private static PrivateKey privKey;
    private Certificate cert;
	private static String alias;
	private static char[] keyStorePassword = null;

	private Log log = LogFactory.getLog(PdfSignature.class.getName());
	
	public PdfSignature(KeyStore keystore, char[] pin, String issuerDN, String certSerialNumber ) throws KeyStoreException,
	  		UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, IOException  
	{

			/**
			 * ApplicationHandler appHandler = new ApplicationHandler(this);
			 * log.info("User Agent : " + appHandler.getUserAgent());
			 * initKeyStore(appHandler.getUserAgent());
			 **/
		
		try {
			log.info("Inicializando el store de certificados...");
			keystore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");			
			keystore.load(null, null);

			provider = keystore.getProvider();
			Enumeration<String> aliasesEnum = keystore.aliases();

			log.info("Buscando el  certificado a utilizar en la firma...");
			// list through windows personal store
			while (aliasesEnum.hasMoreElements()) {
				alias = aliasesEnum.nextElement();
				X509Certificate msCert = (X509Certificate) keystore.getCertificate(alias);
				
				if ((msCert.getIssuerDN().toString().equals(issuerDN )) &&
						(CertHelper.getSerialNumberAsString(msCert.getSerialNumber()).equals(certSerialNumber)))				
				{
					log.info("Certificado a utilizar en la firma : " + certSerialNumber );					
					cert = keystore.getCertificate(alias);
					privKey = (PrivateKey) keystore.getKey(alias, keyStorePassword);					
					break;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public byte[] sign(InputStream content) throws SignatureException,
			IOException {
		CMSProcessableInputStream input = new CMSProcessableInputStream(content);
		CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
		List<Certificate> certList = Arrays.asList(cert);
		Security.addProvider(new BouncyCastleProvider());
		CertStore certStore = null;
		try {
			log.info("Iniciando firma pdf ..." );					
			certStore = CertStore.getInstance("Collection", new	CollectionCertStoreParameters(certList), "BC");
			gen.addSigner(privKey, (X509Certificate) certList.get(0),
					CMSSignedGenerator.DIGEST_SHA256);
			gen.addCertificatesAndCRLs(certStore);
			CMSSignedData signedData = gen.generate(input, false, provider);
			
			log.info("Finalizando firma pdf ..." );							
			return signedData.getEncoded();			
		} catch (Exception e) {
			// should be handled
			e.printStackTrace();
		}
		throw new RuntimeException("Problem while preparing signature");
	}

	class CMSProcessableInputStream implements CMSProcessable {

		InputStream in;

		public CMSProcessableInputStream(InputStream is) {
			in = is;
		}

		public Object getContent() {
			return null;
		}

		public void write(OutputStream out) throws IOException, CMSException {
			// read the content only one time
			byte[] buffer = new byte[8 * 1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
		}
	}
}
