package com.pennywise.ui.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal; import java.math.RoundingMode; import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown=true)
public class SavingsGoal {
    private Long id; private String name; private BigDecimal target; private BigDecimal saved; private LocalDate targetDate;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String n){name=n;}
    public BigDecimal getTarget(){return target;} public void setTarget(BigDecimal t){target=t;}
    public BigDecimal getSaved(){return saved;} public void setSaved(BigDecimal s){saved=s;}
    public LocalDate getTargetDate(){return targetDate;} public void setTargetDate(LocalDate d){targetDate=d;}
    public int getPercent(){
        if(target==null||target.compareTo(BigDecimal.ZERO)==0)return 0;
        BigDecimal s=saved!=null?saved:BigDecimal.ZERO;
        return s.multiply(BigDecimal.valueOf(100)).divide(target,0,RoundingMode.HALF_UP).intValue();
    }
}
