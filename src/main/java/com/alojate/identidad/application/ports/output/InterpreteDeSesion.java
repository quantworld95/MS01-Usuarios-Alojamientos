package com.alojate.identidad.application.ports.output;

public interface InterpreteDeSesion {

    SesionAutenticada interpretar(String token);
}
