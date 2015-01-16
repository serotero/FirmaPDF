package ar.com.qamwan.crypto.system;

import java.applet.Applet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import netscape.javascript.JSObject;
import bitwalker.useragentutils.UserAgent;

public class ApplicationHandler {

	private Log log = LogFactory.getLog(ApplicationHandler.class.getName());

	@SuppressWarnings("unused")
	private ApplicationHandler apphandler = null;

	private JSObject jsObject = null;
	
	private UserAgent userAgent = null;

	public ApplicationHandler(Applet applet) {
		synchronized (JSObject.class) {
			jsObject = JSObject.getWindow(applet);
			userAgent = getUserAgent();
		}
	}

	/**
	 * @return an JSObject to interact.
	 */
	public JSObject getJSObject() {
		return jsObject;
	}

	/**
	 * @return an JSObject to interact.
	 */
	public void cleanHandler() {
		jsObject = null;
		userAgent = null;
		apphandler = null;
		
		synchronized (JSObject.class) {
			    if (jsObject != null) {
			      jsObject = null;
			    }
			    userAgent = null;
				apphandler = null;
			
			    System.gc();
			  }		
	}

	public UserAgent getUserAgent() {
		if (userAgent == null) {

			try {
				log.info("Get JavaScript member: navigator");
				JSObject document = (JSObject) getJSObject().getMember(
						"navigator");
				log.info("Get JavaScript member: userAgent");
				String userAgentString = (String) document.getMember("userAgent");
				userAgent = UserAgent.parseUserAgentString(userAgentString);
				log.info("userAgentString : " + userAgentString + ", userAgent : " + userAgent);
			} catch (Exception ex) {
				log.error("Error accesing web browser window", ex);
			}
		}
		return userAgent;
	}
}
