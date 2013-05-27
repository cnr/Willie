package com.drtshock.willie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;

import org.pircbotx.Base64;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

import com.drtshock.willie.command.CICommandHandler;
import com.drtshock.willie.command.Command;
import com.drtshock.willie.command.DonateCommandHandler;
import com.drtshock.willie.command.HelpCommandHandler;
import com.drtshock.willie.command.IssuesCommandHandler;
import com.drtshock.willie.command.LatestCommandHandler;
import com.drtshock.willie.command.PluginCommandHandler;
import com.drtshock.willie.command.PopcornCommandHandler;
import com.drtshock.willie.command.RepoCommandHandler;
import com.drtshock.willie.command.RulesCommandHandler;
import com.drtshock.willie.command.TWSSCommandHandler;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class Willie extends PircBotX {
	
	public static final Gson gson = new Gson();
	public static final JsonParser parser = new JsonParser();
	public static String GIT_AUTH;
	
	public Properties config;
	public JenkinsServer jenkins;
	public CommandManager commandManager;
	
	private Willie(String[] channels) throws NickAlreadyInUseException, IOException, IrcException{
		super();
		
		File configFile = new File("config.txt");
		this.config = new Properties();
		
		if (!configFile.exists()){
			this.config.put("github-username", "change-me");
			this.config.put("github-password", "change-me");
			this.config.store(new FileOutputStream(configFile), null);
		}else{
			this.config.load(new FileInputStream(configFile));
		}
		
		GIT_AUTH = "Basic " + Base64.encodeToString((this.config.getProperty("github-username") + ":" + this.config.getProperty("github-password")).getBytes(), false);
		
		this.jenkins = new JenkinsServer("http://ci.drtshock.com/");
		this.commandManager = new CommandManager(this);
		
		this.commandManager.registerCommand(new Command("repo", "show Willie's repo", new RepoCommandHandler()));
		this.commandManager.registerCommand(new Command("latest", "<plugin_name> - Get latest file for plugin on BukkitDev", new LatestCommandHandler()));
		this.commandManager.registerCommand(new Command("plugin", "<name> - looks up a plugin on BukkitDev", new PluginCommandHandler()));
		this.commandManager.registerCommand(new Command("issues", "<job_name> [page] - check github issues for jobs on http://ci.drtshock.com", new IssuesCommandHandler()));
		this.commandManager.registerCommand(new Command("ci", "shows Jenkins info", new CICommandHandler()));
		this.commandManager.registerCommand(new Command("rules", "show channel rules", new RulesCommandHandler()));
		this.commandManager.registerCommand(new Command("help", "show this help info", new HelpCommandHandler()));
		this.commandManager.registerCommand(new Command("p", "pop some popcorn!", new PopcornCommandHandler()));
		this.commandManager.registerCommand(new Command("twss", "that's what she said!", new TWSSCommandHandler()));
		this.commandManager.registerCommand(new Command("donate", "shows donation info", new DonateCommandHandler()));
		
		this.setName("Willie");
		this.setVerbose(false);
		this.getListenerManager().addListener(this.commandManager);
		this.connect("irc.esper.net");
		this.setAutoReconnectChannels(true);
		
		for (String channel : channels){
			this.joinChannel(channel);
		}
		
		(new Timer()).schedule(new IssueNotifierTask(this), 300000, 300000); // 5 minutes
	}
	
	public static void main(String[] args){
		try{
			new Willie(new String[]{"#drtshock"});
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
}