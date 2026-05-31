package com.pennywise.repository;
import com.pennywise.model.Transaction; import com.pennywise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.Optional;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByDateDesc(User user);
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);
    Optional<Transaction> findByIdAndUser(Long id, User user);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.account = :account")
    BigDecimal sumAmountByUserAndAccount(@Param("user") User user, @Param("account") String account);
}
