package edu.cmu.iccb;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.cmu.iccb.services.ImageService;

@Controller
public class HomeController {
	
    private ImageService imageService;
    //Variable to check whether or not the user is authorized
    private static boolean AUTHORIZED = false;
    //State of the application
    private static final String APP_STATE = "5ca75bd30";
    @Autowired
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

    public ImageService getImageService() {
		return imageService;
	}

    @RequestMapping(method = RequestMethod.GET, value = "/images")
    public String provideUploadInfo(@RequestParam(value="code", required=false, defaultValue="World") String code,
    		@RequestParam(value="state", required=false, defaultValue="World") String state,
    		Model model, RedirectAttributes redirectAttributes) {
    	
    	//GET and POST requests to GitHub
    	String response="";
    	if (code!=null & state!=null){
    		//Verify if the state received is the same we sent
    		if (!state.equals(APP_STATE)){
    			HomeController.AUTHORIZED = false;
    		} 
    		//Send post request to GitHub
    		response = connectionOAuth(code);
    	}
    	//Verify if we received some response
    	if(!response.equals("")){
    		//Check if we received an access token to authorize the user
    		if(response.contains("access_token"))
    			HomeController.AUTHORIZED=true;
    		else
    			HomeController.AUTHORIZED=false;
    		
    	}
    	//Check if user is authorized
    	if(!HomeController.AUTHORIZED){    
    		//Redirecting to GitHub login
    		return ("redirect:https://github.com/login/oauth/authorize?response_type=code&client_id=4804159c241bbc370c84&state=5ca75bd30");
    	}else{
    		//Show the gallery
    		List<String> imageIds = imageService.getUploadedImages();        
	        model.addAttribute("files", imageIds);     
	        return "uploadForm";
    	}
    }
    //Method to send the POST request to github, returns the response
    private String connectionOAuth(String code){
    	URL url;
		try {
			//Code based on http://stackoverflow.com/questions/4205980/java-sending-http-parameters-via-post-method-easily
			url = new URL("https://github.com/login/oauth/access_token");
			//Setting parameters
	        Map<String,Object> params = new LinkedHashMap<String, Object>();
	        params.put("client_id", "4804159c241bbc370c84");
	        params.put("client_secret", "9ccdd15691c260cba76acefa0485146db20a4368");
	        params.put("code", code);        
	        StringBuilder postData = new StringBuilder();
	        //Formating parameters
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	        //Setting the connection
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        //Receiving response
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        String response="";
	        for (int c; (c = in.read()) >= 0;){
	        	response=response.concat(Character.toString((char)c));
	            System.out.print((char)c);
	        }
	        System.out.println("");
	        
	        return response;
	    } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/images")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String name = file.getOriginalFilename();

        try {        	
        	imageService.saveImage(name, file.getInputStream());           
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", name + " failed to upload");
        }

        return "redirect:/images";
    }

    
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String loginForm(Model model, RedirectAttributes redirectAttributes) {   
        return "login";
    }
    
    @RequestMapping("/login")
    public String gitHubLogin(Model model, RedirectAttributes redirectAttributes) {   
    	
        return "redirect:www.google.com";
    }
}
