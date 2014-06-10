import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigFile {
	public ArrayList<String> configData = new ArrayList<String>();
	public boolean initialized = false;
	public File configFile;
	private BufferedReader in;
	private BufferedWriter out;
	/**
	 * Check all prerequisites to reading or writing to file. Will create all folders and files necessary in-case they are not currently existent.
	 * @param write True if readying to write to file, false if reading.
	 * @return False when any errors occur; true if no errors occurred. Errors will display in System.err.
	 */
	public boolean readyFile(boolean write) {
		initialized=false;
		if(!configFile.isFile()) {
			try {
				try{ 
					configFile.createNewFile();
				} catch (IOException e) {
					configFile.mkdirs();
					configFile.delete();
					configFile.createNewFile();
				}
				if(!configFile.isFile()) {
					initialized=false;
					System.err.println("Could not create the file.");
					return false;
				}
			} catch (IOException e) {
				initialized=false;
				System.err.println("Could not create the file: exception!");
				return false;
			}
		}
		if(!configFile.canWrite()) {
			initialized=false;
			System.err.println("Could not read from file: permission error!");
			return false;
		}
		try {
			if(!write) {
				in = new BufferedReader(new FileReader(configFile));
				initialized=true;
				return true;
			} else {
				out = new BufferedWriter(new FileWriter(configFile));
				if(!out.equals(null)) {
					initialized=true;
					return true;
				}
			}
			initialized = false;
			try{in.close();}catch(Exception e){}
			try{out.close();}catch(Exception e){}
			System.err.println("Could not ready file: buffers not ready!");
			return false;
		} catch (IOException e) {
			initialized = false;
			try{in.close();}catch(IOException e1){}
			try{out.close();}catch(IOException e1){}
			System.err.println("Could not ready file: buffers failed!");
			return false;
		}
	}
	/**
	 * Easy use of reading and writing to files from the user's home directory. A .. directory must be submitted to go before the user's home directory.
	 * @param configFile File to write to from the user's home directory. This address may contain subfolders and does not need to be currently existent.
	 */
	public ConfigFile(String configFile) {
		this.configFile = new File(configFile);
		initialized=false;
	}
	
	/**
	 * Reads data from file and places data in configData. Calls readyFile(false).
	 * @return False when any errors occur; true if no errors occurred. Errors will display in System.err.
	 */
	public boolean readFile() {
		if(!readyFile(false)) {
			System.err.println("Could not read file!");
			return false;
		}
		configData = new ArrayList<String>();
		try {
			while(in.ready()) {
				configData.add(in.readLine());
			}
		} catch (IOException e) {
			System.err.println("Could not read from file!");
			try{in.close();}catch(IOException e1){}
			return false;
		}
		try{in.close();}catch(IOException e1){}
		return true;
	}
	/**
	 * Writes data from configData to file. Calls readyFile(true).
	 * @return False when any errors occur; true if no errors occurred. Errors will display in System.err.
	 */
	public boolean writeFile() {
		if(!readyFile(true)) {
			System.err.println("Could not write to file! File unchanged.");
			return false;
		}
		for(int i=0; i<configData.size(); i++) {
			try {
				out.write(configData.get(i));
			} catch (IOException e) {
				System.err.println("Could not write data " + i + " to file! File corrputed!");
				try{out.close();}catch(IOException e1){}
				return false;
			}
		}
		try{out.close();}catch(IOException e1){}
		return true;
	}
}

