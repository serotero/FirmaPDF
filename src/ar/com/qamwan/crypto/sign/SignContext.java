package ar.com.qamwan.crypto.sign;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 *
 * @author Pablo Del Canto (pcanto@ac.upc.edu)
 */
public class SignContext {

	/** Creates a new instance of JceCertContext */
	private X509Certificate cert = null;

	private PrivateKey key = null;

	public SignContext(X509Certificate cert, PrivateKey key) {
		this.cert = cert;
		this.key = key;
	}

	public X509Certificate getCertificate() {
		return cert;
	}

	public PrivateKey getPrivateKey() {
		return key;
	}

	public String getSubjectDN() {
		if (cert != null) {
			return cert.getSubjectDN().getName();
		} else
			return null;
	}

	public String getIssuerDN() {
		if (cert != null) {
			return cert.getIssuerDN().getName();
		} else
			return null;
	}
}
