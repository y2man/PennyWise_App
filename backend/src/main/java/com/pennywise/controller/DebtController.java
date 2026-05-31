package com.pennywise.controller;
import com.pennywise.model.Debt; import com.pennywise.model.User;
import com.pennywise.repository.DebtRepository; import com.pennywise.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
@RestController @RequestMapping("/api/debts")
public class DebtController {
    private final DebtRepository repo; private final UserRepository userRepo;
    public DebtController(DebtRepository r,UserRepository u){repo=r;userRepo=u;}
    private User getUser(UserDetails ud){return userRepo.findByEmail(ud.getUsername()).orElseThrow();}
    @GetMapping public List<Debt> getAll(@AuthenticationPrincipal UserDetails ud){return repo.findByUser(getUser(ud));}
    @PostMapping public ResponseEntity<Debt> create(@AuthenticationPrincipal UserDetails ud,@RequestBody Req req){
        Debt d=new Debt(); d.setUser(getUser(ud)); d.setName(req.getName()); d.setTotal(req.getTotal());
        d.setPaid(req.getPaid()!=null?req.getPaid():BigDecimal.ZERO); d.setInterestRate(req.getInterestRate());
        d.setDueDate(req.getDueDate()); d.setAccount(req.getAccount()!=null?req.getAccount():"card");
        return ResponseEntity.ok(repo.save(d));
    }
    @PatchMapping("/{id}") public ResponseEntity<Debt> update(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id,@RequestBody Req req){
        Debt d=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(d==null)return ResponseEntity.notFound().build();
        if(req.getName()!=null)d.setName(req.getName()); if(req.getTotal()!=null)d.setTotal(req.getTotal());
        if(req.getPaid()!=null)d.setPaid(req.getPaid()); if(req.getInterestRate()!=null)d.setInterestRate(req.getInterestRate());
        if(req.getDueDate()!=null)d.setDueDate(req.getDueDate()); if(req.getAccount()!=null)d.setAccount(req.getAccount());
        return ResponseEntity.ok(repo.save(d));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id){
        Debt d=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(d==null)return ResponseEntity.notFound().build();
        repo.delete(d); return ResponseEntity.noContent().build();
    }
    public static class Req{
        private String name; private BigDecimal total; private BigDecimal paid;
        private BigDecimal interestRate; private LocalDate dueDate; private String account;
        public String getName(){return name;} public void setName(String n){name=n;}
        public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal t){total=t;}
        public BigDecimal getPaid(){return paid;} public void setPaid(BigDecimal p){paid=p;}
        public BigDecimal getInterestRate(){return interestRate;} public void setInterestRate(BigDecimal r){interestRate=r;}
        public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate d){dueDate=d;}
        public String getAccount(){return account;} public void setAccount(String a){account=a;}
    }
}
