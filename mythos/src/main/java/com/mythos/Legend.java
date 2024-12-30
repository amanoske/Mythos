package com.mythos;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data structure for managing a collection of secrets.
 * This class provides thread-safe operations for storing and retrieving
 * Secret objects.
 */
public class Legend
{
  private final Map<String, Secret> secrets;
  
  /**
   * Constructs a new empty Legend.
   */
  public Legend()
  {
    this.secrets = Collections.synchronizedMap(new HashMap<>());
  }
  
  /**
   * Constructs a new Legend with initial secrets.
   *
   * @param initialSecrets Set of secrets to initialize with
   * @throws IllegalArgumentException if initialSecrets is null or contains null
   */
  public Legend(Set<Secret> initialSecrets)
  {
    if (initialSecrets == null)
      {
        throw new IllegalArgumentException("Initial secrets cannot be null");
      }
    if (initialSecrets.contains(null))
      {
        throw new IllegalArgumentException("Secret set cannot contain null values");
      }
    
    this.secrets = Collections.synchronizedMap(new HashMap<>());
    for (Secret secret : initialSecrets)
      {
        this.secrets.put(secret.getName(), secret);
      }
  }
  
  /**
   * Adds a secret to the legend.
   *
   * @param secret The secret to add
   * @return The previously stored secret with the same name, if any
   * @throws IllegalArgumentException if secret is null
   */
  public Optional<Secret> addSecret(Secret secret)
  {
    if (secret == null)
      {
        throw new IllegalArgumentException("Secret cannot be null");
      }
    
    return Optional.ofNullable(secrets.put(secret.getName(), secret));
  }
  
  /**
   * Removes a secret from the legend.
   *
   * @param secretName The name of the secret to remove
   * @return The removed secret, if it existed
   * @throws IllegalArgumentException if secretName is null
   */
  public Optional<Secret> removeSecret(String secretName)
  {
    if (secretName == null)
      {
        throw new IllegalArgumentException("Secret name cannot be null");
      }
    
    return Optional.ofNullable(secrets.remove(secretName));
  }
  
  /**
   * Retrieves a secret by its name.
   *
   * @param secretName The name of the secret to retrieve
   * @return Optional containing the secret if found, empty otherwise
   * @throws IllegalArgumentException if secretName is null
   */
  public Optional<Secret> getSecret(String secretName)
  {
    if (secretName == null)
      {
        throw new IllegalArgumentException("Secret name cannot be null");
      }
    
    return Optional.ofNullable(secrets.get(secretName));
  }
  
  /**
   * Updates an existing secret's value.
   *
   * @param secretName The name of the secret to update
   * @param newValue The new value for the secret
   * @return Optional containing the updated secret if found, empty otherwise
   * @throws IllegalArgumentException if any parameter is null
   */
  public Optional<Secret> updateSecret(String secretName, String newValue)
  {
    if (secretName == null || newValue == null)
      {
        throw new IllegalArgumentException("Parameters cannot be null");
      }
    
    return Optional.ofNullable(
        secrets.computeIfPresent(secretName,
                               (key, oldSecret) -> oldSecret.withValue(newValue)));
  }
  
  /**
   * Gets the names of all secrets in the legend.
   *
   * @return Unmodifiable set of secret names
   */
  public Set<String> getSecretNames()
  {
    return Collections.unmodifiableSet(secrets.keySet());
  }
  
  /**
   * Gets all secrets in the legend.
   *
   * @return Unmodifiable set of all secrets
   */
  public Set<Secret> getAllSecrets()
  {
    return Collections.unmodifiableSet(
        new HashSet<>(secrets.values()));
  }
  
  /**
   * Checks if a secret with the given name exists.
   *
   * @param secretName The name to check
   * @return true if the secret exists, false otherwise
   * @throws IllegalArgumentException if secretName is null
   */
  public boolean containsSecret(String secretName)
  {
    if (secretName == null)
      {
        throw new IllegalArgumentException("Secret name cannot be null");
      }
    
    return secrets.containsKey(secretName);
  }
  
  /**
   * Returns the number of secrets in the legend.
   *
   * @return The number of secrets
   */
  public int size()
  {
    return secrets.size();
  }
  
  /**
   * Removes all secrets from the legend.
   */
  public void clear()
  {
    secrets.clear();
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      {
        return true;
      }
    if (o == null || getClass() != o.getClass())
      {
        return false;
      }
    
    Legend other = (Legend) o;
    return Objects.equals(secrets, other.secrets);
  }
  
  @Override
  public int hashCode()
  {
    return Objects.hash(secrets);
  }
  
  @Override
  public String toString()
  {
    return String.format("Legend(secretCount=%d, secretNames=%s)",
                        secrets.size(),
                        String.join(", ", getSecretNames()));
  }
  
  /**
   * Securely destroys all secrets in the legend.
   */
  public void destroy()
  {
    synchronized (secrets)
      {
        // Attempt to securely clear each secret
        secrets.values().forEach(Secret::destroy);
        secrets.clear();
        System.gc();
      }
  }
}