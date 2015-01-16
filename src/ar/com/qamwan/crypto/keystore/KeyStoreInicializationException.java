package ar.com.qamwan.crypto.keystore;

import java.security.KeyStoreException;

public class KeyStoreInicializationException extends KeyStoreException{

	private static final long serialVersionUID = 1L;

	public KeyStoreInicializationException(String message)
    {
        super(message);
    }

    public KeyStoreInicializationException(Throwable exception)
    {
        super(exception);
    }

    public KeyStoreInicializationException(String message, Throwable exception)
    {
        super(message, exception);
    }
}
