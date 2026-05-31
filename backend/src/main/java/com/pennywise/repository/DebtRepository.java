package com.pennywise.repository;
import com.pennywise.model.Debt; import com.pennywise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUser(User user);
    Optional<Debt> findByIdAndUser(Long id, User user);
}
