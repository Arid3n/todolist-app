package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    private UsuarioData usuarioEnSesion() {
        Long id = managerUserSession.usuarioLogeado();
        if (id == null) {
            return null;
        }
        return usuarioService.findById(id);
    }

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        model.addAttribute("usuario", usuarioEnSesion());
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable("id") Long id, Model model) {
        EquipoData equipo = equipoService.recuperarEquipo(id);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(id);
        UsuarioData usuarioActual = usuarioEnSesion();

        boolean esMiembro = false;
        if (usuarioActual != null && usuarios != null) {
            esMiembro = usuarios.stream().anyMatch(u -> u.getId().equals(usuarioActual.getId()));
        }

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("esMiembro", esMiembro);
        return "detalleEquipo";
    }

    @PostMapping("/equipos/{id}/usuarios")
    public String unirseAEquipo(@PathVariable("id") Long idEquipo) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            return "redirect:/login";
        }
        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);
        return "redirect:/equipos/" + idEquipo;
    }

    @DeleteMapping("/equipos/{idEquipo}/usuarios/{idUsuario}")
    public String salirDeEquipo(@PathVariable("idEquipo") Long idEquipo,
                                @PathVariable("idUsuario") Long idUsuario) {
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (idLogeado == null) {
            return "redirect:/login";
        }
        // Permitimos que el usuario se elimine a sí mismo
        if (idLogeado.equals(idUsuario)) {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, idUsuario);
        }
        return "redirect:/equipos/" + idEquipo;
    }
}