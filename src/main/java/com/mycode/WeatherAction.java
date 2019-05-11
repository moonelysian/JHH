package com.mycode;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;


import com.klab.ctx.ConversationSession;
import com.klab.svc.BaseAction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * @author 최의신 (choies@kr.ibm.com)
 * 
 * 도시 정보는 다음의 사이트를 참조 한다.
 * 
 * http://bulk.openweathermap.org/sample/
 *
 */
@SuppressWarnings("rawtypes")
public class WeatherAction extends BaseAction
{
	private static final String API_KEY = "2cf4080f96fdb065c76beec757dee95a";
	private static final String SEOUL_ID = "1835847";
	private static final String BUSAN_ID = "1838524";

	private static final String URL = "http://api.openweathermap.org/data/2.5/weather";

	public Map<String, String> getWeather(String id)
	{
		String temp = "";
		String rain = "0";

		try {
			StringBuffer url = new StringBuffer();

			url.append(URL).append("?units=metric&id=").append(id);
			url.append("&APPID=").append(API_KEY);

			HttpResponse<JsonNode> jsonResponse = Unirest
					.post(url.toString())
					.asJson();

			JsonNode rs = jsonResponse.getBody();
			
			JSONObject main = rs.getObject().getJSONObject("main");
			JSONArray weather = rs.getObject().getJSONArray("weather");
			
			System.out.println(rs.toString());
			
			temp = main.get("temp").toString();
			
			for(int i=0; i<weather.length(); i++) {
				JSONObject r = weather.getJSONObject(i);
				if(r.get("main").toString().toUpperCase().indexOf("RAIN")!=-1)
					rain = "rain";
				else
					rain=r.get("main").toString();
			}
			
			System.out.println(rain);

			
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		Map<String, String> weather_info = new HashMap<>();
		weather_info.put("TEMP", temp);
		weather_info.put("RAIN", rain);
		return weather_info;
	}


	/* (non-Javadoc)
	 * @see com.klab.svc.BaseAction#execute(java.lang.String, java.util.Map)
	 */
	@Override
	protected Object execute(String actionId, Map params , ConversationSession session)
	{
		Object exeResult = null;

		try
		{
			Map<String, String> map = new HashMap<String, String>();
			exeResult = map;

			Object city = params.get("city");

			if ( "seoul".equals(city) )
			{	
				map.putAll(getWeather(SEOUL_ID));
			}
			else if ( "busan".equals(city) )
			{
				map.putAll(getWeather(BUSAN_ID));
			}
			else {
				map.put("TEMP", "---");
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		return exeResult;
	}
}
