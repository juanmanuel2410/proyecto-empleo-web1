package com.cornejo.security;

import javax.sql.DataSource; 
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
 
import com.cornejo.config.UserDetailsServiceImpl; 
 
@Configuration
@EnableWebSecurity
public class DatabaseWebSecurity   {
	
	@Autowired
	private DataSource dataSource;

	@Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }
	
	@Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	
	
	 @Bean
	    DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	        authProvider.setUserDetailsService(userDetailsService());
	        authProvider.setPasswordEncoder(passwordEncoder());
	        return authProvider;
	    }
	
	
	  
	 protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	        auth.authenticationProvider(authenticationProvider());
	    } 
	 

	/**
	 * Personalizamos el Acceso a las URLs de la aplicación
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
        http.authorizeRequests()  
    	// Los recursos estáticos no requieren autenticación
        .antMatchers(
                "/bootstrap/**",                        
                "/images/**",
                "/tinymce/**",
                "/logos/**").permitAll()
        
        // Las vistas públicas no requieren autenticación
        .antMatchers("/", 
        			 "/login",
        			 "/signup",
        			 "/search",
        			 "/bcrypt/**",
        			 "/about",
        			 "/verify",
        			 "/recuperar",
        			 "/reset",
        			 "/vacantes/view/**").permitAll()
        
        // Asignar permisos a URLs por ROLES
        .antMatchers("/solicitudes/create/**",
        			 "/solicitudes/save/**").hasAuthority("USUARIO")
        
        .antMatchers("/solicitudes/**").hasAnyAuthority("SUPERVISOR","ADMINISTRADOR")
        .antMatchers("/vacantes/**").hasAnyAuthority("SUPERVISOR","ADMINISTRADOR")
        .antMatchers("/categorias/**").hasAnyAuthority("SUPERVISOR","ADMINISTRADOR")
        .antMatchers("/usuarios/**").hasAnyAuthority("ADMINISTRADOR")
        
        // Todas las demás URLs de la Aplicación requieren autenticación
        .anyRequest().authenticated()
        // El formulario de Login no requiere autenticacion
        .and().formLogin()
        	.loginPage("/login")
        	.usernameParameter("email")
        	.passwordParameter("password") 
        	.permitAll().and().rememberMe().tokenRepository(persistenceTokenRepository())         
        .and()
        .logout()
        .logoutUrl("/logout") // URL para realizar el logout
        .invalidateHttpSession(true) // Invalidar la sesión actual 
        .logoutSuccessUrl("/") // Redirigir a la página principal después del logout 
        .deleteCookies("JSESSIONID") // Eliminar las cookies (si las hay)//.rememberMe() 
        //.tokenValiditySeconds(3*24*6*60) 
        
        ;
        
        return http.build();
    }
	
	@Bean
	public PersistentTokenRepository persistenceTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository= new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		
		return tokenRepository;
	}
	
	/**
	 *  Implementación de Spring Security que encripta passwords con el algoritmo Bcrypt
	 * @return
	 */
	 

}