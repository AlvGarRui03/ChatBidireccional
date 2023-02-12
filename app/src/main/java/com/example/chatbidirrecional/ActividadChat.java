package com.example.chatbidirrecional;

import static com.example.chatbidirrecional.R.id.ET_Mensajes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ActividadChat extends AppCompatActivity {
    private RecyclerView recyclerView;
    RecyclerAdapter recAdapter;
    ArrayList<Paquete> listaMsg = new ArrayList<Paquete>();
    EditText ET_mensaje;
    TextView TV_nombre;
    private String ip;
    private String nombre;
    String mensajeEnviar;
    boolean mantenerBusqueda;

    private ServerSocket serverEntrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_chat);
        ET_mensaje = findViewById(R.id.ET_Mensajes);
        TV_nombre = findViewById(R.id.txt_nombre);
        //Tomamos la id del RecyclerView creado
        recyclerView = (RecyclerView) findViewById(R.id.RV_mensajes);
        //Añadimos el manager del layout para darle formato
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        //Se lo añadimos al recycler
        recyclerView.setLayoutManager(layoutManager);
        //Creamos el adaptador del recyclerView
        recAdapter = new RecyclerAdapter(devolverLista(),this);
        //Se lo pasamos como adaptador
        recyclerView.setAdapter(recAdapter);
        //Tomamos el nombre que nos hemos guardado en el intent de la actividad anterior
        nombre = getIntent().getStringExtra("Nombre");
        //Tomamos la ip del destinatario que nos hemos guardamos en el intent de la anterior actividad
        ip = getIntent().getStringExtra("ip_destinatario");
        //Iniciarmos el servidor
        iniciarBusquedaPaquetes();
        //Cambiamos el texto del TextView por el que le ha pasado el usuario
        TV_nombre.setText(nombre);
        //Inicializamos la variable que marca que el servidor este activo
        mantenerBusqueda=true;


    }
    /**
     *
     * Metodo onResume que reactiva el servidor
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        mantenerBusqueda=true;
        iniciarBusquedaPaquetes();
    }
    /**
     *
     * Metodo onStop que pausa el Servidor
     *
     */
    @Override
    protected void onStop(){
        super.onStop();
        mantenerBusqueda=false;
        finish();
    }
    /**
     *
     * Metodo onDestroy que pausa el servidor
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mantenerBusqueda=false;
    }

    /**
     * Metodo que se activa al pulsar el boton para enviar mensaje, toma el mensaje del EditText y lo pasa
     * por el socket mediante el ObjectOutputSteam y lo añade a la lista de mensajes para cambiarlo en el RecyclerView
     * @throws IOException
     * Se le pasa el view del boton
     * @param view
     *
     *
     */
    public void crearSocket(View view) {
        //Tomamos el texto del EditText
        if(ET_mensaje.getText()!=null || ET_mensaje.getText().toString().length()>0){
                mensajeEnviar = ET_mensaje.getText().toString().trim();
            //Abrimos el hilo, puesto que no se pueden crear sockets en el hilo principal
            Thread creacionSocketEnvio = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //Creamos el socket con la ip del destinatario
                        Socket misocket = new Socket(ip, MainActivity.PUERTO_BASE+1);
                        //Abrimos el OutputStream
                        ObjectOutputStream envioPaquetes = new ObjectOutputStream(misocket.getOutputStream());
                        //Creamos una instancia de la clase paquete para enviar el mensaje
                        Paquete paqueteEnvio = new Paquete();
                        //Compronamos que el socket esta conectado
                        if(misocket.isBound()) {
                            //Indicamos el mensaje en el paquete
                            paqueteEnvio.setMsg(mensajeEnviar);
                            //Indicamos  que el tipo es paquete
                            paqueteEnvio.setTipo(Paquete.TIPO_MENSAJE);
                            //Escribimos el objeto en el OutputStream
                            envioPaquetes.writeObject(paqueteEnvio);
                            //Añadimos el paquete en el ArrayList
                            listaMsg.add(paqueteEnvio);
                            //Cerramos el flujo y el socket
                            envioPaquetes.close();
                            misocket.close();
                        }




                    } catch (IOException e) {
                        System.out.println("Error al crear el socket de envio");

                    }

                }
            });
            //Comenzamos el hilo
            creacionSocketEnvio.start();
            //Indicamos los cambios en el ArrayList
            actualizarLista();
        }
    }
    /**
     *
     *Método para volver a la actividad anterior al pulsar el botón de volver
     *
     * Le pasamos la vista del boton
     *
     */
    public void volverAtras(View view) {
        mantenerBusqueda=false;
        onBackPressed();


    }
    /**
     *
     * Metodo que inicia el hilo para activar el servidor y recibir mensajes por el mismo, posteriormente
     * se añadirá a la lista con el tipo recibido para que el recyclerView lo detecte como mensaje a la izquierda
     * @throws IOException,ClassCastException
     *
     * */
    public void iniciarBusquedaPaquetes () {
        Thread socketHilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Abrimos el serverSocket
                    serverEntrada = new ServerSocket(MainActivity.PUERTO_BASE+1);
                    //Creamos el objeto Paquete
                    Paquete paqueteRecibido = new Paquete();
                    //Mantenemos la busqueda hasta que salgamos de esta actividad
                    while (mantenerBusqueda) {
                        //Abrimos el socket de busqueda
                        Socket socketConexion = serverEntrada.accept();
                        //Abrimos el ObjectInputStream
                        ObjectInputStream flujo_entrada = new ObjectInputStream(socketConexion.getInputStream());
                        //Leemos el objeto
                        paqueteRecibido = (Paquete) flujo_entrada.readObject();
                        //Tomamos el paquete recibido
                        Paquete pqtRecived = new Paquete();
                        //Indicamos que el tipo es respuesta
                        pqtRecived.setTipo(Paquete.TIPO_RESPUESTA);
                        //Tomamos el mensaje del paquete recibifo
                        pqtRecived.setMsg(paqueteRecibido.getMsg());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //Añadimos el mensaje al Array y actualizamos el RecyclerView
                                listaMsg.add(pqtRecived);
                                actualizarLista();
                            }
                        });
                        //Cerramos los flujos
                        socketConexion.close();
                        flujo_entrada.close();
                    }
                    //Y se cierra el servidor tras el bucle
                    serverEntrada.close();



                } catch (IOException e) {
                    Log.e("Socket Recepcion:", "Se ha producido al crear el socket de recepcion");
                } catch (ClassNotFoundException e) {
                   Log.e("Socket Recepcion: " ,"Error encontrar la clase");
                }


            }
        });
        //Iniciamos el hilo
        socketHilo.start();
    }
    /**
     *
     * Metodo para devolver el ArrayList de mensajes
     * @return listaMsg
     */
    public List<Paquete> devolverLista(){
        return listaMsg;}
    /**
     *
     * Metodo que avisa al adapter de que han cambiado los datos de la lista
     */
    public void actualizarLista(){
        recAdapter.notifyDataSetChanged();
    }
}