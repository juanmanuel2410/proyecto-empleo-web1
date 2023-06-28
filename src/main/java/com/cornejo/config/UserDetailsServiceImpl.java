package com.cornejo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.cornejo.model.Usuario;
import com.cornejo.repository.UsuariosRepository; 
 

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UsuariosRepository usuarioRepository;
	
	@Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.getUserByUsername(email);
         
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return new MyUserDetails(usuario);
    }
}