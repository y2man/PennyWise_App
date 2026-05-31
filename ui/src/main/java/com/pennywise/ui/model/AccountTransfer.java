package com.pennywise.ui.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal; import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown=true)
public class AccountTransfer {
    private Long id; private String note; private String fromAccount; private String toAccount;
    private BigDecimal amount; private LocalDate date; private Long debitTxId; private Long creditTxId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNote(){return note;} public void setNote(String n){note=n;}
    public String getFromAccount(){return fromAccount;} public void setFromAccount(String f){fromAccount=f;}
    public String getToAccount(){return toAccount;} public void setToAccount(String t){toAccount=t;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal a){amount=a;}
    public LocalDate getDate(){return date;} public void setDate(LocalDate d){date=d;}
    public Long getDebitTxId(){return debitTxId;} public void setDebitTxId(Long d){debitTxId=d;}
    public Long getCreditTxId(){return creditTxId;} public void setCreditTxId(Long c){creditTxId=c;}
}
