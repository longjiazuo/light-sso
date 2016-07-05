package org.light4j.sso.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class DESedeUtils {
	
	public static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;
	public static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;

	private static final String ALGORITHM = "DESede";
    private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private Cipher cipher = null;
	private int opmode = 0;
	
	public synchronized boolean init(int mode, String key) {
		if (opmode != 0) {
			return true;
		}
		
		if (mode != ENCRYPT_MODE && mode != DECRYPT_MODE) {
			return false;
		}
		
		if (key == null || key.isEmpty()) {
			return false;
		}
		
		try {
			cipher = Cipher.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (cipher == null) {
				return false;
			}
		}
		Key secKey = getSecKey(key);
		if (secKey == null) {
			return false;
		}
		try {
			cipher.init(mode, secKey, new SecureRandom());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		opmode = mode;
		return true;
	}
	
	private static Key getSecKey(String key) {
		SecretKey securekey = null;
		try {
			byte[] material = Arrays.copyOf(Base64.decodeBase64(key.getBytes(UTF8)), 24);
			DESedeKeySpec keySpec = new DESedeKeySpec(material);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			securekey = keyFactory.generateSecret(keySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return securekey;
	}
	
	public synchronized String encrypt(String data) {
		if (opmode != ENCRYPT_MODE) {
			return null;
		}
		if (data == null) {
			return null;
		}
		byte[] encData = null;
		try {
			encData = cipher.doFinal(data.getBytes(UTF8));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (encData == null) {
			return null;
		}
		return new String(Base64.encodeBase64(encData), UTF8);
	}
	
	public synchronized String decrypt(String data) {
		if (opmode != DECRYPT_MODE) {
			return null;
		}
		if (data == null) {
			return null;
		}
		byte[] decData = null;
		try {
			decData = cipher.doFinal(Base64.decodeBase64(data.getBytes(UTF8)));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (decData == null) {
			return null;
		}
		return new String(decData, UTF8);
	}

}
