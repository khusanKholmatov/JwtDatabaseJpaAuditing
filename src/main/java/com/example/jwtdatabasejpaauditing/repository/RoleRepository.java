package com.example.jwtdatabasejpaauditing.repository;

import com.example.jwtdatabasejpaauditing.entity.Role;
import com.example.jwtdatabasejpaauditing.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(RoleName roleName);

}
