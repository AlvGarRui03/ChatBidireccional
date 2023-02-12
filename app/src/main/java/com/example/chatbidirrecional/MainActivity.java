package com.example.chatbidirrecional;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.google.android.material.textfield.TextInputEditText;



public class MainActivity extends AppCompatActivity {
    private TextInputEditText ip;
    private TextInputEditText nombre;
    private TextView miIp;
    private ServerSocket serverEntrada = null;
    private Thread socketHilo = null;
    public static final int PUERTO_BASE = 1234;
    private boolean conexionEstablecida = false;
    private boolean pararBucle = false;
    public String ip_texto;
    String ip_destinatario;
    private TextView infoConexion;
    private String nombreDestinatario;
    Bundle parametros = new Bundle();
    boolean dejarBusqueda=true;
    private String mensajero=" ";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = findViewById(R.id.editText_IP);
        nombre = findViewById(R.id.EditText_Nombre);
        miIp = findViewById(R.id.view_Ip);
        infoConexion = findViewById(R.id.TxT_notificarConexion);

        Context context = this.getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ip_texto = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        miIp.setText("| Su Ip es: " + ip_texto + "|");


        iniciarBusquedaPaquetes();


    }
    /**
     *
     * Metodo onResume que reactiva el servidor
     *
     */
    protected void onResume() {
        super.onResume();
        dejarBusqueda=true;
        iniciarBusquedaPaquetes();
    }
    /**
     *
     * Metodo onStop que pausa el servidor
     *
     */
    @Override
    protected void onStop(){
        super.onStop();
        dejarBusqueda=false;
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
        dejarBusqueda=false;
    }
    /**
     *
     * Metodo crearSocket que crea un socket cliente al pulsar el boton de conectar
     * @param view
     * @exception IOException
     *
     */
    public void crearSocket(View view) {
        //Tomamos el nombre de EditText y la guardamos en un Bundle para poder pasarlo entre actividades
        if (nombre.getText() != null) {
            nombreDestinatario = nombre.getText().toString();
            parametros.putString("Nombre", nombreDestinatario);
        }
        //Tomamos la ip del destinatario y la guardamos en un Bundle para poder pasarlo entre actividades
        if (ip.getText() != null) {
            //Si el texto no es nulo intentamos crear un Socket con esa IP
            ip_destinatario = ip.getText().toString();
            parametros.putString("ip_destinatario", ip_destinatario);
            //Iniciamos el hilo para poder crar el Socket
            Thread creacionSocketEnvio = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //Creamos el socket
                        Socket misocket = new Socket(ip.getText().toString(), PUERTO_BASE);
                        //Creamos el paquete
                        Paquete paqueteComprobacion = new Paquete();
                        //Si esta conectado
                        if (misocket.isBound()) {
                            //Creamos el OutputStream
                            ObjectOutputStream envioPaquetes = new ObjectOutputStream(misocket.getOutputStream());
                            //Indicamos el msg y tipo del paquete
                            paqueteComprobacion.setTipo(Paquete.TIPO_COMPROBACION);
                            paqueteComprobacion.setMensajero(ip_texto);
                            //Enviamos el paquete
                            envioPaquetes.writeObject(paqueteComprobacion);

                            //Creamos el intent
                            Intent i = new Intent(MainActivity.this, ActividadChat.class);
                            if(nombre.getText()!=null) {
                                //Guardamos el nombre del destinatario
                                parametros.putString("Nombre", nombreDestinatario);
                            }
                            //Metemos los datos en el intent
                            i.putExtras(parametros);
                            //Cortamos el ServerSocket
                            dejarBusqueda=false;
                            //Abrimos la nueva actividad
                            startActivity(i);




                        }
                        //Cerramos el socket
                        misocket.close();



                    } catch (IOException e) {
                        Log.e("SocketsEnvio:", "Error al crear el Socket de envio");
                    }


                }
            });
            //Iniciamos el hilo
            creacionSocketEnvio.start();
        }
    }
    /**
     *
     * Metodo que crear el ServerSocket y esta a la espera de de establecer una conexión
     *
     * @exception IOException
     *
     */

        public void iniciarBusquedaPaquetes () {
            //Creamos el hilo para poder crear el socket
            socketHilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Creamos el SS y el paquete
                        serverEntrada = new ServerSocket(PUERTO_BASE);
                        Paquete paqueteRecibido = new Paquete();
                        //Bucle hasta que se cierre la actividad
                        while (dejarBusqueda) {
                            //Cremos el socket que acepta la señal del SS
                            Socket socketConexion = serverEntrada.accept();
                           //Abrimos el InputStream
                            ObjectInputStream flujo_entrada = new ObjectInputStream(socketConexion.getInputStream());
                            //Leemos el objeto
                            paqueteRecibido = (Paquete) flujo_entrada.readObject();
                            //Obtenemos el mensajero del paquete
                            mensajero= paqueteRecibido.getMensajero();
                            //Si el paquete es comprobacion indicamos que se ha establecido una conexion con otro usuario
                            if (paqueteRecibido.getTipo() == Paquete.TIPO_COMPROBACION) {
                                conexionEstablecida = true;
                            }
                            if (conexionEstablecida || pararBucle == false) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        infoConexion.setText(mensajero + " quiere contactar con usted");
                                        pararBucle = true;
                                    }
                                });

                            }
                            //Cerramos los flujos
                            socketConexion.close();
                            flujo_entrada.close();
                        }
                        serverEntrada.close();

                    } catch (IOException e) {
                        Log.e("Socket Recepcion Chat:", "Se ha producido al crear el socket de recepcion");
                    } catch (ClassNotFoundException e) {
                        Log.e("Socket Recepcion Chat: " , "No se ha encontrado una clase");
                    }


                }
            });
            //Iniciamos el hilo
            socketHilo.start();
        }
    }
