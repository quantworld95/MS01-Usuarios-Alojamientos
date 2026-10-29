package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.ModificarTrabajadorCommand;
import com.alojate.identidad.application.dto.RegistrarTrabajadorCommand;
import com.alojate.identidad.application.dto.TrabajadorRegistradoResponse;
import com.alojate.identidad.application.dto.TrabajadorResponse;

import java.util.List;
import java.util.UUID;

public interface GestionarUsuarioCorporativo {

    TrabajadorRegistradoResponse registrar(RegistrarTrabajadorCommand comando);

    List<TrabajadorResponse> listar();

    TrabajadorResponse obtener(UUID id);

    TrabajadorResponse modificar(ModificarTrabajadorCommand comando);
}
