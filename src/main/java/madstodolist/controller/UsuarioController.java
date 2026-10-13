package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    private UsuarioData comprobarAdmin() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoAutorizadoException();
        }
        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }
        return usuario;
    }

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        UsuarioData admin = comprobarAdmin();
        model.addAttribute("usuario", admin);

        List<UsuarioData> usuarios = usuarioService.allUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable(value = "id") Long id, Model model) {
        UsuarioData admin = comprobarAdmin();
        model.addAttribute("usuario", admin);

        UsuarioData usuarioDescripcion = usuarioService.findById(id);
        if (usuarioDescripcion == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuarioDescripcion", usuarioDescripcion);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}")
    public String cambiarBloqueoUsuario(@PathVariable(value = "id") Long id) {
        comprobarAdmin();
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario != null && !Boolean.TRUE.equals(usuario.getAdmin())) {
            usuarioService.cambiaBloqueoUsuario(id, !Boolean.TRUE.equals(usuario.getBloqueado()));
        }
        return "redirect:/registrados";
    }
}