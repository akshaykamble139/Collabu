package com.akshay.Collabu.controllers;

//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.akshay.Collabu.controllers.UserController;
//import com.akshay.Collabu.dto.UserDTO;
//import com.akshay.Collabu.services.UserService;
//
////@AutoConfigureMockMvc
////@WithMockUser(username = "test1", roles = {"USER"})
////@WebMvcTest(UserController.class)
class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private UserService userService;
//
//    @Test
//    void testGetUserById() throws Exception {
//        UserDTO mockUser = new UserDTO(1L, "john_doe", "john@gmail.com");
//        Mockito.when(userService.getUserById(1L)).thenReturn(mockUser);
//
//        mockMvc.perform(get("/users/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.username").value("john_doe"));
//    }
}
