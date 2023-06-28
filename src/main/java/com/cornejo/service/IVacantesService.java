package com.cornejo.service;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cornejo.model.Vacante;

public interface IVacantesService {
	void guardar(Vacante vacante);
	void eliminar(Integer idVacante);
	public List<Vacante> buscarTodas();
	Vacante buscarPorId(Integer idVacante);
	List<Vacante> buscarDestacadas();
	Page<Vacante> buscarTodas(Pageable page);
	List<Vacante> buscarByExample(Example<Vacante> example);
}
