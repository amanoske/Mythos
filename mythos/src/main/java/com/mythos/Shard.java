package com.mythos;

import java.math.BigInteger;
import org.bouncycastle.util.encoders.Hex;

/**
 * Represents a single Shard Key of a split Shamir's secret.
 */
public class Shard
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