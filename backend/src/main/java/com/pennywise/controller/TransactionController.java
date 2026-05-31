package com.pennywise.controller;
import com.pennywise.model.Transaction; import com.pennywise.model.User;
import com.pennywise.repository.TransactionRepository; import com.pennywise.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
@RestController @RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionRepository txRepo; private final UserRepository userRepo;
    public TransactionController(TransactionRepository t, UserRepository u){txRepo=t;userRepo=u;}
    private User getUser(UserDetails ud){return userRepo.findByEmail(ud.getUsername()).orElseThrow();}
    @GetMapping
    public List<Transaction> getAll(@AuthenticationPrincipal UserDetails ud, @RequestParam(required=false) String month){
        User u=getUser(ud);
        if(month!=null){LocalDate s=LocalDate.parse(month+"-01");return txRepo.findByUserAndDateBetweenOrderByDateDesc(u,s,s.withDayOfMonth(s.lengthOfMonth()));}
        return txRepo.findByUserOrderByDateDesc(u);
    }
    @PostMapping
    public ResponseEntity<Transaction> create(@AuthenticationPrincipal UserDetails ud,@RequestBody TxRequest req){
        Transaction tx=new Transaction(); tx.setUser(getUser(ud)); tx.setText(req.getText());
        tx.setAmount(req.getAmount()); tx.setCategory(req.getCategory()); tx.setAccount(req.getAccount()); tx.setDate(req.getDate());
        return ResponseEntity.ok(txRepo.save(tx));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> update(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id,@RequestBody TxRequest req){
        Transaction tx=txRepo.findByIdAndUser(id,getUser(ud)).orElse(null);
        if(tx==null)return ResponseEntity.notFound().build();
        if(req.getText()!=null)tx.setText(req.getText()); if(req.getAmount()!=null)tx.setAmount(req.getAmount());
        if(req.getCategory()!=null)tx.setCategory(req.getCategory()); if(req.getAccount()!=null)tx.setAccount(req.getAccount());
        if(req.getDate()!=null)tx.setDate(req.getDate());
        return ResponseEntity.ok(txRepo.save(tx));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id){
        Transaction tx=txRepo.findByIdAndUser(id,getUser(ud)).orElse(null);
        if(tx==null)return ResponseEntity.notFound().build(); txRepo.delete(tx); return ResponseEntity.noContent().build();
    }
    public static class TxRequest{
        private String text; private BigDecimal amount; private String category; private String account; private LocalDate date;
        public String getText(){return text;} public void setText(String t){text=t;}
        public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal a){amount=a;}
        public String getCategory(){return category;} public void setCategory(String c){category=c;}
        public String getAccount(){return account;} public void setAccount(String a){account=a;}
        public LocalDate getDate(){return date;} public void setDate(LocalDate d){date=d;}
    }
}
