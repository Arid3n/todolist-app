package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    public String salirDeEquipo(@PathVariable("idEquipo") Long idEquipo,
                                @PathVariable("idUsuario") Long idUsuario) {
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (idLogeado != null && idLogeado.equals(idUsuario)) {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, idUsuario);
        }
        return "";
    }

    @GetMapping("/equipos/nuevo")
    public String formNuevoEquipo(Model model) {
        UsuarioData usuario = usuarioEnSesion();
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("equipoData", new EquipoData());
        model.addAttribute("usuario", usuario);
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String nuevoEquipo(@ModelAttribute EquipoData equipoData) {
        UsuarioData usuario = usuarioEnSesion();
        if (usuario == null) {
            return "redirect:/login";
        }
        equipoService.crearEquipo(equipoData.getNombre());
        return "redirect:/equipos";
    }
}