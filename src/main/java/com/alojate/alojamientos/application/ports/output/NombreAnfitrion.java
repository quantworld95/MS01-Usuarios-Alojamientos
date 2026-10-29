package com.alojate.alojamientos.application.ports.output;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface NombreAnfitrion {

    Map<UUID, String> de(Collection<UUID> anfitriones);
}
