package com.bot;

import com.klab.ctx.ConversationSession;
import com.klab.svc.ILogger;

/**
 * @author 최의신 (choies@kr.ibm.com)
 * 
 * 대화 로그를 콘솔로 출력한다.
 *
 */
public class ConsoleLogger implements ILogger
{
	@Override
	public void insertLog(ConversationSession session)
	{
		System.out.println("==[OUTPUT]");
		for(int i =0 ; i < session.getOutputString().size() ; i++)
			System.out.println(session.getOutputString().get(i));

		if ( session.getPostResult() != null )
		{
			System.out.println("==[POST RESULT]");
			System.out.println(session.getPostResult().toString());
		}
	}

}
