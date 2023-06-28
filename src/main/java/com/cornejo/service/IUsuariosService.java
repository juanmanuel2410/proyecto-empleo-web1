package com.cornejo.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;

import com.cornejo.model.Usuario;
import com.cornejo.service.db.UsuarioNotFoundException; 

public interface IUsuariosService {
	
	 
	
	Usuario guardar(Usuario usuario);
	void eliminar(Integer idUsuario);
	List<Usuario> buscarTodos();
	List<Usuario> buscarRegistrados();
	Usuario buscarPorId(Integer idUsuario);
	Usuario buscarPorUsername(String username); 
	int bloquear(int idUsuario);
	int activar(int idUsuario); 
	Usuario buscarEmail(String username) ;
	void verificarCuenta(Usuario usuario,  String sitio) throws UnsupportedEncodingException, MessagingException;
	boolean verificacion(String codigoVerificacion); 
    void recuperarClave(String token,String email) throws UsuarioNotFoundException;
     Usuario claveReset(String resetToken);
	 void updateClave(Usuario usu, String nuevaClave);
	
	
	
}
