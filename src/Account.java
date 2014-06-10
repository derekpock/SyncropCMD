import java.util.ArrayList;

public class Account {
	public String username="", encryptedPass="", newPass="";
	public boolean enabled=true;
	public ArrayList<String> includeDirs, excludeDirs, includeAbsDirs;
	public Account(int accNum) {
		username = SyncropCMD.conf.configData.get(1+(5*accNum)).split("\t")[0];
		encryptedPass = SyncropCMD.conf.configData.get(1+(5*accNum)).split("\t")[1];
		newPass = SyncropCMD.conf.configData.get(1+(5*accNum)).replaceFirst(username + "\t" + encryptedPass, "");
		if(newPass.startsWith("\t")) newPass.replaceFirst("\t", "");
		try {
			enabled = Boolean.parseBoolean(SyncropCMD.conf.configData.get(2+(5*accNum)).trim());
		} catch (Exception e) { e.printStackTrace(); System.exit(1); }
		excludeDirs = new ArrayList<String>();
		includeDirs = new ArrayList<String>();
		includeAbsDirs = new ArrayList<String>();
		
		String[] temp = SyncropCMD.conf.configData.get(3+(5*accNum)).split("\t");
		for(String line:temp) {
			if(line.length()!=0) excludeDirs.add(line);
		}
		temp = SyncropCMD.conf.configData.get(4+(5*accNum)).split("\t");
		for(String line:temp) {
			if(line.length()!=0) includeDirs.add(line);
		}
		temp = SyncropCMD.conf.configData.get(5+(5*accNum)).split("\t");
		for(String line:temp) {
			if(line.length()!=0) includeAbsDirs.add(line);
		}
	}
	public Account() {
		includeDirs = new ArrayList<String>();
		excludeDirs = new ArrayList<String>();
		includeAbsDirs = new ArrayList<String>();
		enabled = true;
	}
}