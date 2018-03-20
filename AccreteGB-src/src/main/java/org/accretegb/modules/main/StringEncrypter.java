package org.accretegb.modules.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class StringEncrypter {
	public final static String ALGORITHM = "DES";
	public final static String PADDING = "DES/ECB/PKCS5Padding";
	private Cipher desCipher;
	private SecretKey secretKey;
	
	public StringEncrypter(byte[] key) throws Exception {
		desCipher = Cipher.getInstance(PADDING);
		secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
		
	}
	public StringEncrypter() throws Exception {
		byte[] key = StringEncrypter.generateKey();
		desCipher = Cipher.getInstance(PADDING);
		secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
	}
	
	public static byte[] generateKey() throws Exception {
		KeyGenerator keygenerator = KeyGenerator.getInstance(ALGORITHM);
		SecretKey myDesKey = keygenerator.generateKey();
		return myDesKey.getEncoded();
	}

	public byte[] encrypt(String string) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] text = string.getBytes();
		byte[] textEncrypted = desCipher.doFinal(text);
		return textEncrypted;
	}
	
	public void encryptToFile(String filename, String[] strings, String regex) throws Exception {
		@SuppressWarnings("resource")
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for(int i = 0; i < strings.length - 1; i++){
			outputStream.write(encrypt(strings[i]));
			outputStream.write(encrypt(regex));
		}
		outputStream.write(encrypt(strings[strings.length - 1]));
		byte[] concatenatedBytes = outputStream.toByteArray( );
		FileUtils.writeByteArrayToFile(new File(filename),concatenatedBytes);
	}
	
	public String[] decryptFromFile(String filename, String regex) throws Exception{
		Path credentialPath = Paths.get(filename);
		byte[] encryptedBytes = Files.readAllBytes(credentialPath);
		String decryptedString = new String(decrypt(encryptedBytes));
		
		String[] rawStrings = decryptedString.split(regex);
		String[] decryptedStrings = new String[rawStrings.length];
		// remove unnecessary unicode characters
		for(int i = 0; i < rawStrings.length; i++){
			decryptedStrings[i] = rawStrings[i].trim();
		}
		return decryptedStrings;
	}
	public byte[] decrypt(byte[] encryptedBytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		desCipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] textDecrypted = desCipher.doFinal(encryptedBytes);
		return textDecrypted;
	}
	public void writeKeyToFile(File file){
		try {
			FileUtils.writeByteArrayToFile(file, secretKey.getEncoded());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void readAndSetKeyFromFile(File file){
		try {
			byte[] key = FileUtils.readFileToByteArray(file);
			if(key.length!=0)
			{
				secretKey = new SecretKeySpec(key, 0, key.length, ALGORITHM);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}