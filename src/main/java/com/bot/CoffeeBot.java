package com.bot;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.klab.ctx.ConversationSession;
import com.klab.data.RDBAdapter;
import com.klab.svc.AppsPropertiy;
import com.klab.svc.ConversationLogger;
import com.klab.svc.ILogger;
import com.klab.svc.SimpleAppFrame;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

public class CoffeeBot extends Thread implements UpdatesListener {
	
	static class CHAT{
		public long chatID;
		public String message;
	}
	
	private BlockingQueue<CHAT> queue;
	private SimpleAppFrame appFrame;
	private ConversationLogger convLogger;
	private ConversationSession session = null;
	private TelegramBot  telegram;
	
	public CoffeeBot() {
		queue = new ArrayBlockingQueue<CHAT>(100);
	}
	
	private boolean prepare() {
		boolean rs = true;
		try {
			RDBAdapter.getInstance().getDB();
			
			String username = AppsPropertiy.getInstance().getProperty("wcs.user");
			String password = AppsPropertiy.getInstance().getProperty("wcs.passwd");
			String workId = AppsPropertiy.getInstance().getProperty("wcs.workid");
		
			appFrame =  new SimpleAppFrame();
			appFrame.setUsername(username);
			appFrame.setPassword(password);
			appFrame.setWorkspaceId(workId);
		
			session = new ConversationSession();
			
			convLogger = new ConversationLogger();
			
			String logger = AppsPropertiy.getInstance().getProperty("logger.className");
			if( logger != null && logger.length() > 0) {
				try {
					convLogger.setLogger((ILogger)Utils.loadClass(logger));
				} catch(Exception e) {
					convLogger.setLogger(new ConsoleLogger());
				}
			}
			else {
				convLogger.setLogger(new ConsoleLogger());
			}
			
			convLogger.start();
			
			telegram = TelegramBotAdapter.build(AppsPropertiy.getInstance().getProperty("telegram.token"));
			
			telegram.setUpdatesListener(this);
		} catch(Exception e) {
			e.printStackTrace();
			rs = false;
		}
		return rs;
	}
	
	public void run() {
		if (!prepare()) {
			System.out.println("@.@ System error");
			return;
		}
		System.out.println("@.@ Ready");
		
		boolean running = true;
		try {
			while(running) {
				CHAT msg = queue.poll(500, TimeUnit.MICROSECONDS);
				if(msg != null) {
					receiveMessage(msg);
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			running = false;
		}
		convLogger.shutdown();
	}
	
	private void sendText(long chatID, String message) {
		SendMessage request = new SendMessage(chatID,message)
				.parseMode(ParseMode.Markdown)
				.disableWebPagePreview(true)
				.disableNotification(true);
		
		telegram.execute(request);
	}
	
	private void sendPhoto(long chatID, String url) {
		InputStream in = null;
		try {
			in = new URL(url).openStream();
			
			SendPhoto photo = new SendPhoto(chatID, IOUtils.toByteArray(in))
					.disableNotification(true);
			
			telegram.execute(photo);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	private void receiveMessage(CHAT msg) {
		
		System.out.println("hihihi");
		try {
			if("/start".equals(msg.message)) {
				session.getContext().clear();
				appFrame.message(session, "");
			}
			else
				appFrame.message(session, msg.message);
			
			convLogger.addDialog(session);
			
			StringBuffer resText = new StringBuffer();
			List<String> list = session.getOutputString();
			for(int i=0; i<list.size();i++) {
				resText.append(list.get(i));
				if (i<list.size()-1)
					resText.append("<br>");
			}
			
			sendText(msg.chatID,resText.toString());
			
			if(session.getPostResult() != null) {
				String url = 
						session.getPostResult().getAsJsonObject().get("PROD_IMAGE").getAsString();
				sendPhoto(msg.chatID, url);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			sendText(msg.chatID, ex.getMessage());
		}
	}
	
	public int process(List<Update> updates) {
		Message msg = updates.get(0).message();
		if(msg.text() != null) {
			try {
				CHAT c = new CHAT();
				c.chatID = msg.chat().id();
				c.message = msg.text();
				
				queue.put(c);
			}catch (InterruptedException e) {}
		}
		return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}

	public static void main(String[] args) {
		CoffeeBot bot = new CoffeeBot();
		bot.start();
		
		try {
			bot.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}

	}

}
