package com.pennywise.controller;
import com.pennywise.model.SavingsGoal; import com.pennywise.model.User;
import com.pennywise.repository.SavingsGoalRepository; import com.pennywise.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
@RestController @RequestMapping("/api/savings-goals")
public class SavingsGoalController {
    private final SavingsGoalRepository repo; private final UserRepository userRepo;
    public SavingsGoalController(SavingsGoalRepository r,UserRepository u){repo=r;userRepo=u;}
    private User getUser(UserDetails ud){return userRepo.findByEmail(ud.getUsername()).orElseThrow();}
    @GetMapping public List<SavingsGoal> getAll(@AuthenticationPrincipal UserDetails ud){return repo.findByUser(getUser(ud));}
    @PostMapping public ResponseEntity<SavingsGoal> create(@AuthenticationPrincipal UserDetails ud,@RequestBody Req req){
        SavingsGoal g=new SavingsGoal(); g.setUser(getUser(ud)); g.setName(req.getName());
        g.setTarget(req.getTarget()); g.setSaved(req.getSaved()!=null?req.getSaved():BigDecimal.ZERO); g.setTargetDate(req.getTargetDate());
        return ResponseEntity.ok(repo.save(g));
    }
    @PatchMapping("/{id}") public ResponseEntity<SavingsGoal> update(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id,@RequestBody Req req){
        SavingsGoal g=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(g==null)return ResponseEntity.notFound().build();
        if(req.getName()!=null)g.setName(req.getName()); if(req.getTarget()!=null)g.setTarget(req.getTarget());
        if(req.getSaved()!=null)g.setSaved(req.getSaved()); if(req.getTargetDate()!=null)g.setTargetDate(req.getTargetDate());
        return ResponseEntity.ok(repo.save(g));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,@PathVariable Long id){
        SavingsGoal g=repo.findByIdAndUser(id,getUser(ud)).orElse(null); if(g==null)return ResponseEntity.notFound().build();
        repo.delete(g); return ResponseEntity.noContent().build();
    }
    public static class Req{
        private String name; private BigDecimal target; private BigDecimal saved; private LocalDate targetDate;
        public String getName(){return name;} public void setName(String n){name=n;}
        public BigDecimal getTarget(){return target;} public void setTarget(BigDecimal t){target=t;}
        public BigDecimal getSaved(){return saved;} public void setSaved(BigDecimal s){saved=s;}
        public LocalDate getTargetDate(){return targetDate;} public void setTargetDate(LocalDate d){targetDate=d;}
    }
}
