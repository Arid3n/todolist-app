package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuario", usuarioEnSesion());
        return "detalleEquipo";
    }
}