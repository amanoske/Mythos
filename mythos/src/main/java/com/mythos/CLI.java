package com.mythos;

import java.io.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;

/**
   * Constructs a new CLI instance.
   * Attempts to use Console for secure reading if available,
   * falls back to BufferedReader if not.
   */

public class CLI 
{
    private final BufferedReader reader;
    private final Console console;
    private static final String PROMPT_PREFIX = "> ";
    
    public CLI()
    {
        this.console = System.console();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

     /**
   * Reads a line of text from the user with a prompt.
   *
   * 
   * @param prompt The prompt to display to the user
   * @return The user's input as a string
   * @throws IOException if an I/O error occurs
   */
    public String readLine(String prompt) throws IOException
    {
        System.out.print(prompt + PROMPT_PREFIX);
        String input = reader.readLine();
        if (input == null)
        {
            throw new IOException("End of input stream reached");
        }
        return input.trim();
    }
    
}
