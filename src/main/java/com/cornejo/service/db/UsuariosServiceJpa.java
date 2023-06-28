package com.cornejo.service.db;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cornejo.model.Usuario;
import com.cornejo.repository.UsuariosRepository;
import com.cornejo.service.IUsuariosService;

import net.bytebuddy.utility.RandomString;
 

@Service
public class UsuariosServiceJpa implements IUsuariosService{

	@Autowired
	private UsuariosRepository usuariosRepo;
	@Autowired
	private JavaMailSender mail;
	
	@Override
	public Usuario guardar(Usuario usuario) {
		String randomCodigo=RandomString .make(64);
		usuario.setVerificacion_code(randomCodigo); 
		return usuariosRepo.save(usuario); 
	}

	@Override 
	public void verificarCuenta(Usuario usuario, String sitio) throws UnsupportedEncodingException, MessagingException { 
    	String verifica="Verifique su cuenta registrada recientemente!!";
    	String asunto= "Empleos_App" ;
    	String mailContenido="<p>Usuario:"+usuario.getNombre()+",</p>";
    	mailContenido+="<p>Por favor, ingrese al siguiente link para validar su registro: </p>";
    	String verificacion=sitio+"/verify?code=" +usuario.getVerificacion_code();
    	mailContenido+="<h3><a href=\""+verificacion+"\">VERIFICATE</a></h3>";
    	
    	mailContenido+="<p> Mucho gusto... <br> Equipo Empleos_App </p>";
    	 
		MimeMessage mensaje=mail.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mensaje);
		helper.setFrom("contact@appEmpleos.com", asunto);
		helper.setTo(usuario.getEmail());
		helper.setSubject(verifica);
		helper.setText(mailContenido, true);
		
		mail.send(mensaje);
				
	}
	
	@Override
	@Transactional 
	public boolean verificacion(String codigoVerificacion) {
		Usuario usu=usuariosRepo.getVerification(codigoVerificacion);
		if(usu==null  ) {
			return false;
		}else {
			usuariosRepo.unlock(usu.getId());
			return true;
		}
		
	}

	@Override
	public void eliminar(Integer idUsuario) {
		usuariosRepo.deleteById(idUsuario);
	}

	@Override
	public List<Usuario> buscarTodos() {
		return usuariosRepo.findAll();
	}

	@Override
	public Usuario buscarPorId(Integer idUsuario) {
		Optional<Usuario> optional = usuariosRepo.findById(idUsuario);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Override
	public Usuario buscarPorUsername(String username) {
		return usuariosRepo.findByUsername(username);
	}

	@Override
	public List<Usuario> buscarRegistrados() {		
		return usuariosRepo.findByFechaRegistroNotNull();
	}

	@Transactional
	@Override
	public int bloquear(int idUsuario) {
		int rows = usuariosRepo.lock(idUsuario);
		return rows;
	}

	@Transactional
	@Override
	public int activar(int idUsuario) {
		int rows = usuariosRepo.unlock(idUsuario);
		return rows;
	}

	@Override
	public Usuario buscarEmail(String username) {
		return usuariosRepo.getUserByUsername(username);
	}

	@Transactional
	@Override
	public void recuperarClave(String token, String email) throws UsuarioNotFoundException {
		Usuario usu=usuariosRepo.getUserByUsername(email);
		if (usu!=null){
			usu.setReset(token);
			usuariosRepo.save(usu);
		}else {
			throw new UsuarioNotFoundException("No encontramos un usuarios con el email"+email);
		}
		 
	}
	@Override
	public Usuario claveReset(String resetToken) {
		return usuariosRepo.findByReset(resetToken);
	}
	
	@Transactional
	@Override
	public void updateClave(Usuario usu, String nuevaClave) {
		BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
		String encode=passwordEncoder.encode(nuevaClave);
		usu.setPassword(encode);
		usu.setReset(null);
		usuariosRepo.save(usu);


	}

	 
	
	
	
	
	
	

}
