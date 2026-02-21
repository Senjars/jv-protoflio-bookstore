package io.github.senjar.bookstoreapp.repository.role;

import io.github.senjar.bookstoreapp.model.Role;
import io.github.senjar.bookstoreapp.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
