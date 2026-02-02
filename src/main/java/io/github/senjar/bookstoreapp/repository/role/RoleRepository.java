package io.github.senjar.bookstoreapp.repository.role;

import io.github.senjar.bookstoreapp.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String email);
}
