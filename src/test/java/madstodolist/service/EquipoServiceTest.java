package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;
    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDeEquipoService() {
        // GIVEN
        // Un usuario añadido a un equipo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user2@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN
        // Lo eliminamos del equipo
        equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN
        // Ya no pertenece a la lista de usuarios del equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).isEmpty();
    }

    @Test
    public void eliminarUsuarioDeEquipoLanzaExcepcionSiNoExiste() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto Excepcion");

        // THEN
        // Equipo inexistente
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(999L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        // Usuario inexistente
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), 999L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void modificarEquipoCambiaNombre() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Nombre Inicial");

        // WHEN
        equipoService.modificarEquipo(equipo.getId(), "Nombre Modificado");

        // THEN
        EquipoData equipoActualizado = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoActualizado.getNombre()).isEqualTo("Nombre Modificado");
    }

    @Test
    public void modificarEquipoLanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() -> equipoService.modificarEquipo(999L, "Nombre"))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void borrarEquipoEliminaEquipoYDesvinculaUsuarios() {
        // GIVEN
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("admin_borrar@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        EquipoData equipo = equipoService.crearEquipo("Equipo a Borrar");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN
        equipoService.borrarEquipo(equipo.getId());

        // THEN
        // Al intentar recuperar el equipo borrado, debe lanzar EquipoServiceException
        assertThatThrownBy(() -> equipoService.recuperarEquipo(equipo.getId()))
                .isInstanceOf(EquipoServiceException.class);

        // Ya no aparece en la lista general de equipos
        assertThat(equipoService.findAllOrdenadoPorNombre()).doesNotContain(equipo);

        // El usuario sigue existiendo intacto en el sistema
        assertThat(usuarioService.findById(usuario.getId())).isNotNull();
    }

    @Test
    public void borrarEquipoLanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() -> equipoService.borrarEquipo(999L))
                .isInstanceOf(EquipoServiceException.class);
    }
}