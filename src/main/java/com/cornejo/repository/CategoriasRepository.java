package com.cornejo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cornejo.model.Categoria;

public interface CategoriasRepository extends JpaRepository<Categoria, Integer> {
	
}
