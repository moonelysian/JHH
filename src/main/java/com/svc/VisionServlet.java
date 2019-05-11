package com.svc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;
import com.klab.svc.AppsPropertiy;

/**
 * @author 최의신
 * 
 */
@WebServlet(
		asyncSupported = true,
		loadOnStartup = 1, 
		urlPatterns = {"/visionSVC"}
		)
@MultipartConfig(fileSizeThreshold=1024*1024*5, // 1MB
                 maxFileSize=1024*1024*10,      // 10MB
                 maxRequestSize=1024*1024*50)   // 50MB
public class VisionServlet extends HttpServlet
{
	private VisualRecognition service;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
		service.setApiKey(AppsPropertiy.getInstance().getProperty("vr.key"));
	}
	
	/**
	 * 파일명을 추출한다.
	 * 
	 * @param part
	 * @return
	 */
	private String extractFileName(Part part)
	{
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		
		return null;
	}
	
    /**
     * @param result
     * @param image
     */
    private void userClassifier(JsonObject result, Part image)
    {
    	try
    	{
			String filename = extractFileName(image);
			if ( filename == null ) filename = "dummy.jpg";
			
			ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
				    .images(IOUtils.toByteArray(image.getInputStream()), filename)
				    .classifierIds(AppsPropertiy.getInstance().getProperty("vr.classifier"))
				    .threshold(0)
				    .build();
				
			VisualClassification regResult = service.classify(options).execute();
			
			JsonArray classes = new JsonArray();
			
			List<ImageClassification> imgCls = regResult.getImages();
			if ( imgCls != null )
			{
				for(ImageClassification ic : imgCls)
				{
					List<VisualClassifier> vc = ic.getClassifiers();
					for(VisualClassifier v : vc)
					{
						List<VisualClass> vss = v.getClasses();
						for(VisualClass one : vss)
						{
							JsonObject c = new JsonObject();
							c.addProperty("class", one.getName());
							c.addProperty("score", one.getScore());
							
							classes.add(c);
						}
					}
				}
			}			
			
    		result.addProperty("returnCode", "SUCCESS");
    		result.addProperty("returnMessage", "OK!");
    		
    		result.add("result", classes);
    		
    	}catch(Exception ex) {
    		result.addProperty("returnCode", "FAIL");
    		result.addProperty("returnMessage", ex.getMessage());
    	}
    }
    
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		JsonObject result = new JsonObject();
		
		String func = request.getParameter("func");
		Part image = request.getPart("image");
		
		if ( image != null )
		{
	    	if ( "USR_CLS".equals(func) )
	    	{
	    		userClassifier(result, image);
	    	}
	    	else {
	    		result.addProperty("returnCode", "FAIL");
	    		result.addProperty("returnMessage", "정의되지 않은 기능입니다.");
	    	}
		}
		else {
			result.addProperty("returnCode", "FAIL");
    		result.addProperty("returnMessage", "이미지 파일이 없습니다.");
		}
    	
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().print(result.toString());
	}
}