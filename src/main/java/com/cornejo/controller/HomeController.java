package com.cornejo.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cornejo.config.MyUserDetails;
import com.cornejo.model.Perfil;
import com.cornejo.model.Usuario;
import com.cornejo.model.Vacante;
import com.cornejo.service.ICategoriasService;
import com.cornejo.service.IUsuariosService;
import com.cornejo.service.IVacantesService;
import com.cornejo.service.db.UsuarioNotFoundException;
import com.cornejo.util.Utileria;

import net.bytebuddy.utility.RandomString;
 

@Controller
public class HomeController {
	
	@Autowired
	private ICategoriasService serviceCategorias;
	
	@Autowired
	private JavaMailSender mail;
	
	// Inyectamos una instancia desde nuestro ApplicationContext
    @Autowired
	private IVacantesService serviceVacantes;
    
    @Autowired
   	private IUsuariosService serviceUsuarios;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
  
	@GetMapping("/")
	public String mostrarHome() {
		return "home";
	}
	
	/**
	 * Método que esta mapeado al botón Ingresar en el menú
	 * @param authentication
	 * @param session
	 * @return
	 */
	@GetMapping("/index")
	public String mostrarIndex(Authentication authentication, HttpSession session) {		
		
		// Como el usuario ya ingreso, ya podemos agregar a la session el objeto usuario.
		String username = authentication.getName();		
		
		for(GrantedAuthority rol: authentication.getAuthorities()) {
			System.out.println("ROL: " + rol.getAuthority());
		}
		
		if (session.getAttribute("usuario") == null){
			Usuario usuario = serviceUsuarios.buscarPorUsername(username);	
			//System.out.println("Usuario: " + usuario);
			session.setAttribute("usuario", usuario);
		}
		
		return "redirect:/";
	}
	
	
	 
	@GetMapping("/usuhome")
	public String mostrarPerfil( @AuthenticationPrincipal MyUserDetails user,Model model ) {		
		model.addAttribute("user", user); 
		
		return "user_home";
	}
	/**
	 * Método que muestra el formulario para que se registren nuevos usuarios.
	 * @param usuario
	 * @return
	 */
	@GetMapping("/signup")
	public String registrarse(Usuario usuario) {
		return "formRegistro";
	}
	
	/**
	 * Método que guarda en la base de datos el usuario registrado
	 * @param usuario
	 * @param attributes
	 * @return
	 * @throws MessagingException 
	 * @throws UnsupportedEncodingException 
	 */
	@PostMapping("/signup")
	public String guardarRegistro(Usuario usuario, RedirectAttributes attributes,HttpServletRequest request,Model model) throws UnsupportedEncodingException, MessagingException {
		// Recuperamos el password en texto plano
		String pwdPlano = usuario.getPassword();
		// Encriptamos el pwd BCryptPasswordEncoder
		String pwdEncriptado = passwordEncoder.encode(pwdPlano); 
		// Hacemos un set al atributo password (ya viene encriptado)
		usuario.setPassword(pwdEncriptado);	
		 usuario.setEstatus(0); // Activado por defecto
		usuario.setFechaRegistro(new Date()); // Fecha de Registro, la fecha actual del servidor
		
		// Creamos el Perfil que le asignaremos al usuario nuevo
		Perfil perfil = new Perfil();
		perfil.setId(3); // Perfil USUARIO
		usuario.agregar(perfil);
		
		System.out.println(perfil);
		 
		/**
		 * Guardamos el usuario en la base de datos. El Perfil se guarda automaticamente
		 */
		serviceUsuarios.guardar(usuario);
		String sitio=Utileria.generarSitioUrl(request);
		serviceUsuarios.verificarCuenta(usuario,sitio);
		 
				
		//attributes.addFlashAttribute("msg", "Has sido registrado. ¡Ahora verifica tu email!");
		
		return "verfication";
	}
	
	@GetMapping("/verify")
	public String verificarCuenta(@Param("code")String code,Model model)   {
		boolean verificacion=serviceUsuarios.verificacion(code);
		String pageTitle=verificacion?"Verificacion Exitosa!!":"Verificacion Fallida";
		model.addAttribute("pageTitle",pageTitle);
		return "register/" + (verificacion?"veri_exito":"veri_fallo");
	}
	
	
	
	
	@GetMapping("/recuperar")
	public String recuperarClave( Model model)   { 
		model.addAttribute("pageTitle", "Recuperacion Contraseña");
		
		return "recuperar_Clave";
	}
	
