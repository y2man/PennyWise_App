package com.pennywise.ui.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal; import java.math.RoundingMode; import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown=true)
public class Debt {
    private Long id; private String name; private BigDecimal total; private BigDecimal paid;
    private BigDecimal interestRate; private LocalDate dueDate; private String account;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String n){name=n;}
    public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal t){total=t;}
    public BigDecimal getPaid(){return paid;} public void setPaid(BigDecimal p){paid=p;}
    public BigDecimal getInterestRate(){return interestRate;} public void setInterestRate(BigDecimal r){interestRate=r;}
    public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate d){dueDate=d;}
    public String getAccount(){return account;} public void setAccount(String a){account=a;}
    public int getPercent(){
        if(total==null||total.compareTo(BigDecimal.ZERO)==0)return 0;
        BigDecimal p=paid!=null?paid:BigDecimal.ZERO;
        return p.multiply(BigDecimal.valueOf(100)).divide(total,0,RoundingMode.HALF_UP).intValue();
    }
}
