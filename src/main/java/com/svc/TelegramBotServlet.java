package com.svc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.klab.ctx.ConversationSession;
import com.klab.ctx.SessionManager;
import com.klab.svc.AppsPropertiy;
import com.klab.svc.ConsoleLogger;
import com.klab.svc.ConversationLogger;
import com.klab.svc.ILogger;
import com.klab.svc.SimpleAppFrame;
import com.klab.svc.Utils;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;

/**
 * @author 최의신
 *
 */
@WebServlet(
		asyncSupported = true,
		loadOnStartup = 1, 
		urlPatterns = {"/telegramSVC"}
		)
public class TelegramBotServlet extends HttpServlet
{
	private SimpleAppFrame appFrame;
	private ConversationLogger convLogger;
	
	private TelegramBot telegramBot;
	
    /* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		telegramBot = TelegramBotAdapter.build(AppsPropertiy.getInstance().getProperty("telegram.token"));
		
		SetWebhook request = new SetWebhook().url("https://" + AppsPropertiy.getInstance().getProperty("telegram.host") + ".mybluemix.net/telegramSVC");

		/*
		 * 비동기 방식
		 */
		telegramBot.execute(request, new Callback<BaseRequest, BaseResponse>() {
			@Override
			public void onResponse(BaseRequest request, BaseResponse response) {
				System.out.println("@.@ 등록 성공!!!");
			}

			@Override
			public void onFailure(BaseRequest request, IOException e) {
				System.out.println("@.@ 등록 실패!!!");
			}
		});
		
		appFrame = new SimpleAppFrame();
		appFrame.setUsername(AppsPropertiy.getInstance().getProperty("wcs.user"));
		appFrame.setPassword(AppsPropertiy.getInstance().getProperty("wcs.passwd"));
		appFrame.setWorkspaceId(AppsPropertiy.getInstance().getProperty("wcs.workid"));

		/*
		 * 대화를 저장할 로거를 생성
		 */
		convLogger = new ConversationLogger();
		
		String logger = AppsPropertiy.getInstance().getProperty("logger.className");
		if ( logger != null && logger.length() > 0 )
		{
			try {
				convLogger.setLogger((ILogger)Utils.loadClass(logger));
			} catch (Exception e) {
				convLogger.setLogger(new ConsoleLogger());
			}
		}
		else {
			convLogger.setLogger(new ConsoleLogger());
		}
		
		convLogger.start();		
	}
	
	/**
	 * 수신된 메시지를 처리한다.
	 * 
	 * @param chatId
	 * @param text
	 * @param session
	 */
	@SuppressWarnings("unchecked")
	private void receiveMessage(long chatId, String text, ConversationSession session)
	{
		try
		{
			if ( "/start".equals(text) ) {
				session.getContext().clear();
				appFrame.message(session, "");
			}
			else
				appFrame.message(session, text);

			/*
			 * 대화 이력을 저장한다.
			 */
			if ( convLogger != null )
				convLogger.addDialog(session);
			
			StringBuffer resText = new StringBuffer();
			List<String> list = session.getOutputString();
			for(int i = 0; i < list.size(); i++)
			{
				resText.append(list.get(i));
				if ( i < list.size()-1 )
					resText.append("<br>");
			}

			sendText(chatId, resText.toString());
			
			// 사진
			if ( session.getPostResult() != null )
			{
				JsonObject obj = session.getPostResult().getAsJsonObject();
				if (obj.has("PROD_IMAGE")) {
					String url = obj.get("PROD_IMAGE").getAsString();
					sendPhoto(chatId, url);
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			sendText(chatId, ex.getMessage());
		}		
	}	
	
	/**
	 * 텍스트를 텔레그램으로 전송한다.
	 * 
	 * @param chatId CHAT ID
	 * @param message 문자열 메시지
	 */
	private void sendText(long chatId, String message)
	{
		SendMessage request = new SendMessage(chatId, message)
		        .parseMode(ParseMode.Markdown)
		        .disableWebPagePreview(true)
		        .disableNotification(true);

		telegramBot.execute(request);
	}
	
	/**
	 * 이미지를 텔레그램으로 전송한다.
	 * 
	 * @param chatId CHAT ID
	 * @param url 이미지 URL
	 */
	private void sendPhoto(long chatId, String url)
	{
		InputStream in = null;
		try {
			in = new URL( url ).openStream();
			
			SendPhoto photo = new SendPhoto(chatId, IOUtils.toByteArray(in))
			        .disableNotification(true);
			
			telegramBot.execute(photo);
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}	
	
	/* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String conType = request.getContentType();
        
        if ( "application/json".equals(conType) )
        {
        	Update update = BotUtils.parseUpdate(request.getReader());

        	Message msg = update.message();
        	
        	if ( msg != null && msg.text() != null )
        	{
        		ConversationSession session = SessionManager.getInstance().getSession(String.valueOf(msg.chat().id()));
        		
        		receiveMessage(msg.chat().id(), msg.text(), session);
        	}
        	else if ( msg != null && msg.photo() != null )
        	{
            	System.out.println("@.@ Photo");
        	}
//        	else if ( msg.audio() != null )
//        	{
//            	System.out.println("@.@ Audio");
//        		msgType = "AUDIO를 수신";
//        	}
//        	else if ( msg.video() != null )
//        	{
//            	System.out.println("@.@ Video");
//        		msgType = "VIDEO를 수신";
//        	}
        	else {
                System.err.println("@.@ 알수없는 오류!!!!");
        	}
        }

        /*
         * 응답
         */
        response.setContentType("application/json; charset=utf-8");
        JSONObject payload = new JSONObject();
        response.getWriter().print(payload.toString());
    }
}