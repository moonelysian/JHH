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

public class InfoAction extends BaseAction{
	protected Object execute(String actionId,final Map params , ConversationSession session)
	{
		Object exeResult = null;
		
		String[] input = session.getInputString().split(" ");
		String name = input[0];
		
		try
		{
			if ( "INFO".equals(actionId) )
			{
				StringBuffer sql = new StringBuffer();
				
				if("P_NUMBER".equals(params.get("info_type"))) {
				
				sql.append("SELECT	P_NUMBER ");
				sql.append("FROM	PEOPLE ");
				sql.append("WHERE	NAME = ?");
				
				}
				
				else if("FAVOR".equals(params.get("info_type"))){
					sql.append("SELECT	FAVOR ");
					sql.append("FROM	PEOPLE ");
					sql.append("WHERE	NAME = ?");
				}
				
				final Map<String, String> result = new HashMap<String, String>();
				
				Object [] prm = new Object[1];
				prm[0] = name;
				
				AdapterUtil.executeQuery(sql.toString(), prm, new IResultSet()
				{
					@Override
					public void fetch(ResultSet rs)
					{
					
						try{
						if( rs.next() )
							{
							result.put("INFO", rs.getString(params.get("info_type").toString()));
							}
						}catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				exeResult = result;				
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		return exeResult;
	}
}
