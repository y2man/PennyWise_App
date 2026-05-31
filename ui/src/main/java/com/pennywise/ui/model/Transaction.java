package com.pennywise.ui.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal; import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown=true)
public class Transaction {
    private Long id; private String text; private BigDecimal amount; private String category; private String account; private LocalDate date;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getText(){return text;} public void setText(String t){text=t;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal a){amount=a;}
    public String getCategory(){return category;} public void setCategory(String c){category=c;}
    public String getAccount(){return account;} public void setAccount(String a){account=a;}
    public LocalDate getDate(){return date;} public void setDate(LocalDate d){date=d;}
    public boolean isIncome(){return amount!=null&&amount.compareTo(BigDecimal.ZERO)>0;}
    public boolean isExpense(){return amount!=null&&amount.compareTo(BigDecimal.ZERO)<0;}
}
