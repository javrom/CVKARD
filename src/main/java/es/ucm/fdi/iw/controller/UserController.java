package es.ucm.fdi.iw.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.User;

@Controller	
@RequestMapping("user")
public class UserController {
	
	private static Logger log = Logger.getLogger(UserController.class);
	
	@Autowired
	private LocalData localData;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * Returns a users' photo
	 * @param id of user to get photo from
	 * @return
	 */
	@RequestMapping(value="/photo/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	public void userPhoto(@PathVariable("id") String id, 
			HttpServletResponse response) {
	    File f = localData.getFile("user", id);
	    InputStream in = null;
	    try {
		    if (f.exists()) {
		    	in = new BufferedInputStream(new FileInputStream(f));
		    } else {
		    	in = new BufferedInputStream(
		    			this.getClass().getClassLoader().getResourceAsStream("unknown-user.jpg"));
		    }
	    	FileCopyUtils.copy(in, response.getOutputStream());
	    } catch (IOException ioe) {
	    	log.info("Error retrieving file: " + f + " -- " + ioe.getMessage());
	    }
	}
	
	/**
	 * Uploads a photo for a user
	 * @param id of user 
	 * @param photo to upload
	 * @return
	 */
	@RequestMapping(value="/photo/{id}", method=RequestMethod.POST)
    public @ResponseBody RedirectView handleFileUpload(@RequestParam("photo") MultipartFile photo,
    		@PathVariable("id") String id){
        if (!photo.isEmpty()) {
            try {
                byte[] bytes = photo.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(
                        		new FileOutputStream(localData.getFile("user", id)));
                stream.write(bytes);
                stream.close();
                return new RedirectView("/perfilusuario?avatar=actualizado+con+exito");
            } catch (Exception e) {
                return new RedirectView("/perfilusuario?avatar=fallo+al+actualizar+el+avatar");
            }
        } else {
        	return new RedirectView("/perfilusuario?avatar=archivo+vacio");
        }
	}
	/**
	 * Uploads a photo for a user
	 * @param id of user 
	 * @param photo to upload
	 * @return
	 */
	@RequestMapping(value="/updateInfoUser", method=RequestMethod.POST)
	@ResponseBody
	@Transactional // needed to allow DB change
    public RedirectView updateEmployee(
    		@RequestParam("dni-form") String DNI,
			@RequestParam("name-form") String name,
			@RequestParam("lastName-form") String lastName,
			@RequestParam("birthday-form") String birthday,
			HttpServletRequest request, HttpServletResponse response, 
			Model model, HttpSession session){
		log.info("dentro del update\n dni = "+DNI+"\n name = "+name+"\nlastName = "+lastName+"\nbirthday = "+birthday+"\n");
		try {
			User u = (User) session.getAttribute("user");
			u.setName(name);
			u.setLastName(lastName);
			u.setDNI(DNI);
			u.setBirthday(birthday);
			entityManager.merge(u);
			session.removeAttribute("user");
			session.setAttribute("user", u);
			response.setStatus(HttpServletResponse.SC_OK);
			return new RedirectView("/perfilusuario?update=tus+datos+se+actualizaron+correctamente");
		} catch (NoResultException nre) {
			log.error("fallo al encontrar el usuario para actualizar\n");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new RedirectView("/perfilusuario?update=false");
		}	
	}
	
	@RequestMapping(value="/updateInfoBusiness", method=RequestMethod.POST)
	@ResponseBody
	@Transactional // needed to allow DB change
	 public RedirectView updateBusiness(
	    		@RequestParam("email-form") String email,
				@RequestParam("name-form") String name,
				HttpServletRequest request, HttpServletResponse response, 
				Model model, HttpSession session){
			log.info("dentro del update\n email = "+email+"\n name = "+name+"\n");
			String updated = "error+no+se+ha+conseguido+update"; 
			try {
				User u = (User) session.getAttribute("user");
				u.setName(name);
				u.setEmail(email);
				session.setAttribute("user", u);
				entityManager.merge(u);
				response.setStatus(HttpServletResponse.SC_OK);
				updated = "se+ha+actualizado+con+exito"; 
			} catch (NoResultException nre) {
				log.error("fallo al encontrar el usuario para actualizar\n");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				
			}
			return new RedirectView("/perfilempresa?update="+ updated);
				
	}
	
	@RequestMapping(value="/updateCard", method=RequestMethod.POST)
	@ResponseBody
	@Transactional // needed to allow DB change
	 public RedirectView updateCard(
	    		@RequestParam("html_data") String html_data,
				HttpServletRequest request, HttpServletResponse response, 
				Model model, HttpSession session){
			String card = "error+no+se+ha+conseguido+actuaizar+la+presentacion"; 
			try {
				User u = (User) session.getAttribute("user");
				u.setCard(html_data);
				session.setAttribute("user", u);
				entityManager.merge(u);
				response.setStatus(HttpServletResponse.SC_OK);
				card = "se+ha+actualizado+con+exito"; 
			} catch (NoResultException nre) {
				log.error("fallo al encontrar el usuario para actualizar\n");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				
			}
			return new RedirectView("/perfilusuario?card="+ card);
				
	}
}