import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.fips.FipsSHS;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;
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
        SP800SecureRandomBuilder builder = new SP800SecureRandomBuilder();
        random = builder.buildHash(new FipsSHS.HashMechanism(), null, false);
      }
    catch (Exception e)
      {
        throw new RuntimeException("Failed to initialize FIPS random number generator", e);
      }
  }
  
  /**
   * Splits a secret into n shares, where k shares are required to reconstruct it.
   *
   * @param secret The secret to split, encoded as a byte array
   * @param n The total number of shares to generate
   * @param k The threshold of shares required to reconstruct
   * @return Array of n shares
   * @throws IllegalArgumentException if parameters are invalid
   */
  public Share[] split(byte[] secret, int n, int k)
  {
    if (k > n)
      {
        throw new IllegalArgumentException("Threshold k cannot be greater than total shares n");
      }
    if (k < 2)
      {
        throw new IllegalArgumentException("Threshold k must be at least 2");
      }
    
    // Convert secret to BigInteger
    BigInteger s = new BigInteger(1, secret);
    if (s.compareTo(PRIME) >= 0)
      {
        throw new IllegalArgumentException("Secret is too large");
      }
    
    // Generate random coefficients for polynomial
    BigInteger[] coeff = new BigInteger[k - 1];
    for (int i = 0; i < k - 1; i++)
      {
        coeff[i] = new BigInteger(PRIME.bitLength(), random).mod(PRIME);
      }
    
    // Generate shares
    Share[] shares = new Share[n];
    for (int i = 0; i < n; i++)
      {
        BigInteger x = BigInteger.valueOf(i + 1);
        BigInteger y = s;
        
        // Evaluate polynomial
        for (int j = 0; j < k - 1; j++)
          {
            y = y.add(coeff[j].multiply(x.pow(j + 1))).mod(PRIME);
          }
        
        shares[i] = new Share(x, y);
      }
    
    return shares;
  }
  
  /**
   * Reconstructs the secret from k or more shares using Lagrange interpolation.
   *
   * @param shares Array of shares to use for reconstruction
   * @param k The threshold of shares required
   * @return The reconstructed secret as a byte array
   * @throws IllegalArgumentException if not enough valid shares are provided
   */
  public byte[] reconstruct(Share[] shares, int k)
  {
    if (shares.length < k)
      {
        throw new IllegalArgumentException(
          "Not enough shares provided. Need at least " + k + " shares.");
      }
    
    BigInteger secret = BigInteger.ZERO;
    
    // Use the first k shares for reconstruction
    for (int i = 0; i < k; i++)
      {
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;
        
        for (int j = 0; j < k; j++)
          {
            if (i != j)
              {
                numerator = numerator.multiply(shares[j].x.negate())
                  .mod(PRIME);
                denominator = denominator.multiply(
                  shares[i].x.subtract(shares[j].x)).mod(PRIME);
              }
          }
        
        BigInteger value = shares[i].y.multiply(numerator)
          .multiply(denominator.modInverse(PRIME)).mod(PRIME);
        secret = secret.add(value).mod(PRIME);
      }
    
    return secret.toByteArray();
  }
  
  /**
   * Represents a single share of a split secret.
   */
  public static class Share
  {
    public final BigInteger x;
    public final BigInteger y;
    
    public Share(BigInteger x, BigInteger y)
    {
      this.x = x;
      this.y = y;
    }
    
    @Override
    public String toString()
    {
      return String.format("Share(x=%s, y=%s)",
                          Hex.toHexString(x.toByteArray()),
                          Hex.toHexString(y.toByteArray()));
    }
  }
  
  /**
   * Example usage of the Shamir secret sharing implementation.
   */
  public static void main(String[] args)
  {
    try
      {
        Shamir shamir = new Shamir();
        
        // Example secret
        byte[] secret = "This is a secret message".getBytes();
        
        // Split into 5 shares with a threshold of 3
        Share[] shares = shamir.split(secret, 5, 3);
        
        System.out.println("Generated shares:");
        for (Share share : shares)
          {
            System.out.println(share);
          }
        
        // Reconstruct secret using first 3 shares
        Share[] reconstruction = new Share[3];
        System.arraycopy(shares, 0, reconstruction, 0, 3);
        
        byte[] reconstructed = shamir.reconstruct(reconstruction, 3);
        System.out.println("\nReconstructed secret: "
                          + new String(reconstructed));
      }
    catch (Exception e)
      {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
      }
  }
}