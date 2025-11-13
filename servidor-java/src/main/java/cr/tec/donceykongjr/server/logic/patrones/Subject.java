package cr.tec.donceykongjr.server.logic.patrones;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase base para el patrón Observer.
 * Permite que los sujetos notifiquen cambios a sus observadores.
 */
public abstract class Subject {
    private List<Observer> observadores;
    
    /**
     * Constructor del sujeto.
     */
    public Subject() {
        this.observadores = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Agrega un observador a la lista.
     */
    public void agregarObservador(Observer observador) {
        observadores.add(observador);
    }
    
    /**
     * Elimina un observador de la lista.
     */
    public void eliminarObservador(Observer observador) {
        observadores.remove(observador);
    }
    
    /**
     * Notifica a todos los observadores de un cambio.
     */
    protected void notificarObservadores(Object dato) {
        for (Observer observador : observadores) {
            observador.actualizar(dato);
        }
    }
    
    /**
     * Notifica a todos los observadores sin datos adicionales.
     */
    protected void notificarObservadores() {
        notificarObservadores(null);
    }
}

