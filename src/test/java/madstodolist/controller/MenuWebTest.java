package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MenuWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void menuNavbarSinUsuarioLogeado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Login"),
                        containsString("Registro"),
                        not(containsString("Tareas")),
                        not(containsString("Cerrar sesión"))
                )));
    }

    @Test
    public void menuNavbarConUsuarioLogeado() throws Exception {
        Long usuarioId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(usuarioId);
        usuario.setNombre("Andres Radu");
        usuario.setEmail("andres@ua");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        when(usuarioService.findById(usuarioId)).thenReturn(usuario);
        this.mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("/usuarios/1/tareas"),
                        containsString("Tareas"),
                        containsString("Andres Radu"),
                        containsString("Cerrar sesión Andres Radu"),
                        not(containsString("href=\"/login\"")),
                        not(containsString("href=\"/registro\""))
                )));
    }
}