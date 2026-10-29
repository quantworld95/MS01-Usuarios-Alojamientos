package com.alojate.alojamientos.application.ports.output;

import java.util.UUID;

public interface AccesoAdministrador {

    void exigirPuedeModerar(UUID administradorId);
}
