package com.mythos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;

/**
 * Utility class for encrypting and decrypting files using AES-256-GCM.
 * Uses BouncyCastle standard implementation.
 */
	public class AESFile
	{
		private static final int KEY_SIZE_BITS = 256;
		private static final int IV_SIZE_BYTES = 12;  // GCM recommended IV size
		private static final int TAG_SIZE_BITS = 128; // GCM authentication tag size
		private static final int BUFFER_SIZE = 8192;  // File read/write buffer size
		private final SecureRandom random;

		public AESFile()
		{
			this.random = new SecureRandom();
		}

		/**
		 * Encrypts a file using AES-256-GCM.
		 *
		 * @param inputPath Path to the file to encrypt
		 * @param outputPath Path where the encrypted file should be written
		 * @param key The 256-bit encryption key
		 * @param associatedData Optional associated data for authentication (can be null)
		 * @throws IOException if there are file I/O errors
		 * @throws IllegalArgumentException if the key size is invalid
		 */
		public void encryptFile(Path inputPath, Path outputPath, byte[] key, byte[] associatedData)
			throws IOException
		{
			if (key.length != KEY_SIZE_BITS / 8)
				{
					throw new IllegalArgumentException("Invalid key size");
				}

			// Generate IV
			byte[] iv = new byte[IV_SIZE_BYTES];
			random.nextBytes(iv);

			File inputFile = inputPath.toFile();
			File outputFile = outputPath.toFile();

			try (FileInputStream in = new FileInputStream(inputFile);
				FileOutputStream out = new FileOutputStream(outputFile))
				{
					// Write IV to output file
					out.write(iv);

					// Initialize AES-GCM cipher
					GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
					AEADParameters parameters = new AEADParameters(
						new KeyParameter(key),
						TAG_SIZE_BITS,
						iv,
						associatedData);
					cipher.init(true, parameters);

					byte[] buffer = new byte[BUFFER_SIZE];
					byte[] outputBuffer = new byte[cipher.getUpdateOutputSize(BUFFER_SIZE)];
					int bytesRead;

					while ((bytesRead = in.read(buffer)) != -1)
						{
							int processed = cipher.processBytes(buffer, 0, bytesRead, outputBuffer, 0);
							if (processed > 0)
								{
									out.write(outputBuffer, 0, processed);
								}
						}

					// Process final block and write authentication tag
					byte[] finalBlock = new byte[cipher.getOutputSize(0)];
					try
                    {
                        int finalProcessed = cipher.doFinal(finalBlock, 0);
                        if (finalProcessed > 0)
                            {
                                out.write(finalBlock, 0, finalProcessed);
                            }
                    }
                    finally
                    {
                        return;
                    }
				}
		}

		/**
		 * Decrypts a file that was encrypted using AES-256-GCM.
		 *
		 * @param inputPath Path to the encrypted file
		 * @param outputPath Path where the decrypted file should be written
		 * @param key The 256-bit decryption key
		 * @param associatedData Optional associated data for authentication (can be null)
		 * @throws IOException if there are file I/O errors
		 * @throws IllegalArgumentException if the key size is invalid
		 */
		public void decryptFile(Path inputPath, Path outputPath, byte[] key, byte[] associatedData)
			throws IOException
		{
			if (key.length != KEY_SIZE_BITS / 8)
				{
					throw new IllegalArgumentException("Invalid key size");
				}

			File inputFile = inputPath.toFile();
			File outputFile = outputPath.toFile();

			try (FileInputStream in = new FileInputStream(inputFile);
				FileOutputStream out = new FileOutputStream(outputFile))
				{
					// Read IV from input file
					byte[] iv = new byte[IV_SIZE_BYTES];
					if (in.read(iv) != IV_SIZE_BYTES)
						{
							throw new IOException("Invalid encrypted file format");
						}

					// Initialize AES-GCM cipher
					GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
					AEADParameters parameters = new AEADParameters(
						new KeyParameter(key),
						TAG_SIZE_BITS,
						iv,
						associatedData);
					cipher.init(false, parameters);

					byte[] buffer = new byte[BUFFER_SIZE];
					byte[] outputBuffer = new byte[cipher.getUpdateOutputSize(BUFFER_SIZE)];
					long remaining = inputFile.length() - IV_SIZE_BYTES;
					
					while (remaining > 0)
						{
							int toRead = (int)Math.min(BUFFER_SIZE, remaining);
							int bytesRead = in.read(buffer, 0, toRead);
							if (bytesRead == -1)
								{
									throw new IOException("Unexpected end of file");
								}

							int processed = cipher.processBytes(buffer, 0, bytesRead, outputBuffer, 0);
							if (processed > 0)
								{
									out.write(outputBuffer, 0, processed);
								}
							remaining -= bytesRead;
						}

					// Process final block and verify authentication
					byte[] finalBlock = new byte[cipher.getOutputSize(0)];
					int finalProcessed = cipher.doFinal(finalBlock, 0);
					if (finalProcessed > 0)
						{
							out.write(finalBlock, 0, finalProcessed);
						}
				}
			catch (Exception e)
				{
					throw new IOException("Decryption failed: " + e.getMessage(), e);
				}
		}

		/**
		 * Generates a new AES-256 key.
		 *
		 * @return The generated key as a byte array
		 */
		public byte[] generateKey()
		{
			byte[] key = new byte[KEY_SIZE_BITS / 8];
			random.nextBytes(key);
			return key;
		}

		/**
		 * Utility method to securely compare two byte arrays in constant time.
		 *
		 * @param a First byte array
		 * @param b Second byte array
		 * @return true if arrays are equal, false otherwise
		 */
		private boolean constantTimeEquals(byte[] a, byte[] b)
		{
			return Arrays.constantTimeAreEqual(a, b);
		}
	}