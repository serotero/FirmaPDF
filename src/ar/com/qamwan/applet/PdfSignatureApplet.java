package ar.com.qamwan.applet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Calendar;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FileUtils;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

import ar.com.qamwan.crypto.util.Base64;
import ar.com.qamwan.crypto.sign.*;
import ar.com.qamwan.crypto.system.ApplicationHandler;

public class PdfSignatureApplet extends java.applet.Applet {

	private static final long serialVersionUID = -3478949216625397106L;
	private KeyStore keystore;
	private char[] keyStorePassword;
	private String jsResponse;
	private String jsError;
	public static String issuerDN;
	public static String certSerialNumber;
	public static String name;
	public static String location;
	public static String reason;
	public static String fileName;
	public static String tempPath;
	public static String encodedPdf;
	

	ApplicationHandler appHandler;
	
	private Log log = LogFactory.getLog(PdfSignatureApplet.class.getName());
	
	public PdfSignatureApplet() {
	}

	@Override
	public void init() {
		
		log.info("Inicializando applet de firmado PDF...");
		this.appHandler = new ApplicationHandler(this);
		log.info(this.appHandler.getUserAgent());		
		
		//Obtencion de parametros
		log.info("Validando parametros");		
		try {
			loadAndCheckParameters();
			log.info("Parametros validos");			
			log.info("PDF a firmar  " + fileName);

			PdfSignature signing;
			try {
				File signedPDF;
				signing = new PdfSignature(keystore,
						keyStorePassword, issuerDN, certSerialNumber);
				signedPDF=signPDF(fileName,signing);
				// Devuelvo un encodedB64 a guardar y un file localmente
				byte[] pdfBytes=FileUtils.readFileToByteArray(signedPDF);				
				jsOK(b64Encode(pdfBytes));

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error("Problemas al firmar.", e1);
				jsError("La firma no pudo ser efectuada.\n (Mayor información en consola)");
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Problemas al firmar.", e);
			jsError("La firma no pudo ser efectuada.\n (Mayor información en consola)");
			
		}
	}
	
	@Override
	public void stop() {
		log.info("Stop applet... ");
		if (this.keystore != null) {
			log.info("Cleaning up Keystore... ");
			// this.keystore.cleanUp();
			this.keystore = null;
			log.info("KeyStore removed.");
		}
		
		this.appHandler.cleanHandler();
		this.appHandler = null;
		log.info("Stop applet Done.");

	}

	@Override
	public void destroy() {
		log.info("preparing for unloading...");
	}

	private void javascriptCallBack(String function, String[] parameters) {
		
		log.info("Calling javascript, \"function( " + parameters.toString() + " )\"");
		this.appHandler.getJSObject().call(function, parameters);
	}

	protected void jsError(String message) {
		log.info(" Entrando en salida errónea con valor " + message);		
		String[] param = { message };
		javascriptCallBack(jsError, param);
	}

	protected void jsOK(String outputFile) {
		log.info(" Entrando en salida OK con valor " + outputFile);
		String[] param = { outputFile };
		javascriptCallBack(jsResponse, param);
	}

	public File signPDF(String document, SignatureInterface signing) throws IOException, COSVisitorException,
			SignatureException {
		byte[] buffer = new byte[8 * 1024];
		if (!(document != null ))
			new RuntimeException("");

		File outputDocument = new File(tempPath);
		FileInputStream fis = new FileInputStream(document);
		FileOutputStream fos = new FileOutputStream(outputDocument);

		int c;
		while ((c = fis.read(buffer)) != -1) {
			fos.write(buffer, 0, c);
		}
		fis.close();
		fis = new FileInputStream(outputDocument);

		// load document
		PDDocument doc = PDDocument.load(document);

		// create signature dictionary
		PDSignature signature = new PDSignature();
		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
		signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
		signature.setName(name);
		signature.setLocation(location);
		signature.setReason(reason);

		// the signing date
		signature.setSignDate(Calendar.getInstance());

		// register signature 
		doc.addSignature(signature, signing);

		// write incremental 
		doc.saveIncremental(fis, fos);
				
		doc.close();		
		
		return outputDocument;
	}
	
	private void loadAndCheckParameters() throws Exception {

		StringBuilder message = new StringBuilder("");
		
		// Parameters
		certSerialNumber = getParameter("certSerialNumber");
		issuerDN = getParameter("issuerDN");
		name=getParameter("name");
		location=getParameter("location");
		reason=getParameter("reason");
		fileName = getParameter("fileName");
		tempPath = getParameter("tempPath");
		jsResponse = getParameter("jsResponse");
		jsError = getParameter("jsError");

		boolean hasErrors = false;
		if (certSerialNumber == null) {
			message.append(" |certSerialNumber is null| ");
			hasErrors = true;
		}
		if (issuerDN == null) {
			message.append(" |issuerDN is null| ");
			hasErrors = true;
		}
		if (name == null) {
			message.append(" |name is null| ");
			hasErrors = true;
		}
		if (location == null) {
			message.append(" |location is null| ");
			hasErrors = true;
		}
		if (reason == null) {
			message.append(" |reason is null| ");
			hasErrors = true;
		}
		
		if (fileName == null) {
			message.append(" |fileName is null| ");
			hasErrors = true;
		}
		
		if (tempPath == null ){
			message.append(" |tempPath is null| ");
			hasErrors = true;			
		}
			
		if (jsResponse == null) {
			message.append(" |jsResponse is null| ");
			hasErrors = true;
		}
		
		if (jsError == null) {
			message.append(" |jsError is null| ");
			hasErrors = true;
		}
		
		if (hasErrors) {
			throw new Exception(
					"Existen problemas con los parametros del Applet: "
							+ message);
		} else {
			
			log.info("Parametros : certSerialNumber=" + certSerialNumber + ", issuerDN=" +  issuerDN + ", name="+ name + ", location="+location +", reason="+ reason + ", fileName=" + fileName + ", Output file=" + tempPath );
		}		
	}
	
	private String b64Encode(byte[] pdfFile) throws IOException {
		String pdfB64 = null;
		try {
			String encodedData = Base64.encodeBytes(pdfFile);
			pdfB64 = new String(encodedData.getBytes(), "UTF-8");

		} catch (Exception e) {
			log.error("No se puede encodear en B64 el PDF Firmado", e);
		}
		return pdfB64;
	}

}