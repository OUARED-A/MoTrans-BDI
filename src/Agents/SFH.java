package Agents;
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class SFH extends JFrame {	
private AgentSFH myAgent; 
  //  private Agent1 myAgent; 	
    SFH(AgentSFH a) {
        super(a.getLocalName());
        myAgent = a;
    

    };	
}