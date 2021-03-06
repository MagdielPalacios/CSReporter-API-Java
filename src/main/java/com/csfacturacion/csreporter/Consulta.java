/*
 * Copyright 2016 CSFacturación, todos los derechos reservados.
 */
package com.csfacturacion.csreporter;

import java.util.List;
import java.util.UUID;

/**
 * Representa una consulta realizada al portal CFDIMeta del SAT.
 *
 * @author emerino
 */
public interface Consulta {

    /**
     * 'Enum' que sirve para listar los distintos STATUS posibles de una
     * consulta.
     *
     */
    enum Status {

        EN_ESPERA,
        EN_PROCESO,
        DESCARGANDO,
        FALLO_AUTENTICACION,
        FALLO_500_MISMO_HORARIO,
        FALLO,
        COMPLETADO,
        COMPLETADO_CON_FALTANTES,
        REPETIR;

        public boolean isCompletado() {
            return this.name().startsWith("COMPLETADO")
                    || isFallo();
        }

        public boolean isFallo() {
            return this.name().startsWith("FALLO");
        }

    }

    /**
     * Los parámetros utilizados para generar esta Consulta.
     * @return los parámetros utilizados.
     */
    Parametros getParametros();

    /**
     * El status actual de la consulta, reportado por el WS. EN_ESPERA: No han
     * comenzado a descargarse los CFDIs, se encuentra en cola la petición.
     * EN_PROCESO: La descarga de CFDIs está en curso. DESCARGANDO: Ya se tiene
     * el total de resultados, pero aún se están descargando los XMLs.
     * FALLO_AUTENTICACION: Ocurre cuando no se ha podido autenticar con el RFC
     * y contraseñas provistos con el portal del SAT. FALLO_500_MISMO_HORARIO:
     * Ocurre cuando se obtienen más de 500 resultados con la misma fecha y
     * horario (minuto exacto). FALLO: Distintos errores pueden causar este
     * estado. COMPLETADO: Los CFDIs de la consulta se han descargado. REPETIR:
     * Cuando una consulta necesita repetirse (generalmente para descargar XMLs
     * faltantes).
     *
     * @return El status actual de la consulta.
     */
    Status getStatus();

    /**
     * Cuando una consulta ha terminado, su status puede ser:
     * FALLO_AUTENTICACION FALLO_500_MISMO_HORARIO FALLO COMPLETADO
     *
     * Para verificar que no se haya completado con error, verificar el método
     * {@link #isFallo()} o directamente el status de la consulta.
     *
     * @return true si se devuelve cualquiera de los status anteriores o false
     * de otro modo.
     */
    boolean isTerminada();

    /**
     * Cualquiera de los siguientes status deben marcar esta consulta como
     * fallo:
     *
     * FALLO_AUTENTICACION FALLO_500_MISMO_HORARIO FALLO
     *
     * @return true si se devuelve cualquiera de los status anteriores o false
     * de otro modo.
     */
    boolean isFallo();

    /**
     * Si la consulta ha sido marcada con status REPETIR, no habrá ningún
     * resultado disponible y será necesario repetir esta consulta.
     *
     * @see CSReporter#repetir(java.util.UUID)
     * @return true si el status es REPETIR, false de otro modo.
     */
    boolean isRepetir();

    /**
     * Cuando se realiza una consulta a través de un IDescargaSAT, se genera un
     * folio único que identifica la consulta.
     *
     * @return el UUID que identifica a la consulta.
     */
    UUID getFolio();

    /**
     * Total de registros encontrados en el portal del SAT para esta consulta.
     *
     * @return El total de resultados de la consulta, 0 si no se encontró
     * ninguno.
     */
    long getTotalResultados();

    /**
     * Los resultados se envían paginados, devuelve el total de páginas
     * disponibles para obtener resultados.
     *
     * @return total de páginas disponibles o 0 si no hay resultados.
     */
    int getPaginas();

    /**
     * Los resultados se devuelven paginados, por lo que este método permite
     * obtener un arreglo de CFDIs (simples) para una página determinada.
     *
     * @param pagina que se desea obtener.
     * @return El total de registros encontrados en la página dada o un arreglo
     * vacío si no hay suficientes resultados.
     */
    List<CFDIMeta> getResultados(int pagina)
            throws ResultadosInsuficientesException;

    /**
     * Este método sirve cuando se quiere obtener los resultados en objetos que
     * no sean CFDIMeta sino una extensión de éste. Esto es útil en casos donde
     * se requiere persistir el CFDIMeta, en lugar de tener que copiar
     * manualmente cada CFDIMeta a otro objeto, se obtienes los resultados
     * directamente con la clase esperada.
     *
     * @param pagina que se desea obtener.
     * @param clazz la clase a la que se convertirán los resultados.
     * @return El total de registros encontrados en la página dada o un arreglo
     * vacío si no hay suficientes resultados.
     */
    <T extends CFDIMeta> List<T> getResultados(
            int pagina,
            Class<T> clazz)
            throws ResultadosInsuficientesException;

    /**
     * Determina si hay resultados disponibles para esta consulta.
     *
     * @return true si hay resultados disponibles, false de otro modo.
     */
    boolean hasResultados();

    /**
     * Un CFDIMeta se puede buscar directamente por folio si es un resultado de
     * esta consulta.
     *
     * @param folio del CFDIMeta
     * @return el CFDIMeta correspondiente o null si no se encontró en esta
     * consulta.
     */
    CFDIMeta getCFDI(UUID folio);

    /**
     * Devuelve el XML del CFDIMeta asociado con el folio dado. En ocasiones
     * puede no haber un XML asociado, en estos casos devuelve null.
     *
     * @param folio del CFDIMeta.
     * @return el XML asociado con el CFDIMeta o null si no hay ninguno.
     * @throws XMLNoEncontradoException si no se encuentra el XML solicitado.
     */
    String getCFDIXML(UUID folio) throws XMLNoEncontradoException;

    /**
     * Devuelve el XML del CFDIMeta dado. En ocasiones puede no haber un XML
     * asociado, en estos casos devuelve null.
     *
     * @param cfdi del CFDIMeta.
     * @return el XML asociado con el CFDIMeta o null si no hay ninguno.
     * @throws XMLNoEncontradoException si no se encuentra el XML solicitado.
     */
    String getCFDIXML(CFDIMeta cfdi) throws XMLNoEncontradoException;
}