	@PostMapping("/recuperar")
	public String recuperarClave2( HttpServletRequest request,Model model)   { 
		String email=request.getParameter("email");
		String token=RandomString .make(45);
		
		
		try {
			serviceUsuarios.recuperarClave( token,email);
			//mandar email
			String resetLink=Utileria.generarSitioUrl(request)+"/reset?token=" +token; 
			enviarCorreo(email,resetLink);
			model.addAttribute("message","Y se ha mandadno el correo con el link de recuperacion");
			
		} catch (UsuarioNotFoundException e) {
			model.addAttribute("error",e.getMessage());
		} catch (UnsupportedEncodingException |MessagingException e) {
			model.addAttribute("error","Error al enviar el email:"+e.getMessage());
		} 
		
		System.out.println("Email:"+email);
		System.out.println("Token:"+token);

		 return "recuperar_Clave";
	}
	
	 
	private void enviarCorreo(String email, String resetLink) throws UnsupportedEncodingException, MessagingException {
		MimeMessage mensaje=mail.createMimeMessage();
		MimeMessageHelper helper= new MimeMessageHelper(mensaje);
		helper.setFrom("contact@appEmpleos.com", "Equipo EmmpleosApp");
		helper.setTo(email);
		String asunto="Ingresa al link para restablecer tu contraseña";
		 
		
		String content="<p>Hola,</p>";
		content+="<p>Ya puedes recuperar tu contraseñaa.. Has click en el siguiente link: </p>"; 
		content+="<h3><a href=\""+resetLink+"\">Cambiar contraseña</a></h3>"; 
		content+="<p>Ignora el email si ya recuperaste tu contraseña</p>";
		
		helper.setSubject(asunto);
	    helper.setText(content, true); // Establecer como HTML
		mail.send(mensaje);
	}
	
	
	
	@GetMapping("/reset")
	public String resetear(@Param(value="token") String token, Model model )   { 
		 
		 Usuario usu=serviceUsuarios.claveReset(token) ;
		 if(usu==null) {
			 model.addAttribute("title", "RESETEAR CLAVE");
			 model.addAttribute("message", "Token invalido");
			 return "message2";
		 }
		 model.addAttribute("token", token);
		 return "cambiar_Clave";
	}
	
	
	@PostMapping("/reset")
	public String resetearForm(HttpServletRequest request,Model model)   { 
		 
		 String token=request.getParameter("token");
		 String contra=request.getParameter("password");
		 Usuario usu=serviceUsuarios.claveReset(token) ;
		 
		 if(usu==null) {
			 model.addAttribute("title", "RESETEAR CLAVE");
			 model.addAttribute("message", "Token invalido");
			 return "message2";
		 }else {
			 serviceUsuarios.updateClave(usu, contra);
			 model.addAttribute("message", "Ya se ha actualizado su contraseña");
		 }
		 return "message2";
	 
	}
	
	
	
	
	
	
	

	/**
	 * Método para realizar búsquedas desde el formulario de búsqueda del HomePage
	 * @param vacante
	 * @param model
	 * @return
	 */
	@GetMapping("/search")
	public String buscar(@ModelAttribute("search") Vacante vacante, Model model) {
		
		/**
		 * La busqueda de vacantes desde el formulario debera de ser únicamente en Vacantes con estatus 
		 * "Aprobada". Entonces forzamos ese filtrado.
		 */
		vacante.setEstatus("Aprobada");
		
		// Personalizamos el tipo de busqueda...
		ExampleMatcher matcher  = ExampleMatcher.matching().
			// and descripcion like '%?%'
			withMatcher("descripcion", ExampleMatcher.GenericPropertyMatchers.contains());
		
		Example<Vacante> example = Example.of(vacante, matcher);
		List<Vacante> lista = serviceVacantes.buscarByExample(example);
		model.addAttribute("vacantes", lista);
		return "home";
	}
	
	/**
	 * Metodo que muestra la vista de la pagina de Acerca
	 * @return
	 */
	@GetMapping("/about")
	public String mostrarAcerca() {			
		return "acerca";
	}
	
	/**
	 * Método que muestra el formulario de login personalizado.
	 * @return
	 */
	@GetMapping("/login")
	public String mostrarLogin() {
		return "formLogin";
	}
	
	/**
	 * Método personalizado para cerrar la sesión del usuario
	 * @param request
	 * @return
	 */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(request, null, null);
		return "redirect:/";
	}
	
	/**
     * Utileria para encriptar texto con el algorito BCrypt
     * @param texto
     * @return
     */
    @GetMapping("/bcrypt/{texto}")
    @ResponseBody
   	public String encriptar(@PathVariable("texto") String texto) {    	
   		return texto + " Encriptado en Bcrypt: " + passwordEncoder.encode(texto);
   	}
	
	/**
	 * Metodo que agrega al modelo datos genéricos para todo el controlador
	 * @param model
	 */
	@ModelAttribute
	public void setGenericos(Model model){
		Vacante vacanteSearch = new Vacante();
		vacanteSearch.reset();
		model.addAttribute("search", vacanteSearch);
		model.addAttribute("vacantes", serviceVacantes.buscarDestacadas());	
		model.addAttribute("categorias", serviceCategorias.buscarTodas());	
	}
	
	/**
	 * InitBinder para Strings si los detecta vacios en el Data Binding los settea a NULL
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
}
