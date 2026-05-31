package com.pennywise.model;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name = "savings_goals")
public class SavingsGoal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private String name;
    @Column(precision = 15, scale = 2) private BigDecimal target;
    @Column(precision = 15, scale = 2) private BigDecimal saved = BigDecimal.ZERO;
    @Column(name = "target_date") private LocalDate targetDate;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User u) { user = u; }
    public String getName() { return name; } public void setName(String n) { name = n; }
    public BigDecimal getTarget() { return target; } public void setTarget(BigDecimal t) { target = t; }
    public BigDecimal getSaved() { return saved; } public void setSaved(BigDecimal s) { saved = s; }
    public LocalDate getTargetDate() { return targetDate; } public void setTargetDate(LocalDate d) { targetDate = d; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime c) { createdAt = c; }
}
