package com.mycode;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.klab.ctx.ConversationSession;
import com.klab.data.AdapterUtil;
import com.klab.data.IResultSet;
import com.klab.svc.BaseAction;


public class FavorAction extends BaseAction {
	@Override
	protected Object execute(String actionId, Map params , ConversationSession session)
	{
		Object exeResult = null;
		
		System.out.println(session.getInputString());
		
		try
		{
			if ( "FAVOR".equals(actionId) )
			{
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT	NAME ");
				sql.append("FROM	PEOPLE ");
				sql.append("WHERE	FAVOR = ?");
				
				final Map<String, String> result = new HashMap<String, String>();
				
				Object [] prm = new Object[1];
				prm[0] = params.get("favor");
				
				final List <String> nameList = new ArrayList();
				
				AdapterUtil.executeQuery(sql.toString(), prm, new IResultSet()
				{
					@Override
					public void fetch(ResultSet rs)
					{
					
						try{
						while( rs.next() )
							{
								nameList.add(rs.getString("NAME"));
							}
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				
				String str = "";
				for(String s:nameList)
					str += (s+" ");
				
				result.put("PEOPLE", str);
				exeResult = result;				
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		return exeResult;
	}
}
