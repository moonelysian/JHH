package com.svc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.klab.ctx.ConversationSession;
import com.klab.ctx.SessionManager;
import com.klab.svc.AppsPropertiy;
import com.klab.svc.ConsoleLogger;
import com.klab.svc.ConversationLogger;
import com.klab.svc.ILogger;
import com.klab.svc.SimpleAppFrame;
import com.klab.svc.Utils;

/**
 * @author 최의신
 *
 */
@WebServlet(
		asyncSupported = true,
		loadOnStartup = 1, 
		urlPatterns = {"/kakaoSVC/*"}
		)
public class KakaoServlet extends HttpServlet
{
	private SimpleAppFrame appFrame;
	private ConversationLogger convLogger;

	
    /* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

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
     *  /keyboard 요청을 처리한다.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	JsonObject res = new JsonObject();
    	
    	res.addProperty("type", "buttons");
    	
    	JsonArray buts = new JsonArray();
    	buts.add("시작");
    	
    	res.add("buttons", buts);
    	
    	/*
    	 * 응답생성
    	 */
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().print(res.toString());
    }
    
	
	/**
	 *  /message 요청을 처리한다.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
       	JsonObject res = new JsonObject();
    	JsonObject msg = new JsonObject();
    	res.add("message", msg);
		
    	String req = IOUtils.toString(request.getInputStream(), "utf-8");
    	if ( req != null && req.length() > 0 )
    	{
    		try
    		{
            	JsonParser jp = new JsonParser();
            	JsonObject json = jp.parse(req).getAsJsonObject();

            	String userKey = json.get("user_key").getAsString();
            	String type = json.get("type").getAsString();
            	String text = json.get("content").getAsString();
            	
    			ConversationSession session = SessionManager.getInstance().getSession(userKey);
    			
    			if ( "text".equals(type) )
    			{
    				if ( "시작".equals(text) ) {
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

                	msg.addProperty("text", resText.toString());
    				
    				// 사진
    				if ( session.getPostResult() != null )
    				{
    					JsonObject obj = session.getPostResult().getAsJsonObject();
    					if (obj.has("PROD_IMAGE")) {
        					JsonObject photo = new JsonObject();
        					photo.addProperty("url", obj.get("PROD_IMAGE").getAsString());
        					photo.addProperty("width", 100);
        					photo.addProperty("height", 100);
        					
        	            	msg.add("photo", photo);
    					}
    				}
    			}

    		}catch(Exception ex) {
    			msg.addProperty("text", ex.getMessage());
    		}
    	}
    	else {
			msg.addProperty("text", "입력 내용이 없습니다.");
    	}

    	/*
    	 * 응답생성
    	 */
        response.setContentType("application/json; charset=utf-8");
        Gson gson = new Gson();
        response.getWriter().print(gson.toJson(res));
	}
}