package Agents;
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class Negociateur extends JFrame {	
private AgentNegocier myAgent; 
  //  private Agent1 myAgent; 	
    Negociateur(AgentNegocier a) {
        super(a.getLocalName());
        myAgent = a;
    

    };	
}