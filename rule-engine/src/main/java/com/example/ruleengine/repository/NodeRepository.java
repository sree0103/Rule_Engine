package com.example.ruleengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ruleengine.model.Node;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

}
