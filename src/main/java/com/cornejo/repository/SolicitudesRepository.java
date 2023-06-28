package com.cornejo.repository;
 

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cornejo.model.Solicitud;

public interface SolicitudesRepository extends JpaRepository<Solicitud, Integer> {
 
	@Query("SELECT s FROM Solicitud s WHERE s.usuario.id = :idUsuario")
    List<Solicitud> findByUsuarioId(Integer idUsuario);
	
	@Query("SELECT s FROM Solicitud s WHERE s.vacante.id = :idVacante")
    List<Solicitud> findByVacanteId(Integer idVacante);
}
