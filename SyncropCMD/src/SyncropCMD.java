import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SyncropCMD {
	private static List<String> args;
	private static PrintStream o = System.out;
	private static int numArgs;
	public static ConfigFile conf;
	private static String CONF_PATH = ".syncrop/.syncropconfig";
	private static final int PASSWORD_MAX_LENGTH=30;
	private static Scanner scan;
	private static boolean unix=true, mac=false;
	private static char slash;
	private static String ID;
	private static ArrayList<Account> accounts;
		public static void main(String[] rawArgs) {
			args = Arrays.asList(rawArgs);
			numArgs=args.size();
			if(numArgs==0) {
				o.println("Try `syncrop help`.");
				System.exit(1);
			}
			String osname = System.getProperty("os.name").toLowerCase();
			if(osname.contains("window")) {
				slash='\\';
				unix=false;
				try {
					Process p = Runtime.getRuntime().exec("cmd /c \"echo %APPDATA%\"");
					p.waitFor();
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					CONF_PATH = br.readLine().concat("\\.syncrop\\");
				} catch (Exception e) { e.printStackTrace(); return; }
			} else if (osname.contains("mac")) {
				slash='/';
				unix=true;
				mac=true;
				CONF_PATH = System.getProperty("user.home").concat("/Library/Application Support/.syncrop/");
			} else {
				slash='/';
				unix=true;
				mac=false;
				CONF_PATH = System.getProperty("user.home").concat("/.syncrop/");
			}
			conf = new ConfigFile(CONF_PATH+".syncropconfig");
			scan = new Scanner(System.in);
			
			
			readConf();
			
	
			if(se(0, "info")) {
				o.println("ID: " + ID);
				for(Account account:accounts) {
					o.println("\nUsername: " + account.username);
					o.println("Enabled:  " + account.enabled);
					o.println("Relative Includes:");
					for(String line:account.includeDirs) o.println("   ~" + slash + line);
					o.println("Absolute Includes:");
					for(String line:account.includeAbsDirs) o.println("   " + line);
					o.println("Excludes:");
					for(String line:account.excludeDirs) o.println("   " + line);
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//exclude
			} else if (se(0, "exclude")) {
				if(numArgs>=3) {
					boolean accountExists=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					String folder="";
					for(int i=2; i<args.size(); i++) {
						folder = folder.concat(args.get(i));
					}
					if(folder.endsWith(slash+"")) {
						folder = folder.substring(0, folder.length()-1);
					}
					boolean good=true;
					for(String line:accounts.get(accountNumber).excludeDirs) good=((!(line.equals(folder))) && good);	
					if(good) accounts.get(accountNumber).excludeDirs.add(folder);
					else {
						o.println("Exclude " + folder + " already exists for this account!");
						System.exit(1);
					}
					if(writeConf()) o.println(folder + " excluded successfully to syncrop.");
					else {
						o.println("An error occurred while trying to exclude " + folder);
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop exclude ACCOUNT FOLDER");
					System.exit(1);
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//include
			} else if (se(0, "include")) {
				if(numArgs>=3) {
					boolean accountExists=false, isAbs=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					String folder="";
					for(int i=2; i<args.size(); i++) {
						folder = folder.concat(args.get(i));
					}
					if(folder.endsWith(""+slash)) {
						folder = folder.substring(0, folder.length()-1);
					}
					if(unix) {
						isAbs=folder.startsWith("/");
					} else {
						try {
							isAbs=folder.substring(1).startsWith(":");
						} catch (Exception e) { isAbs=false; }
					}
					boolean good=true;
					if(isAbs) {
						for(String line:accounts.get(accountNumber).includeAbsDirs) good=((!(line.equals(folder))) && good);
					} else {
						for(String line:accounts.get(accountNumber).includeDirs) good=((!(line.equals(folder))) && good);	
					}					
					if(good) {
						if(isAbs) accounts.get(accountNumber).includeAbsDirs.add(folder);
						else accounts.get(accountNumber).includeDirs.add(folder);
					} else {
						if(isAbs) o.println("Include " + folder + " already exists for this account!");
						else o.println("Include ~" + slash + folder + " already exists for this account!");
						System.exit(1);
					}
					if(writeConf()) {
						if(isAbs) o.println(folder + " included successfully to syncrop.");
						else o.println("~" + slash + folder + " included successfully to syncrop.");
					} else {
						if(isAbs) o.println("An error occurred while trying to include " + folder);
						else o.println("An error occurred while trying to include ~" + slash + folder);
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop include ACCOUNT FOLDER");
					System.exit(1);
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//uninclude
			} else if (se(0, "uninclude")) {
				if(numArgs==2) {
					boolean accountExists=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					o.println("Enter number of include to uninclude: ");
					String dirName;
					for(int i=0; i<(accounts.get(accountNumber).includeDirs.size()+accounts.get(accountNumber).includeAbsDirs.size()); i++) {
						dirName = "";
						try {
							dirName = accounts.get(accountNumber).includeDirs.get(i);
						} catch (IndexOutOfBoundsException e) {
							dirName = accounts.get(accountNumber).includeAbsDirs.get(i-accounts.get(accountNumber).includeDirs.size());
						}
						o.println((i+1)+": "+dirName);
					}
					try{
						int input = scan.nextInt();
						if(input<=0 || input>(accounts.get(accountNumber).includeDirs.size()+accounts.get(accountNumber).includeAbsDirs.size())) {
							o.println("Invalid Choice!");
							System.exit(1);
						}
						try {
							accounts.get(accountNumber).includeDirs.remove(input-1);
						} catch (IndexOutOfBoundsException e) {
							accounts.get(accountNumber).includeAbsDirs.remove(input-accounts.get(accountNumber).includeDirs.size()-1);
						}
						if(writeConf()) {
							o.println("Unincluded " + input + " successfully.");
						} else {
							o.println("An error occurred while trying to uninclude " + input);
							System.exit(1);
						}
					} catch (Exception e) {
						o.println("Invalid Integer!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop uninclude ACCOUNTNAME");
					System.exit(1);
				}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//unexclude
			} else if (se(0, "unexclude")) {
				if(numArgs==2) {
					boolean accountExists=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					o.println("Enter number of exclude to unexclude: ");
					for(int i=0; i<accounts.get(accountNumber).excludeDirs.size(); i++) {
						o.println((i+1)+": "+accounts.get(accountNumber).excludeDirs.get(i));
					}
					try{
						int input = scan.nextInt();
						if(input<=0 || input>accounts.get(accountNumber).excludeDirs.size()) {
							o.println("Invalid Choice!");
							System.exit(1);
						}
						accounts.get(accountNumber).excludeDirs.remove(input-1);
						if(writeConf()) {
							o.println("Unexcluded " + input + " successfully.");
						} else {
							o.println("An error occurred while trying to unexclude " + input);
							System.exit(1);
						}
					} catch (Exception e) {
						o.println("Invalid Integer!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop unexclude ACCOUNTNAME");
					System.exit(1);
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//others
			} else if (se(0, "help")) {	
					o.println("`help`      Show this list\n"
							+ "`info`      See current account and folder information\n"
							+ "`status`    See whether or not syncrop is currently running\n"
							+ "`start`     Initiate the Syncrop Client Daemon\n"
							+ "`stop`      Kill the Syncrop Client Daemon\n"
							+ "`log`       View constant output of syncrop log\n"
							+ "`rmlog`     Remove the log file, resetting `log`\n"
							+ "`addacct`   Add a blank account to syncrop\n"
							+ "`chpass`    Change a LOCAL password for an account (not on the server)\n"
							+ "`rmacct`    Remove an account and lose all known synced includes and excludes\n"
							+ "`include`   Include a relative or absolute folder to an account\n"
							+ "`uninclude` Uninclude a folder from an account\n"
							+ "`exclude`   Exclude an absolute folder from an account\n"
							+ "`unexclude` Unexclude a folder form an account\n"
							+ "`toggle`    Enable/disable toggle an entire account\n");
					
			} else if (se(0, "toggle")) {
				if(numArgs==2) {
					boolean accountExists=false, accountStatus=true;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accounts.get(i).enabled=!accounts.get(i).enabled;
							accountStatus = accounts.get(i).enabled;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					if(writeConf()) {
						o.println("Account " + args.get(1) + " toggled " + accountStatus + ".");			
					} else {
						o.println("An error occurred while toggling " + args.get(1) + " to " + accountStatus + ". Aborting!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop toggle ACCOUNT");
					System.exit(1);
				}
			} else if(se(0, "addacct")) {
				if(numArgs==1) {
					Account account = new Account();
					o.print("Enter Account Username: ");
					account.username = scan.nextLine();
					boolean accountExists=false;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(account.username.trim())){
							accountExists=true;
							break;
						}
					}
					if(accountExists) {
						o.println("Account " + account.username + " already exists!");
						System.exit(1);
					}
					o.print("Enter Account Password: ");
					char[] temp = System.console().readPassword();
					if(temp.length<1 || temp.length>PASSWORD_MAX_LENGTH) {
						o.println("Password must be between 1 and 30 characters (inclusive)! Aborting!");
						System.exit(1);
					}
					o.print("Enter Password Again: ");
					char[] temp2 = System.console().readPassword();
					String char1="";
					String char2="";
					for(char c:temp) char1=char1.concat(c+"");
					for(char c:temp2) char2=char2.concat(c+"");
					if(!char1.equals(char2)) {
						o.println("Passwords different! Aborting!");
						System.exit(1);
					}
					account.newPass = char1;
					accounts.add(account);
					if(writeConf()) {
						o.println("Added account successfully!");
						o.println("Password will be encrypted when syncrop daemon is running.");
					} else {
						o.println("An error occurred when adding the account. Aborting!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop addacct");
					System.exit(1);
				}
			} else if(se(0, "rmacct")) {
				if(numArgs==2) {
					boolean accountExists=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					o.println("***WARNING***");
					o.println("CONTINUING WILL HALT ALL SYNCING TO/FROM THIS ACCOUNT");
					o.println("ALL INFORMATION CONCERNING THE ACCOUNT WILL BE LOST ON THIS SYSTEM");
					o.println("ENTER THE ACCOUNT NAME AGAIN TO CONFIRM DELETION");
					String input = scan.nextLine();
					if(input.equals(accounts.get(accountNumber).username)){
						accounts.remove(accountNumber);
					} else {
						o.println("Account Deletion Canceled!");
						System.exit(0);
					}
					if(writeConf()) {
						o.println("Removed account successfully!");
					} else {
						o.println("An error occurred when removing the account. Aborting!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop rmacct ACCOUNT");
					System.exit(1);
				}
			} else if(se(0, "chpass")) {
				if(numArgs==2) {
					boolean accountExists=false; int accountNumber=0;
					for(int i=0; i<accounts.size(); i++) {
						if(accounts.get(i).username.equals(args.get(1))){
							accountExists=true;
							accountNumber = i;
							break;
						}
					}
					if(!accountExists) {
						o.println("Account " + args.get(1)+ " does not exist!");
						System.exit(1);
					}
					o.print("Enter New Password: ");
					char[] temp = System.console().readPassword();
					if(temp.length<1 || temp.length>PASSWORD_MAX_LENGTH) {
						o.println("Password must be between 1 and 30 characters (inclusive)! Aborting!");
						System.exit(1);
					}
					o.print("Enter Password Again: ");
					char[] temp2 = System.console().readPassword();
					String char1="";
					String char2="";
					for(char c:temp) char1=char1.concat(c+"");
					for(char c:temp2) char2=char2.concat(c+"");
					if(!char1.equals(char2)) {
						o.println("Passwords different! Aborting!");
						System.exit(1);
					}
					accounts.get(accountNumber).newPass = char1;
					if(writeConf()) {
						o.println("Password changed successfully!");
						o.println("Password will be encrypted when syncrop daemon is running.");
					} else {
						o.println("An error occurred changing the password. Aborting!");
						System.exit(1);
					}
				} else {
					o.println("Usage: syncrop chpass ACCOUNT");
					System.exit(1);
				}
			} else if(se(0, "start")) { 
				boolean killed=false;
				try {
					Socket clientSocket = new Socket("localhost", 50002);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					out.println("SHUTDOWN");
					while(!in.readLine().equals("DONE")) {
						o.println("Unknown response from syncrop, trying to kill again...");
						out.println("SHUTDOWN");
					}
					killed=true;
					clientSocket.close();
				} catch (Exception e) {
					killed=false;
				}
				//TODO
				if(unix) {
					if(mac) {
						
					} else {
						try{
							Runtime.getRuntime().exec("java -jar /usr/share/syncrop/Syncrop.jar");
						} catch (Exception e) { o.println("Syncrop not correctly installed! Cannot find syncrop files! Aborting!"); return; }
					}
				} else {
					try {
						Process p = Runtime.getRuntime().exec("cmd /c \"reg query HKCU\\Software\\Syncrop /v Path | findstr /r /c:Path\"");
						p.waitFor();
						BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String[] inputs = br.readLine().split(" ");
						String path = inputs[inputs.length-1].trim();
						Runtime.getRuntime().exec("cmd /c \"java -jar " + path + "Syncrop.jar\"");
					} catch (Exception e) { o.println("Syncrop not correctly installed! Cannot find syncrop files! Aborting!"); return; }
				}
				if(killed) {
					o.println("Syncrop successfully restarted.");
				} else {
					o.println("Syncrop successfully started.");
				}
			} else if(se(0, "stop")) { 
				boolean killed=false;
				try {
					Socket clientSocket = new Socket("localhost", 50002);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					out.println("SHUTDOWN");
					while(!in.readLine().equals("DONE")) {
						o.println("Unknown response from syncrop, trying to kill again...");
						out.println("SHUTDOWN");
					}
					killed=true;
					clientSocket.close();
				} catch (Exception e) {
					killed=false;
				}
				if(killed) {
					o.println("Syncrop successfully stopped.");
				} else {
					o.println("Syncrop was not found running, so not stopped.");
				}
			} else if (se(0,"status")) {
				boolean killed=false;
				try {
					Socket clientSocket = new Socket("localhost", 50002);
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					out.println("PING");
					while(!in.readLine().equals("PONG")) {
						o.println("Unknown response from syncrop, trying to ping again...");
						out.println("PING");
					}
					killed=true;
					clientSocket.close();
				} catch (Exception e) {
					killed=false;
				}
				if(killed) {
					o.println("Syncrop currently running.");
				} else {
					o.println("Syncrop currently stopped.");
				}
			} else if(se(0,"log")) {
				o.println("Do CTRL+C to stop log output.\nTip: Place an & at the end of the command to have it run in the background but\n still display when there is output.");
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				try{ 
					@SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(new FileReader(new File(CONF_PATH+".syncroplog.log")));
					while(true) {
						if(br.ready()) {
							o.println(br.readLine());
						} else {
							Thread.sleep(100);
						}
					}
				} catch (FileNotFoundException e) {
					o.println("Log file does not exist. Start syncrop to create the file.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (se(0, "rmlog")) {
				o.println("Removing log file. Be sure to restart syncrop after this.");
				try {
					new File(CONF_PATH+".syncroplog.log").delete();
					o.println("Log file removed successfully.");
				} catch (Exception e) {
					o.println("Log file failed to remove. Maybe it doesn't exist?");
				}
			} else {
				o.println("Try `syncrop help`");
			}
		}
	private static void readConf() {
		if(!conf.readFile()) {
			o.println("\nAn error occurred when reading ~"+slash+CONF_PATH+".syncropconfig");
			System.exit(1);
		}
		if((conf.configData.size()-1)%5!=0) {
			o.print("You have not yet setup syncrop or syncrop is misconfigured. Would you like to configure it now? ");
			String input = scan.nextLine();
			if(input.toLowerCase().startsWith("y")) {
				Account account = new Account();
				ID = System.currentTimeMillis()*Math.PI+"";
				o.print("Enter Account Username: ");
				account.username = scan.nextLine();
				o.print("Enter Account Password: ");
				char[] temp = System.console().readPassword();
				if(temp.length<1 || temp.length>PASSWORD_MAX_LENGTH) {
					o.println("Password must be between 1 and 30 characters (inclusive)! Aborting!");
					System.exit(1);
				}
				o.print("Enter Password Again: ");
				char[] temp2 = System.console().readPassword();
				String char1="";
				String char2="";
				for(char c:temp) char1=char1.concat(c+"");
				for(char c:temp2) char2=char2.concat(c+"");
				if(!char1.equals(char2)) {
					o.println("Passwords different! Aborting!");
					System.exit(1);
				}
				account.newPass = char1;
				accounts=new ArrayList<Account>();
				accounts.add(account);
				if(writeConf()) {
					o.println("Syncrop configuration completed.");
					o.println("Password will be encrypted when syncrop daemon is running.");
				} else {
					o.println("Syncrop configuration failed! Manual configuration required.");
				}
				System.exit(0);
			} else {
				o.println("Syncrop configuration aborted.");
				System.exit(0);
			}
		} else {
			accounts = new ArrayList<Account>();
			ID = conf.configData.get(0);
			for(int i=0; i<((conf.configData.size()-1)/5); i++) {
				accounts.add(new Account(i));
			}
		}
	}
	private static boolean writeConf() {
		conf.configData = new ArrayList<String>();
		conf.configData.add(ID+"\n");
		for(Account account:accounts) {
			if(account.newPass.length()!=0) conf.configData.add(account.username + "\t" + account.encryptedPass + "\t" + account.newPass + "\n");
			else conf.configData.add(account.username + "\t" + account.encryptedPass + "\n");
			conf.configData.add(account.enabled + "\n");
			String temp = "";
			for(String line:account.excludeDirs) {
				temp = temp.concat(line + "\t");
			}
			if(temp.length()!=0) temp = temp.substring(0, temp.length()-1);
			conf.configData.add(temp+"\n");
			
			temp = "";
			for(String line:account.includeDirs) {
				temp = temp.concat(line + "\t");
			}
			if(temp.length()!=0) temp = temp.substring(0, temp.length()-1);
			conf.configData.add(temp+"\n");
			
			temp = "";
			for(String line:account.includeAbsDirs) {
				temp = temp.concat(line + "\t");
			}
			if(temp.length()!=0) temp = temp.substring(0, temp.length()-1);
			conf.configData.add(temp+"\n");
		}
		return conf.writeFile();
	}
	private static boolean se(int index, String str2) {
		return args.get(index).trim().equalsIgnoreCase(str2.toLowerCase().trim());
	}
	private static class Account {
		public String username="", encryptedPass="", newPass="";
		public boolean enabled=true;
		public ArrayList<String> includeDirs, excludeDirs, includeAbsDirs;
		public Account(int accNum) {
			username = conf.configData.get(1+(5*accNum)).split("\t")[0];
			encryptedPass = conf.configData.get(1+(5*accNum)).split("\t")[1];
			newPass = conf.configData.get(1+(5*accNum)).replaceFirst(username + "\t" + encryptedPass, "");
			if(newPass.startsWith("\t")) newPass.replaceFirst("\t", "");
			try {
				enabled = Boolean.parseBoolean(conf.configData.get(2+(5*accNum)).trim());
			} catch (Exception e) { e.printStackTrace(); System.exit(1); }
			excludeDirs = new ArrayList<String>();
			includeDirs = new ArrayList<String>();
			includeAbsDirs = new ArrayList<String>();
			
			String[] temp = conf.configData.get(3+(5*accNum)).split("\t");
			for(String line:temp) {
				if(line.length()!=0) excludeDirs.add(line);
			}
			temp = conf.configData.get(4+(5*accNum)).split("\t");
			for(String line:temp) {
				if(line.length()!=0) includeDirs.add(line);
			}
			temp = conf.configData.get(5+(5*accNum)).split("\t");
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
}