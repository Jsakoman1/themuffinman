package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @Query("select u from AppUser u where lower(u.email) = lower(:email)")
    Optional<AppUser> findByEmail(String email);

    @Query("select count(u) > 0 from AppUser u where lower(u.email) = lower(:email)")
    boolean existsByEmail(String email);

    @Query("select count(u) > 0 from AppUser u where lower(u.email) = lower(:email) and u.id <> :id")
    boolean existsByEmailAndIdNot(String email, Long id);

    long countByRole(AppUserRole role);

    @Query("""
            select u from AppUser u
            where lower(u.username) like lower(concat('%', :query, '%'))
               or lower(u.email) like lower(concat('%', :query, '%'))
            order by u.username asc
            """)
    List<AppUser> searchByUsernameOrEmail(String query);
}
