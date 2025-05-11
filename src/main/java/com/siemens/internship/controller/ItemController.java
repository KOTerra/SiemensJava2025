package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api/items")
public class ItemController {


    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody @Valid Item item, BindingResult result) {
        System.out.println("BBBB");
        if (result.hasErrors()) {
            System.out.println("AAAA");
            List<org.springframework.validation.FieldError> fieldErrors = result.getFieldErrors();
            for (org.springframework.validation.FieldError fieldError : fieldErrors) {
                System.out.println(fieldError.getField() + ": " + fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        if (item.getEmail().isEmpty() || !item.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //changed from NO_CONTENT
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            if (item.getEmail().isEmpty() || !item.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK); // changed from CREATED
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //changed from ACCEPTED
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); //changed from CONFLICT
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() throws ExecutionException, InterruptedException {
        return new ResponseEntity<>(itemService.processItemsAsync().get(), HttpStatus.OK);
    }
}
