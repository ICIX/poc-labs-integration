package com.example.demo.repository;


import com.example.demo.model.ApplicationResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationResourceRepository extends JpaRepository<ApplicationResource,Integer> {
    List<ApplicationResource> findByKey(String key);
}
