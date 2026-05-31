package com.pennywise.controller;
import com.pennywise.model.AccountTransfer; import com.pennywise.model.Transaction; import com.pennywise.model.User;
import com.pennywise.repository.AccountTransferRepository; import com.pennywise.repository.TransactionRepository; import com.pennywise.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.Map;
@RestController @RequestMapping("/api/transfers")
public class AccountTransferController {
    private final AccountTransferRepository repo; private final TransactionRepository txRepo; private final UserRepository userRepo;
    public AccountTransferController(AccountTransferRepository r,TransactionRepository t,UserRepository u){repo=r;txRepo=t;userRepo=u;}
    private User getUser(UserDetails ud){return userRepo.findByEmail(ud.getUsername()).orElseThrow();}
    @GetMapping public List<AccountTransfer> getAll(@AuthenticationPrincipal UserDetails ud){return repo.findByUserOrderByDateDesc(getUser(ud));}
    @PostMapping public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,@RequestBody Req req){
        if(req.getFromAccount().equals(req.getToAccount()))return ResponseEntity.badRequest().body(Map.of("error","Accounts must differ"));
        User user=getUser(ud);
        BigDecimal bal=txRepo.sumAmountByUserAndAccount(user,req.getFromAccount());
        if(bal==null)bal=BigDecimal.ZERO;
        if(req.getAmount().compareTo(bal)>0)return ResponseEntity.badRequest().body(Map.of("error","Insufficient balance","balance",bal));
        Transaction debit=new Transaction(); debit.setUser(user); debit.setText("Transfer to "+cap(req.getToAccount()));
        debit.setAmount(req.getAmount().negate()); debit.setCategory("Transfer"); debit.setAccount(req.getFromAccount()); debit.setDate(req.getDate()); txRepo.save(debit);
        Transaction credit=new Transaction(); credit.setUser(user); credit.setText("Transfer from "+cap(req.getFromAccount()));
        credit.setAmount(req.getAmount()); credit.setCategory("Transfer"); credit.setAccount(req.getToAccount()); credit.setDate(req.getDate()); txRepo.save(credit);
        AccountTransfer t=new AccountTransfer(); t.setUser(user); t.setNote(req.getNote()!=null?req.getNote():"Transfer");
        t.setFromAccount(req.getFromAccount()); t.setToAccount(req.getToAccount()); t.setAmount(req.getAmount()); t.setDate(req.getDate());
        t.setDebitTxId(debit.getId()); t.setCreditTxId(credit.getId());
        return ResponseEntity.ok(repo.save(t));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id){
        AccountTransfer t=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(t==null)return ResponseEntity.notFound().build();
        txRepo.findById(t.getDebitTxId()).ifPresent(txRepo::delete); txRepo.findById(t.getCreditTxId()).ifPresent(txRepo::delete);
        repo.delete(t); return ResponseEntity.noContent().build();
    }
    private String cap(String s){return s==null||s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    public static class Req{
        private String fromAccount; private String toAccount; private BigDecimal amount; private LocalDate date; private String note;
        public String getFromAccount(){return fromAccount;} public void setFromAccount(String f){fromAccount=f;}
        public String getToAccount(){return toAccount;} public void setToAccount(String t){toAccount=t;}
        public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal a){amount=a;}
        public LocalDate getDate(){return date;} public void setDate(LocalDate d){date=d;}
        public String getNote(){return note;} public void setNote(String n){note=n;}
    }
}
