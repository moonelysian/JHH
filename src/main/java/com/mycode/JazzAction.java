package com.mycode;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.klab.ctx.ConversationSession;
import com.klab.data.AdapterUtil;
import com.klab.data.IResultSet;
import com.klab.svc.BaseAction;
import com.mycode.WeatherAction;

public class JazzAction extends BaseAction {
	
	private static final String SEOUL_ID = "1835847";
	
	protected Object execute(String actionId, Map params, ConversationSession session)
	{
		final Map<String, String> exeResult = new HashMap<String, String>();
		
		WeatherAction wa = new WeatherAction();		
		String str = wa.getWeather(SEOUL_ID).get("TEMP").toString();
		float temp = Float.parseFloat(str);
		
		exeResult.put("TEMP",str);
		
		try {
			if("JAZZ".equals(actionId)) {
				StringBuffer sql = new StringBuffer();
				
				if(temp>5) {
					
					sql.append("SELECT	TITLE ");
					sql.append("FROM	JAZZ ");
					sql.append("WHERE	ID=1");
					
					
				}else if(temp<=5 && temp>2) {
					
					sql.append("SELECT	TITLE ");
					sql.append("FROM	JAZZ ");
					sql.append("WHERE	ID=2");
					
				}else if(temp<=2) {
					
					sql.append("SELECT	TITLE ");
					sql.append("FROM	JAZZ ");
					sql.append("WHERE	ID=3");
					
				}
				
				Object [] prm = new Object[0];
						
				AdapterUtil.executeQuery(sql.toString(), prm, new IResultSet()
				{
					@Override
					public void fetch(ResultSet rs)
					{
						try{
							if(rs.next()) {
								exeResult.put("JAZZ",rs.getString("TITLE"));
							}
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				});

			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(exeResult);
		
		return exeResult;
	}
}
