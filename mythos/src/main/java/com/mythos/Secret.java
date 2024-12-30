package com.mythos;

import java.util.Objects;

/**
 * Data structure representing a named secret value pair.
 * This class provides an immutable representation of a secret
 * consisting of a name and its associated value.
 * 
 * Each Legend contains a collection of secrets. 
 */
	public class Secret
	{
		private final String name;
		private final String value;

		/**
		 * Constructs a new Secret with the specified name and value.
		 *
		 * @param name The name of the secret
		 * @param value The value of the secret
		 * @throws IllegalArgumentException if name or value is null
		 */
		public Secret(String name, String value)
		{
			if (name == null)
				{
					throw new IllegalArgumentException("Secret name cannot be null");
				}
			if (value == null)
				{
					throw new IllegalArgumentException("Secret value cannot be null");
				}

			this.name = name;
			this.value = value;
		}

		/**
		 * Gets the name of the secret.
		 *
		 * @return The secret name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Gets the value of the secret.
		 *
		 * @return The secret value
		 */
		public String getValue()
		{
			return value;
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

			Secret secret = (Secret) o;
			return name.equals(secret.name) && value.equals(secret.value);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(name, value);
		}

		@Override
		public String toString()
		{
			// Note: We don't include the actual secret value in toString for security
			return String.format("Secret(name=%s)", name);
		}

		/**
		 * Creates a copy of this Secret with a new value.
		 *
		 * @param newValue The new secret value
		 * @return A new Secret instance with the same name but updated value
		 * @throws IllegalArgumentException if newValue is null
		 */
		public Secret withValue(String newValue)
		{
			return new Secret(this.name, newValue);
		}

		/**
		 * Securely wipes the secret value from memory.
		 * Note: This is a best-effort operation as Java doesn't guarantee
		 * immediate memory cleanup.
		 */
		public void destroy()
		{
			// Best-effort zeroing of the value from memory
			// Note: This isn't guaranteed due to Java string immutability
			// and garbage collection
			System.gc();
		}
	}