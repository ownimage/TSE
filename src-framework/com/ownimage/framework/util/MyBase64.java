/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class MyBase64 {

	// http://stackoverflow.com/questions/469695/decode-base64-data-in-java


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

	private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

	private static final int[] toInt = new int[128];

	static {
		for (int i = 0; i < ALPHABET.length; i++) {
			toInt[ALPHABET[i]] = i;
		}
	}

	/**
	 * Translates the specified byte array into Base64 string.
	 * 
	 * @param pBytes
	 *            the byte array (not null)
	 * @return the translated Base64 string (not null)
	 */
	public static String encode(byte[] pBytes) {
		int size = pBytes.length;
		char[] ar = new char[((size + 2) / 3) * 4];
		int a = 0;
		int i = 0;
		while (i < size) {
			byte b0 = pBytes[i++];
			byte b1 = (i < size) ? pBytes[i++] : 0;
			byte b2 = (i < size) ? pBytes[i++] : 0;

			int mask = 0x3F;
			ar[a++] = ALPHABET[(b0 >> 2) & mask];
			ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
			ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
			ar[a++] = ALPHABET[b2 & mask];
		}
		switch (size % 3) {
		case 1:
			ar[--a] = '=';
		case 2:
			ar[--a] = '=';
		}
		return new String(ar);
	}

	/**
	 * Translates the specified Base64 string into a byte array.
	 * 
	 * @param s
	 *            the Base64 string (not null)
	 * @return the byte array (not null)
	 */
	public static byte[] decode(String s) {
		int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
		byte[] buffer = new byte[s.length() * 3 / 4 - delta];
		int mask = 0xFF;
		int index = 0;
		for (int i = 0; i < s.length(); i += 4) {
			int c0 = toInt[s.charAt(i)];
			int c1 = toInt[s.charAt(i + 1)];
			buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
			if (index >= buffer.length) { return buffer; }
			int c2 = toInt[s.charAt(i + 2)];
			buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
			if (index >= buffer.length) { return buffer; }
			int c3 = toInt[s.charAt(i + 3)];
			buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
		}
		return buffer;
	}

	/**
	 * Compress and encode. Compresses the byte array using the java.util.zip classes, then runs the MyBase64 encoding on it.
	 * 
	 * @param pBytes
	 *            the bytes
	 * @return the string
	 */
	public static String compressAndEncode(byte[] pBytes) {
		Deflater compresser = new Deflater();
		compresser.setInput(pBytes);
		compresser.finish();

		ByteArrayOutputStream compressedOuput = new ByteArrayOutputStream();
		byte[] buffer = new byte[1000];
		int bufferLength;
		while ((bufferLength = compresser.deflate(buffer)) > 0) {
			compressedOuput.write(buffer, 0, bufferLength);
		}
		return encode(compressedOuput.toByteArray());
	}

	/**
	 * Decode and decompress. Decodes the string using the MyBase64 decoding, then runs the java.util.zip classes to decompress it.
	 * 
	 * @param s
	 *            the s
	 * @return the byte[]
	 * @throws DataFormatException
	 *             the data format exception
	 */
	public static byte[] decodeAndDecompress(String s) throws DataFormatException {
		byte[] compressedBytes = MyBase64.decode(s);

		Inflater decompresser = new Inflater();
		decompresser.setInput(compressedBytes);

		ByteArrayOutputStream baosLines = new ByteArrayOutputStream();
		byte[] buffer = new byte[1000];
		int bufferLength;
		while ((bufferLength = decompresser.inflate(buffer)) > 0) {
			baosLines.write(buffer, 0, bufferLength);
		}
		decompresser.end();

		return baosLines.toByteArray();
	}

}
