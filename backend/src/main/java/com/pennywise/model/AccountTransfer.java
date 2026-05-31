package com.pennywise.model;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name = "account_transfers")
public class AccountTransfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private String note;
    @Column(name = "from_account") private String fromAccount;
    @Column(name = "to_account") private String toAccount;
    @Column(precision = 15, scale = 2) private BigDecimal amount;
    private LocalDate date;
    @Column(name = "debit_tx_id") private Long debitTxId;
    @Column(name = "credit_tx_id") private Long creditTxId;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public String getNote() { return note; } public void setNote(String n) { note = n; }
    public String getFromAccount() { return fromAccount; } public void setFromAccount(String f) { fromAccount = f; }
    public String getToAccount() { return toAccount; } public void setToAccount(String t) { toAccount = t; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal a) { amount = a; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate d) { date = d; }
    public Long getDebitTxId() { return debitTxId; } public void setDebitTxId(Long d) { debitTxId = d; }
    public Long getCreditTxId() { return creditTxId; } public void setCreditTxId(Long c) { creditTxId = c; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
}
