package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.CambiarActivoRolCommand;
import com.alojate.identidad.application.dto.CrearRolCommand;
import com.alojate.identidad.application.dto.ModificarRolCommand;
import com.alojate.identidad.application.dto.ModuloResponse;
import com.alojate.identidad.application.dto.RolResponse;

import java.util.List;

public interface GestionarRoles {

    List<ModuloResponse> listarModulos();

    List<RolResponse> listarRoles();

    RolResponse obtener(int id);

    RolResponse crear(CrearRolCommand comando);

    RolResponse modificar(ModificarRolCommand comando);

    RolResponse cambiarActivo(CambiarActivoRolCommand comando);
}
