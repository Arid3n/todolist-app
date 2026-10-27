package madstodolist.model;

import java.io.Serializable;

public class Equipo implements Serializable {

    private String nombre;

    public Equipo() {
    }

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}