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

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }


    @Test
    public void getRegistradosDevuelveListado() throws Exception {
        // GIVEN: Un administrador logeado
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdmin(true);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(admin);
        when(usuarioService.allUsuarios()).thenReturn(Arrays.asList(admin));

        // WHEN / THEN
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuarios registrados")));
    }

    @Test
    public void getDescripcionUsuarioDevuelveDatos() throws Exception {
        // GIVEN: Un administrador logeado y el usuario a consultar
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdmin(true);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setNombre("Ana García");
        usuario.setEmail("ana@ua");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(admin);
        when(usuarioService.findById(2L)).thenReturn(usuario);

        // WHEN / THEN
        this.mockMvc.perform(get("/registrados/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Descripción de usuario"),
                        containsString("Ana García"),
                        containsString("ana@ua")
                )));
    }

    @Test
    public void loginAdminRedirigeARegistrados() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setEmail("admin@ua");
        admin.setAdmin(true);

        when(usuarioService.login("admin@ua", "123")).thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@ua")).thenReturn(admin);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "admin@ua")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void getRegistroMuestraCheckboxSoloSiNoExisteAdmin() throws Exception {
        // GIVEN: No existe admin
        when(usuarioService.existeAdmin()).thenReturn(false);

        // WHEN / THEN: Se muestra el checkbox
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrarse como administrador")));

        // GIVEN: Ya existe admin
        when(usuarioService.existeAdmin()).thenReturn(true);

        // WHEN / THEN: No aparece el checkbox
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Registrarse como administrador"))));
    }

    @Test
    public void accesoRegistradosSinLoginDevuelveUnauthorized() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void accesoRegistradosUsuarioNoAdminDevuelveUnauthorized() throws Exception {
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setId(2L);
        usuarioNormal.setAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(2L);
        when(usuarioService.findById(2L)).thenReturn(usuarioNormal);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void accesoDescripcionSinLoginDevuelveUnauthorized() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void accesoDescripcionUsuarioNoAdminDevuelveUnauthorized() throws Exception {
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setId(2L);
        usuarioNormal.setAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(2L);
        when(usuarioService.findById(2L)).thenReturn(usuarioNormal);

        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void adminPuedeBloquearODesbloquearUsuario() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdmin(true);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setBloqueado(false);
        usuario.setAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(admin);
        when(usuarioService.findById(2L)).thenReturn(usuario);

        this.mockMvc.perform(post("/registrados/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void usuarioNoAdminNoPuedeBloquear() throws Exception {
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setId(2L);
        usuarioNormal.setAdmin(false);

        when(managerUserSession.usuarioLogeado()).thenReturn(2L);
        when(usuarioService.findById(2L)).thenReturn(usuarioNormal);

        this.mockMvc.perform(post("/registrados/3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginUsuarioBloqueadoMuestraError() throws Exception {
        when(usuarioService.login("bloqueado@ua", "123"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "bloqueado@ua")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("formLogin"))
                .andExpect(content().string(containsString("String erronea")));
    }
}
