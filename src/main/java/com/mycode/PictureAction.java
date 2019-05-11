package com.mycode;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.klab.ctx.ConversationSession;
import com.klab.data.AdapterUtil;
import com.klab.data.IResultSet;
import com.klab.svc.BaseAction;

/**
 * @author 최의신 (choies@kr.ibm.com)
 *
 */
@SuppressWarnings("rawtypes")
public class PictureAction extends BaseAction
{
	/* (non-Javadoc)
	 * @see com.klab.svc.BaseAction#execute(java.lang.String, java.util.Map)
	 */
	@Override
	protected Object execute(String actionId, Map params, ConversationSession session)
	{
		Object exeResult = null;
		
		try
		{
			if ( "SELECT_PICTURE".equals(actionId) )
			{
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT	PROD_IMAGE ");
				sql.append("FROM	COFFEE_PIC ");
				sql.append("WHERE	PROD_CD = ?");
				
				final Map<String, String> result = new HashMap<String, String>();
				
				Object [] prm = new Object[1];
				prm[0] = params.get("prodCd");
				
				AdapterUtil.executeQuery(sql.toString(), prm, new IResultSet()
				{
					@Override
					public void fetch(ResultSet rs)
					{
						try{
							if ( rs.next() )
							{
								result.put("PROD_IMAGE", rs.getString("PROD_IMAGE"));
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
