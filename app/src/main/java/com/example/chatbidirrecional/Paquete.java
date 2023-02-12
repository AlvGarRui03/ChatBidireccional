package com.example.chatbidirrecional;

import java.io.Serializable;

public class Paquete  implements Serializable {
    public static int TIPO_MENSAJE=1;
    public static int TIPO_COMPROBACION=0;
    public static int TIPO_RESPUESTA=-1;
    private int tipo;
    private String msg;
    private String Mensajero;
    public Paquete(){

    }

    /**
     * Getter del tipo de paquete
     * @return tipo
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Setter del tipo de paquete
     * @param tipo
     */
    public void setTipo(int tipo) {
        if(tipo == TIPO_COMPROBACION || tipo == TIPO_MENSAJE || tipo == TIPO_RESPUESTA){
        this.tipo = tipo;
        }
    }

    /**
     *
     * Metodo que devuelve el mensajero
     * @return Mensajero
     */
    public String getMensajero() {
        return Mensajero;
    }

    /**
     * Metodo Setter de mensajero
     * @param mensajero
     */
    public void setMensajero(String mensajero) {
        Mensajero = mensajero;
    }

    /**
     * Getter del mensaje
     * @return msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Setter del mensaje
     * @param msg
     */

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
