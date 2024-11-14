package pe.org.jhsystem.test.mqtt.capsulamqtt;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MainActivity extends AppCompatActivity {

    String clienteID = "";

    // Conexion al servidor
    static String MQTTHOST = "tcp://y4ku-mqtt.cloud.shiftr.io:1883";
    static String MQTTUSER = "y4ku-mqtt";
    static String MQTTPASS = "S9pGGIwCnZivmk2m";

    static String TOPIC = "LED";
    static String TOPIC_MSG_ON = "ENCENDER";
    static String TOPIC_MSG_OFF = "APAGAR";

    MqttAndroidClient cliente;
    MqttConnectOptions opciones;

    Boolean permisoPublicar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getNombreCliente();
        connectBroker();

        Button btnON = findViewById((R.id.btnON));
        btnON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensaje(TOPIC, TOPIC_MSG_ON);
            }
        });

        Button btnOFF = findViewById((R.id.btnOFF));
        btnOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensaje(TOPIC, TOPIC_MSG_OFF);
            }
        });

    }

    private void checkConnection(){
        if (this.cliente.isConnected()){
            this.permisoPublicar = true;
        } else {
            this.permisoPublicar = false;
            connectBroker();;
        }
    }

    private void enviarMensaje(String topic, String msg){
        checkConnection();
        if (this.permisoPublicar){
            try {
                int qos = 0;
                this.cliente.publish(topic, msg.getBytes(), qos, false);
                Toast.makeText(getBaseContext(), topic + " : " + msg, Toast.LENGTH_SHORT).show();;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void getNombreCliente() {
        String manufacturer = Build.MANUFACTURER;
        String modelName = Build.MODEL;
        this.clienteID = manufacturer + " " + modelName;

        TextView txtIdCliente = findViewById(R.id.txtIdCliente);
        txtIdCliente.setText(this.clienteID);
    }

    private void connectBroker() {
        this.cliente = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, this.clienteID);
        this.opciones = new MqttConnectOptions();
        this.opciones.setUserName(MQTTUSER);
        this.opciones.setPassword(MQTTPASS.toCharArray());
        try {
            IMqttToken token = this.cliente.connect(opciones);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getBaseContext(),"CONECTADO!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(),"CONECCION FALLIDA!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();;
        }
    }

    private void suscribirseTopic(){
        try{
            this.cliente.subscribe(TOPIC, 0);
        } catch (MqttSecurityException e){
            e.printStackTrace();
        } catch (MqttException e){
            e.printStackTrace();
        }

        this.cliente.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), "Se desconecto el servidor!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                TextView txtInfo = findViewById(R.id.txtInfo);
                if (topic.matches(TOPIC)){
                    String msg = new String(message.getPayload());
                    if (msg.matches(TOPIC_MSG_ON)){
                        txtInfo.setText(msg);
                        txtInfo.setBackgroundColor(GREEN);
                    }
                    if (msg.matches(TOPIC_MSG_OFF)){
                        txtInfo.setText(msg);
                        txtInfo.setBackgroundColor(RED);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}