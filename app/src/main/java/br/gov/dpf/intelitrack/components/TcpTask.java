package br.gov.dpf.intelitrack.components;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class TcpTask extends AsyncTask<TcpTask.OnMessageReceived, String, Boolean> {

    // tcp connection object
    private Socket mSocket;

    // used to send messages
    private PrintWriter mBufferOut;

    // used to read messages from the server
    private BufferedReader mBufferIn;

    // sends message received notifications
    private OnMessageReceived mMessageListener = null;

    // stores string values
    private String mAuth, mServer, mPort;

    // List of commands to server communication
    private HashMap<String, String> mResponses;

    // flag indicating connection status
    private boolean mRunning = false;

    //Set connection params and get user UID
    public TcpTask(String server, String port)
    {
        //Store connection params
        mServer = server;
        mPort = port;

        //Retrieve auth uid
        mAuth = FirebaseAuth.getInstance().getUid();

        //Initialize hash map
        mResponses = new HashMap<>();
    }

    //Set code expected from server and response message
    public void addResponse(String code, String response)
    {
        //Add response from server
        mResponses.put(code, response);
    }

    private void sendMessage(String message)
    {
        try
        {
            //If buffer is active and no errors
            if (mBufferOut != null && !mBufferOut.checkError())
            {
                //Send message to server
                mBufferOut.println("CLIENT_AUTH_" + mAuth + "_" + message);
                mBufferOut.flush();
            }
        }
        catch (Exception e)
        {
            //Log error
            Log.w("TCP", "Error", e);

            //Update status (unknown error)
            publishProgress("Conexão perdida, tentando novamente...");

            //If is connected
            if(mSocket != null && mSocket.isConnected()){

                //Disconnect
                disconnect();
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void disconnect()
    {
        //Close previous connection
        try
        {
            //Log connection end
            Log.d("TCP", "Disconnecting...");

            //Close write buffer
            if (mBufferOut != null)
            {
                mBufferOut.flush();
                mBufferOut.close();
            }

            //Close read buffer
            if (mBufferIn != null)
            {
                mBufferIn.close();
            }

            //Check if any connection is already initiated
            if(mSocket != null)
            {
                //Try to close pending connection
                mSocket.close();
            }
        }
        catch (Exception e)
        {
            //Log error
            Log.w("TCP", "Error disconnecting: " + e.getMessage());
        }
        finally
        {
            //Release socket references
            mSocket = null;
            mBufferIn = null;
            mBufferOut = null;

            //Change flag status
            mRunning = false;
        }
    }

    private void connect() {
        try
        {
            //Initialize flag
            mRunning = true;

            //create a socket to make the connection with the server
            Socket socket = new Socket();

            //Log connection start
            Log.d("TCP", "Connecting...");

            //Initialize connection
            socket.connect(new InetSocketAddress(mServer, Integer.parseInt(mPort)), 10000);

            //sends the message to the server
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //receives the message which the server sends back
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Send initial message
            sendMessage("CONNECT");

            //Log connection start
            Log.d("TCP", "Connected");

            //Update connection progress on UI
            publishProgress("CONNECTED");

            //in this while the client listens for the messages sent by the server
            while (mRunning && !isCancelled()) {

                //Waits for server response
                String mServerMessage = mBufferIn.readLine();

                //Update connection progress on UI
                publishProgress(mServerMessage);

                //Check if this response is expected from server
                if(mResponses.containsKey(mServerMessage))
                {
                    //Send stored response to server
                    sendMessage(mResponses.get(mServerMessage));
                }
            }
        }
        catch (Exception e)
        {
            //Log error
            Log.w("TCP", "Error", e);

            if(e instanceof SocketTimeoutException)
            {
                //Update status (timeout)
                publishProgress("Conexão não disponível");
            }
            else
            {
                //Update status (unknown error)
                publishProgress("Erro de conexão com servidor");
            }

            //If is connected
            if(mSocket != null && mSocket.isConnected()){

                //Disconnect
                disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        //If listener attached
        if (values[0] != null && mMessageListener != null)
        {
            //call the method messageReceived
            mMessageListener.messageReceived(values[0]);
        }
    }

    @Override
    protected void onCancelled(Boolean mayInterrupt) {
        super.onCancelled(mayInterrupt);

        //Flag task end
        mRunning = false;

        //Flag to end task
        disconnect();
    }

    @Override
    protected Boolean doInBackground(OnMessageReceived... values) {

        //Get message listener
        mMessageListener = values[0];

        //Initialize connection
        connect();

        return null;
    }


    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
