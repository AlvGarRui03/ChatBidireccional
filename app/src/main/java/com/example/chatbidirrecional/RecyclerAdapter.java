package com.example.chatbidirrecional;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>{
    private List<Paquete> listadoMensajes;
    private Activity activity;
    public RecyclerAdapter(List<Paquete> listadoMensajes,Activity activity) {
        this.listadoMensajes = listadoMensajes;
        this.activity=activity;
    }

    /**
     * Metodo que devuelve el RHolder
     * @param parent
     * @param viewType
     * @return recyclerHolder
     */
    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       //Indicamos el diseño del Holder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diseniolista_msg,parent, false);
        RecyclerHolder recyclerHolder = new RecyclerHolder(view);
        return recyclerHolder;
    }

    /**
     * Metodo que nos ayuda marcar los datos que irán dentro del RecyclerView
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.RecyclerHolder holder, int position) {
        //Obtenermos el paquete
        Paquete mensaje = listadoMensajes.get(position);
        //Si es de tipo Mensaje se indicará a la derecha
        if(mensaje.getTipo() == Paquete.TIPO_MENSAJE){
            holder.mensajeEnviado.setBackgroundResource(R.drawable.mensaje_enviado);
            holder.mensajeEnviado.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.mensajeEnviado.setText(mensaje.getMsg());
        //Si es de tipo respuesta se indicará a la izquierda
        }if(mensaje.getTipo() == Paquete.TIPO_RESPUESTA) {
            holder.mensajeRecibido.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.mensajeRecibido.setBackgroundResource(R.drawable.mensaje_recibido);
            holder.mensajeRecibido.setText(mensaje.getMsg());
        }

    }

    /**
     * Metodo que obtiene el tamaño del List de mensajes
     * @return listadoMensajes.size()
     */

    @Override
    public int getItemCount() {
        return listadoMensajes.size();
    }

    /**
     * Obtenermos las vistas desde el layout
     */
    public class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView mensajeEnviado;
        TextView mensajeRecibido;

        public RecyclerHolder(@NonNull View itemView) {
            super(itemView);

            mensajeEnviado = (TextView) itemView.findViewById(R.id.txt_enviado);
            mensajeRecibido = (TextView) itemView.findViewById(R.id.txt_recibido);
        }

}

}
