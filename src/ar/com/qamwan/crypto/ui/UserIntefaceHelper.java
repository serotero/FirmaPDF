package ar.com.qamwan.crypto.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

public class UserIntefaceHelper {
	
	private UserIntefaceHelper(){}
	
	public static Frame findParentFrame(Container c) {
		while (c != null) {
			if (c instanceof Frame)
				return (Frame) c;

			c = c.getParent();
		}
		return null;
	}

	public static Frame getParentFrame(Component c) {
		Container cont = c.getParent();
		if (cont instanceof Frame) {
			return (Frame) cont;
		} else {
			return getParentFrame(cont);
		}
	}
	
}
