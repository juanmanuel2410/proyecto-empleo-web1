package com.cornejo.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList; 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cornejo.model.Perfil;
import com.cornejo.model.Usuario;

public class MyUserDetails implements UserDetails{
	
	private static final long serialVersionUID = 1L;
	private Usuario usuario;
	
	public MyUserDetails(Usuario usuario) {
		super();
		this.usuario = usuario;
	}

	 @Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<Perfil> roles = usuario.getPerfiles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Perfil role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getPerfil()));
        }
        return authorities;
	} 

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return usuario.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return usuario.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public String getNombre() {
		return usuario.getNombre();
	}
	public Usuario getUsuario() {
		return this.usuario;
	}

	 

}