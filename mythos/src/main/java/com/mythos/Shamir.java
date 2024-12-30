package com.mythos;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.util.encoders.Hex;

/**
 * Implementation of Shamir's Secret Sharing scheme using BouncyCastle FIPS libraries.
 * This implementation uses a prime field GF(p) where p is a large prime number.
 */
public class Shamir
{
    private static final BigInteger PRIME = new BigInteger(
      "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    private final SecureRandom random;
    
    /**
     * Constructs a new Shamir secret sharing instance with FIPS-compliant
     * secure random number generation.
     *
     * @throws RuntimeException if the FIPS random number generator cannot be initialized
     */
    public Shamir()
    {
        try
          {
            // Use the FIPS approved secure random directly
            random = CryptoServicesRegistrar.getSecureRandom();
          }
        catch (Exception e)
          {
            throw new RuntimeException("Failed to initialize FIPS random number generator", e);
          }
    }
    
    /**
     * Splits a secret into n shards, where k shards are required to reconstruct it.
     *
     * @param secret The secret to split, encoded as a byte array
     * @param n The total number of shards to generate
     * @param k The threshold of shards required to reconstruct
     * @return Array of n shards
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Shard[] split(byte[] secret, int n, int k)
    {
        if (k > n)
            throw new IllegalArgumentException("Threshold k cannot be greater than total shards n");
        if (k < 2)
            throw new IllegalArgumentException("Threshold k must be at least 2");
        
        // Convert secret to BigInteger
        BigInteger s = new BigInteger(1, secret);
        if (s.compareTo(PRIME) >= 0)
            throw new IllegalArgumentException("Secret is too large");
        
        // Generate random coefficients for polynomial
        BigInteger[] coeff = new BigInteger[k - 1];
        for (int i = 0; i < k - 1; i++)
        {
            coeff[i] = new BigInteger(PRIME.bitLength(), random).mod(PRIME);
        }
        
        // Generate shards
        Shard[] shards = new Shard[n];
        for (int i = 0; i < n; i++)
        {
            BigInteger x = BigInteger.valueOf(i + 1);
            BigInteger y = s;
            
            // Evaluate polynomial
            for (int j = 0; j < k - 1; j++)
            {
                y = y.add(coeff[j].multiply(x.pow(j + 1))).mod(PRIME);
            }
            
            shards[i] = new Shard(x, y);
        }
        
        return shards;
    }
    
    /**
     * Reconstructs the secret from k or more shards using Lagrange interpolation.
     *
     * @param shards Array of shards to use for reconstruction
     * @param k The threshold of shards required
     * @return The reconstructed secret as a byte array
     * @throws IllegalArgumentException if not enough valid shards are provided
     */
    public byte[] reconstruct(Shard[] shards, int k)
    {
      if (shards.length < k)
        {
          throw new IllegalArgumentException(
            "Not enough shards provided. Need at least " + k + " shards.");
        }
      
      BigInteger secret = BigInteger.ZERO;
      
      // Use the first k shards for reconstruction
      for (int i = 0; i < k; i++)
      {
          BigInteger numerator = BigInteger.ONE;
          BigInteger denominator = BigInteger.ONE;
          
          for (int j = 0; j < k; j++)
          {
              if (i != j)
              {
                  numerator = numerator.multiply(shards[j].x.negate())
                    .mod(PRIME);
                  denominator = denominator.multiply(
                    shards[i].x.subtract(shards[j].x)).mod(PRIME);
              }
          }
          
          BigInteger value = shards[i].y.multiply(numerator)
            .multiply(denominator.modInverse(PRIME)).mod(PRIME);
          secret = secret.add(value).mod(PRIME);
        }
      
      return secret.toByteArray();
    }
    
    /**
     * Represents a single shard of a split secret.
     */
    public static class Shard
    {
        public final BigInteger x;
        public final BigInteger y;
        
        public Shard(BigInteger x, BigInteger y)
        {
            this.x = x;
            this.y = y;
        }
        
      @Override
        public String toString()
        {
          return String.format("Shard(x=%s, y=%s)",
                              Hex.toHexString(x.toByteArray()),
                              Hex.toHexString(y.toByteArray()));
        }
    }
}