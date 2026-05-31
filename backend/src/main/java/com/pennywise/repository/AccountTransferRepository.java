package com.pennywise.repository;
import com.pennywise.model.AccountTransfer; import com.pennywise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface AccountTransferRepository extends JpaRepository<AccountTransfer, Long> {
    List<AccountTransfer> findByUserOrderByDateDesc(User user);
    Optional<AccountTransfer> findByIdAndUser(Long id, User user);
}
