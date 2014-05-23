// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class UniversalParser {
	private int size;
	private int currPosition;
	private byte[] data;
	private boolean fileLoaded;
	
	public UniversalParser(String fname) {
		currPosition = 0;
		size = 0;
		fileLoaded = false;
		FileHandle file = null;
		try { file = Gdx.files.getFileHandle(fname, FileType.Local); }
		catch(GdxRuntimeException e) { return; }
		if(!file.exists())
			return;
		size = (int)file.length();
		data = new byte[size];
		file.readBytes(data, 0, size);
		fileLoaded = true;
	}
	
	public boolean isFileLoaded() {
		return fileLoaded;
	}
	
	public boolean isEOFReached() {
		return !(currPosition < size);
	}
	public String next() {
		String retVal = "";
		String whiteChars = new String(" \n\t\r");
		String syntaxChars = new String("!@#$%^&*()-=+[]{};:\'\"|\\|<>?,/|");
		boolean isString = false;
		boolean isNumber = false;
		boolean decDotHasBeenUsed = false;
		boolean isCommand = false;

		
		while(currPosition < size) {
			//JOptionPane.showMessageDialog(null, "\"" + retVal + "\"\nPosition: " + currPosition + "\nChar: " + (char)data[currPosition] + "\nisString: " + isString + "\nisNumber: " + isNumber + "\nisCommand: " + isCommand, "retVal", JOptionPane.INFORMATION_MESSAGE);
			boolean skip = false;
			if(isString) {
				if(data[currPosition] == '\"') {
					currPosition++;
					return retVal;
				}
				else
					retVal += (char)data[currPosition];
			}
			else if(isNumber) {
				if(data[currPosition] == '.') {
					if(!decDotHasBeenUsed)
						retVal += (char)data[currPosition];
					else
						return retVal;
					decDotHasBeenUsed = true;
				}
				else
				if(data[currPosition] >= '0' && data[currPosition] <= '9')
					retVal += (char)data[currPosition];
				else
					return retVal;
			}
			else
			if(isCommand) {
				for(int i = 0; i < whiteChars.length(); i++) {
					if(whiteChars.getBytes()[i] == data[currPosition]) {
						currPosition++;
						return retVal;
					}
				}
				for(int i = 0; i < syntaxChars.length(); i++) {
					if(syntaxChars.getBytes()[i] == data[currPosition])
						return retVal;
				}
				retVal += (char)data[currPosition];
			}
			else {
				if((data[currPosition] >= '0' && data[currPosition] <= '9') || (data[currPosition] == '-' && data[currPosition+1] >= '0' && data[currPosition+1] <= '9')) {
					retVal += (char)data[currPosition];
					currPosition++;
					isNumber = true;
					continue;
				}
				if(data[currPosition] == '"') {
					currPosition++;
					isString = true;
					continue;
				}
				if((data[currPosition] >= 'a' && data[currPosition] <= 'z') || (data[currPosition] >= 'A' && data[currPosition] <= 'Z')) {
					isCommand = true;
					continue;
				}

				for(int i = 0; i < whiteChars.length(); i++) {
					if(whiteChars.getBytes()[i] == data[currPosition]) {
						//JOptionPane.showMessageDialog(null, "SDSDFSDFSDF", "retVal", JOptionPane.INFORMATION_MESSAGE);
						skip = true;
						break;
					}
				}
				if(!skip)
				 for(int i = 0; i < syntaxChars.length(); i++) {
					if(syntaxChars.getBytes()[i] == data[currPosition]) {
						//JOptionPane.showMessageDialog(null, "syntaxChars", "retVal", JOptionPane.INFORMATION_MESSAGE);
						retVal += (char)data[currPosition];
						currPosition++;
						return retVal;
					}
				}
			}
			currPosition++;
		}
		return retVal;
	}
}
