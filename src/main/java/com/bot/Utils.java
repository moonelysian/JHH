package com.bot;

/**
 * @author 최의신 (choies@kr.ibm.com)
 *
 */
public class Utils
{
    /**
     * 지정된 이름의 클래스를 로드한다.
     *
     * @param className 로그할 클래스 이름
     * @return 로드한 클래스 인스턴
     * @throws Exception className이 null 이거나, 지정된 클래스를 로드할 수 없는 경
     *
     */
    public static Object loadClass(String className) throws Exception {
        if (className == null)
            throw new Exception("className = null");

        Object o = null;

        try {
            o = Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return o;
    }
}
