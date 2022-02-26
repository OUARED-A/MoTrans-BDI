package Agents;
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class Evaluateur extends JFrame {	
private AgentEvaluer myAgent; 
  //  private Agent1 myAgent; 	
    Evaluateur(AgentEvaluer a) {
        super(a.getLocalName());
        myAgent = a;
    

    };	
}