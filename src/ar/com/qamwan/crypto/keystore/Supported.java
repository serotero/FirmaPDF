package ar.com.qamwan.crypto.keystore;

import java.util.EnumSet;

import bitwalker.useragentutils.Browser;
import bitwalker.useragentutils.OperatingSystem;
import bitwalker.useragentutils.UserAgent;

public class Supported  {
	
	private Supported(){}
	public static EnumSet<Browser> IE_BROWSERS = EnumSet.of(Browser.IE6, Browser.IE7,Browser.IE8,Browser.IE9);
	public static EnumSet<Browser> FIREFOX_BROWSERS = EnumSet.of(null);
	public static EnumSet<Browser> BROWSERS;
	
	static{
		BROWSERS = EnumSet.copyOf(IE_BROWSERS);
		BROWSERS.addAll(EnumSet.copyOf(FIREFOX_BROWSERS));
	}
	
	public static EnumSet<OperatingSystem> OPERATING_SYSTEMS = EnumSet.of(OperatingSystem.WINDOWS_7,OperatingSystem.WINDOWS_VISTA,OperatingSystem.WINDOWS_XP);
	
	public static boolean isBrowserSupported(UserAgent userAgent){
		boolean isSupported = false;
		if(BROWSERS.contains(userAgent.getBrowser())){
			isSupported = true;
		}
		return isSupported;
	}
	
	public static boolean isOSSupported(UserAgent userAgent){
		boolean isSupported = false;
		if(OPERATING_SYSTEMS.contains(userAgent.getOperatingSystem())){
			isSupported = true;
		}
		return isSupported;
	}
	
	public static boolean isValidFirefox(Browser browser){
		boolean isSupported = false;
		if(FIREFOX_BROWSERS.contains(browser)){
			isSupported = true;
		}
		return isSupported;
	}
	
	public static boolean isValidIExplorer(Browser browser){
		boolean isSupported = false;
		if(IE_BROWSERS.contains(browser)){
			isSupported = true;
		}
		return isSupported;
	}
		
}