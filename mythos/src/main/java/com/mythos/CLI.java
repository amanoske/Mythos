package com.mythos;

import java.io.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

/**
   * Constructs a new CLI instance.
   * Attempts to use Console for secure reading if necessary,
   * falls back to BufferedReader if not.
   */

public class CLI 
{
    private final BufferedReader reader;
    private final Console console;
    private static final String PROMPT_PREFIX = "> ";
    ArrayList<Shard> keyring; 
    Legend currentLegend;
    
    public CLI()
    {
        this.console = System.console();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        keyring = new ArrayList<Shard>();
        currentLegend = new Legend();
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

    /**
   * Lists commands for the CLI
   *
   * 
   */
  public void displayHelp() 
  {
      System.out.println("List of Commands: ");
      System.out.println("********************************");
      System.out.println("******LEGEND COMMANDS******");
      System.out.println("********************************");
      System.out.println("legend status: Lists the current Legend and checks whether Mythos is able to access the current Legend with the Shard Keys it has in its Keyring.");
      System.out.println("legend new [name]: Create a new Legend file with name 'name'.");
      System.out.println("legend set [path]: Set the current active Legend to the Legend located at the path given.");
      System.out.println("********************************");
      System.out.println("******KEYRING COMMANDS******");
      System.out.println("********************************");
      System.out.println("keyring list: Lists number of shard keys in the current Keyring.");
      System.out.println("keyring add [path]: Add a Shard Key located in a .skey file at the path given.");
      System.out.println("keyring clear: Remove all Shard Keys from the current Keyring.");
  }
    
}
