package ar.com.qamwan.applet;

public class PdfSignerException extends Exception {

	/**
	 * Si ocurre un problema al firmar se dirparara esta excepcion
	 */
	private static final long serialVersionUID = 1L;
	
	public PdfSignerException(String messString, Throwable throwable) {
		super(messString, throwable);
	}
	
	public PdfSignerException(String messString) {
		super(messString);
	}

}
