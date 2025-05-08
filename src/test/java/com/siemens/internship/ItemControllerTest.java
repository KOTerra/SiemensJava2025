package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateValidItem() throws Exception {
        Item item = new Item(null, "Test Item", "Item Description", "NEW", "test@example.com");
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(item)));
        verify(itemService).save(any(Item.class));
    }

    @Test
    void testCreateInvalidItem() throws Exception {
        Item item = new Item(null, "Test Item", "Item Description", "NEW", "invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllItems() throws Exception {
        List<Item> items = List.of(
                new Item(1L, "Item 1", "Description 1", "NEW", "test1@example.com"),
                new Item(2L, "Item 2", "Description 2", "OLD", "test2@example.com")
        );
        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(items)));
        verify(itemService).findAll();
    }

    @Test
    void testGetItemByIdFound() throws Exception {
        Item item = new Item(1L, "Test Item", "Item Description", "NEW", "test@example.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(item)));
        verify(itemService).findById(1L);
    }

    @Test
    void testGetItemByIdNotFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
        verify(itemService).findById(1L);
    }

    @Test
    void testUpdateItemValid() throws Exception {
        Item existingItem = new Item(1L, "Old Name", "Old Description", "OLD", "old@example.com");
        Item updatedItem = new Item(1L, "New Name", "New Description", "NEW", "new@example.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemService.save(any(Item.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedItem)));
        verify(itemService).findById(1L);
        verify(itemService).save(any(Item.class));
    }

    @Test
    void testUpdateItemInvalid() throws Exception {
        Item invalidItem = new Item(1L, "New Name", "New Description", "NEW", "invalid-email");
        when(itemService.findById(1L)).thenReturn(Optional.of(new Item()));

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testUpdateItemNotFound() throws Exception {
        Item updatedItem = new Item(1L, "New Name", "New Description", "NEW", "new@example.com");
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNotFound());
        verify(itemService).findById(1L);
    }

    @Test
    void testDeleteItemFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(new Item()));

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
        verify(itemService).findById(1L);
        verify(itemService).deleteById(1L);
    }

    @Test
    void testDeleteItemNotFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNotFound());
        verify(itemService).findById(1L);
    }
}
