package com.jjangg96s.primecoin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jjangg96s.primecoin.exception.NoKeyException;


public class ArgumentUtil {

	private List<String> keys;
	private List<String> values;

	/**
	 * Set Argument List
	 * 
	 * @param args Argument List
	 */
	private void setArgs(List<String> args) {
		this.keys = new ArrayList<String>();
		this.values = new ArrayList<String>();

		// retrive args
		// argument key must start with '-'
		for (int i = 0; i < args.size(); i++) {
			if (args.get(i).trim().startsWith("-")) {
				// key
				keys.add(args.get(i).trim());
				values.add("");
			} else {
				// value
				values.set(keys.size() - 1, args.get(i).trim());
			}
		}
	}

	/**
	 * Constructor
	 * 
	 * @param args String array of args
	 */
	public ArgumentUtil(String[] args) {
		setArgs(Arrays.asList(args));
	}

	/**
	 * Constructor
	 * 
	 * @param args List of args
	 */
	public ArgumentUtil(List<String> args) {
		setArgs(args);
	}

	@Override
	public String toString() {
		if (keys == null || values == null)
			return "No List";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < keys.size(); i++) {
			if (values.get(i).equals(""))
				sb.append(keys.get(i) + "\n");
			else
				sb.append(keys.get(i) + " = " + values.get(i) + "\n");
		}

		return sb.toString();
	}

	/**
	 * Get Argument value from key
	 * 
	 * @param key key
	 * @return value
	 * @throws NoKeyException if there is no key return exception
	 */
	public String getValue(String key) throws NoKeyException {
		if (keys == null || values == null)
			throw new NoKeyException();

		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equalsIgnoreCase(key)) {
				return values.get(i);
			}
		}

		throw new NoKeyException();
	}

	/**
	 * Get Argument key exist
	 * 
	 * @param key key
	 * @return boolean
	 */
	public boolean isExist(String key) {
		if (keys == null)
			return false;

		return keys.contains(key);
	}
	
	
	/**
	 * Get Argument size
	 * @return argument size
	 */
	public int getArgumentCount()
	{
		if(keys == null)
			return 0;
		
		return keys.size();
	}

}
