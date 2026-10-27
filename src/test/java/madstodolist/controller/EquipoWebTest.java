package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipoController.class)
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getEquiposDevuelveListado() throws Exception {
        EquipoData e1 = new EquipoData();
        e1.setId(1L);
        e1.setNombre("Equipo Alpha");

        EquipoData e2 = new EquipoData();
        e2.setId(2L);
        e2.setNombre("Equipo Beta");

        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Arrays.asList(e1, e2));

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(containsString("Equipo Alpha")))
                .andExpect(content().string(containsString("Equipo Beta")));
    }
}+