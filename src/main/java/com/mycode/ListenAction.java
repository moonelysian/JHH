package com.mycode;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.klab.ctx.ConversationSession;
import com.klab.data.AdapterUtil;
import com.klab.data.IResultSet;
import com.klab.svc.BaseAction;

public class ListenAction extends BaseAction
{
	protected Object execute(String actionId, Map params , ConversationSession session)
	{
		Object exeResult = null;
		
		try
		{
			System.out.println(params);
			StringBuffer sql = new StringBuffer();
			String title = params.get("title").toString();
			
			System.out.println(title);

			if ( "LISTEN".equals(actionId) ){	
				sql.append("SELECT	URL ");
				sql.append("FROM	JAZZ ");
				sql.append("WHERE	TITLE = ?");
				}
				
			final Map<String, String> result = new HashMap<String, String>();
				
			Object [] prm = new Object[1];
			prm[0] = title;
				
			AdapterUtil.executeQuery(sql.toString(), prm, new IResultSet()
			{
				@Override
				public void fetch(ResultSet rs)
				{
					try{
						if( rs.next() ){
							result.put("URL", rs.getString("URL"));
							}
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				exeResult = result;				
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return exeResult;
	}
}

