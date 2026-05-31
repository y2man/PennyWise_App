package com.pennywise.repository;
import com.pennywise.model.Budget; import com.pennywise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
    Optional<Budget> findByIdAndUser(Long id, User user);
}
