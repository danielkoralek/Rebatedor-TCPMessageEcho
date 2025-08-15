/*
 * Created on Mar 30, 2004
 */
package com.koralek.rebatedor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import koralek.k1.util.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Main Classe principal, responsável pelo listener do
 * Rebatedor.
 * 
 * @author dkprado Created on Mar 30, 2004
 */
public class Main {
    
    int delayResposta = 0;
    String respostaPadrao=null;
    XMLUtil xmlUtil = null;

    /*
     * Main!
     */
    public static void main(String[] args) {
        new Main(Integer.parseInt(args[0]));
    }

    /**
     * Construtor padrão!
     */
    public Main(int _delay) {
        this.delayResposta =_delay;
        xmlUtil = new XMLUtil();
        try {
            lerConfiguracoes();
            try {
                escutar(5001);
            } catch (IOException e) {
                System.out.println("Erro iniciando o Listener: " + e.toString());
            }
        } catch (Exception e1) {
            System.out.println("Erro lendo as configuracoes: " + e1.toString());
        }
    }

    /**
     * Procedimento interno de leitura das configurações do Rebatedor.
     * Leitura do arquivo "conf/rebatedor.xml".
     */
    private void lerConfiguracoes() throws Exception {
        /* Abre o arquivo XML, criando um documento para ele */
        Document doc = xmlUtil.getDocumentFromXML("conf/rebatedor.xml");
        Element	root = doc.getDocumentElement();
        NodeList mensagem = root.getElementsByTagName("resposta").item(0).getChildNodes();
        respostaPadrao = xmlUtil.getNodeText(mensagem.item(0));
    }
    
    /**
     * Procedimento de escuta do listener
     * @param porta	O número da porta onde o listener aguardará conexões!
     */
    private void escutar(int porta) throws IOException {
        System.out.println("Inciando o Rebatedor...\n");
        ServerSocket server = new ServerSocket(porta);
        System.out.println("   IP: " + server.getInetAddress());
        System.out.println("   Porta: " + porta);
        System.out.println("   Delay: " + delayResposta);
        System.out.println("\n-------\nMensagens recebidas:\n");
        while (true) {
            Socket sck = server.accept();
            new Rebatedor(sck);
        }
    }

    /**
     * Rebatedor 
     * Innerclass extendendo Thread, com a
     * responsabilidade de rebater a mensgem recebida pelo
     * listener.
     * 
     * @author dkprado 
     * Created on Mar 30, 2004
     */
    private class Rebatedor extends Thread {
        
        /**
         * Socket recebido do listener...
         */
        private Socket sck = null;
        
        /**
         * Construtor
         * @param _sck	Socket estabelecido e fornecido pelo Listener.
         */
        public Rebatedor(Socket _sck){
            this.sck = _sck;
            this.start();
        }
        
        /**
         * Procedimento do Rebatedor (devolução da mensagem)
         */
        public void run(){
            try {
                
                /* Cria os controles de buffer */
                
                BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
                PrintWriter out = new PrintWriter(sck.getOutputStream());
                
                /* Lê a mensagem recebida */
                
                String entrada = in.readLine();
                if(entrada==null){
                    entrada="!null message!";
                } else {
                    System.out.println("[" + sck.getRemoteSocketAddress() + "] " + entrada);
                }
                
                /* Delay programado */
                
                try {
                    sleep(delayResposta);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                
                /* Responde a mensagem e encerra os controles de buffer */
                
                out.print(respostaPadrao);
                out.flush();
                out.close(); 
                in.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }//Rebatedor

}//Main
