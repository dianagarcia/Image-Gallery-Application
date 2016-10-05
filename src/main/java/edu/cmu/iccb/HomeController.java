package edu.cmu.iccb;


import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.cmu.iccb.services.ImageService;

@Controller
public class HomeController {
	
    private ImageService imageService;
    
    @Autowired
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

    public ImageService getImageService() {
		return imageService;
	}

    @RequestMapping(method = RequestMethod.GET, value = "/gallery")
    public String provideUploadInfo(Model model, RedirectAttributes redirectAttributes) {
        List<String> imageIds = imageService.getUploadedImages();        
        model.addAttribute("files", imageIds);     
        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/gallery")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String name = file.getOriginalFilename();
		System.out.println("file name *******************************************" +name);
        try {        	
        	imageService.saveImage(name, file.getInputStream());           
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", name + " failed to upload");
        }

        return "redirect:/gallery";
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String homePage() {    	
        return "login";
    }
    

    @RequestMapping(method = RequestMethod.GET, value = "/github/success")
    public String githubSuccess(RedirectAttributes redirectAttributes,
    					   @CookieValue(value = "JSESSIONID") String accessToken) {
    	
    	PreAuthenticatedAuthenticationToken auth = 
                new PreAuthenticatedAuthenticationToken("github", accessToken, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
            
        SecurityContextHolder.getContext().setAuthentication(auth);
              
    	return "redirect:/gallery";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/github/login")
    public String githubLogin(RedirectAttributes redirectAttributes,
    					   @CookieValue(value = "JSESSIONID") String accessToken) {
    		
    	// If we actuall make it to this code, we have been authenticated so redirect to images
    	return "redirect:/gallery";
    }
}
