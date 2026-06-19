package com.pennywise.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Table(name = "budgets")
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") @JsonIgnore private User user;
    private String category;
    @Column(name = "budget_limit", precision = 15, scale = 2) private BigDecimal limit;
    private String period;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public String getCategory() { return category; } public void setCategory(String c) { category = c; }
    public BigDecimal getLimit() { return limit; } public void setLimit(BigDecimal l) { limit = l; }
    public String getPeriod() { return period; } public void setPeriod(String p) { period = p; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
}
