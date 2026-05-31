package com.pennywise.controller;
import com.pennywise.model.Budget; import com.pennywise.model.User;
import com.pennywise.repository.BudgetRepository; import com.pennywise.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.List;
@RestController @RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetRepository repo; private final UserRepository userRepo;
    public BudgetController(BudgetRepository r,UserRepository u){repo=r;userRepo=u;}
    private User getUser(UserDetails ud){return userRepo.findByEmail(ud.getUsername()).orElseThrow();}
    @GetMapping public List<Budget> getAll(@AuthenticationPrincipal UserDetails ud){return repo.findByUser(getUser(ud));}
    @PostMapping public ResponseEntity<Budget> create(@AuthenticationPrincipal UserDetails ud,@RequestBody Req req){
        Budget b=new Budget(); b.setUser(getUser(ud)); b.setCategory(req.getCategory());
        b.setLimit(req.getLimit()); b.setPeriod(req.getPeriod()!=null?req.getPeriod():"monthly");
        return ResponseEntity.ok(repo.save(b));
    }
    @PatchMapping("/{id}") public ResponseEntity<Budget> update(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id,@RequestBody Req req){
        Budget b=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(b==null)return ResponseEntity.notFound().build();
        if(req.getCategory()!=null)b.setCategory(req.getCategory()); if(req.getLimit()!=null)b.setLimit(req.getLimit()); if(req.getPeriod()!=null)b.setPeriod(req.getPeriod());
        return ResponseEntity.ok(repo.save(b));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id){
        Budget b=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(b==null)return ResponseEntity.notFound().build();
        repo.delete(b); return ResponseEntity.noContent().build();
    }
    public static class Req{
        private String category; private BigDecimal limit; private String period;
        public String getCategory(){return category;} public void setCategory(String c){category=c;}
        public BigDecimal getLimit(){return limit;} public void setLimit(BigDecimal l){limit=l;}
        public String getPeriod(){return period;} public void setPeriod(String p){period=p;}
    }
}
