package celestibytes.fireworks;

import celestibytes.fireworks.twitch.TwitchChat.IRCMsg;

public class TestMain {

	public static void main(String[] args) {
		IRCMsg chanmsg = new IRCMsg(":user!~something@example.com PRIVMSG #testchan :Hello World!");
		System.out.println("chan: " + chanmsg);
		
		IRCMsg ping = new IRCMsg("PING :123456");
		System.out.println("ping: " + ping);
		
		IRCMsg test = new IRCMsg(":tractor CMD arg1 arg2 arg3 arg4 :.dat daa dee dii doo");
		System.out.println("test: " + test);
	}
	
}
