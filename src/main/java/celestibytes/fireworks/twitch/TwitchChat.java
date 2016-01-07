package celestibytes.fireworks.twitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TwitchChat {
	
	private ChatHook chatHook;
	
	public boolean isActive() {
		return chatHook != null && chatHook.isActive();
	}
	
	public void start() {
		chatHook = new ChatHook();
		chatHook.start();
	}
	
	public List<String> getFireworks() {
		if(isActive()) {
			return chatHook.getMessages();
		}
		
		return null;
	}
	
	public static final class IRCMsg {
		public final String source, command;
		public final String data;
		public final String[] args;
		
		public final boolean fromServer;
		public final boolean valid;
		
		public IRCMsg(String raw) {
			if(raw != null) {
//				System.out.println("DEBUG: " + raw);
				String[] split = raw.split(" ");
				
				if(split.length == 1) {
					valid = true;
					fromServer = true;
					source = null;
					command = split[0];
					data = null;
					args = new String[0];
				} else if(split.length > 1) {
					if(raw.startsWith(":")) {
//						System.out.println("\traw: " + raw);
						valid = true;
						source = split[0].substring(1);
						fromServer = !source.contains("@");
						command = split[1];
						
						String data = null;
						
						int i;
						for(i = 1; i < split.length; i++) {
							if(split[i].startsWith(":")) {
								data = getData(split, i);
								break;
							}
						}
						
						if(data == null) {
							this.data = null;
						} else {
							this.data = data;
						}
						
						args = new String[i - 2];
						if(args.length > 0) {
							System.arraycopy(split, 2, args, 0, args.length);
						}
						
//						int i;
//						for(i = 2; i < split.length; i++) {
//							if(split[i].startsWith(":")) {
//								args = new String[i - 2];
//								
//								if(args.length > 0) {
//									System.arraycopy(split, 2, args, 0, args.length);
//								}
//								
//								break;
//							}
//						}
					} else {
						valid = true;
						source = null;
						fromServer = true;
						command = split[0];
						String data = null;
						
						int i;
						for(i = 1; i < split.length; i++) {
							if(split[i].startsWith(":")) {
								data = getData(split, i);
								break;
							}
						}
						
						if(data == null) {
							this.data = null;
						} else {
							this.data = data;
						}
						
						args = new String[i - 1];
						if(args.length > 0) {
							System.arraycopy(split, 1, args, 0, args.length);
						}
					}
				} else {
					valid = false;
					source = null;
					command = null;
					data = null;
					fromServer = true;
					args = new String[0];
				}
			} else {
				valid = false;
				source = null;
				command = null;
				data = null;
				fromServer = true;
				args = new String[0];
			}
		}
		
		private String getData(String[] str, int start) {
			if(start < 0 || start >= str.length) {
				return null;
			}
			
			StringBuilder sb = new StringBuilder();
			for(int i = start; i < str.length; i++) {
				if(i == start) {
					sb.append(str[i].substring(1));
				} else {
					sb.append(" " + str[i]);
				}
				
			}
			
			return sb.toString();
		}
		
		public IRCMsg(String source, String command, String args, String data) {
			this.source = source;
			this.command = command;
			this.args = args.split(" ");
			this.data = data;
			fromServer = false;
			valid = command != null;
		}
		
		public String toNetString() {
			StringBuilder sb = new StringBuilder();
			
			if(source != null) {
				sb.append(":" + source);
			}
			
			sb.append(" " + command);
			
			for(String s : args) {
				sb.append(" " + s);
			}
			
			if(data != null) {
				sb.append(":" + data);
			}
			
			return sb.toString();
		}
		
		public String getDataAsString() {
			if(data != null) {
				return ":" + data;
			}
			
			return "";
		}
		
		public String getArgsAsString() {
			if(args != null) {
				StringBuilder sb = new StringBuilder();
				
				for(int i = 0; i < args.length; i++) {
					if(i > 0) {
						sb.append(" ");
					}
					
					sb.append(args[i]);
				}
				
				return sb.toString();
			}
			
			return "";
		}
		
		@Override
		public String toString() {
			StringBuilder argsstr = null;
			if(args != null && args.length > 0) {
				argsstr = new StringBuilder();
				
				for(int i = 0; i < args.length; i++) {
					argsstr.append(", args[" + i + "]='" + args[i] + "'");
				}
			}
			
			return "{valid=" + valid + ", source=" + (source == null ? "<null>" : source) + ", command: " + (command == null ? "<null>" : command) + ", fromServer=" + fromServer + (argsstr != null ? argsstr.toString() : ", args=<null>") + ", data=" + getDataAsString() + "}";
		}
	}
	
	private static final class ChatHook extends Thread {
		private List<String> msgs = new LinkedList<>();
		private boolean ignoreMessages = false;
		
		private String channel = null, username = null, password = "~";
		
		private boolean stopping = false;
		
		private Socket sock = null;
		
		public ChatHook() {
			channel = "#garophel";
		}
		
		public synchronized boolean isActive() {
			return sock != null && !stopping;
		}
		
		public synchronized void quit() {
			stopping = true;
		}
		
		public List<String> getMessages() {
			List<String> ret;
			synchronized (msgs) {
				ret = new LinkedList<>(msgs);
				msgs.clear();
			}
			
			return ret;
		}
		
		@Override
		public void run() {
			this.setName("ChatHook Thread");
			
			Random rand = new Random();
			username = "justinfan" + (200000 + rand.nextInt(800000));
			
			InputStream is = null;
			OutputStream os = null;
			
			try {
				sock = new Socket("irc.twitch.tv", 6667);
				
				is = sock.getInputStream();
				os = sock.getOutputStream();
				
				sendString(os, "PASS " + password);
				sendString(os, "NICK " + username);
				
				IRCMsg succ = new IRCMsg(readString(is));
				if(succ.valid && "001".equals(succ.command)) {
					sendString(os, "JOIN " + channel);
					
					while(!stopping) {
						IRCMsg recv = new IRCMsg(readString(is));
						if(!recv.valid) {
							break;
						}
						
						if("PING".equalsIgnoreCase(recv.command)) {
							sendString(os, "PONG " + recv.getDataAsString());
						}
						
						if("PRIVMSG".equalsIgnoreCase(recv.command)) {
							if(recv.data != null && recv.data.length() > 4 && recv.data.startsWith("!fw ")) {
								synchronized (msgs) {
									msgs.add(recv.data.substring(4));
								}
							}
							
							System.out.println("PRIVMSG: " + recv);
							if("!fwbot exit".equals(recv.data)) {
								System.out.println("bot exit!");
								stopping = true;
							}
						}
					}
				}
				
				sendString(os, "QUIT quit");
				
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			if(sock != null) {
				try {
					sock.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private String readString(InputStream is) throws IOException {
			int c = -1;
			int maxLen = 512;
			StringBuilder input = new StringBuilder();
			boolean ending = false;
			while(maxLen > 0) {
				maxLen--;
				c = is.read();
				if(c == -1) {
					System.out.println("Read error!");
				} else {
					if(ending) {
						if(c == '\n') {
							System.out.println("readString: " + input.toString());
							return input.toString();
						} else if(c == '\r') {
							System.out.println("double \\r?");
							return null;
						} else {
							return null;
						}
					} else if(c == '\r') {
						ending = true;
					} else {
						input.append((char) c);
					}
				}
			}
			
			System.out.println("long message or some weird error");
			return null;
		}
		
		private void sendString(OutputStream os, String txt) throws IOException {
			System.out.println("sendString: " + txt);
			os.write(txt.getBytes());
			os.write("\r\n".getBytes());
			os.flush();
		}
	}
}
