package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj) {
        User user = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("WRONG ID"));
        double newBalance = user.getBalance() + obj.getAmount();
        user.setBalance((newBalance));
        userRepo.save(user);
        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("RS" + obj.getAmount() + " Deposit successful ");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "DEPOSIT SUCCESSFUL";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj) {
        User user = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("WRONG ID"));
        double newBalance = user.getBalance() - obj.getAmount();
        if (newBalance < 0) {
            return "INSUFFICENT FUNDS";
        }
        user.setBalance((newBalance));
        userRepo.save(user);
        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("RS" + obj.getAmount() + " Withdrawal Successful ");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "WITHDRAWAL SUCCESSFUL";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj) {
        User sender = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("NOT FOUND"));
        User rec = userRepo.findByusername(obj.getUsername());
        if (rec == null) {
            return "RECEIVER NOT FOUND";
        }
        if (sender.getId() == rec.getId()) {
            return "SELF TRANSACTION NOT ALLOWED";
        }
        if (obj.getAmount() < 1) {
            return "INVALID AMOUNT";
        }
        double sbalance = sender.getBalance() - obj.getAmount();
        if (sbalance < 0) {
            return "INSUFFICIENT BALANCE";
        }
        double rblance = rec.getBalance() + obj.getAmount();
        sender.setBalance((sbalance));
        rec.setBalance((rblance));
        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("RS" + obj.getAmount() + " Sent to user " + obj.getUsername());
        t1.setUserId(sender.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rblance);
        t2.setDescription("RS" + obj.getAmount() + " Received from user " + sender.getUsername());
        t2.setUserId(rec.getId());
        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer Done Successfully";
    }
    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);
    }
}