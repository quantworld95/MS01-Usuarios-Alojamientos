package com.alojate.alojamientos.application.ports.output;

import java.util.UUID;

public interface AccesoAnfitrion {

    void exigirPuedeRegistrar(UUID anfitrionId);
}
