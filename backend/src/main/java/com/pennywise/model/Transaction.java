package com.pennywise.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;

@Entity @Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)  @JsonIgnore private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false) private String text;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private String category;
    @Column(nullable = false) private String account;
    @Column(nullable = false) private LocalDate date;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public String getText() { return text; } public void setText(String t) { text = t; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal a) { amount = a; }
    public String getCategory() { return category; } public void setCategory(String c) { category = c; }
    public String getAccount() { return account; } public void setAccount(String a) { account = a; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate d) { date = d; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
}
