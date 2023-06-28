package com.cornejo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cornejo.model.Usuario;

public interface UsuariosRepository extends JpaRepository<Usuario, Integer> {
	// Buscar usuario por username
	Usuario findByUsername(String username);
	List<Usuario> findByFechaRegistroNotNull();
	
	@Modifying
    @Query("UPDATE Usuario u SET u.estatus=0 WHERE u.id = :paramIdUsuario")
    int lock(@Param("paramIdUsuario") int idUsuario);
	
	@Modifying
    @Query("UPDATE Usuario u SET u.estatus=1 WHERE u.id = :paramIdUsuario")
    int unlock(@Param("paramIdUsuario") int idUsuario); 
	
	@Query("SELECT U FROM Usuario U WHERE U.email = :email AND U.estatus = 1")
	public Usuario getUserByUsername(@Param("email")String username);
	
	
	@Query("SELECT U FROM Usuario U WHERE U.verificacion_code =?1")
	public Usuario getVerification( String code);
	
	public Usuario findByReset(String token);
	 
	
}
