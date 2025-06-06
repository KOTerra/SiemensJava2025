package com.siemens.internship.service;

import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.model.Item;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    @Getter
    private List<Item> processedItems = new ArrayList<>();
    @Getter
    private AtomicInteger processedCount = new AtomicInteger(0);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {  //using CompletableFuture<List> because List is not Async friendly
        List<Long> itemIds = itemRepository.findAllIds();

        //thread-safe list
        processedItems = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    itemRepository.findById(id).ifPresent(item -> {
                        item.setStatus("PROCESSED");
                        itemRepository.save(item);
                        processedItems.add(item);
                        processedCount.incrementAndGet();   //count++ would not be atomic

                    });

                } catch (InterruptedException e) {
                    throw new RuntimeException("Processing error for item ID: " + id, e);
                }
            }, executor);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems)
                .exceptionally(ex -> {
                    System.err.println("Error processing items: " + ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

}

