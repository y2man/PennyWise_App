package com.pennywise.model;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name = "debts")
public class Debt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private String name;
    @Column(precision = 15, scale = 2) private BigDecimal total;
    @Column(precision = 15, scale = 2) private BigDecimal paid = BigDecimal.ZERO;
    @Column(name = "interest_rate", precision = 5, scale = 2) private BigDecimal interestRate;
    @Column(name = "due_date") private LocalDate dueDate;
    private String account;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public String getName() { return name; } public void setName(String n) { name = n; }
    public BigDecimal getTotal() { return total; } public void setTotal(BigDecimal t) { total = t; }
    public BigDecimal getPaid() { return paid; } public void setPaid(BigDecimal p) { paid = p; }
    public BigDecimal getInterestRate() { return interestRate; } public void setInterestRate(BigDecimal r) { interestRate = r; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate d) { dueDate = d; }
    public String getAccount() { return account; } public void setAccount(String a) { account = a; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
}
