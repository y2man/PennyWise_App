package com.pennywise.ui.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; import java.math.BigDecimal;
@JsonIgnoreProperties(ignoreUnknown=true)
public class Budget {
    private Long id; private String category; private BigDecimal limit; private String period;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getCategory(){return category;} public void setCategory(String c){category=c;}
    public BigDecimal getLimit(){return limit;} public void setLimit(BigDecimal l){limit=l;}
    public String getPeriod(){return period;} public void setPeriod(String p){period=p;}
}
