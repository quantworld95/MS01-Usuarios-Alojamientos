package com.alojate.identidad.infrastructure.adapters.output.transaction;

import com.alojate.identidad.application.ports.output.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class SpringUnitOfWork implements UnitOfWork {

    private final TransactionTemplate transacciones;

    public SpringUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transacciones = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        return transacciones.execute(estado -> trabajo.get());
    }
}
