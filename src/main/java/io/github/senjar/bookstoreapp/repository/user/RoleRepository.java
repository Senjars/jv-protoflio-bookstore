package io.github.senjar.bookstoreapp.repository.user;

import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
