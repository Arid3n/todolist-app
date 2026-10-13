package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "No autorizado: se requieren permisos de administrador")
public class UsuarioNoAutorizadoException extends RuntimeException {
}