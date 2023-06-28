package com.cornejo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException; 

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource; 
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cornejo.config.MyUserDetails;
 

@Controller
@RequestMapping("/contacto")
public class ContactController {
	
	@Autowired
	private JavaMailSender mail;
	
	
	@GetMapping("/contac")
	public String formularioContact(@AuthenticationPrincipal MyUserDetails user,Model model ) {
		model.addAttribute("user", user); 
		return "formContact";
	}
	
	
	
	
	
	@PostMapping("/contac")
	public String submitContact( HttpServletRequest request,@RequestParam("file")MultipartFile multi ) throws MessagingException, UnsupportedEncodingException {
    	String name=request.getParameter("nombre");
    	String email=request.getParameter("email");
    	String asunto=request.getParameter("asunto"); 
    	String content=request.getParameter("content");
    	
    	MimeMessage mensaje= mail.createMimeMessage();
    	MimeMessageHelper helper=new MimeMessageHelper(mensaje, true);
    	 
    	String mailEmisor=name+"ha enviado un mensaje";
    	String mailContenido= "<p><b>El usuario:</b> "+name +"</p>";
    	mailContenido+="<p><b>Identificacion E-mail:</b>"+ email +"</p>";
    	mailContenido+="<p><b>Asunto:</b>"+ asunto +"</p>";
    	mailContenido+="<p><b>Contenido:</b>"+ content +"</p>";
    	mailContenido+="<hr><img src='https://firebasestorage.googleapis.com/v0/b/applogin-51d51.appspot.com/o/Imagenes%2Flogoem.jpg?alt=media&token=a4668753-d647-4a4d-8bee-9d912e73fb53&_gl=1*y6n2pa*_ga*OTA3Mjk2NzgyLjE2ODYxMjQ4OTY.*_ga_CW55HF8NVT*MTY4NjEyNDkwMi4xLjEuMTY4NjEyNDk3MC4wLjAuMA' style='width: 260px;'  />";
    	
    	helper.setFrom("contact@appEmpleos.com","AppEmpleos_Contact");
    	helper.setTo("luciaturante2000@gmail.com"); 
    	helper.setSubject(mailEmisor);
    	helper.setText(mailContenido , true);
    	
    	if(!multi.isEmpty()) {
    		String fileName=StringUtils.cleanPath(multi.getOriginalFilename());
    		InputStreamSource source=new InputStreamSource() {
				
				@Override
				public InputStream getInputStream() throws IOException {
					// TODO Auto-generated method stub
					return multi.getInputStream();
				}
			};
			helper.addAttachment(fileName, source);
    	}
    	
    	mail.send(mensaje);
    	

		return "message";
	}

}
